package io.demo.common.util;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.StreamReadConstraints;
import com.fasterxml.jackson.core.json.JsonReadFeature;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.type.CollectionType;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

/**
 * JSON utility class that encapsulates common JSON serialization and deserialization methods.
 * Supports conversion between objects and JSON strings, byte arrays, collections, maps, etc.
 */
public class JSON {

    // ObjectMapper instance for JSON operations
    private static final ObjectMapper objectMapper = JsonMapper.builder()
            .enable(JsonReadFeature.ALLOW_UNESCAPED_CONTROL_CHARS)  // Allow unescaped control characters in JSON
            .build();

    private static final TypeFactory typeFactory = objectMapper.getTypeFactory();

    // Default maximum string length
    public static final int DEFAULT_MAX_STRING_LEN = Integer.MAX_VALUE;

    // Static initialization block to configure ObjectMapper
    static {
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);  // Ignore unknown properties
        objectMapper.configure(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS, true);  // Use BigDecimal for floats
        objectMapper.configure(JsonParser.Feature.ALLOW_COMMENTS, true);  // Allow comments in JSON
        objectMapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);  // Auto-detect all fields
        objectMapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);  // Allow serialization of empty objects
        objectMapper.configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);  // Accept single value as array
        objectMapper.getFactory()
                .setStreamReadConstraints(StreamReadConstraints.builder().maxStringLength(DEFAULT_MAX_STRING_LEN).build());  // Set length limit for reading character streams
        objectMapper.registerModule(new JavaTimeModule());  // Register Java 8 time module
    }

    /**
     * Serialize an object to a JSON string.
     *
     * @param value Object to be serialized
     * @return JSON string
     */
    public static String toJSONString(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (IOException e) {
            throw new RuntimeException("JSON serialization failed", e);
        }
    }

    /**
     * Serialize an object to a formatted JSON string (with indentation).
     *
     * @param value Object to be serialized
     * @return Formatted JSON string
     */
    public static String toFormatJSONString(Object value) {
        try {
            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(value);
        } catch (IOException e) {
            throw new RuntimeException("JSON serialization failed", e);
        }
    }

    /**
     * Serialize an object to a byte array.
     *
     * @param value Object to be serialized
     * @return JSON byte array
     */
    public static byte[] toJSONBytes(Object value) {
        try {
            return objectMapper.writeValueAsBytes(value);
        } catch (IOException e) {
            throw new RuntimeException("JSON serialization failed", e);
        }
    }

    /**
     * Deserialize a JSON string to a Java object.
     *
     * @param content JSON string
     * @return Deserialized Java object
     */
    public static Object parseObject(String content) {
        return parseObject(content, Object.class);
    }

    /**
     * Deserialize a JSON string to a specified type of Java object.
     *
     * @param content JSON string
     * @param valueType Target Java class
     * @param <T> Type of the Java class
     * @return Deserialized Java object
     */
    public static <T> T parseObject(String content, Class<T> valueType) {
        try {
            return objectMapper.readValue(content, valueType);
        } catch (IOException e) {
            throw new RuntimeException("JSON deserialization failed", e);
        }
    }

    /**
     * Deserialize a JSON string to a specified type of Java object.
     *
     * @param content JSON string
     * @param valueType Target Java type reference
     * @param <T> Type of the Java class
     * @return Deserialized Java object
     */
    public static <T> T parseObject(String content, TypeReference<T> valueType) {
        try {
            return objectMapper.readValue(content, valueType);
        } catch (IOException e) {
            throw new RuntimeException("JSON deserialization failed", e);
        }
    }

    /**
     * Deserialize JSON data from an input stream to a Java object.
     *
     * @param src Input stream
     * @param valueType Target Java class
     * @param <T> Type of the Java class
     * @return Deserialized Java object
     */
    public static <T> T parseObject(InputStream src, Class<T> valueType) {
        try {
            return objectMapper.readValue(src, valueType);
        } catch (IOException e) {
            throw new RuntimeException("JSON deserialization failed", e);
        }
    }

    /**
     * Deserialize a JSON string to a collection of Java objects.
     *
     * @param content JSON string
     * @return Deserialized collection object
     */
    public static List parseArray(String content) {
        return parseArray(content, Object.class);
    }

    /**
     * Deserialize a JSON string to a collection of specified type of Java objects.
     *
     * @param content JSON string
     * @param valueType Collection element type
     * @param <T> Collection element type
     * @return Deserialized collection object
     */
    public static <T> List<T> parseArray(String content, Class<T> valueType) {
        CollectionType javaType = typeFactory.constructCollectionType(List.class, valueType);
        try {
            return objectMapper.readValue(content, javaType);
        } catch (IOException e) {
            throw new RuntimeException("JSON deserialization failed", e);
        }
    }

    /**
     * Deserialize a JSON string to a collection of specified type of Java objects.
     *
     * @param content JSON string
     * @param valueType Collection element type reference
     * @param <T> Collection element type
     * @return Deserialized collection object
     */
    public static <T> List<T> parseArray(String content, TypeReference<T> valueType) {
        try {
            JavaType subType = typeFactory.constructType(valueType);
            CollectionType javaType = typeFactory.constructCollectionType(List.class, subType);
            return objectMapper.readValue(content, javaType);
        } catch (IOException e) {
            throw new RuntimeException("JSON deserialization failed", e);
        }
    }

    /**
     * Deserialize a JSON string to a Map object.
     *
     * @param jsonObject JSON string
     * @return Deserialized Map object
     */
    public static Map parseMap(String jsonObject) {
        try {
            return objectMapper.readValue(jsonObject, new TypeReference<>() {});
        } catch (JsonProcessingException e) {
            throw new RuntimeException("JSON deserialization failed", e);
        }
    }
}