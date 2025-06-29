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
import com.fasterxml.jackson.databind.ObjectMapper;

import java.lang.reflect.Type;
import java.util.Objects;

/**
 * Parses JSON output into a specified type using Jackson.
 *
 * @param <T> the type to parse the output into
 * @author vyckey
 */
public class JsonOutputParser<T> implements OutputParser<String, T> {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper()
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
    private final TypeReference<T> typeRef;

    public JsonOutputParser(TypeReference<T> typeRef) {
        this.typeRef = Objects.requireNonNull(typeRef, "typeRef must not be null");
    }

    public JsonOutputParser(Class<T> type) {
        this.typeRef = new TypeReference<>() {
            @Override
            public Type getType() {
                return type;
            }
        };
    }

    @Override
    public T parse(String output) throws OutputParsingException {
        try {
            return OBJECT_MAPPER.readValue(output, this.typeRef);
        } catch (JsonProcessingException e) {
            throw new OutputParsingException(e.getMessage(), e);
        }
    }
}
