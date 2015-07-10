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
import com.crosstreelabs.jaxrs.api.versioned.ValueObjectRegistry;
import com.crosstreelabs.jaxrs.api.versioned.annotation.Version;
import com.crosstreelabs.jaxrs.api.versioned.fixtures.vo.UserV1;
import com.crosstreelabs.jaxrs.api.versioned.fixtures.vo.UserV2;
import com.crosstreelabs.jaxrs.api.versioned.fixtures.vo.hierarchical.BookVO;
import com.crosstreelabs.jaxrs.api.versioned.fixtures.vo.hierarchical.ResourceVO;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.Collection;
import javax.validation.Valid;
import javax.validation.ValidationException;
import javax.ws.rs.Encoded;
import javax.ws.rs.NotAcceptableException;
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
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;

@RunWith(Parameterized.class)
public class JsonValueObjectProviderTest {
    
    protected static final Annotation[] EMPTY_ANNOTATIONS = new Annotation[0];
    protected static final Annotation[] ENCODED;
    protected static final MediaType USER1_TYPE = MediaType.valueOf(UserV1.TYPE_STR+"+json;v=1");
    protected static final MediaType USER2_TYPE = MediaType.valueOf(UserV2.TYPE_STR+"+json;v=2");
    protected static final MediaType BOOK1_TYPE = MediaType.valueOf(BookVO.TYPE_STR+"+json;v=1");
    
    static {
        Encoded e = mock(Encoded.class);
        doReturn(Encoded.class).when(e).annotationType();
        ENCODED = new Annotation[]{
            e
        };
        
        ValueObjectRegistry.register(UserV1.class);
        ValueObjectRegistry.register(UserV2.class);
        ValueObjectRegistry.register(BookVO.class);
    }
    
    @Parameterized.Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][] {
            { new Jackson1JsonValueObjectProvider() },
            { new Jackson2JsonValueObjectProvider() },
            { new GsonJsonValueObjectProvider() }
        });
    }
    
    private final AbstractValueObjectReaderWriter underTest;
    
    public JsonValueObjectProviderTest(
            final AbstractValueObjectReaderWriter underTest) {
        this.underTest = underTest;
    }
    
    @Test
    public void testIsReadable() {
        assertThat(underTest.isReadable(Object.class, Object.class, EMPTY_ANNOTATIONS, MediaType.APPLICATION_JSON_TYPE),
                is(false));
        assertThat(underTest.isReadable(Object.class, Object.class, EMPTY_ANNOTATIONS, USER1_TYPE),
                is(false));
        assertThat(underTest.isReadable(ResourceVO.class, ResourceVO.class, EMPTY_ANNOTATIONS, BOOK1_TYPE),
                is(true));
        try {
            underTest.isReadable(UserV1.class, UserV1.class, EMPTY_ANNOTATIONS, MediaType.APPLICATION_JSON_TYPE);
            fail("Test should have thrown NotSupportedException");
        } catch (NotSupportedException ex) {}
        assertThat(underTest.isReadable(UserV1.class, UserV1.class, EMPTY_ANNOTATIONS, USER1_TYPE),
                is(true));
    }

    @Test
    public void testIsWriteable() {
        assertThat(underTest.isWriteable(Object.class, Object.class, EMPTY_ANNOTATIONS, MediaType.APPLICATION_JSON_TYPE),
                is(false));
        assertThat(underTest.isWriteable(Object.class, Object.class, EMPTY_ANNOTATIONS, USER1_TYPE),
                is(false));
        assertThat(underTest.isWriteable(UserV1.class, UserV1.class, EMPTY_ANNOTATIONS, MediaType.APPLICATION_JSON_TYPE),
                is(false));
        assertThat(underTest.isWriteable(UserV1.class, UserV1.class, EMPTY_ANNOTATIONS, USER1_TYPE),
                is(true));
        assertThat(underTest.isWriteable(ResourceVO.class, ResourceVO.class, EMPTY_ANNOTATIONS, BOOK1_TYPE),
                is(false));
    }
    
    @Test
    public void testReadFromNullInputStreamYieldsEmptyVO() throws Exception {
        assertThat(underTest.readFrom((Class)UserV1.class, UserV1.class, EMPTY_ANNOTATIONS, USER1_TYPE, new MultivaluedHashMap<String, String>(), getResource("unit/representations/missing.json")),
                is(instanceOf(UserV1.class)));
    }
    
    @Test
    public void testReadFromRealInputStreamYieldsPopulatedVO() throws Exception {
        Object result = underTest.readFrom((Class)UserV1.class, UserV1.class, EMPTY_ANNOTATIONS, USER1_TYPE, new MultivaluedHashMap<String, String>(), getResource("unit/representations/user.v1.json"));
        assertThat(result, is(instanceOf(UserV1.class)));
        assertThat(((UserV1)result).name, is(equalTo("Thomas")));
        assertThat(((UserV1)result).username, is(equalTo("thomas.wilson")));
        assertThat(((UserV1)result).email, is(equalTo("thomas.wilson@crosstreelabs.com")));
    }
    
    @Test(expected = NotSupportedException.class)
    public void testReadFromInvalidContentTypeThrowsException() throws Exception {
        underTest.readFrom((Class)Object.class, Object.class, ENCODED, MediaType.APPLICATION_JSON_TYPE, null, null);
    }
    
    @Test
    public void ensureThatConsumerWorks() throws Exception {
        Object result = underTest.readFrom((Class)UserV2.class, UserV2.class, EMPTY_ANNOTATIONS, USER2_TYPE, new MultivaluedHashMap<String, String>(), getResource("unit/representations/user.v2.json"));
        assertThat(result, is(instanceOf(UserV2.class)));
        assertThat(((UserV2)result).getName(), is(equalTo("Thomas")));
        assertThat(((UserV2)result).getUsername(), is(equalTo("thomas.wilson")));
        assertThat(((UserV2)result).getEmail(), is(equalTo("thomas.wilson@crosstreelabs.com")));
        assertThat(((UserV2)result).getAge(), is(equalTo(27)));
    }
    
//    @Test
//    public void ensureHandlesEncodingOnRead() throws Exception {
//        Object result = underTest.readFrom((Class)UserV1.class, UserV1.class, ENCODED, USER1_TYPE, new MultivaluedHashMap<String, String>(), getResource("unit/representations/user.v1.specialchars.json"));
//        assertThat(result, is(instanceOf(UserV1.class)));
//        assertThat(((UserV1)result).name, is(equalTo("Thoma%8A")));
//        assertThat(((UserV1)result).username, is(equalTo("thomas.wilson")));
//        assertThat(((UserV1)result).email, is(equalTo("thomas.wilson@crosstreelabs.com")));
//    }
    
    @Test
    public void testWritingNullResultsInEmptyJsonObject() throws Exception {
        UserV1 user = new UserV1();
        user.name = "Thomas";
        user.username = "thomas.wilson";
        user.email = "thomas.wilson@crosstreelabs.com";
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        underTest.writeTo(user, UserV1.class, UserV1.class, EMPTY_ANNOTATIONS, USER1_TYPE, new MultivaluedHashMap<String, Object>(), baos);
        JSONAssert.assertEquals("{\"name\":\"Thomas\",\"username\":\"thomas.wilson\",\"email\":\"thomas.wilson@crosstreelabs.com\"}", baos.toString(), JSONCompareMode.STRICT);
    }
    
    @Test(expected = NotAcceptableException.class)
    public void ensureWritingVOWithoutContentTypeFails() throws Exception {
        underTest.writeTo(new Uncontented(), Uncontented.class, Uncontented.class, EMPTY_ANNOTATIONS, USER1_TYPE, null, null);
    }
    
    @Test(expected = ValidationException.class)
    public void testWritingValidation() throws Exception {
        Valid v = mock(Valid.class);
        doReturn(Valid.class).when(v).annotationType();
        ByteArrayOutputStream entityStream = new ByteArrayOutputStream();
        MultivaluedHashMap<String, Object> headers = new MultivaluedHashMap<>();
        underTest.writeTo(new UserV1(), UserV1.class, UserV1.class, new Annotation[]{v}, USER1_TYPE, headers, entityStream);
    }
    
    @Test
    public void ensureHierarchicalResourceResolutionWorks() throws Exception {
        Object result = underTest.readFrom((Class)ResourceVO.class, ResourceVO.class, EMPTY_ANNOTATIONS, BOOK1_TYPE, new MultivaluedHashMap<String, String>(), getResource("unit/representations/book.v1.json"));
        assertThat(result, is(instanceOf(BookVO.class)));
        assertThat(((BookVO)result).title, is(equalTo("The Cat In The Hat")));
        assertThat(((BookVO)result).description, is(equalTo("The Cat in the Hat is a children's book.")));
        assertThat(((BookVO)result).author, is(equalTo("Dr. Seuss")));
    }
    
    protected InputStream getResource(final String resource) {
        return getClass().getClassLoader().getResourceAsStream(resource);
    }
    
    @Version(value = 1, contentType = {})
    public static class Uncontented implements ValueObject {
        
    }
}