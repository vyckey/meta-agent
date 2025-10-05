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

import org.metaagent.framework.common.converter.Converter;

/**
 * Interface for parsing the output of an agent into a specific type.
 *
 * @param <O> the type of output
 * @param <T> the type to parse the output into
 * @author vyckey
 */
@FunctionalInterface
public interface OutputParser<O, T> extends Converter<O, T> {

    /**
     * Parses the output of an agent into a specific type.
     *
     * @param output the output to parse
     * @return the parsed output
     * @throws OutputParsingException if parsing fails
     */
    T parse(O output) throws OutputParsingException;

    /**
     * Default implementation of the convert method, which calls the parse method.
     *
     * @param source the source output to convert
     * @return the converted output
     * @throws OutputParsingException if parsing fails
     */
    @Override
    default T convert(O source) throws OutputParsingException {
        return parse(source);
    }
}
