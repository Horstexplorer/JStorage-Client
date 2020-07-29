/*
 *     Copyright 2020 Horstexplorer @ https://www.netbeacon.de
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.netbeacon.jstorage.client.notification.objects;

import de.netbeacon.jstorage.client.notification.NotificationManager;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.*;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Takes care of requesting & receiving the notifications
 */
public class NotificationSocket implements Runnable {

    private final NotificationManager notificationManager;
    private final int port;
    private final boolean unsecure;
    private final HashMap<String, ArrayList<String>> selectedNotifications;

    private final AtomicBoolean retry = new AtomicBoolean(true);

    private final Logger logger = LoggerFactory.getLogger(NotificationSocket.class);

    /**
     * Creates a new instance of this class
     * @param notificationManager the superordinate notification manager
     * @param selectedNotifications the selection
     * @param port the port
     * @param unsecure if ssl error should be ignored
     */
    public NotificationSocket(NotificationManager notificationManager, HashMap<String, ArrayList<String>> selectedNotifications, int port, boolean unsecure){
        this.notificationManager = notificationManager;
        this.selectedNotifications = selectedNotifications;
        this.port = port;
        this.unsecure = unsecure;
    }

    @Override
    public void run() {
        SSLSocket socket = null;
        BufferedReader bufferedReader = null;
        BufferedWriter bufferedWriter = null;
        while(retry.get()){
            try{
                retry.set(false); // disable
                // check if token is available
                if(notificationManager.getJStorageClient().getLoginToken() == null){
                    throw new Exception("No Token Found");
                }
                // open connection
                SSLSocketFactory sslSocketFactory;
                if(unsecure){
                    final TrustManager[] trustAllCerts = new TrustManager[]{
                            new X509TrustManager() {
                                @Override
                                public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType) throws CertificateException {
                                }

                                @Override
                                public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType) throws CertificateException {
                                }

                                @Override
                                public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                                    return new java.security.cert.X509Certificate[]{};
                                }
                            }
                    };
                    final SSLContext sslContext = SSLContext.getInstance("SSL");
                    sslContext.init(null, trustAllCerts, new SecureRandom());
                    sslSocketFactory = sslContext.getSocketFactory();
                }else{
                    sslSocketFactory = (SSLSocketFactory) SSLSocketFactory.getDefault();
                }
                socket = (SSLSocket) sslSocketFactory.createSocket(notificationManager.getJStorageClient().getHost(), this.port);
                // business, handshakes, success!!1
                socket.startHandshake();
                // get ze streams ;3
                bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
                // send ze token
                bufferedWriter.write("Token: "+notificationManager.getJStorageClient().getLoginToken());
                bufferedWriter.newLine();
                // send ze requested notifications
                StringBuilder stringBuilder = new StringBuilder();
                for(Map.Entry<String, ArrayList<String>> entry : selectedNotifications.entrySet()){
                    if(entry.getValue().isEmpty()){
                        stringBuilder.append(entry.getKey().toLowerCase()).append(" ");
                    }else{
                        for(String s : entry.getValue()){
                            stringBuilder.append(entry.getKey()).append(":").append(s).append(" ");
                        }
                    }
                }
                bufferedWriter.write(" "+stringBuilder.toString().trim());
                bufferedWriter.newLine();
                bufferedWriter.newLine(); // close headers
                bufferedWriter.flush();
                // analyze response
                String auth = bufferedReader.readLine();
                if(!auth.contains("200 OK")){
                    throw new Exception("Invalid Response: "+auth);
                }
                // start reading ;3
                retry.set(true); // should try reconnecting
                while(true){
                    try{
                        String line = bufferedReader.readLine();
                        notificationManager.dispatchNotification(new DataNotification(new JSONObject(line)));
                    }catch (JSONException ignore){}
                }

            }catch (Exception e){
                logger.error("An Error Occurred Connecting To The Notification Socket", e);
            }finally {
                try{TimeUnit.MILLISECONDS.sleep(new Random().nextInt(100)+250);}catch (Exception e){}
                try{if(bufferedReader != null){bufferedReader.close();}}catch (Exception ignore){}
                try{if(bufferedWriter != null){bufferedWriter.close();}}catch (Exception ignore){}
                try{if(socket != null){socket.close();}}catch (Exception ignore){}
            }
        }
    }
}
