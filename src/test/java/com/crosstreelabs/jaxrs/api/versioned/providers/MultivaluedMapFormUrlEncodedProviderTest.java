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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import javax.ws.rs.Encoded;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import org.glassfish.jersey.internal.util.collection.MultivaluedStringMap;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import org.junit.Test;
import static org.mockito.Mockito.mock;

public class MultivaluedMapFormUrlEncodedProviderTest {
    protected static final MultivaluedMapFormUrlEncodedProvider UNDER_TEST
            = new MultivaluedMapFormUrlEncodedProvider();
    protected static final Annotation[] EMPTY_ANNOTATIONS = new Annotation[0];
    protected static final Annotation[] ENCODED = new Annotation[]{
            mock(Encoded.class)
    };
    
    @Test
    public void testIsReadable() {
        assertThat(UNDER_TEST.isReadable(MultivaluedMap.class, MultivaluedMap.class, EMPTY_ANNOTATIONS, MediaType.APPLICATION_FORM_URLENCODED_TYPE),
                is(true));
        assertThat(UNDER_TEST.isReadable(Map.class, Map.class, EMPTY_ANNOTATIONS, MediaType.APPLICATION_FORM_URLENCODED_TYPE),
                is(false));
        assertThat(UNDER_TEST.isReadable(HashMap.class, HashMap.class, EMPTY_ANNOTATIONS, MediaType.APPLICATION_FORM_URLENCODED_TYPE),
                is(false));
        assertThat(UNDER_TEST.isReadable(MultivaluedMap.class, MultivaluedMap.class, EMPTY_ANNOTATIONS, MediaType.MULTIPART_FORM_DATA_TYPE),
                is(false));
    }

    @Test
    public void testReadFrom() throws Exception {
        MultivaluedMap expected = new MultivaluedHashMap();
        expected.add("a", "1");
        expected.addAll("b", "2", "3", "4");
        expected.add("c", "5");
        
        assertThat(UNDER_TEST.readFrom(MultivaluedMap.class, MultivaluedMap.class, EMPTY_ANNOTATIONS, MediaType.APPLICATION_FORM_URLENCODED_TYPE, new MultivaluedStringMap(), qs("a=1&b=2&b=3&b=4&c=5")),
                is(expected));
    }

    @Test
    public void testIsWriteable() {
        assertThat(UNDER_TEST.isWriteable(MultivaluedMap.class, MultivaluedMap.class, EMPTY_ANNOTATIONS, MediaType.APPLICATION_FORM_URLENCODED_TYPE),
                is(true));
        assertThat(UNDER_TEST.isWriteable(Map.class, Map.class, EMPTY_ANNOTATIONS, MediaType.APPLICATION_FORM_URLENCODED_TYPE),
                is(false));
        assertThat(UNDER_TEST.isWriteable(HashMap.class, HashMap.class, EMPTY_ANNOTATIONS, MediaType.APPLICATION_FORM_URLENCODED_TYPE),
                is(false));
        assertThat(UNDER_TEST.isWriteable(MultivaluedMap.class, MultivaluedMap.class, EMPTY_ANNOTATIONS, MediaType.MULTIPART_FORM_DATA_TYPE),
                is(false));
    }

    @Test
    public void testWriteTo() throws Exception {
        MultivaluedMap map = new MultivaluedHashMap();
        map.add("a", "1");
        map.addAll("b", "2", "3", "4");
        map.add("c", "5");
        
        assertThat(write(map, MultivaluedMap.class, MultivaluedMap.class, EMPTY_ANNOTATIONS, MediaType.APPLICATION_FORM_URLENCODED_TYPE, new MultivaluedHashMap<String, Object>()),
                allOf(
                        containsString("a=1"),
                        containsString("b=2"),
                        containsString("b=3"),
                        containsString("b=4"),
                        containsString("c=5")
                ));
    }
    
    protected String write(final MultivaluedMap data,
            final Class<?> type,
            final Type genericType,
            final Annotation[] annotations,
            final MediaType mediaType,
            final MultivaluedMap<String, Object> httpHeaders) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        UNDER_TEST.writeTo(data, type, genericType, annotations, mediaType, httpHeaders, baos);
        return baos.toString();
    }
    protected static InputStream qs(final String str) {
        return new ByteArrayInputStream(str.getBytes(StandardCharsets.UTF_8));
    }
}