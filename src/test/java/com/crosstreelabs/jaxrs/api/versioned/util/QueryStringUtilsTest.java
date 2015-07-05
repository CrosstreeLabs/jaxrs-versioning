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
package com.crosstreelabs.jaxrs.api.versioned.util;

import java.util.Map;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;
import static com.crosstreelabs.jaxrs.api.versioned.util.QueryStringUtils.toMap;
import static com.crosstreelabs.jaxrs.api.versioned.util.QueryStringUtils.keyToPath;
import com.google.gson.Gson;
import java.util.HashMap;

public class QueryStringUtilsTest {
    private static final Gson GSON = new Gson();
    
    @Test
    public void testKeyToPath() throws Exception {
        assertThat(keyToPath("a"), is(new String[]{"a"}));
        assertThat(keyToPath("a[]"), is(new String[]{"a",""}));
        assertThat(keyToPath("a[b]"), is(new String[]{"a","b"}));
        assertThat(keyToPath("a[b][c]"), is(new String[]{"a","b","c"}));
        assertThat(keyToPath("a[b].c"), is(new String[]{"a","b","c"}));
    }
    
    @Test
    public void testToMap() throws Exception {
        assertThat(toMap("a=1"),
                is(GSON.fromJson("{\"a\":\"1\"}", Map.class)));
        assertThat(toMap("a=1&b=2"),
                is(GSON.fromJson("{\"a\":\"1\",\"b\":\"2\"}", Map.class)));
        assertThat(toMap("a=1&b=2&c[]=1&c[a]=2"),
                is(GSON.fromJson("{\"a\":\"1\",\"b\":\"2\",\"c\":{\"0\":\"1\",\"a\":\"2\"}}", Map.class)));
    }
    
}