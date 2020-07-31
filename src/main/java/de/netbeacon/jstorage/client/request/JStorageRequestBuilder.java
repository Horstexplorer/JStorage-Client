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

import de.netbeacon.jstorage.client.JStorageClient;
import okhttp3.*;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * This class takes care of building requests
 */
public class JStorageRequestBuilder {

    private final JStorageClient client;
    private final RequestType type;
    private final HashMap<String, String> args = new HashMap<>();
    private JSONObject payload;

    private final Logger logger = LoggerFactory.getLogger(JStorageRequestBuilder.class);

    /**
     * Creates a new instance of this class
     * @param client the JStorageClient in charge of this request(builder)
     * @param type of the request
     */
    public JStorageRequestBuilder(JStorageClient client, RequestType type){
        this.client = client;
        this.type = type;
    }

    /**
     * Can be used to add url parameters
     * @param key key
     * @param value value
     */
    public JStorageRequestBuilder setArg(String key, String value){
        args.put(key.toLowerCase(), value);
        return this;
    }

    /**
     * Can be used to add a payload to the request
     * @param jsonObject payload
     */
    public JStorageRequestBuilder setPayload(JSONObject jsonObject){
        this.payload = jsonObject;
        return this;
    }

    /**
     * Can be used to build the request
     * @return JStorageRequest
     */
    public JStorageRequest build(){
        // check args
        if(!args.keySet().containsAll(type.getRequiredArguments())){
            logger.warn("Missing Arguments For This Type Of Request. Expected: "+ Arrays.toString(type.getRequiredArguments().toArray())+" Provided: "+Arrays.toString(args.keySet().toArray()));
        }
        // check payload
        if(payload == null && type.requiresBody()){
            logger.warn("Missing Payload For This Type Of Request. Adding Empty Body - This Might Cause Errors");
        }
        // build url
        HttpUrl.Builder urlBuilder = new HttpUrl.Builder();
        urlBuilder
                .scheme("https")
                .host(client.getHost())
                .port(client.getPort());
        // add path
        for(String subpath : type.getRequestPath()){
            urlBuilder.addPathSegment(subpath);
        }
        // add args
        for(Map.Entry<String, String> arg : args.entrySet()){
            urlBuilder.addQueryParameter(arg.getKey(), arg.getValue());
        }
        // build request
        okhttp3.Request.Builder requestBuilder = new okhttp3.Request.Builder();
        requestBuilder.url(urlBuilder.build());
        // add required auth
        switch(type.getRequiredAuthMode()){
            case -1:
                if(client.getLoginToken() != null){
                    requestBuilder.header("Token", (client.getLoginToken() != null)? client.getLoginToken() : "");
                }else if(client.getUserId() != null && client.getPassword() != null){
                    requestBuilder.header("Authorization", Credentials.basic(
                            (client.getUserId() != null)? client.getUserId() : "",
                            (client.getPassword() != null)? client.getPassword() : ""));
                }else{
                    logger.warn("Missing Auth For This Request. Adding Empty Auth.");
                }
                break;
            case 0:
                if(client.getUserId() == null || client.getPassword() == null){
                    logger.warn("Missing Auth ID/Password For This Request. Adding Empty Auth.");
                }
                requestBuilder.header("Authorization", Credentials.basic(
                        (client.getUserId() != null)? client.getUserId() : "",
                        (client.getPassword() != null)? client.getPassword() : ""));
                break;
            case 1:
                if(client.getLoginToken() == null){
                    logger.warn("Missing Auth Token For This Request. Adding Empty Auth.");
                }
                requestBuilder.header("Token", (client.getLoginToken() != null)? client.getLoginToken() : "");
                break;
        }
        // choose method
        switch(type.getRequestType()){
            case "GET":
                requestBuilder.get();
                break;
            case "PUT":
                if(payload == null){
                    requestBuilder.put(RequestBody.create("{}".getBytes(), MediaType.get("application/json")));
                }else{
                    requestBuilder.put(RequestBody.create(payload.toString().getBytes(), MediaType.get("application/json")));
                }
                break;
            case "DELETE":
                if(payload == null){
                    requestBuilder.delete();
                }else{
                    requestBuilder.delete(RequestBody.create(payload.toString().getBytes(), MediaType.get("application/json")));
                }
                break;
        }

        Call call = client.getOkHttpClient().newCall(requestBuilder.build());
        return new JStorageRequest(client, call, type);
    }
}
