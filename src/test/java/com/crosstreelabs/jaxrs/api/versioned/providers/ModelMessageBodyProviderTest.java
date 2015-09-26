/*
 * Copyright 2015 twilson.
 *
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
import com.crosstreelabs.jaxrs.api.versioned.ValueObjectRegistry;
import com.crosstreelabs.jaxrs.api.versioned.annotation.Version;
import com.crosstreelabs.jaxrs.api.versioned.mapper.impl.Jackson2JsonMapper;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;

public class ModelMessageBodyProviderTest {
    public static final Annotation[] EMPTY_ANNOTATIONS = new Annotation[0];
    public static final ModelMessageBodyProvider UNDER_TEST
            = new ModelMessageBodyProvider(new Jackson2JsonMapper());
    
    static {
        ValueObjectRegistry.clear();
        ValueObjectRegistry.register(UserVO.class);
        ValueObjectRegistry.register(ClientVO.class);
    }
    
    @Test
    public void ensureCannotWriteNonModel() {
        assertThat(UNDER_TEST.isWriteable(UserVO.class, UserVO.class, EMPTY_ANNOTATIONS, MediaType.valueOf("application/vnd.crosstreelabs.user+json;v=1")),
                is(equalTo(false)));
    }
    @Test
    public void ensureCannotWriteIncorrectStructure() {
        assertThat(UNDER_TEST.isWriteable(UserModel.class, UserModel.class, EMPTY_ANNOTATIONS, MediaType.valueOf("application/vnd.crosstreelabs.user+xml;v=1")),
                is(equalTo(false)));
    }
    @Test
    public void ensureCanWrite() {
        assertThat(UNDER_TEST.isWriteable(UserModel.class, UserModel.class, EMPTY_ANNOTATIONS, MediaType.valueOf("application/vnd.crosstreelabs.user+json;v=1")),
                is(equalTo(true)));
    }
    
    @Version(version = 1, models = UserModel.class, contentType = "application/vnd.crosstreelabs.user")
    public static class UserVO implements ValueObject {}
    public static class UserModel {}
    @Version(version = 1, contentType = "application/vnd.crosstreelabs.client")
    public static class ClientVO implements ValueObject {}
}