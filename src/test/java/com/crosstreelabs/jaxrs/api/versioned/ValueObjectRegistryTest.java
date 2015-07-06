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
package com.crosstreelabs.jaxrs.api.versioned;

import com.crosstreelabs.jaxrs.api.versioned.fixtures.vo.UserV1;
import com.crosstreelabs.jaxrs.api.versioned.fixtures.vo.UserV2;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Set;
import javax.ws.rs.core.MediaType;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;
import org.junit.Before;
import org.junit.Test;

public class ValueObjectRegistryTest {
    protected static final ValueObjectRegistry REGISTRY = new ValueObjectRegistry();
    
    @Before
    public void before() throws Exception {
        Field field = ValueObjectRegistry.class.getDeclaredField("CLASSES");
        field.setAccessible(true);
        Set set = (Set)field.get(null);
        set.clear();
    }
    
    @Test
    public void testGetClasses() {
        assertThat(ValueObjectRegistry.getClasses(), is(empty()));
        ValueObjectRegistry.register(UserV1.class);
        assertThat(ValueObjectRegistry.getClasses(), hasItem(UserV1.class));
    }
    @Test(expected = UnsupportedOperationException.class)
    public void ensureCannotManipulateClassesSet() {
        ValueObjectRegistry.getClasses().add(UserV1.class);
    }

    @Test
    public void testFindForMediaType() {
        assertThat(ValueObjectRegistry.findForMediaType(MediaType.valueOf("application/vnd.crosstreelabs.user.v1+json")),
                is(nullValue()));
        ValueObjectRegistry.register(Unversioned.class);
        assertThat(ValueObjectRegistry.findForMediaType(MediaType.valueOf("application/vnd.crosstreelabs.user.v1+json")),
                is(nullValue()));
        ValueObjectRegistry.register(UserV1.class);
        assertThat(ValueObjectRegistry.findForMediaType(MediaType.valueOf("application/vnd.crosstreelabs.user.v1+json")),
                is(equalTo((Class)UserV1.class)));
    }

    @Test
    public void testRegisterArray() {
        assertThat(ValueObjectRegistry.findForMediaType(MediaType.valueOf("application/vnd.crosstreelabs.user.v1+json")),
                is(nullValue()));
        ValueObjectRegistry.register(UserV1.class, UserV2.class, Unversioned.class);
        assertThat(ValueObjectRegistry.getClasses(), hasItems(UserV1.class, UserV2.class, Unversioned.class));
    }

    @Test
    public void testRegisterCollection() {
        assertThat(ValueObjectRegistry.findForMediaType(MediaType.valueOf("application/vnd.crosstreelabs.user.v1+json")),
                is(nullValue()));
        ValueObjectRegistry.register(Arrays.asList(UserV1.class, UserV2.class, Unversioned.class));
        assertThat(ValueObjectRegistry.getClasses(), hasItems(UserV1.class, UserV2.class, Unversioned.class));
    }
    
    public static class Unversioned implements ValueObject {}
}
