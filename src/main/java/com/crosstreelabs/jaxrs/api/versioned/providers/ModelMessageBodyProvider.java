package com.crosstreelabs.jaxrs.api.versioned.providers;

import com.crosstreelabs.jaxrs.api.versioned.ValueObject;
import com.crosstreelabs.jaxrs.api.versioned.ValueObjectRegistry;
import com.crosstreelabs.jaxrs.api.versioned.annotation.Version;
import com.crosstreelabs.jaxrs.api.versioned.mapper.Mapper;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import javax.ws.rs.NotSupportedException;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ModelMessageBodyProvider implements MessageBodyWriter<Object> {
    private static final Logger LOGGER = LoggerFactory.getLogger(ModelMessageBodyProvider.class);
    private final Mapper mapper;
    
    public ModelMessageBodyProvider(final Mapper mapper) {
        this.mapper = mapper;
    }
    
    @Override
    public boolean isWriteable(final Class<?> type,
            final Type genericType,
            final Annotation[] annotations,
            final MediaType mediaType) {
        Class<? extends ValueObject> vo = ValueObjectRegistry.findForMediaType(mediaType);
        if (vo == null) {
            throw new NotSupportedException();
        }
        if (!valueObjectHasModel(vo, type)) {
            return false;
        }
        for (String str : mapper.supportedStructures()) {
            if (mediaType.getSubtype().endsWith("+"+str)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public long getSize(final Object t,
            final Class<?> type,
            final Type genericType,
            final Annotation[] annotations,
            final MediaType mediaType) {
        return -1;
    }

    @Override
    public void writeTo(final Object t,
            final Class<?> type,
            final Type genericType,
            final Annotation[] annotations,
            final MediaType mediaType,
            final MultivaluedMap<String, Object> httpHeaders,
            final OutputStream entityStream)
            throws IOException, WebApplicationException {
        Class<? extends ValueObject> vo = ValueObjectRegistry.findForMediaType(mediaType);
        
        // See if the value object knows how to consume the model
        try {
            Method method = vo.getDeclaredMethod("consume", t.getClass());
            ValueObject o = vo.newInstance();
            method.invoke(o, t);
            entityStream.write(mapper.asBytes(o));
            return;
        } catch (IllegalAccessException | IllegalArgumentException
                | InstantiationException | InvocationTargetException
                | NoSuchMethodException | SecurityException ex) {
            LOGGER.warn(ex.getClass().getName()+": "+ex.getMessage());
            LOGGER.debug("", ex);
        }
        
        // Otherwise, we'll try to map it
        try {
            entityStream.write(mapper.asBytes(mapper.convertValue(t, vo)));
        } catch (Exception ex) {
            LOGGER.warn(ex.getClass().getName()+": "+ex.getMessage());
            LOGGER.debug("", ex);
            throw ex;
        }
    }
    
    
    protected boolean valueObjectHasModel(final Class<? extends ValueObject> vo,
            final Class<?> model) {
        Version version = vo.getAnnotation(Version.class);
        for (Class<?> known : version.models()) {
            if (known.equals(model)) {
                return true;
            }
        }
        return false;
    }
    
}
