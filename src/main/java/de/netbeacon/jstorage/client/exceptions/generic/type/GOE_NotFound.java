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

package de.netbeacon.jstorage.client.exceptions.generic.type;

import de.netbeacon.jstorage.client.exceptions.generic.GenericObjectException;

/**
 * Specialized exception
 */
public class GOE_NotFound extends GenericObjectException {

    /**
     * Creates a new instance of this class
     * @param errorCode error code matching super
     * @param message error message matching super
     */
    public GOE_NotFound(int errorCode, String message) {
        super(errorCode, message);
    }

}
