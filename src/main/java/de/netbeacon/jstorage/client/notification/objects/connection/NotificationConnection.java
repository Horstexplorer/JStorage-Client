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

package de.netbeacon.jstorage.client.notification.objects.connection;

import de.netbeacon.jstorage.client.notification.NotificationManager;

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
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantLock;

public class NotificationConnection {

    private final NotificationManager notificationManager;
    private final HashMap<String, ArrayList<String>> selectedNotifications;
    private final int notificationSocketPort;
    private boolean unsecureSSL;

    private final ReentrantLock lock = new ReentrantLock();
    private final AtomicBoolean reconnect = new AtomicBoolean(true);
    private SSLSocket sslSocket;
    private final Executor connectionWorker = Executors.newSingleThreadExecutor();

    public NotificationConnection(NotificationManager notificationManager, HashMap<String, ArrayList<String>> selectedNotifications, int notificationSocketPort){
        this.notificationManager = notificationManager;
        this.selectedNotifications = selectedNotifications;
        this.notificationSocketPort = notificationSocketPort;
    }

    public void setUnsecureSSL(boolean value){
        this.unsecureSSL = value;
    }

    public void setAutoReconnect(boolean value){
        this.reconnect.set(value);
    }


    public boolean connect(){
        BufferedReader bufferedReader = null;
        BufferedWriter bufferedWriter = null;
        try{
            lock.lock();
            // check if token is available
            if(notificationManager.getJStorageClient().getLoginToken() == null){
                throw new Exception("No Token Found");
            }
            SSLSocketFactory sslSocketFactory;
            if(this.unsecureSSL){
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
            sslSocket = (SSLSocket) sslSocketFactory.createSocket(notificationManager.getJStorageClient().getHost(), this.notificationSocketPort);
            // business, handshakes, success!!1
            sslSocket.startHandshake();
            // get ze streams ;3
            bufferedReader = new BufferedReader(new InputStreamReader(sslSocket.getInputStream()));
            bufferedWriter = new BufferedWriter(new OutputStreamWriter(sslSocket.getOutputStream()));
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
            connectionWorker.execute(new NotificationConnectionWorker(this, sslSocket, bufferedReader, bufferedWriter, this.notificationManager));
            return true;
        }catch (Exception e){
            try{if(bufferedReader != null){bufferedReader.close();}}catch (Exception ignore){}
            try{if(bufferedWriter != null){bufferedWriter.close();}}catch (Exception ignore){}
            try{sslSocket.close();}catch (Exception ignore){}
            return false;
        }finally {
            lock.unlock();
        }
    }

    public void disconnect(){
        reconnect.set(false);
        if(sslSocket != null){
            try{sslSocket.close();}catch (Exception ignore){} // this should let the worker die
        }
    }

    public boolean allowReconnect() {
        return reconnect.get();
    }
}
