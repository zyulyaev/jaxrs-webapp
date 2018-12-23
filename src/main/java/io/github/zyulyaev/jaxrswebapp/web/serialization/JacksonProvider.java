package io.github.zyulyaev.jaxrswebapp.web.serialization;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;

import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.Provider;

/**
 * Jackson's {@link ObjectMapper} provider. Does some configuration of that object mapper.
 */
@Provider
public class JacksonProvider implements ContextResolver<ObjectMapper> {
    private final ObjectMapper objectMapper = new ObjectMapper()
            .enable(DeserializationFeature.FAIL_ON_IGNORED_PROPERTIES)
            .registerModule(new SimpleModule() {{
                addSerializer(new InstantSerializer());
            }});

    @Override
    public ObjectMapper getContext(Class<?> type) {
        return objectMapper;
    }
}
