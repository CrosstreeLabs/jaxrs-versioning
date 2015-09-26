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
import com.google.gson.Gson;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public class GsonJsonMapper implements Mapper {
    public static final String[] SUPPORTS = new String[]{"json"};
    private static final Gson GSON = new Gson();

    @Override
    public String[] supportedStructures() {
        return SUPPORTS;
    }

    @Override
    public <T> T convertValue(final Object from, final Class<T> to) {
        return GSON.fromJson(GSON.toJson(from), to);
    }

    @Override
    public <T> T readValue(final InputStream is, final Class<T> to) {
        return GSON.fromJson(new InputStreamReader(is), to);
    }

    @Override
    public byte[] asBytes(final Object from) throws IOException {
        return GSON.toJson(from).getBytes(StandardCharsets.UTF_8);
    }
    
}