package com.gvaneyck.ggengine.server.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;

public class JSON {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    public static String writeValueAsString(Object value) {
        try {
            return OBJECT_MAPPER.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to write value", e);
        }
    }

    public static <T> T convertValue(Object fromValue, Class<T> toValueType) {
        try {
            return OBJECT_MAPPER.convertValue(fromValue, toValueType);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Failed to convert value", e);
        }
    }

    public static <T> T readValue(String content, Class<T> valueType) {
        try {
            return OBJECT_MAPPER.readValue(content, valueType);
        } catch (IOException e) {
            throw new RuntimeException("Failed to read value", e);
        }
    }

    public static <T> T readValue(String content, TypeReference valueTypeRef) {
        try {
            return OBJECT_MAPPER.readValue(content, valueTypeRef);
        } catch (IOException e) {
            throw new RuntimeException("Failed to read value", e);
        }
    }
}
