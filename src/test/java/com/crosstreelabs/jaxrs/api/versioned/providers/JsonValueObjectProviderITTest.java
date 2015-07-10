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

import com.crosstreelabs.jaxrs.api.versioned.ValueObjectRegistry;
import com.crosstreelabs.jaxrs.api.versioned.exception.ValidationExceptionMapper;
import com.crosstreelabs.jaxrs.api.versioned.fixtures.vo.UserV1;
import com.crosstreelabs.testing.jersey.Jersey;
import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;
import java.net.URI;
import java.util.Arrays;
import java.util.Collection;
import javax.annotation.Priority;
import javax.validation.Valid;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;
import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

@RunWith(Parameterized.class)
public class JsonValueObjectProviderITTest {
    
    @Rule
    public final Jersey jersey;
    
    @Parameterized.Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][]{
            { Jackson1JsonValueObjectReaderWriterTestable.class },
            { Jackson2JsonValueObjectReaderWriterTestable.class },
            { GsonJsonValueObjectReaderWriterTestable.class }
        });
    }
    
    static {
        ValueObjectRegistry.register(UserV1.class);
    }
    
    public JsonValueObjectProviderITTest(final Class<?> cls) throws Exception {
        jersey = new Jersey(cls, ValidationExceptionMapper.class, JacksonJsonProvider.class, UserResource.class);
    }
    
    @Test
    public void ensureValidEntityConversionWorks() {
        UserV1 user = new UserV1();
        user.name = "John Smith";
        user.username = "john.smith";
        user.email = "john.smith@example.com";
        
        assertThat(jersey.getClient().target("/")
                .request()
                .post(Entity.entity(user, MediaType.valueOf(UserV1.TYPE_STR+"+json;v=1")))
                .getStatus(),
                is(201));
    }
    
    @Test
    public void ensureInvalidEntityConversionReturnsError() throws Exception {
        UserV1 user = new UserV1();
        user.name = "John Smith";
        
        Response response = jersey.getClient().target("/")
                .request("application/json")
                .post(Entity.entity(user, MediaType.valueOf(UserV1.TYPE_STR+"+json;v=1")));
        assertThat(response.getStatus(), is(400));
        assertThat(response.readEntity(String.class), anyOf(
                is(equalTo("{\"message\":\"username may not be null\"}")),
                is(equalTo("{\"message\":\"email may not be null\"}"))
        ));
    }
    
    @Path("/")
    public static class UserResource {
        @POST
        public Response post(@Valid final UserV1 user) {
            assertThat(user.name, is(equalTo("John Smith")));
            assertThat(user.username, is(equalTo("john.smith")));
            assertThat(user.email, is(equalTo("john.smith@example.com")));
            
            return Response.created(URI.create("/abc123")).build();
        }
    }
    
    @Provider
    @Priority(Integer.MAX_VALUE)
    public static class Jackson1JsonValueObjectReaderWriterTestable
            extends Jackson1JsonValueObjectProvider {}
    @Provider
    @Priority(Integer.MAX_VALUE)
    public static class Jackson2JsonValueObjectReaderWriterTestable
            extends Jackson2JsonValueObjectProvider {}
    @Provider
    @Priority(Integer.MAX_VALUE)
    public static class GsonJsonValueObjectReaderWriterTestable
            extends GsonJsonValueObjectProvider {}
}