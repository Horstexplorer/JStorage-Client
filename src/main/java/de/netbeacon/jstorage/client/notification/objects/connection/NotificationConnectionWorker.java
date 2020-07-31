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
import de.netbeacon.jstorage.client.notification.objects.notifiation.DataNotification;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLSocket;
import java.io.BufferedReader;
import java.io.BufferedWriter;

public class NotificationConnectionWorker implements Runnable{

    private final NotificationConnection notificationConnection;
    private final SSLSocket sslSocket;
    private final BufferedReader bufferedReader;
    private final BufferedWriter bufferedWriter;
    private final NotificationManager notificationManager;

    private final Logger logger = LoggerFactory.getLogger(NotificationConnectionWorker.class);

    public NotificationConnectionWorker(NotificationConnection notificationConnection, SSLSocket sslSocket, BufferedReader bufferedReader, BufferedWriter bufferedWriter, NotificationManager notificationManager){
        this.notificationConnection = notificationConnection;
        this.sslSocket = sslSocket;
        this.bufferedReader = bufferedReader;
        this.bufferedWriter = bufferedWriter;
        this.notificationManager = notificationManager;
    }

    @Override
    public void run() {
        try{
            while(true){
                try{
                    String line = bufferedReader.readLine();
                    notificationManager.dispatchNotification(new DataNotification(new JSONObject(line)));
                }catch (JSONException ignore){}
            }
        }catch (Exception e){
            try{bufferedReader.close();}catch (Exception ignore){}
            try{bufferedWriter.close();}catch (Exception ignore){}
            try{sslSocket.close();}catch (Exception ignore){}
            // try reconnecting
            if(notificationConnection.allowReconnect()){
                if(notificationConnection.connect()){
                    logger.warn("Notification Worker Died. Reconnecting.");
                }else{
                    logger.warn("Notification Worker Died. Reconnecting Failed.");
                }
            }else{
                logger.warn("Notification Worker Died. Not Reconnecting.");
            }
        }
    }
}
