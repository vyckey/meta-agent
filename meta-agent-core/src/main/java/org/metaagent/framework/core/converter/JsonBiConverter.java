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

package org.metaagent.framework.core.converter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Objects;

/**
 * description is here
 *
 * @author vyckey
 */
public abstract class JsonBiConverter<T> implements BiConverter<String, T> {
    public static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    protected final ObjectMapper objectMapper;

    protected JsonBiConverter(ObjectMapper objectMapper) {
        this.objectMapper = Objects.requireNonNull(objectMapper, "objectMapper is required");
    }

    @Override
    public String reverse(T target) {
        if (target == null) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(target);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Failed to convert object to JSON.", e);
        }
    }

    protected RuntimeException wrapException(String json, Exception e) {
        String jsonStr;
        if (json.length() < 100) {
            jsonStr = json.replaceAll("\n", "");
        } else {
            jsonStr = json.substring(0, 100).replaceAll("\n", "") + "...";
        }
        return new IllegalArgumentException("Failed to convert JSON to object. json:" + jsonStr, e);
    }

    public static <T> JsonBiConverter<T> create(Class<T> type) {
        return create(OBJECT_MAPPER, type);
    }

    public static <T> JsonBiConverter<T> create(ObjectMapper objectMapper, Class<T> type) {
        return new JsonBiConverter<>(objectMapper) {
            @Override
            public T convert(String json) {
                try {
                    return objectMapper.readValue(json, type);
                } catch (JsonProcessingException e) {
                    throw wrapException(json, e);
                }
            }
        };
    }

    public static <T> JsonBiConverter<T> create(TypeReference<T> typeRef) {
        return create(OBJECT_MAPPER, typeRef);
    }

    public static <T> JsonBiConverter<T> create(ObjectMapper objectMapper, TypeReference<T> typeRef) {
        return new JsonBiConverter<>(objectMapper) {
            @Override
            public T convert(String json) {
                try {
                    return objectMapper.readValue(json, typeRef);
                } catch (JsonProcessingException e) {
                    throw wrapException(json, e);
                }
            }
        };
    }
}
