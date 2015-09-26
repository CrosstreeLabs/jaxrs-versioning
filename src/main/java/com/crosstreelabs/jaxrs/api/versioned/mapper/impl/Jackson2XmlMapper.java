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
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import java.io.IOException;
import java.io.InputStream;

public class Jackson2XmlMapper implements Mapper {
    public static final String[] SUPPORTS = new String[]{"xml"};
    private static final ObjectMapper MAPPER = new XmlMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.NONE)
            .setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);

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
    public byte[] asBytes(final Object from) throws IOException {
        return MAPPER.writer().withRootName("xml").writeValueAsBytes(from);
    }
    
}