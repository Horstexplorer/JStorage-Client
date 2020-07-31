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
import de.netbeacon.jstorage.client.notification.objects.connection.NotificationConnection;
import de.netbeacon.jstorage.client.notification.objects.notifiation.DataNotification;
import de.netbeacon.jstorage.client.notification.objects.listener.NotificationListener;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Takes care of managing notifications
 */
public class NotificationManager {

    private final JStorageClient jStorageClient;
    private final ArrayList<NotificationListener> notificationListeners = new ArrayList<>();
    private NotificationConnection notificationConnection;

    /**
     * Creates a new instance of this class
     * @param jStorageClient the superordinate jstorageclient
     */
    public NotificationManager(JStorageClient jStorageClient){
        this.jStorageClient = jStorageClient;
    }

    /**
     * Start receiving notifications with the given settings
     * @param selectedNotifications notifications to receive
     * @param port port of the notification socket
     * @param unsecureSSL use unsecure ssl
     * @return boolean, true on successful start
     */
    public boolean start(HashMap<String, ArrayList<String>> selectedNotifications, int port, boolean unsecureSSL){
        // stop old
        if(notificationConnection != null){
            notificationConnection.disconnect();
        }
        // start new
        notificationConnection = new NotificationConnection(this, selectedNotifications, port);
        notificationConnection.setUnsecureSSL(unsecureSSL);
        return notificationConnection.connect();
    }

    /**
     * Stop recieving notifications
     */
    public void stop(){
        if(notificationConnection != null){
            notificationConnection.disconnect();
        }
    }

    /**
     * Add a notification listener
     * @param notificationListener listener
     */
    public void addNotificationListener(NotificationListener notificationListener){
        notificationListeners.add(notificationListener);
    }

    /**
     * Remove all listeners
     */
    public void clearNotificationListener(){
        notificationListeners.clear();
    }

    /**
     * Used by the notification socket to put notifications into the system
     * @param dataNotification notification
     */
    public void dispatchNotification(DataNotification dataNotification){
        for(NotificationListener n : notificationListeners){
            n.onNotification(dataNotification);
        }
    }

    /**
     * Used by the notification socket to get the login details
     * @return JStorageClient
     */
    public JStorageClient getJStorageClient(){
        return jStorageClient;
    }
}
