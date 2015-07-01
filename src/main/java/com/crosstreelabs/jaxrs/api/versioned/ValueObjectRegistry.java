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

import com.crosstreelabs.jaxrs.api.versioned.annotation.Version;
import com.crosstreelabs.jaxrs.api.versioned.util.VersionUtils;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import javax.ws.rs.core.MediaType;

/**
 * Keeps track of all known value objects. 
 */
public class ValueObjectRegistry {
    private static final Set<Class<? extends ValueObject>> CLASSES = new HashSet<>();
    
    public static Set<Class<? extends ValueObject>> getClasses() {
        return Collections.unmodifiableSet(CLASSES);
    }
    public static Class<? extends ValueObject> findForMediaType(final MediaType type) {
        for (Class<? extends ValueObject> cls : CLASSES) {
            if (!cls.isAnnotationPresent(Version.class)) {
                continue;
            }
            Version version = cls.getAnnotation(Version.class);
            if (VersionUtils.isCompatible(type, version)) {
                return cls;
            }
        }
        return null;
    }
    public static void register(final Class<? extends ValueObject> cls) {
        CLASSES.add(cls);
    }
    public static void register(final Class<? extends ValueObject>...classes) {
        register(Arrays.asList(classes));
    }
    public static void register(final Collection<Class<? extends ValueObject>> classes) {
        CLASSES.addAll(classes);
    }
    
}