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
package com.crosstreelabs.jaxrs.api.versioned.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Version {
    /**
     * The version number of the value object. Used to distinguish between two
     * value objects of the same type, but where one succeeds the others. Allows
     * client applications to submit older value objects and still have them
     * accepted.
     * @return The version number
     */
    int value();
    /**
     * One or more content types that this value object represents. Must *not*
     * include version number or structure representation. For example:
     * `application/vnd.crosstreelabs.user`.
     * 
     * There is no guarantee, nor is there any specification about which of many
     * content types will be used when serializing and transmitting a value
     * object. One may reasonably expect the first content type to be used, but
     * there is no guarantee.
     * @return One or more content types
     */
    String[] contentType();
}