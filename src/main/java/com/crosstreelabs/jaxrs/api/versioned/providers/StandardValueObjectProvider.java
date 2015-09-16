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
package com.crosstreelabs.jaxrs.api.versioned.providers;

import com.crosstreelabs.jaxrs.api.versioned.AbstractValueObjectReaderWriter;
import com.crosstreelabs.jaxrs.api.versioned.ValueObject;
import com.crosstreelabs.jaxrs.api.versioned.mapper.Mapper;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.util.Map;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;

public class StandardValueObjectProvider
        extends AbstractValueObjectReaderWriter {
    
    private final Mapper mapper;
    
    public StandardValueObjectProvider(final Mapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public Map readMap(final InputStream entityStream) throws IOException {
        return mapper.readValue(entityStream, Map.class);
    }

    @Override
    public ValueObject readObject(final InputStream entityStream,
            final ValueObject vo) throws IOException {
        return mapper.readValue(entityStream, vo.getClass());
    }

    @Override
    public void write(final ValueObject obj,
            final Annotation[] annotations,
            final MediaType mediaType,
            final MultivaluedMap<String, Object> headers,
            final OutputStream entityStream) throws IOException {
        entityStream.write(mapper.asBytes(obj));
    }
    
}