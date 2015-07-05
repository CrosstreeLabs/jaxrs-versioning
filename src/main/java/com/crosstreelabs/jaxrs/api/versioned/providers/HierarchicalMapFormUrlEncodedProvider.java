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

import com.crosstreelabs.jaxrs.api.versioned.util.AnnotationUtils;
import com.crosstreelabs.jaxrs.api.versioned.util.EncodingUtils;
import com.crosstreelabs.jaxrs.api.versioned.util.QueryStringUtils;
import com.crosstreelabs.jaxrs.api.versioned.util.StreamUtils;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import javax.ws.rs.Consumes;
import javax.ws.rs.Encoded;
import javax.ws.rs.Produces;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;

@Provider
@Produces("application/x-www-form-urlencoded")
@Consumes("application/x-www-form-urlencoded")
public class HierarchicalMapFormUrlEncodedProvider
        implements MessageBodyReader<Map>,
                MessageBodyWriter<Map> {
    @Override
    public boolean isReadable(final Class<?> type,
            final Type genericType,
            final Annotation[] annotations,
            final MediaType mediaType) {
        return Map.class.equals(type) || HashMap.class.equals(type);
    }

    @Override
    public Map readFrom(
            final Class<Map> type,
            final Type genericType,
            final Annotation[] annotations,
            final MediaType mediaType,
            final MultivaluedMap<String, String> httpHeaders,
            final InputStream entityStream) throws IOException {
        if (isContentLengthZero(httpHeaders)) {
            return new HashMap<>();
        }
        Map<String, Object> result
                = QueryStringUtils.toMap(StreamUtils.toString(entityStream));
        
        if (AnnotationUtils.find(Encoded.class, annotations) != null) {
            return result;
        }
        return EncodingUtils.decode(result);
    }

    @Override
    public boolean isWriteable(final Class<?> type,
            final Type genericType,
            final Annotation[] annotations,
            final MediaType mediaType) {
        return Map.class.isAssignableFrom(type) || HashMap.class.equals(type);
    }

    @Override
    public long getSize(final Map map,
            final Class<?> type,
            final Type genericType,
            final Annotation[] annotations,
            final MediaType mediaType) {
        return -1;
    }

    @Override
    public void writeTo(final Map data,
            final Class<?> type,
            final Type genericType,
            final Annotation[] annotations,
            final MediaType mediaType,
            final MultivaluedMap<String, Object> httpHeaders,
            final OutputStream entityStream) throws IOException {
        
        boolean encoded = AnnotationUtils.find(Encoded.class, annotations) != null;
        entityStream.write(QueryStringUtils.toQueryString(data, encoded).getBytes(StandardCharsets.UTF_8));
    }

    protected static boolean isContentLengthZero(final MultivaluedMap httpHeaders) {
        if (httpHeaders == null) {
            return false;
        }
        String contentLength = (String)httpHeaders.getFirst(HttpHeaders.CONTENT_LENGTH);
        if (contentLength != null) {
            long length = Long.parseLong(contentLength);
            if (length == 0) {
                return true;
            }
        }
        return false;
    }
}