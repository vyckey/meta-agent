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

package org.metaagent.framework.common.converter;

/**
 * BiConverter is a two-way converter interface that allows conversion.
 *
 * @author vyckey
 */
public interface BiConverter<S, T> extends Converter<S, T> {
    /**
     * Converts the source object to the target type.
     *
     * @param source the source object to convert
     * @return the converted object of type T
     */
    @Override
    T convert(S source);

    /**
     * Converts the target object back to the source type.
     *
     * @param target the target object to convert
     * @return the converted object of type S
     */
    S reverse(T target);

    /**
     * Creates a BiConverter that reverses the conversion.
     *
     * @return a BiConverter that reverses the conversion
     */
    default BiConverter<T, S> reverse() {
        BiConverter<S, T> self = this;
        return new BiConverter<>() {
            @Override
            public S convert(T target) {
                return self.reverse(target);
            }

            @Override
            public T reverse(S source) {
                return self.convert(source);
            }
        };
    }
}
