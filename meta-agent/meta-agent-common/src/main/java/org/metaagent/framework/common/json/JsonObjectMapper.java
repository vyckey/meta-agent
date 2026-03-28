/*
 * MIT License
 *
 * Copyright (c) 2025 MetaAgent
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package org.metaagent.framework.common.json;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tools.jackson.core.JacksonException;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.JavaType;
import tools.jackson.databind.PropertyNamingStrategies;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.type.TypeFactory;

import java.lang.reflect.Type;

/**
 * {@link JsonMapper} for JSON
 *
 * <p>example 1:</p>
 * <pre>{@code
 * @Data
 * class Student {
 *     private Integer sAge;
 *     private String sName;
 * }
 *
 * Student student = JsonObjectMapper.CAMEL_CASE.fromJson("{\"sAge\": 18, \"sName\":\"Bob\"}", Student.class);
 * String json = JsonObjectMapper.SNAKE_CASE.toJson(student);
 * Assert.assertEquals("{\"s_age\":18,\"s_name\":\"Bob\"}", json);
 * }</pre>
 *
 * <p>example 2:</p>
 * <pre>{@code
 * class MyJsonUtils {
 *     public static final JsonObjectMapper INSTANCE = new JsonObjectMapper(new ObjectMapper()
 *             .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
 *             // custom features for yourself
 *     );
 * }
 * }</pre>
 *
 * @author vyckey
 */
public class JsonObjectMapper {
    private static final Logger log = LoggerFactory.getLogger(JsonObjectMapper.class);
    public static final JsonObjectMapper CAMEL_CASE = new JsonObjectMapper(JsonMapper.builder()
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
            .enable(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT)
            .build()
    );
    public static final JsonObjectMapper SNAKE_CASE = new JsonObjectMapper(JsonMapper.builder()
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
            .enable(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT)
            .propertyNamingStrategy(new PropertyNamingStrategies.SnakeCaseStrategy())
            .build()
    );

    private final JsonMapper jsonMapper;

    public JsonObjectMapper(JsonMapper jsonMapper) {
        this.jsonMapper = jsonMapper;
    }

    protected JsonMapper getJsonMapper() {
        return jsonMapper;
    }

    protected void handleException(JacksonException e) {
        log.error(e.getMessage(), e);
    }

    public String toJson(Object object) {
        try {
            return jsonMapper.writeValueAsString(object);
        } catch (JacksonException e) {
            handleException(e);
        }
        return null;
    }

    public <T> T fromJson(String json, Class<T> valueType) {
        try {
            return jsonMapper.readValue(json, valueType);
        } catch (JacksonException e) {
            handleException(e);
        }
        return null;
    }

    public <T> T fromJson(String json, TypeReference<T> valueTypeRef) {
        try {
            return jsonMapper.readValue(json, valueTypeRef);
        } catch (JacksonException e) {
            handleException(e);
        }
        return null;
    }

    public <T> T fromJson(String json, JavaType valueType) {
        try {
            return jsonMapper.readValue(json, valueType);
        } catch (JacksonException e) {
            handleException(e);
        }
        return null;
    }

    public <T> T fromJson(String json, Type type) {
        try {
            JavaType javaType = TypeFactory.createDefaultInstance().constructType(type);
            return jsonMapper.readValue(json, javaType);
        } catch (JacksonException e) {
            handleException(e);
        }
        return null;
    }

    public <T> T fromJson(byte[] json, Class<T> clazz) {
        if (json == null) {
            return null;
        }
        try {
            return jsonMapper.readValue(json, clazz);
        } catch (JacksonException e) {
            handleException(e);
        }
        return null;
    }

    public <T> T fromJson(byte[] json, TypeReference<T> type) {
        if (json == null) {
            return null;
        }
        try {
            return jsonMapper.readValue(json, type);
        } catch (JacksonException e) {
            handleException(e);
        }
        return null;
    }

    public <T> T convert(Object object, Class<T> clazz) {
        return jsonMapper.convertValue(object, clazz);
    }

    public <T> T convert(Object object, TypeReference<T> type) {
        return jsonMapper.convertValue(object, type);
    }
}
