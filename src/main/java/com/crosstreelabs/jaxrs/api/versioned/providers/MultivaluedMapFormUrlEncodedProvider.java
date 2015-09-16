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
import com.crosstreelabs.jaxrs.api.versioned.util.StringUtils;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.Encoded;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;

/**
 * This provider is pilfered largely from
 * https://github.com/resteasy/Resteasy/blob/master/jaxrs/resteasy-jaxrs/src/main/java/org/jboss/resteasy/plugins/providers/FormUrlEncodedProvider.java
 */
@Provider
@Produces("application/x-www-form-urlencoded")
@Consumes("application/x-www-form-urlencoded")
public class MultivaluedMapFormUrlEncodedProvider
        implements MessageBodyReader<MultivaluedMap>,
                MessageBodyWriter<MultivaluedMap> {
    
    @Context
    HttpServletRequest request;
    
    @Override
    public boolean isReadable(final Class<?> type,
            final Type genericType,
            final Annotation[] annotations,
            final MediaType mediaType) {
        return MultivaluedMap.class.equals(type)
                && MediaType.APPLICATION_FORM_URLENCODED_TYPE.isCompatible(mediaType);
    }

    @Override
    public MultivaluedMap readFrom(
            final Class<MultivaluedMap> type,
            final Type genericType,
            final Annotation[] annotations,
            final MediaType mediaType,
            final MultivaluedMap<String, String> httpHeaders,
            final InputStream entityStream) throws IOException {
        // Request body params
        MultivaluedMap result = parseForm(entityStream);
        
        // Query string params
        if (request != null) {
            Map<String, String[]> tmp = request.getParameterMap();
            Collection<String> coll = new ArrayList<>();
            for (String key : tmp.keySet()) {
                for (String value : tmp.get(key)) {
                    coll.add(key+"="+value);
                }
            }
            String tmpStr = StringUtils.join(coll,"&");
            result.putAll(QueryStringUtils.toMap(tmpStr));
        }
        
        // Decode if required
        if (AnnotationUtils.find(Encoded.class, annotations) == null) {
            return (MultivaluedMap)EncodingUtils.decode(result, StandardCharsets.UTF_8);
        }
        return result;
    }

    @Override
    public boolean isWriteable(final Class<?> type,
            final Type genericType,
            final Annotation[] annotations,
            final MediaType mediaType) {
        return MultivaluedMap.class.isAssignableFrom(type)
                && MediaType.APPLICATION_FORM_URLENCODED_TYPE.isCompatible(mediaType);
    }

    @Override
    public long getSize(final MultivaluedMap stringStringMultivaluedMap,
            final Class<?> type,
            final Type genericType,
            final Annotation[] annotations,
            final MediaType mediaType) {
        return -1;
    }

    @Override
    public void writeTo(final MultivaluedMap data,
            final Class<?> type,
            final Type genericType,
            final Annotation[] annotations,
            final MediaType mediaType,
            final MultivaluedMap<String, Object> httpHeaders,
            final OutputStream entityStream) throws IOException {
        MultivaluedMap<String, String> formData = (MultivaluedMap<String, String>)data;
        boolean encoded = AnnotationUtils.find(Encoded.class, annotations) != null;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        OutputStreamWriter writer = new OutputStreamWriter(baos, "UTF-8");

        boolean first = true;
        for (Map.Entry<String, List<String>> entry : formData.entrySet()) {
            String encodedName = entry.getKey();
            if (!encoded) encodedName = URLEncoder.encode(entry.getKey(), "UTF-8");

            for (String value : entry.getValue()) {
                if (first) first = false;
                else writer.write("&");
                if (!encoded) {
                    value = URLEncoder.encode(value, "UTF-8");
                }
                writer.write(encodedName);
                writer.write("=");
                writer.write(value);
            }
            writer.flush();
        }

        byte[] bytes = baos.toByteArray();
        entityStream.write(bytes);
    }

    protected static MultivaluedMap<String, String> parseForm(
            final InputStream entityStream)
            throws IOException {
        String str = StreamUtils.toString(entityStream);

        MultivaluedMap<String, String> result = new MultivaluedHashMap<>();
        if (str == null || str.isEmpty()) {
            return result;
        }
        String[] params = str.split("&");

        for (String param : params) {
            if (param.indexOf('=') >= 0) {
                String[] nv = param.split("=");
                String val = nv.length > 1 ? nv[1] : "";
                result.add(nv[0], val);
            } else {
                result.add(param, "");
            }
        }
        return result;
    }
}