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

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;

public class EncodingUtils {
    
    public static String decode(final String str, final Charset charset) {
        try {
            return URLDecoder.decode(str, charset.name());
        } catch (UnsupportedEncodingException ex) {
            throw new AssertionError(charset.name()+" not supported");
        }
    }
    public static Map<String, Object> decode(final Map<String, Object> map, final Charset charset) {
        try {
            Map<String, Object> decoded = map.getClass().newInstance();
            for (Map.Entry<String, Object> entry : map.entrySet()) {
                String key = decode(entry.getKey(), charset);
                if (entry.getValue() instanceof Map) {
                    decoded.put(key, decode((Map)entry.getValue(), charset));
                } else if (entry.getValue() instanceof List) {
                    decoded.put(key, decode((List)entry.getValue(), charset));
                } else {
                    decoded.put(key, decode((String)entry.getValue(), charset));
                }
            }
            return decoded;
        } catch (IllegalAccessException | InstantiationException ex) {
            throw new RuntimeException(ex);
        }
    }
    public static List decode(final List list, final Charset charset) {
        try {
            List decoded = list.getClass().newInstance();
            for (Object obj : list) {
                if (obj instanceof Map) {
                    decoded.add(decode((Map)obj, charset));
                } else if (obj instanceof List) {
                    decoded.add(decode((List)obj, charset));
                } else {
                    decoded.add(decode((String)obj, charset));
                }
            }
            return decoded;
        } catch (IllegalAccessException | InstantiationException ex) {
            throw new RuntimeException(ex);
        }
    }
    public static String encode(final String str, final Charset charset) {
        try {
            return URLEncoder.encode(str, charset.name());
        } catch (UnsupportedEncodingException ex) {
            throw new RuntimeException(ex);
        }
    }
}
