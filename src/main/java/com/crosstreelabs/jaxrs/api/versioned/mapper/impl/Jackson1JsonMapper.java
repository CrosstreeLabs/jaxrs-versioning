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
package com.crosstreelabs.jaxrs.api.versioned.mapper.impl;

import com.crosstreelabs.jaxrs.api.versioned.mapper.Mapper;
import java.io.IOException;
import java.io.InputStream;
import org.codehaus.jackson.annotate.JsonAutoDetect;
import org.codehaus.jackson.annotate.JsonMethod;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.ObjectMapper;

public class Jackson1JsonMapper implements Mapper {
    public static final String[] SUPPORTS = new String[]{"json"};
    private static final ObjectMapper MAPPER = new ObjectMapper()
            .configure(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .setVisibility(JsonMethod.ALL, JsonAutoDetect.Visibility.NONE)
            .setVisibility(JsonMethod.FIELD, JsonAutoDetect.Visibility.ANY);

    @Override
    public String[] supportedStructures() {
        return SUPPORTS;
    }

    @Override
    public <T> T convertValue(final Object from, final Class<T> to) {
        return MAPPER.convertValue(from, to);
    }

    @Override
    public <T> T readValue(final InputStream is, final Class<T> to) throws IOException {
        return MAPPER.readValue(is, to);
    }

    @Override
    public byte[] asBytes(Object from) throws IOException {
        return MAPPER.writeValueAsBytes(from);
    }
    
}