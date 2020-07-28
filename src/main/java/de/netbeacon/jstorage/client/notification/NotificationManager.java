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

package de.netbeacon.jstorage.client.notification;

import de.netbeacon.jstorage.client.JStorageClient;
import de.netbeacon.jstorage.client.notification.objects.DataNotification;
import de.netbeacon.jstorage.client.notification.objects.NotificationListener;
import de.netbeacon.jstorage.client.notification.objects.NotificationSocket;

import java.util.ArrayList;
import java.util.HashMap;

public class NotificationManager {

    private final JStorageClient jStorageClient;
    private final ArrayList<NotificationListener> notificationListeners = new ArrayList<>();
    private Thread notificationSocketThread;

    public NotificationManager(JStorageClient jStorageClient){
        this.jStorageClient = jStorageClient;
    }

    public void start(HashMap<String, ArrayList<String>> selectedNotifications, int port, boolean unsecure){
        // stop old
        if(notificationSocketThread != null){
            notificationSocketThread.interrupt();
        }
        // start new
        notificationSocketThread = new Thread(new NotificationSocket(this, selectedNotifications, port, unsecure));
        notificationSocketThread.setDaemon(true);
        notificationSocketThread.start();
    }

    public void stop(){
        if(notificationSocketThread != null){
            notificationSocketThread.interrupt();
        }
    }

    public void addNotificationListener(NotificationListener notificationListener){
        notificationListeners.add(notificationListener);
    }

    public void clearNotificationListener(){
        notificationListeners.clear();
    }

    public void dispatchNotification(DataNotification dataNotification){
        for(NotificationListener n : notificationListeners){
            n.onNotification(dataNotification);
        }
    }

    public JStorageClient getJStorageClient(){
        return jStorageClient;
    }
}
