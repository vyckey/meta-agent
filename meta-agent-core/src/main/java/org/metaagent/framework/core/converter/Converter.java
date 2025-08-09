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

import java.util.Objects;
import java.util.Optional;

/**
 * Converter interface for converting objects from one type to another.
 *
 * @author vyckey
 */
@FunctionalInterface
public interface Converter<S, T> {
    /**
     * Creates a converter that returns the source object as is.
     *
     * @param <T> the type of the source and target
     * @return a converter that returns the source object unchanged
     */
    static <T> Converter<T, T> self() {
        return source -> source;
    }

    /**
     * Converts the source object to the target type.
     *
     * @param source the source object to convert
     * @return the converted object of type T
     */
    T convert(S source);

    /**
     * Converts the source object to an Optional of the target type.
     *
     * @param source the source object to convert
     * @return an Optional containing the converted object, or empty if the source is null
     */
    default Optional<T> convertOptional(S source) {
        return Optional.ofNullable(convert(source));
    }

    /**
     * Creates a new converter that first converts the source object to type T
     * and then applies the next converter to the result.
     *
     * @param next the next converter to apply
     * @param <R>  the type of the final result
     * @return a new converter that applies both conversions
     */
    default <R> Converter<S, R> andThen(Converter<T, R> next) {
        Objects.requireNonNull(next, "Next converter cannot be null");
        return s -> {
            T target = convert(s);
            return target == null ? null : next.convert(target);
        };
    }
}
