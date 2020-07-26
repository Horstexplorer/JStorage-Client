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

package de.netbeacon.jstorage.client.request;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * This class contains all possible request types with their expected data
 */
public enum RequestType {
    /*          Cache Actions           */
    CacheAction_CacheInfo(Arrays.asList("cache", "info"), "GET", new ArrayList<>(), false, 1),
    CacheAction_CacheSettings(Arrays.asList("cache", "settings"), "PUT", new ArrayList<>(), true, 1),

    CacheAction_CacheClear(Arrays.asList("cache", "mng", "clear"), "DELETE", Arrays.asList("identifier"), false, 1),
    CacheAction_CreateCache(Arrays.asList("cache", "mng", "create"), "PUT", Arrays.asList("identifier"), false, 1),
    CacheAction_DeleteCache(Arrays.asList("cache", "mng", "delete"), "DELETE", Arrays.asList("identifier"), false, 1),

    CacheAction_GetCachedData(Arrays.asList("cache", "data", "get"), "GET", Arrays.asList("cache", "identifier"), false, 1),
    CacheAction_CreateCachedData(Arrays.asList("cache", "data", "create"), "PUT", Arrays.asList("cache", "identifier"), true, 1),
    CacheAction_DeleteCachedData(Arrays.asList("cache", "data", "delete"), "DELETE", Arrays.asList("cache", "identifier"), false, 1),

    /*          Data Actions            */
    DataAction_CreateDataBase(Arrays.asList("data", "db", "create"), "PUT", Arrays.asList("identifier"), false, 1),
    DataAction_CreateDataTable(Arrays.asList("data", "db", "table", "create"), "PUT", Arrays.asList("database", "identifier"), false, 1),
    DataAction_CreateDataSet(Arrays.asList("data", "db", "table", "dataset", "create"), "PUT", Arrays.asList("database", "table", "identifier"), false, 1),
    DataAction_CreateDataType(Arrays.asList("data", "db", "table", "dataset", "datatype", "create"), "PUT", Arrays.asList("database", "table", "dataset", "identifier"), false, 1),

    DataAction_DataBaseInfo(Arrays.asList("data", "db", "info"), "GET", Arrays.asList(), false, 1),
    DataAction_DataBaseSettings(Arrays.asList("data", "db", "settings"), "PUT", Arrays.asList("identifier"), true, 1),
    DataAction_DataTableInfo(Arrays.asList("data", "db", "table", "info"), "GET", Arrays.asList("database", "identifier"), false, 1),
    DataAction_DataTableSettings(Arrays.asList("data", "db", "table", "settings"), "PUT", Arrays.asList("database", "identifier"), true, 1),
    DataAction_DataSetInfo(Arrays.asList("data", "db", "table", "dataset", "info"), "GET", Arrays.asList("database", "table", "identifier"), false, 1),
    DataAction_DataSetSettings(Arrays.asList("data", "db", "table", "dataset"), "PUT", Arrays.asList(), true, 1),

    DataAction_DeleteDataBase(Arrays.asList("data", "db", "delete"), "DELETE", Arrays.asList("identifier"), false, 1),
    DataAction_DeleteDataTable(Arrays.asList("data", "db", "table", "delete"), "DELETE", Arrays.asList("database", "identifier"), false, 1),
    DataAction_DeleteDataSet(Arrays.asList("data", "db", "table", "dataset", "delete"), "DELETE", Arrays.asList("database", "table", "identifier"), false, 1),
    DataAction_DeleteDataType(Arrays.asList("data", "db", "table", "dataset", "datatype", "delete"), "DELETE", Arrays.asList("database", "table", "dataset", "identifier"), false, 1),

    DataAction_GetDataSet(Arrays.asList("data", "db", "table", "dataset", "get"), "GET", Arrays.asList("database", "table", "identifier"), false, 1),
    DataAction_GetDataType(Arrays.asList("data", "db", "table", "dataset", "datatype", "get"), "GET", Arrays.asList("database", "table", "dataset", "identifier"), false, 1),

    DataAction_UpdateDataType(Arrays.asList("data", "db", "table", "dataset", "datatype", "update"), "PUT", Arrays.asList("database", "table", "dataset", "identifier"), true, 1),

    DataAction_MultiSelect(Arrays.asList("data", "tool", "multiselect"), "PUT", Arrays.asList(), true, 1),

    /*          Info Action         */

    InfoAction_Basic(Arrays.asList("info", "basic", "info"), "GET", Arrays.asList(), false, -1),
    InfoAction_Statistics(Arrays.asList("info", "stats", "statistics"), "GET", Arrays.asList(), false, 1),

    /*          User Action         */

    UserAction_CreateUser(Arrays.asList("user", "mng", "create"), "PUT", Arrays.asList("identifier"), false, 1),
    UserAction_DeleteUser(Arrays.asList("user", "mng", "delete"), "DELETE", Arrays.asList("identifier"), false, 1),
    UserAction_UserChangePassword(Arrays.asList("user", "mng", "changepw"), "PUT", Arrays.asList("password"), false, 0),
    UserAction_GetNewLoginToken(Arrays.asList("user", "mng", "getnewlogintoken"), "PUT", Arrays.asList(), false, 0),
    UserAction_UserInfo(Arrays.asList("user", "info"), "GET", Arrays.asList(), false, 1),
    UserAction_X(Arrays.asList("user", "settings"), "PUT", Arrays.asList(), true, 1),
    ;


    private final List<String> requestPath;
    private final String requestType;
    private final List<String> requiredArguments;
    private final boolean requiresBody;
    private final int requiredAuthMode;

    private RequestType(List<String> requestPath, String requestType, List<String> requiredArguments, boolean requiresBody, int requiredAuthMode){
        this.requestPath = requestPath;
        this.requestType = requestType;
        this.requiresBody = requiresBody;
        this.requiredArguments = requiredArguments;
        this.requiredAuthMode = requiredAuthMode;
    }

    public List<String> getRequestPath() {
        return requestPath;
    }

    public String getRequestType() {
        return requestType;
    }

    public boolean requiresBody() {
        return requiresBody;
    }

    public List<String> getRequiredArguments() {
        return requiredArguments;
    }

    public int getRequiredAuthMode() {
        return requiredAuthMode;
    }

}
