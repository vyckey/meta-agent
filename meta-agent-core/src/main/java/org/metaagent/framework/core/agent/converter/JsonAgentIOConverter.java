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

package org.metaagent.framework.core.agent.converter;

import com.fasterxml.jackson.core.type.TypeReference;
import lombok.Getter;
import org.metaagent.framework.core.agent.output.AgentOutput;
import org.metaagent.framework.core.converter.JsonBiConverter;
import org.metaagent.framework.core.util.json.JsonSchemaGenerator;

import java.lang.reflect.Type;

/**
 * JSON format implementation of {@link AgentIOConverter}.
 *
 * @author vyckey
 */
@Getter
public class JsonAgentIOConverter<I, O, S> extends DefaultAgentIOConverter<I, O> {

    public JsonAgentIOConverter(Class<I> inputType, Class<O> outputType) {
        super(
                JsonSchemaGenerator.generateForType(inputType),
                JsonSchemaGenerator.generateForType(outputType),
                JsonBiConverter.create(new TypeReference<>() {
                    @Override
                    public Type getType() {
                        return inputType;
                    }
                }),
                JsonBiConverter.create(new TypeReference<AgentOutput<O>>() {
                    @Override
                    public Type getType() {
                        return outputType;
                    }
                }).reverse()
        );
    }
}
