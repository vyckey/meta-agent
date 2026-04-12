/*
 * MIT License
 *
 * Copyright (c) 2026 MetaAgent
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

package org.metaagent.framework.common.util;

import com.fasterxml.jackson.annotation.JsonCreator;

/**
 * ObjectHolder represents a holder for an object.
 *
 * @author vyckey
 */
public final class ObjectHolder<T> {
    private T value;

    private ObjectHolder(T value) {
        this.value = value;
    }

    @JsonCreator
    public static <T> ObjectHolder<T> of(T value) {
        return new ObjectHolder<>(value);
    }

    public static <T> ObjectHolder<T> empty() {
        return new ObjectHolder<>(null);
    }

    public boolean isEmpty() {
        return value == null;
    }

    public T get() {
        return value;
    }

    public void set(T value) {
        this.value = value;
    }
}
