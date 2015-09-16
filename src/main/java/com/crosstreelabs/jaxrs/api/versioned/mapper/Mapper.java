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
package com.crosstreelabs.jaxrs.api.versioned.mapper;

import java.io.IOException;
import java.io.InputStream;

public interface Mapper {
    <T> T convertValue(Object from, Class<T> to);
    
    <T> T readValue(InputStream is, Class<T> to) throws IOException;
    
    String asString(Object from) throws IOException;
    byte[] asBytes(Object from) throws IOException;
}