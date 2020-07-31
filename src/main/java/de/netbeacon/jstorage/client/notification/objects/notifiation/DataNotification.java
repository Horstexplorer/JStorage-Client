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

package de.netbeacon.jstorage.client.notification.objects.notifiation;

import org.json.JSONObject;

/**
 * This class represents a received notification
 */
public class DataNotification {

    public enum Content{
        heartbeat,
        created,
        updated,
        deleted
    }

    private final long timestamp;
    private final Content content;
    private String database;
    private String table;
    private String dataset;
    private String datatype;


    /**
     * Creates a new instance of this class
     * @param jsonObject JSONObject
     */
    public DataNotification(JSONObject jsonObject){
        content = Content.valueOf(jsonObject.getString("content"));
        timestamp = jsonObject.getLong("timestamp");
        if(jsonObject.has("database")){
            database = jsonObject.getString("database");
        }
        if(jsonObject.has("table")){
            table = jsonObject.getString("table");
        }
        if(jsonObject.has("dataset")){
            dataset = jsonObject.getString("dataset");
        }
        if(jsonObject.has("datatype")){
            datatype = jsonObject.getString("datatype");
        }
    }

    /**
     * Used to return the content
     * @return String or null
     */
    public Content getContent() {
        return content;
    }

    /**
     * Used to return the timestamp
     * @return String or null
     */
    public long getTimestamp() {
        return timestamp;
    }

    /**
     * Used to return the database
     * @return String or null
     */
    public String getDatabase() {
        return database;
    }

    /**
     * Used to return the table
     * @return String or null
     */
    public String getTable() {
        return table;
    }

    /**
     * Used to return the dataset
     * @return String or null
     */
    public String getDataset() {
        return dataset;
    }

    /**
     * Used to return the datatype
     * @return String or null
     */
    public String getDatatype() {
        return datatype;
    }

}
