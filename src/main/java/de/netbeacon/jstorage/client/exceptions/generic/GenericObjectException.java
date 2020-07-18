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

package de.netbeacon.jstorage.client.exceptions.generic;

import de.netbeacon.jstorage.client.exceptions.JStorageException;

/**
 * Blueprint for wrapping server side exceptions of the type GenericObjectException
 */
public abstract class GenericObjectException extends JStorageException {

    /**
     * Constructor matching super
     * @param errorCode contains the error code
     * @param message  contains the error message
     */
    public GenericObjectException(int errorCode, String message) {
        super(errorCode, message);
    }

}
