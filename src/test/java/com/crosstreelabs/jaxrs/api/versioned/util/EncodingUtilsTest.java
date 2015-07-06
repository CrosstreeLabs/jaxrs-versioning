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

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.assertThat;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.mockito.Mockito.*;

public class EncodingUtilsTest {
    public static final Charset UTF_8 = StandardCharsets.UTF_8;
    
    @Test
    public void testDecodeString() throws Exception {
        assertThat(EncodingUtils.decode("%c5%92", UTF_8), is(equalTo("Œ")));
    }
    
    @Test
    public void testEncodeString() throws Exception {
        assertThat(EncodingUtils.encode("Œ", UTF_8), is(equalTo("%C5%92")));
    }
    
}