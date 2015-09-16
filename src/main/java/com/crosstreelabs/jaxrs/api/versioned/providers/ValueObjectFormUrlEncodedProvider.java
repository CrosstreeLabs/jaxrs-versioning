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

import com.crosstreelabs.jaxrs.api.versioned.ValueObject;
import com.crosstreelabs.jaxrs.api.versioned.mapper.Mapper;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.MessageBodyWriter;

public class ValueObjectFormUrlEncodedProvider
        implements MessageBodyReader<ValueObject>,
                MessageBodyWriter<ValueObject> {
    
    private static final HierarchicalMapFormUrlEncodedProvider MAP_PROVIDER
            = new HierarchicalMapFormUrlEncodedProvider();
    private final Mapper mapper;
    @Context
    private HttpServletRequest request;
    
    public ValueObjectFormUrlEncodedProvider(final Mapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public boolean isReadable(final Class<?> type,
            final Type genericType,
            final Annotation[] annotations,
            final MediaType mediaType) {
        return ValueObject.class.isAssignableFrom(type);
    }

    @Override
    public ValueObject readFrom(final Class<ValueObject> type,
            final Type genericType,
            final Annotation[] annotations,
            final MediaType mediaType,
            final MultivaluedMap<String, String> httpHeaders,
            final InputStream entityStream)
            throws IOException {
        Map<String, Object> map = MAP_PROVIDER.readFrom(request, annotations, entityStream);
        return mapper.convertValue(map, type);
    }

    @Override
    public boolean isWriteable(final Class<?> type,
            final Type genericType,
            final Annotation[] annotations,
            final MediaType mediaType) {
        return ValueObject.class.isAssignableFrom(type);
    }

    @Override
    public long getSize(final ValueObject t,
            final Class<?> type,
            final Type genericType,
            final Annotation[] annotations,
            final MediaType mediaType) {
        return -1;
    }

    @Override
    public void writeTo(final ValueObject t,
            final Class<?> type,
            final Type genericType,
            final Annotation[] annotations,
            final MediaType mediaType,
            final MultivaluedMap<String, Object> httpHeaders,
            final OutputStream entityStream)
            throws IOException, WebApplicationException {
        entityStream.write(mapper.asBytes(t));
    }
    
}