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

package de.netbeacon.jstorage.client.exceptions.datastorage.type;

import de.netbeacon.jstorage.client.exceptions.datastorage.DataStorageException;

/**
 * Specialized exception
 */
public class DSE_ExpectationFailed extends DataStorageException {

    public enum Type{
        DataNotFound,
        DataAlreadyExisting,
        DataMismatch,
        NotReady,
        Validation
    }

    private final Type type;

    /**
     * Creates a new instance of this class
     * @param errorCode error code matching super
     * @param message error message matching super
     * @param type of the exception
     */
    public DSE_ExpectationFailed(int errorCode, String message, Type type) {
        super(errorCode, message);
        this.type = type;
    }

    /**
     * Used to get the specialized exception type
     * @return type
     */
    public Type getType() {
        return type;
    }

}
