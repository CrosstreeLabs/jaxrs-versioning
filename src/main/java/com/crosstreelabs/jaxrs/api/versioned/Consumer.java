/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.crosstreelabs.jaxrs.api.versioned;

import java.util.Map;

/**
 * A ValueObject implementation may optionally implement the Consumer interface
 * to indicate that automatic object mapping should not occur, and that the
 * implementation should receive a Map of data from the request in order to
 * apply that data to itself in a customizable fashion.
 */
public interface Consumer {
    /**
     * Consumes the appropriate data from the given map. This allows complete
     * customization of how data is retrieved from a request, and avoids
     * reliance on any additional libraries for object mapping.
     * @param data The data from the request.
     * @return The value object instance
     */
    ValueObject consume(Map data);
}