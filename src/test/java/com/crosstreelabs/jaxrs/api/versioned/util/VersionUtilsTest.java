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

import com.crosstreelabs.jaxrs.api.versioned.annotation.Version;
import static com.crosstreelabs.jaxrs.api.versioned.util.VersionUtils.defaultMediaType;
import static com.crosstreelabs.jaxrs.api.versioned.util.VersionUtils.isCompatible;
import javax.ws.rs.core.MediaType;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;
import org.junit.Test;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class VersionUtilsTest {
    protected static final VersionUtils UTIL = new VersionUtils();
    
    @Test
    public void testIsCompatible() {
        Version version = v(1, "application/vnd.crosstreelabs.user");
        
        assertThat(isCompatible(t("application/vnd.crosstreelabs.user"), version), is(false));
        assertThat(isCompatible(t("application/vnd.crosstreelabs.user.v1"), version), is(true));
        assertThat(isCompatible(t("application/vnd.crosstreelabs.user.v1+json"), version), is(true));
        assertThat(isCompatible(t("application/vnd.crosstreelabs.user.v1+xml"), version), is(true));
        assertThat(isCompatible(t("application/vnd.crosstreelabs.user.v2"), version), is(false));
        assertThat(isCompatible(t("application/vnd.crosstreelabs.user.v2+json"), version), is(false));
        assertThat(isCompatible(t("application/vnd.crosstreelabs.book"), version), is(false));
    }

    /**
     * Test of defaultMediaType method, of class VersionUtils.
     */
    @Test
    public void testDefaultMediaType() {
        assertThat(defaultMediaType(v(1, "application/vnd.crosstreelabs.user")), is(equalTo("application/vnd.crosstreelabs.user.v1")));
        assertThat(defaultMediaType(v(1, new String[0])), is(nullValue()));
    }
    
    protected MediaType t(final String type) {
        return MediaType.valueOf(type);
    }
    protected Version v(final int version, final String contentType) {
        return v(version, new String[]{contentType});
    }
    protected Version v(final int version, final String[] contentTypes) {
        Version v = mock(Version.class);
        when(v.value()).thenReturn(version);
        when(v.contentType()).thenReturn(contentTypes);
        return v;
    }
    
}
