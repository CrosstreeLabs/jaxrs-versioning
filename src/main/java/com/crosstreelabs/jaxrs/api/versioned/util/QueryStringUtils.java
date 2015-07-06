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
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class QueryStringUtils {
    public static Map<String, Object> toMap(final String queryString)
            throws UnsupportedEncodingException {
        final Map result = new IndexedMap();
        if (queryString == null || queryString.isEmpty()) {
            return result;
        }
        
        // Split into parameter pairs
        final String params[] = queryString.split("&");
        for (String param : params) {
            final String[] parts = param.split("=", 2);
            final String[] path = keyToPath(parts[0]);
            final String value = parts.length > 1 ? parts[1] : "";
            
            // Now iterate over the key path until we're done
            Map map = result;
            for (int i = 0; i < path.length; i++) {
                String part = path[i];
                // If no element exists for this part, we can add it straight
                // away
                if (!map.containsKey(part)) {
                    // This is the end of the key path, so just put the value
                    if (i >= path.length - 1) {
                        map.put(part, value);
                    } else {
                        Map m = new IndexedMap();
                        map.put(part, m);
                        map = m;
                    }
                    continue;
                }
                
                Object current = map.get(part);
                // If it's already a map, we can continue on down the path
                if (current instanceof Map) {
                    if (i >= path.length - 1) {
                        ((Map)current).put(part, value);
                    } else {
                        map = (Map)current;
                    }
                }
                // If it's a scalar object, we need to convert it to a list
                // (in map form for weird indices)
                else {
                    Map m = new IndexedMap();
                    m.put("0", current);
                    if (i >= path.length) {
                        m.put("1", value);
                    }
                    m.put("2", value);
                    map.put(part, m);
                    map = m;
                }
            }
        }
        return result;
    }
    
    public static String toQueryString(final Map<?,?> map, final boolean encoded, final Charset charset) {
        StringBuilder sb = new StringBuilder();
        boolean first = true;
        for (Map.Entry entry : map.entrySet()) {
            String encodedName = (String)entry.getKey();
            if (!encoded) {
                encodedName = EncodingUtils.encode(encodedName, charset);
            }
            
            if (entry.getValue() instanceof Map) {
                for (String str : toSubQueryString((Map)entry.getValue(), encoded, charset)) {
                    sb.append("&").append(encodedName).append(str);
                    first = false;
                }
            } else if (entry.getValue() instanceof List) {
                for (String str : toQueryString((List)entry.getValue(), encoded, charset)) {
                    sb.append("&").append(encodedName).append(str);
                    first = false;
                }
            } else {
                String value = (String)entry.getValue();
                if (!encoded) {
                    value = EncodingUtils.encode(entry.getValue().toString(), charset);
                }
                sb.append("&").append(encodedName).append("=").append(value);
                first = false;
            }
        }

        return sb.toString();
    }
    protected static List<String> toSubQueryString(final Map<?,?> map, final boolean encoded, final Charset charset) {
        List<String> result = new ArrayList<>();
        for (Map.Entry entry : map.entrySet()) {
            String encodedName = (String)entry.getKey();
            if (!encoded) {
                encodedName = EncodingUtils.encode(encodedName, charset);
            }
            encodedName = "["+encodedName+"]";
            
            if (entry.getValue() instanceof Map) {
                for (String str : toSubQueryString((Map)entry.getValue(), encoded, charset)) {
                    result.add(encodedName+str);
                }
            } else if (entry.getValue() instanceof List) {
                for (String str : toQueryString((List)entry.getValue(), encoded, charset)) {
                    result.add(encodedName+str);
                }
            } else {
                String value = (String)entry.getValue();
                if (!encoded) {
                    value = EncodingUtils.encode(entry.getValue().toString(), charset);
                }
                result.add(encodedName+"="+value);
            }
        }
        return result;
    }
    public static List<String> toQueryString(final List list, final boolean encoded, final Charset charset) {
        List<String> result = new ArrayList();
        Iterator it = list.iterator();
        for (int i = 0; it.hasNext(); i++) {
            Object obj = it.next();
            
            String current = i+"";
            if (obj instanceof Map) {
                current += toQueryString((Map)obj, encoded, charset);
            } else if (obj instanceof List) {
                current += toQueryString((List)obj, encoded, charset);
            } else {
                String value = (String)obj;
                if (!encoded) {
                    value = EncodingUtils.encode((String)obj, charset);
                }
                current += "="+value;
            }
            result.add(current);
        }
        return result;
    }
    
    protected static String[] keyToPath(final String key) {
        List<String> result = new ArrayList<>();
        boolean bracketed = false;
        String current = "";
        for (int i = 0; i < key.length(); i++) {
            char ch = key.charAt(i);
            
            if (ch == '[' && !bracketed) {
                bracketed = true;
                result.add(current.trim());
                current = "";
                continue;
            }
            if (ch == ']' && bracketed) {
                bracketed = false;
                result.add(current.trim());
                current = "";
                if (key.length() > i+1 && key.charAt(i+1) == '.') {
                    i++;
                }
                if (key.length() > i+1 && key.charAt(i+1) == '[') {
                    bracketed = true;
                    i++;
                }
                continue;
            }
            if (ch == '.') {
                result.add(current.trim());
                current = "";
                continue;
            }
            if (i >= key.length() - 1) {
                result.add((current + ch).trim());
                continue;
            }
            
            current += ch;
        }
        
        return result.toArray(new String[result.size()]);
        
    }
    
    public static class IndexedMap extends HashMap<String, Object> {
        private int idx = 0;

        @Override
        public Object put(String k, Object v) {
            if (k == null || k.isEmpty()) {
                return super.put(""+idx++, v);
            } else {
                return super.put(k, v);
            }
        }
    }
}