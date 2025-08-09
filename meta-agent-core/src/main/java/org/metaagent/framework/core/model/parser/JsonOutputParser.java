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

package org.metaagent.framework.core.model.parser;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.lang.reflect.Type;
import java.util.Objects;

/**
 * Parses JSON output into a specified type using Jackson.
 *
 * @param <T> the type to parse the output into
 * @author vyckey
 */
public final class JsonOutputParser<T> implements StringOutputParser<T> {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper()
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

    private final ObjectMapper objectMapper;
    private final Type type;
    private final TypeReference<T> typeRef;

    public JsonOutputParser(ObjectMapper objectMapper, Type type) {
        this.objectMapper = Objects.requireNonNull(objectMapper, "ObjectMapper is required");
        this.type = Objects.requireNonNull(type, "Type is required");
        this.typeRef = null;
    }

    public JsonOutputParser(ObjectMapper objectMapper, TypeReference<T> typeRef) {
        this.objectMapper = Objects.requireNonNull(objectMapper, "ObjectMapper is required");
        this.type = null;
        this.typeRef = Objects.requireNonNull(typeRef, "TypeReference is required");
    }

    public JsonOutputParser(Type type) {
        this(OBJECT_MAPPER, type);
    }

    public JsonOutputParser(TypeReference<T> typeRef) {
        this(OBJECT_MAPPER, typeRef);
    }

    private String reviseJsonIfNeeded(String text) {
        text = text.trim();
        if (text.startsWith("```") && text.endsWith("```")) {
            if (text.startsWith("```json")) {
                return text.substring(7, text.length() - 3).trim();
            } else {
                return text.substring(3, text.length() - 3).trim();
            }
        }
        return text;
    }

    @SuppressWarnings("unchecked")
    @Override
    public T parse(String output) throws OutputParsingException {
        if (output == null) {
            return null;
        }
        String text = reviseJsonIfNeeded(output);
        try {
            if (typeRef != null) {
                return objectMapper.readValue(text, this.typeRef);
            }
            if (type instanceof Class<?>) {
                return objectMapper.readValue(text, (Class<? extends T>) type);
            } else if (type instanceof JavaType) {
                return objectMapper.readValue(text, (JavaType) type);
            } else {
                return objectMapper.readValue(text, new TypeReference<T>() {
                    @Override
                    public Type getType() {
                        return type;
                    }
                });
            }
        } catch (JsonProcessingException e) {
            throw new OutputParsingException(e.getMessage(), e);
        }
    }
}
