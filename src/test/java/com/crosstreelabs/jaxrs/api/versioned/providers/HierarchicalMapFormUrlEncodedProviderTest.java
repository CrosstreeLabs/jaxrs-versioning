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

import com.google.gson.Gson;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
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
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;
import org.junit.Test;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

public class HierarchicalMapFormUrlEncodedProviderTest {
    protected static final HierarchicalMapFormUrlEncodedProvider UNDER_TEST
            = new HierarchicalMapFormUrlEncodedProvider();
    protected static final Gson GSON = new Gson();
    protected static final Annotation[] EMPTY_ANNOTATIONS = new Annotation[0];
    protected static final Annotation[] ENCODED;
    static {
        Encoded e = mock(Encoded.class);
        doReturn(Encoded.class).when(e).annotationType();
        ENCODED = new Annotation[]{
                e
        };
    }

    @Test
    public void testIsReadable() {
        assertThat(UNDER_TEST.isReadable(Map.class, Map.class, EMPTY_ANNOTATIONS, MediaType.APPLICATION_FORM_URLENCODED_TYPE),
                is(true));
        assertThat(UNDER_TEST.isReadable(HashMap.class, HashMap.class, EMPTY_ANNOTATIONS, MediaType.APPLICATION_FORM_URLENCODED_TYPE),
                is(true));
        assertThat(UNDER_TEST.isReadable(Map.class, Map.class, EMPTY_ANNOTATIONS, MediaType.MULTIPART_FORM_DATA_TYPE),
                is(true));
        assertThat(UNDER_TEST.isReadable(MultivaluedMap.class, MultivaluedMap.class, EMPTY_ANNOTATIONS, MediaType.APPLICATION_FORM_URLENCODED_TYPE),
                is(false));
    }

    @Test
    public void testReadFrom() throws Exception {
        Map expected = GSON.fromJson("{\"a\":\"1\",\"b\":{\"0\":\"2\",\"a\":\"3\",\"1\":\"4\"},\"c\":{\"a\":{\"b\":\"5\"}},\"d\":\"\"}", Map.class);
        
        assertThat(UNDER_TEST.readFrom(Map.class, Map.class, EMPTY_ANNOTATIONS, MediaType.APPLICATION_FORM_URLENCODED_TYPE, new MultivaluedStringMap(), qs("a=1&b[]=2&b[a]=3&b[]=4&c.a[b]=5&d")),
                is(expected));
        assertThat(UNDER_TEST.readFrom(Map.class, Map.class, EMPTY_ANNOTATIONS, MediaType.APPLICATION_FORM_URLENCODED_TYPE, new MultivaluedStringMap(), qs("")),
                is(Collections.EMPTY_MAP));
    }
    
    @Test
    public void testReadEncoding() throws Exception {
        Map<String, Object> result = UNDER_TEST.readFrom(Map.class, Map.class, ENCODED, MediaType.APPLICATION_FORM_URLENCODED_TYPE, new MultivaluedHashMap<String, String>(), qs("a%c5%92=%c5%a0"));
        assertThat(result, is(Collections.singletonMap("a%c5%92", (Object)"%c5%a0")));
        result = UNDER_TEST.readFrom(Map.class, Map.class, EMPTY_ANNOTATIONS, MediaType.APPLICATION_FORM_URLENCODED_TYPE, new MultivaluedHashMap<String, String>(), qs("a%c5%92=%c5%a0"));
        assertThat(result, is(Collections.singletonMap("aŒ", (Object)"Š")));
    }

    @Test
    public void testIsWriteable() {
        assertThat(UNDER_TEST.isWriteable(Map.class, Map.class, EMPTY_ANNOTATIONS, MediaType.APPLICATION_FORM_URLENCODED_TYPE),
                is(true));
        assertThat(UNDER_TEST.isWriteable(HashMap.class, HashMap.class, EMPTY_ANNOTATIONS, MediaType.APPLICATION_FORM_URLENCODED_TYPE),
                is(true));
        assertThat(UNDER_TEST.isWriteable(Map.class, Map.class, EMPTY_ANNOTATIONS, MediaType.MULTIPART_FORM_DATA_TYPE),
                is(true));
        assertThat(UNDER_TEST.isWriteable(MultivaluedMap.class, MultivaluedMap.class, EMPTY_ANNOTATIONS, MediaType.APPLICATION_FORM_URLENCODED_TYPE),
                is(true));
    }
    
    @Test
    public void testGetSize() {
        assertThat(UNDER_TEST.getSize(null, null, null, ENCODED, MediaType.WILDCARD_TYPE),
                is(notNullValue()));
    }

    @Test
    public void testWriteTo() throws Exception {
        Map map = GSON.fromJson("{\"a\":\"1\",\"b\":{\"0\":\"2\",\"a\":\"3\",\"1\":\"4\"},\"c\":{\"a\":{\"b\":\"5\"}}}", Map.class);
        
        assertThat(write(map, MultivaluedMap.class, MultivaluedMap.class, EMPTY_ANNOTATIONS, MediaType.APPLICATION_FORM_URLENCODED_TYPE, new MultivaluedHashMap<String, Object>()),
                allOf(
                        containsString("a=1"),
                        containsString("b[0]=2"),
                        containsString("b[a]=3"),
                        containsString("b[1]=4"),
                        containsString("c[a][b]=5")
                ));
    }

    protected String write(final Map data,
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
