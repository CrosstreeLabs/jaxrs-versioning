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

import com.crosstreelabs.jaxrs.api.versioned.ValueObjectRegistry;
import com.crosstreelabs.jaxrs.api.versioned.fixtures.vo.UserV1;
import com.crosstreelabs.testing.jersey.Jersey;
import java.net.URI;
import java.util.Arrays;
import java.util.Collection;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RunWith(Parameterized.class)
public class JsonValueObjectReaderWriterIT {
    private static final Logger LOGGER = LoggerFactory.getLogger(JsonValueObjectReaderWriterIT.class);
    private final Class<?> cls;
    @Rule
    public final Jersey jersey;
    
    @Parameterized.Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][] {
            { Jackson1JsonValueObjectReaderWriter.class },
            { Jackson2JsonValueObjectReaderWriter.class },
            { GsonJsonValueObjectReaderWriter.class }
        });
    }
    
    public JsonValueObjectReaderWriterIT(final Class<?> cls) {
        this.cls = cls;
        this.jersey = new Jersey(cls, UserResource.class);
        ValueObjectRegistry.register(UserV1.class);
    }
    
    @Test
    public void test() {
        UserV1 user = new UserV1();
        user.name = "Thomas";
        user.username = "thomas.wilson";
        user.email = "thomas.wilson@crosstreelabs.com";
        
        assertThat(jersey.getClient().target("/")
                .request()
                .post(Entity.entity(user, MediaType.valueOf("application/vnd.crosstreelabs.user.v1+json")))
                .getStatus(),
                is(201));
    }
    
    @Path("/")
    public static class UserResource {
        @POST
        public Response post(final UserV1 user) {
            assertThat(user.name, is(equalTo("Thomas")));
            assertThat(user.username, is(equalTo("thomas.wilson")));
            assertThat(user.email, is(equalTo("thomas.wilson@crosstreelabs.com")));
            
            return Response.created(URI.create("/abc")).build();
        }
    }
}