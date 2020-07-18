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
import de.netbeacon.jstorage.client.exceptions.JStorageException;
import de.netbeacon.jstorage.client.exceptions.crypt.type.CE_Crypt;
import de.netbeacon.jstorage.client.exceptions.crypt.type.CE_General;
import de.netbeacon.jstorage.client.exceptions.datastorage.type.*;
import de.netbeacon.jstorage.client.exceptions.generic.type.*;
import de.netbeacon.jstorage.client.exceptions.http.type.HE_BadAccess;
import de.netbeacon.jstorage.client.exceptions.http.type.HE_General;
import de.netbeacon.jstorage.client.exceptions.http.type.HE_Processing;
import de.netbeacon.jstorage.client.result.JStorageResult;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.Response;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.HashMap;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

/**
 * This class represents an request and provides functions to execute it
 */
public class JStorageRequest {

    private final JStorageClient client;
    private final Call call;
    private final RequestType requestType;

    /**
     * Creates a new instance of this class
     * @param client the client which should handle the request
     * @param call the actual request wrapped as call
     * @param requestType the type of the request
     */
    protected JStorageRequest(JStorageClient client, Call call, RequestType requestType){
        this.client = client;
        this.call = call;
        this.requestType = requestType;
    }

    /**
     * Can be used to get the request type
     * @return request type
     */
    public RequestType getRequestType() {
        return requestType;
    }

    /**
     * Can be used to execute the request async
     */
    public void queue(){
        queue(null, null);
    }

    /**
     * Can be used to execute the request async
     * @param success will be executed with the result after successful execution
     */
    public void queue(Consumer<JStorageResult> success){
        queue(success, null);
    }

    /**
     * Can be used to execute the request async
     * @param success will be executed with the result after successful execution
     * @param failure will be executed with the exception after failed execution
     */
    public void queue(Consumer<JStorageResult> success, Consumer<JStorageException> failure){
        call.enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                if(failure != null){
                    client.getScalingExecutor().execute(()->failure.accept(new HE_Processing(0, e.getMessage())));
                }
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                // get status code
                int statusCode = response.code();
                if(statusCode == 200 && response.isSuccessful() && success != null){
                    JStorageResult jStorageResult = new JStorageResult();
                    // get body if valid
                    if(response.body() != null && MediaType.parse("application/json").equals(response.body().contentType()) && response.body().contentLength() > 0){
                        jStorageResult.setResult(response.body().bytes());
                    }
                    // execute
                    client.getScalingExecutor().execute(()->success.accept(jStorageResult));
                }else if(failure != null){
                    // get header data
                    HashMap<String, String> headers = new HashMap<>();
                    String additionalInformation = response.header("Additional-Information");
                    if(additionalInformation != null && !additionalInformation.isEmpty()){
                        headers.put("additionalinformation", additionalInformation);
                    }
                    String internalStatus = response.header("Internal-Status");
                    if(internalStatus != null && !internalStatus.isEmpty()){
                        headers.put("internalstatus", internalStatus);
                    }
                    client.getScalingExecutor().execute(()->failure.accept(getException(statusCode, headers)));
                }
            }
        });
    }

    /**
     * Can be used to execute the request
     * <br>
     * Might throw an JStorageException if execution failed
     * @return result on success
     */
    public JStorageResult complete(){
        try(Response response = call.execute()){
            // get status code
            int statusCode = response.code();
            if(statusCode == 200 && response.isSuccessful()){
                JStorageResult jStorageResult = new JStorageResult();
                // get body if valid
                if(response.body() != null && MediaType.parse("application/json").equals(response.body().contentType()) && response.body().contentLength() > 0){
                    jStorageResult.setResult(response.body().bytes());
                }
                // return
                return  jStorageResult;
            }else{
                // get header data
                HashMap<String, String> headers = new HashMap<>();
                String additionalInformation = response.header("Additional-Information");
                if(additionalInformation != null && !additionalInformation.isEmpty()){
                    headers.put("additionalinformation", additionalInformation);
                }
                String internalStatus = response.header("Internal-Status");
                if(internalStatus != null && !internalStatus.isEmpty()){
                    headers.put("internalstatus", internalStatus);
                }
                throw getException(statusCode, headers);
            }
        } catch (IOException e) {
            throw new HE_Processing(0, e.getMessage());
        }
    }

    /**
     * Returns a completable future instead allowing the request to get canceled
     * @return CompletableFuture<JStorageResult>
     */
    public CompletableFuture<JStorageResult> submit(){
        CompletableFuture<JStorageResult> cf = new CompletableFuture<>();
        client.getScalingExecutor().execute(()->{cf.complete(this.complete());});
        return cf;
    }


    /**
     * Internal helper which exception to return
     * @param statusCode http
     * @param headers of the request
     * @return JStorageException
     */
    private JStorageException getException(int statusCode, HashMap<String, String> headers){
        if(headers.containsKey("internalstatus")){
            String[] params = headers.get("internalstatus").split("\\s");
            if(params.length >= 2){
                String exceptionClass = params[0];
                int exceptionType = Integer.parseInt(params[1]);
                String exceptionMsg = headers.get("internalstatus").replaceAll(params[0], "").replaceAll(params[1], "");

                switch(exceptionClass){
                    case "datastorageexception":
                        switch (exceptionType){
                            case 0:
                                return new DSE_General(exceptionType, exceptionMsg);
                            case 101:
                                return new DSE_LoadUnload(exceptionType, exceptionMsg, DSE_LoadUnload.Type.ActionPerform_Load);
                            case 102:
                                return new DSE_LoadUnload(exceptionType, exceptionMsg, DSE_LoadUnload.Type.ActionPerform_Unload);
                            case 110:
                                return new DSE_LoadUnload(exceptionType, exceptionMsg, DSE_LoadUnload.Type.ActionRunning_Unknown);
                            case 111:
                                return new DSE_LoadUnload(exceptionType, exceptionMsg, DSE_LoadUnload.Type.ActionRunning_Load);
                            case 112:
                                return new DSE_LoadUnload(exceptionType, exceptionMsg, DSE_LoadUnload.Type.ActionRunning_Unload);
                            case 120:
                                return new DSE_LoadUnload(exceptionType, exceptionMsg, DSE_LoadUnload.Type.Timeout);
                            case 200:
                            case 201:
                            case 202:
                            case 203:
                            case 204:
                            case 205:
                            case 206:
                                return new DSE_ExpectationFailed(exceptionType, exceptionMsg, DSE_ExpectationFailed.Type.DataNotFound);
                            case 210:
                            case 211:
                            case 212:
                            case 213:
                            case 214:
                            case 215:
                            case 216:
                                return new DSE_ExpectationFailed(exceptionType, exceptionMsg, DSE_ExpectationFailed.Type.DataAlreadyExisting);
                            case 220:
                            case 221:
                                return new DSE_ExpectationFailed(exceptionType, exceptionMsg, DSE_ExpectationFailed.Type.DataMismatch);
                            case 230:
                            case 231:
                            case 232:
                                return new DSE_ExpectationFailed(exceptionType, exceptionMsg, DSE_ExpectationFailed.Type.NotReady);
                            case 240:
                            case 241:
                            case 242:
                                return new DSE_ExpectationFailed(exceptionType, exceptionMsg, DSE_ExpectationFailed.Type.Validation);
                            case 300:
                                return new DSE_DataInconsistency(exceptionType, exceptionMsg);
                            case 400:
                                return new DSE_DataLocked(exceptionType, exceptionMsg);
                            default:
                                return new DSE_General(exceptionType, exceptionMsg);
                        }
                    case "genericobjectexception":
                        switch (exceptionType){
                            case 0:
                                return new GOE_General(exceptionType, exceptionMsg);
                            case 100:
                                return new GOE_NotReady(exceptionType, exceptionMsg, GOE_NotReady.Type.Undefined);
                            case 101:
                                return new GOE_NotReady(exceptionType, exceptionMsg, GOE_NotReady.Type.Loading);
                            case 102:
                                return new GOE_NotReady(exceptionType, exceptionMsg, GOE_NotReady.Type.Unloading);
                            case 200:
                                return new GOE_NotFound(exceptionType, exceptionMsg);
                            case 300:
                                return new GOE_AlreadyExisting(exceptionType, exceptionMsg);
                            case 400:
                                return new GOE_Format(exceptionType, exceptionMsg);
                            default:
                                return new GOE_General(exceptionType, exceptionMsg);
                        }
                    case "cryptexception":
                        switch (exceptionType){
                            case 0:
                                return new CE_General(exceptionType, exceptionMsg, CE_General.Type.NotReady);
                            case 1:
                                return new CE_General(exceptionType, exceptionMsg, CE_General.Type.InvalidPassword);
                            case 2:
                                return new CE_General(exceptionType, exceptionMsg, CE_General.Type.SetupFailed);
                            case 10:
                            case 11:
                                return new CE_Crypt(exceptionType, exceptionMsg, CE_Crypt.Type.Encryption);
                            case 20:
                            case 21:
                                return new CE_Crypt(exceptionType, exceptionMsg, CE_Crypt.Type.Decryption);
                            default:
                                return new CE_General(exceptionType, exceptionMsg, CE_General.Type.Unknown);
                        }
                    default:
                        return new HE_Processing(0, "Unknown Exception");
                }
            }else{
                // invalid params
                return new HE_Processing(0, "Insufficient Parameters For Parsing The Error");
            }
        }else{
            // default http error
            switch (statusCode){
                case 403:
                case 401:
                    return new HE_BadAccess(statusCode, "Authorization Failed");
                default:
                    return new HE_General(statusCode, "Unexpected HTTP Response");
            }
        }
    }
}
