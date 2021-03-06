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
package com.crosstreelabs.jaxrs.api.versioned.fixtures.vo.hierarchical;

import com.crosstreelabs.jaxrs.api.versioned.annotation.Version;
import javax.ws.rs.core.MediaType;

@Version(version = 1, contentType = {BookVO.TYPE_STR})
public class BookVO extends ResourceVO {
    public static final String TYPE_STR = "application/vnd.crosstreelabs.book";
    public static final MediaType TYPE = MediaType.valueOf(TYPE_STR);
    
    public String author;
}