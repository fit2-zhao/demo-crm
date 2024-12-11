package io.demo.config;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdScalarDeserializer;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;

/**
 * Configuration class for customizing the Jackson deserialization process.
 * <p>
 * This class adds custom logic to trim leading and trailing whitespace from String fields during deserialization.
 * </p>
 **/
@Configuration
public class RequestParamTrimConfig {

    /**
     * Defines a {@link Jackson2ObjectMapperBuilderCustomizer} Bean to customize Jackson's ObjectMapper settings,
     * specifically for String field deserialization.
     * <p>
     * Through this customization, all String fields deserialized from JSON will automatically have leading and trailing whitespace removed.
     * </p>
     *
     * @return Customized {@link Jackson2ObjectMapperBuilderCustomizer} instance
     */
    @Bean
    public Jackson2ObjectMapperBuilderCustomizer jackson2ObjectMapperBuilderCustomizer() {
        return jacksonObjectMapperBuilder -> {
            // Define custom deserialization logic for String fields
            jacksonObjectMapperBuilder
                    .deserializerByType(String.class, new StdScalarDeserializer<String>(String.class) {
                        @Override
                        public String deserialize(JsonParser jsonParser, DeserializationContext ctx)
                                throws IOException {
                            // Trim leading and trailing whitespace during deserialization
                            return StringUtils.trim(jsonParser.getValueAsString());
                        }
                    });
        };
    }
}