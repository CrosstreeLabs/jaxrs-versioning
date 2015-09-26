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
import javax.ws.rs.core.MediaType;

public class VersionUtils {
    public static boolean isCompatible(final MediaType mediaType,
            final Version version) {
        // Remove suffix
        MediaType target = mediaType;
        int targetVersion = mediaType.getParameters().containsKey("v")
                ? Integer.valueOf(mediaType.getParameters().get("v"))
                : -1;
        if (mediaType.toString().contains("+")) {
            String str = mediaType.getType()+"/"+mediaType.getSubtype();
            str = str.substring(0, str.indexOf("+"));
            target = MediaType.valueOf(str);
        }
        for (String allowedType : version.contentType()) {
            if (MediaType.valueOf(allowedType).isCompatible(target)
                    && (targetVersion == -1
                    || targetVersion == version.version())) {
                return true;
            }
        }
        return false;
    }
    public static String defaultMediaType(final Version version) {
        if (version.contentType() == null || version.contentType().length == 0) {
            return null;
        }
        
        return version.contentType()[0]+";v="+version.version();
    }
    /**
     * Adds missing parameters where required.
     * @param mediaType
     * @param version
     * @return 
     */
    public static MediaType normalize(final MediaType mediaType,
            final Version version) {
        if (mediaType.getParameters().containsKey("v")) {
            return mediaType;
        }
        return MediaType.valueOf(mediaType.toString()+";v="+version.version());
    }
}
