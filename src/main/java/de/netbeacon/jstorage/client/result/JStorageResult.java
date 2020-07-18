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

package de.netbeacon.jstorage.client.result;

import org.json.JSONObject;

/**
 * This class contains the result of a successful request
 */
public class JStorageResult {

    private byte[] result = new byte[0];

    /**
     * Creates a new instance of this class
     */
    public JStorageResult(){}

    /**
     * Used to add data to this result
     * @param bytes jsonobject as string as bytes
     */
    public void setResult(byte[] bytes){
        if(bytes != null && bytes.length > 0){
            result = bytes;
        }
    }

    /**
     * Can be used to get the raw response bytes
     * @return bytes[]
     */
    public byte[] getResultRaw() {
        return result;
    }

    /**
     * Can be used to get an JSONObject from the bytes
     * @return JSONObject
     */
    public JSONObject getResult(){
        return new JSONObject(new String(result));
    }
}
