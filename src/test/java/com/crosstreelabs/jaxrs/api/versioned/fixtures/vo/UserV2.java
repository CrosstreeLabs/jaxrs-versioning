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
package com.crosstreelabs.jaxrs.api.versioned.fixtures.vo;

import com.crosstreelabs.jaxrs.api.versioned.Consumer;
import com.crosstreelabs.jaxrs.api.versioned.ValueObject;
import com.crosstreelabs.jaxrs.api.versioned.annotation.Version;
import java.util.Map;
import javax.ws.rs.core.MediaType;
import org.apache.commons.collections4.MapUtils;

@Version(value = 2, contentType = {UserV2.TYPE_STR})
public class UserV2 implements ValueObject, Consumer {
    public static final String TYPE_STR = "application/vnd.crosstreelabs.user";
    public static final MediaType TYPE = MediaType.valueOf(TYPE_STR);

    private String name;
    private String username;
    private String email;
    private int age;

    public String getName() {
        return name;
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public int getAge() {
        return age;
    }
    
    @Override
    public ValueObject consume(final Map data) {
        name = MapUtils.getString(data, "name");
        username = MapUtils.getString(data, "username");
        email = MapUtils.getString(data, "email");
        age = MapUtils.getIntValue(data, "age", 0);
        return this;
    }
    
}