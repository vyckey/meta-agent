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

package org.metaagent.framework.core.tool.converter;

import com.fasterxml.jackson.core.type.TypeReference;
import org.metaagent.framework.core.converter.JsonBiConverter;
import org.metaagent.framework.core.converter.JsonStringConverter;

/**
 * A converter that handles JSON serialization and deserialization for tool inputs and outputs.
 *
 * @author vyckey
 */
public class JsonToolConverter<I, O> extends DefaultToolConverter<I, O> implements ToolConverter<I, O> {
    public JsonToolConverter(JsonBiConverter<I> inputConverter) {
        super(inputConverter, new JsonStringConverter<>());
    }

    public static <I, O> JsonToolConverter<I, O> create(Class<I> inputType) {
        return new JsonToolConverter<>(JsonBiConverter.create(inputType));
    }

    public static <I, O> JsonToolConverter<I, O> create(TypeReference<I> inputType) {
        return new JsonToolConverter<>(JsonBiConverter.create(inputType));
    }

}
