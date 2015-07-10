package com.crosstreelabs.jaxrs.api.versioned.providers;

import com.crosstreelabs.jaxrs.api.versioned.AbstractValueObjectReaderWriter;
import com.crosstreelabs.jaxrs.api.versioned.ValueObject;
import com.google.gson.Gson;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Map;
import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.Provider;

/**
 * The DTOMessageReaderWriter produces DTO instances from request bodies.
 * When a specific DTO instance is required in a controller, this provider
 * pushes the request body data into a new DTO instance of the corresponding
 * type. If an abstract DTO instance is requested, the provider will attempt to
 * use the provided content type to determine the concrete DTO instance
 * required.
 * 
 * When a DTO instance is returned from a controller, this provider assumes the
 * response status code to be 200 OK and serializes the DTO according to the
 * requested structure, as determined by the accepted content type.
 */
@Provider
@Consumes({"*/*+json"})
@Produces({"*/*+json"})
public class GsonJsonValueObjectProvider
        extends AbstractValueObjectReaderWriter {
    private static final Gson GSON = new Gson();
    
    //~ AbstractValueObjectReaderWriter impl ~~~~~~~~~~~~~~~
    @Override
    public boolean isReadable(final Class<?> type,
            final Type genericType,
            final Annotation[] annotations,
            final MediaType mediaType) {
        return super.isReadable(type, genericType, annotations, mediaType)
                && mediaType.getSubtype().endsWith("+json");
    }

    @Override
    public Map readMap(final InputStream entityStream) {
        return GSON.fromJson(new InputStreamReader(entityStream), Map.class);
    }

    @Override
    public ValueObject readObject(final InputStream entityStream,
            final ValueObject vo) throws IOException {
        return GSON.fromJson(new InputStreamReader(entityStream), vo.getClass());
    }

    @Override
    public boolean isWriteable(final Class<?> type,
            final Type genericType,
            final Annotation[] annotations,
            final MediaType mediaType) {
        return super.isWriteable(type, genericType, annotations, mediaType)
                && mediaType.getSubtype().endsWith("+json");
    }

    @Override
    public void write(final ValueObject obj, final Annotation[] annotations,
            final MediaType mediaType,
            final MultivaluedMap<String, Object> headers,
            final OutputStream entityStream) throws IOException {
        entityStream.write(GSON.toJson(obj).getBytes());
    }
    
}