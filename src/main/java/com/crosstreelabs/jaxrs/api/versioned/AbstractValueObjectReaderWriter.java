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
import com.crosstreelabs.jaxrs.api.versioned.util.AnnotationUtils;
import com.crosstreelabs.jaxrs.api.versioned.util.VersionUtils;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.Map;
import javax.validation.ValidationException;
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.NotAcceptableException;
import javax.ws.rs.NotSupportedException;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.MessageBodyWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The AbstractValueObjectReaderWriter enforces basic and required functionality
 * that must exist across all value object reader/writer implementations in
 * order to retrieve value objects from request bodies, and to serialize value
 * objects for transit.
 * 
 * When a supported value object instance is required in a location for which
 * a MessageBodyReader may be used, an implementation may convert the request
 * body into a new instance of the corresponding type. Abstractions are
 * supported, whereby the supplied content-type is used to negotiate the type
 * of value object to be returned.
 * 
 * When a value object is returned from a location for which a MessageBodyWriter
 * may be used, an implementation may serialize the value object for transit. In
 * this case, the response status code is assumed to be, and set to, 200 OK.
 * The Accept header *must* match the content type of the provided value object,
 * otherwise a 406 Not Acceptable will be returned, along with the serialized
 * representation of the value object.
 */
public abstract class AbstractValueObjectReaderWriter
        implements MessageBodyReader<ValueObject>, MessageBodyWriter<ValueObject> {
    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractValueObjectReaderWriter.class);
    
    public abstract Map readMap(InputStream entityStream) throws IOException;
    
    public abstract ValueObject readObject(InputStream entityStream,
            ValueObject vo) throws IOException;
    
    public abstract void write(ValueObject obj, Annotation[] annotations,
            MediaType mediaType, MultivaluedMap<String, Object> headers,
            OutputStream entityStream) throws IOException;
    
    //~ MessageBodyReader/Writer impl ~~~~~~~~~~~~~~~~~~~~~
    /**
     * Determines whether or not the reader can handle the given type. The type
     * is required to implement the ValueObject interface.
     * @param type
     * @param genericType
     * @param annotations
     * @param mediaType
     * @return 
     */
    @Override
    public boolean isReadable(final Class<?> type,
            final Type genericType,
            final Annotation[] annotations,
            final MediaType mediaType) {
        // Ensure we're getting a ValueObject
        if (!ValueObject.class.isAssignableFrom(type)) {
            return false;
        }
        
        // If the requested type is neither an interface nor abstract, and is
        // also versioned, then we check it's content types for compatibility
        // and return the appropriate response.
        if (!type.isInterface() && !Modifier.isAbstract(type.getModifiers())
                && type.isAnnotationPresent(Version.class)) {
            Version version = type.getAnnotation(Version.class);
            if (VersionUtils.isCompatible(mediaType, version)) {
                return true;
            }
        }
        
        // If the request type is an interface, is abstract, is not versioned,
        // or is otherwise incompatible according to above, we check the content
        // type itself.
        Class<? extends ValueObject> cls = ValueObjectRegistry.findForMediaType(mediaType);
        if (cls == null || !type.isAssignableFrom(cls)) {
            throw new NotSupportedException();
        }
        return true;
    }

    @Override
    public ValueObject readFrom(final Class<ValueObject> type,
            final Type genericType,
            final Annotation[] annotations,
            final MediaType mediaType,
            final MultivaluedMap<String, String> httpHeaders,
            final InputStream entityStream)
            throws IOException, WebApplicationException {
        Class<? extends ValueObject> cls = ValueObjectRegistry.findForMediaType(mediaType);
        if (cls == null) {
            throw new NotSupportedException();
        }
        ValueObject vo = newInstance(cls);
        if (entityStream != null) {
            if (vo instanceof Consumer) {
                ((Consumer)vo).consume(readMap(entityStream));
            } else {
                vo = readObject(entityStream, vo);
            }
        }
        if (requiresValidation(annotations)) {
            validate(vo);
        }
        return vo;
    }

    @Override
    public boolean isWriteable(final Class<?> type,
            final Type genericType,
            final Annotation[] annotations,
            final MediaType mediaType) {
        // Ensure we're serializing a ValueObject
        if (!ValueObject.class.isAssignableFrom(type)) {
            return false;
        }
        
        // Ensure it's versioned
        if (!type.isAnnotationPresent(Version.class)) {
            return false;
        }
        return VersionUtils.isCompatible(mediaType, type.getAnnotation(Version.class));
    }

    @Override
    public long getSize(final ValueObject t,
            final Class<?> type,
            final Type genericType,
            final Annotation[] annotations,
            final MediaType mediaType) {
        return -1;
    }

    @Override
    public void writeTo(final ValueObject obj,
            final Class<?> type,
            final Type genericType,
            final Annotation[] annotations,
            final MediaType mediaType,
            final MultivaluedMap<String, Object> httpHeaders,
            final OutputStream entityStream)
            throws IOException, WebApplicationException {
        if (!type.isAnnotationPresent(Version.class) || !(obj instanceof ValueObject)) {
            throw new InternalServerErrorException();
        }
        ValueObject vo = (ValueObject)obj;
        Version version = type.getAnnotation(Version.class);
        if (version.contentType().length == 0) {
            throw new NotAcceptableException();
        }
        if (requiresValidation(annotations)) {
            validate(vo);
        }
        if (!VersionUtils.isCompatible(mediaType, version)) {
            httpHeaders.putSingle("Content-Type", VersionUtils.defaultMediaType(version));
        } else {
            httpHeaders.putSingle("Content-Type", mediaType.toString());
        }
        write(vo, annotations, mediaType, httpHeaders, entityStream);
    }
    
    //~ Internal helpers ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    
    protected <T> T newInstance(final Class<T> type)
            throws IOException {
        try {
            return type.newInstance();
        } catch (IllegalAccessException | InstantiationException ex) {
            throw new IOException(ex);
        }
    }
    
    protected void validate(final ValueObject vo) {
        try {
            Class validationHelper = Class.forName("com.crosstreelabs.jaxrs.api.versioned.util.ValidationUtils");
            Method method = validationHelper.getDeclaredMethod("validate", Object.class);
            method.invoke(null, vo);
        } catch (InvocationTargetException ex) {
            if (ex.getCause() instanceof ValidationException) {
                throw (ValidationException)ex.getCause();
            }
            LOGGER.debug("Unable to validate vo", ex);
        } catch (ClassNotFoundException | IllegalAccessException
                | IllegalArgumentException | NoSuchMethodException
                | SecurityException ex) {
            LOGGER.warn("Unable to validate vo", ex);
        }
    }
    
    protected boolean requiresValidation(final Annotation[] annotations) {
        try {
            Class valid = Class.forName("javax.validation.Valid");
            return AnnotationUtils.find(valid, annotations) != null;
        } catch (ClassNotFoundException ex) {
            LOGGER.warn("Validation library not present");
        }
        return false;
    }
    
}