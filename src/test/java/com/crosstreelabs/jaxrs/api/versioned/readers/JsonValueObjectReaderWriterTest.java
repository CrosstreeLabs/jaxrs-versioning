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
package com.crosstreelabs.jaxrs.api.versioned.readers;

import com.crosstreelabs.jaxrs.api.versioned.AbstractValueObjectReaderWriter;
import com.crosstreelabs.jaxrs.api.versioned.ValueObject;
import com.crosstreelabs.jaxrs.api.versioned.ValueObjectRegistry;
import com.crosstreelabs.jaxrs.api.versioned.fixtures.vo.UserV1;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.Collection;
import javax.ws.rs.NotSupportedException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedHashMap;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;

@RunWith(Parameterized.class)
public class JsonValueObjectReaderWriterTest {
    
    protected static final Annotation[] EMPTY_ANNOTATIONS = new Annotation[0];
    protected static final MediaType VO_TYPE = MediaType.valueOf(UserV1.TYPE_STR+".v1+json");
    
    @Parameterized.Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][] {
            { new Jackson1JsonValueObjectReaderWriter() },
            { new Jackson2JsonValueObjectReaderWriter() },
            { new GsonJsonValueObjectReaderWriter() }
        });
    }
    
    private final AbstractValueObjectReaderWriter underTest;
    
    public JsonValueObjectReaderWriterTest(
            final AbstractValueObjectReaderWriter underTest) {
        this.underTest = underTest;
        ValueObjectRegistry.register(UserV1.class);
    }
    
    @Test
    public void testIsReadable() {
        assertThat(underTest.isReadable(Object.class, Object.class, EMPTY_ANNOTATIONS, MediaType.APPLICATION_JSON_TYPE),
                is(false));
        assertThat(underTest.isReadable(Object.class, Object.class, EMPTY_ANNOTATIONS, VO_TYPE),
                is(false));
        try {
            underTest.isReadable(UserV1.class, UserV1.class, EMPTY_ANNOTATIONS, MediaType.APPLICATION_JSON_TYPE);
            fail("Test should have thrown NotSupportedException");
        } catch (NotSupportedException ex) {}
        assertThat(underTest.isReadable(UserV1.class, UserV1.class, EMPTY_ANNOTATIONS, VO_TYPE),
                is(true));
    }

    @Test
    public void testIsWriteable() {
        assertThat(underTest.isWriteable(Object.class, Object.class, EMPTY_ANNOTATIONS, MediaType.APPLICATION_JSON_TYPE),
                is(false));
        assertThat(underTest.isWriteable(Object.class, Object.class, EMPTY_ANNOTATIONS, VO_TYPE),
                is(false));
        assertThat(underTest.isWriteable(UserV1.class, UserV1.class, EMPTY_ANNOTATIONS, MediaType.APPLICATION_JSON_TYPE),
                is(false));
        assertThat(underTest.isWriteable(UserV1.class, UserV1.class, EMPTY_ANNOTATIONS, VO_TYPE),
                is(true));
    }
    
    @Test
    public void testReadFromNullInputStreamYieldsEmptyVO() throws Exception {
        Class<?> type = UserV1.class;
        assertThat(underTest.readFrom((Class<Object>)type, UserV1.class, EMPTY_ANNOTATIONS, VO_TYPE, new MultivaluedHashMap<String, String>(), getResource("unit/representations/missing.json")),
                is(instanceOf(UserV1.class)));
    }
    
    @Test
    public void testReadFromRealInputStreamYieldsPopulatedVO() throws Exception {
        Class<?> type = UserV1.class;
        Object result = underTest.readFrom((Class<Object>)type, UserV1.class, EMPTY_ANNOTATIONS, VO_TYPE, new MultivaluedHashMap<String, String>(), getResource("unit/representations/user.v1.json"));
        assertThat(result, is(instanceOf(UserV1.class)));
        assertThat(((UserV1)result).name, is(equalTo("Thomas")));
        assertThat(((UserV1)result).username, is(equalTo("thomas.wilson")));
        assertThat(((UserV1)result).email, is(equalTo("thomas.wilson@crosstreelabs.com")));
    }
    
    @Test
    public void testWritingNullResultsInEmptyJsonObject() throws Exception {
        UserV1 user = new UserV1();
        user.name = "Thomas";
        user.username = "thomas.wilson";
        user.email = "thomas.wilson@crosstreelabs.com";
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        underTest.writeTo(user, UserV1.class, UserV1.class, EMPTY_ANNOTATIONS, VO_TYPE, new MultivaluedHashMap<String, Object>(), baos);
        JSONAssert.assertEquals("{\"name\":\"Thomas\",\"username\":\"thomas.wilson\",\"email\":\"thomas.wilson@crosstreelabs.com\"}", baos.toString(), JSONCompareMode.STRICT);
    }
    
    protected InputStream getResource(final String resource) {
        return getClass().getClassLoader().getResourceAsStream(resource);
    }
}