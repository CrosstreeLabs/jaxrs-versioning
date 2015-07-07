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
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import javax.validation.Valid;
import javax.validation.ValidationException;
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.NotAcceptableException;
import javax.ws.rs.NotSupportedException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedHashMap;
import org.custommonkey.xmlunit.Diff;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;

@RunWith(Parameterized.class)
public class XmlValueObjectProviderTest {
    
    protected static final Annotation[] EMPTY_ANNOTATIONS = new Annotation[0];
    protected static final MediaType USER1_TYPE = MediaType.valueOf(UserV1.TYPE_STR+"+xml;v=1");
    protected static final MediaType USER2_TYPE = MediaType.valueOf(UserV2.TYPE_STR+"+xml;v=2");
    protected static final MediaType BOOK1_TYPE = MediaType.valueOf(BookVO.TYPE_STR+"+xml;v=1");
    
    @Parameterized.Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][] {
            { new Jackson2XmlValueObjectProvider() }
        });
    }
    
    private final AbstractValueObjectReaderWriter underTest;
    
    public XmlValueObjectProviderTest(
            final AbstractValueObjectReaderWriter underTest) {
        this.underTest = underTest;
        ValueObjectRegistry.register(UserV1.class);
        ValueObjectRegistry.register(UserV2.class);
        ValueObjectRegistry.register(BookVO.class);
    }
    
    @Test
    public void testIsReadable() {
        assertThat(underTest.isReadable(Object.class, Object.class, EMPTY_ANNOTATIONS, MediaType.APPLICATION_XML_TYPE),
                is(false));
        assertThat(underTest.isReadable(Object.class, Object.class, EMPTY_ANNOTATIONS, USER1_TYPE),
                is(false));
        assertThat(underTest.isReadable(ResourceVO.class, ResourceVO.class, EMPTY_ANNOTATIONS, BOOK1_TYPE),
                is(true));
        try {
            underTest.isReadable(UserV1.class, UserV1.class, EMPTY_ANNOTATIONS, MediaType.APPLICATION_XML_TYPE);
            fail("Test should have thrown NotSupportedException");
        } catch (NotSupportedException ex) {}
        assertThat(underTest.isReadable(UserV1.class, UserV1.class, EMPTY_ANNOTATIONS, USER1_TYPE),
                is(true));
    }

    @Test
    public void testIsWriteable() {
        assertThat(underTest.isWriteable(Object.class, Object.class, EMPTY_ANNOTATIONS, MediaType.APPLICATION_XML_TYPE),
                is(false));
        assertThat(underTest.isWriteable(Object.class, Object.class, EMPTY_ANNOTATIONS, USER1_TYPE),
                is(false));
        assertThat(underTest.isWriteable(UserV1.class, UserV1.class, EMPTY_ANNOTATIONS, MediaType.APPLICATION_XML_TYPE),
                is(false));
        assertThat(underTest.isWriteable(UserV1.class, UserV1.class, EMPTY_ANNOTATIONS, USER1_TYPE),
                is(true));
        assertThat(underTest.isWriteable(ResourceVO.class, ResourceVO.class, EMPTY_ANNOTATIONS, BOOK1_TYPE),
                is(false));
    }
    
    @Test
    public void testReadFromNullInputStreamYieldsEmptyVO() throws Exception {
        Class<?> type = UserV1.class;
        assertThat(underTest.readFrom((Class<Object>)type, UserV1.class, EMPTY_ANNOTATIONS, USER1_TYPE, new MultivaluedHashMap<String, String>(), getResource("unit/representations/missing.xml")),
                is(instanceOf(UserV1.class)));
    }
    
    @Test
    public void testReadFromRealInputStreamYieldsPopulatedVO() throws Exception {
        Class<?> type = UserV1.class;
        Object result = underTest.readFrom((Class<Object>)type, UserV1.class, EMPTY_ANNOTATIONS, USER1_TYPE, new MultivaluedHashMap<String, String>(), getResource("unit/representations/user.v1.xml"));
        assertThat(result, is(instanceOf(UserV1.class)));
        assertThat(((UserV1)result).name, is(equalTo("Thomas")));
        assertThat(((UserV1)result).username, is(equalTo("thomas.wilson")));
        assertThat(((UserV1)result).email, is(equalTo("thomas.wilson@crosstreelabs.com")));
    }
    
    @Test
    public void ensureThatConsumerWorks() throws Exception {
        Class<?> type = UserV2.class;
        Object result = underTest.readFrom((Class<Object>)type, UserV2.class, EMPTY_ANNOTATIONS, USER2_TYPE, new MultivaluedHashMap<String, String>(), getResource("unit/representations/user.v2.xml"));
        assertThat(result, is(instanceOf(UserV2.class)));
        assertThat(((UserV2)result).getName(), is(equalTo("Thomas")));
        assertThat(((UserV2)result).getUsername(), is(equalTo("thomas.wilson")));
        assertThat(((UserV2)result).getEmail(), is(equalTo("thomas.wilson@crosstreelabs.com")));
        assertThat(((UserV2)result).getAge(), is(equalTo(27)));
    }
    
    @Test
    public void testWritingNullResultsInEmptyJsonObject() throws Exception {
        UserV1 user = new UserV1();
        user.name = "Thomas";
        user.username = "thomas.wilson";
        user.email = "thomas.wilson@crosstreelabs.com";
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        underTest.writeTo(user, UserV1.class, UserV1.class, EMPTY_ANNOTATIONS, USER1_TYPE, new MultivaluedHashMap<String, Object>(), baos);
        String expected = "<xml><name>Thomas</name><username>thomas.wilson</username><email>thomas.wilson@crosstreelabs.com</email></xml>";
        Diff diff = new Diff(expected, baos.toString());
        assertTrue(diff.similar());
    }
    
    @Test(expected = InternalServerErrorException.class)
    public void ensureWritingNonValueObjectFails() throws Exception {
        underTest.writeTo(new Object(), Object.class, Object.class, EMPTY_ANNOTATIONS, USER1_TYPE, null, null);
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
        Class<?> type = ResourceVO.class;
        Object result = underTest.readFrom((Class<Object>)type, ResourceVO.class, EMPTY_ANNOTATIONS, BOOK1_TYPE, new MultivaluedHashMap<String, String>(), getResource("unit/representations/book.v1.xml"));
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