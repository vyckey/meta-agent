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

import java.util.List;
import java.util.Objects;

/**
 * PageResult represents a page of results.
 *
 * @author vyckey
 */
public record PageResult<T>(List<T> items, String nextCursor, boolean hasMore) {
    public PageResult {
        Objects.requireNonNull(items, "items is required");
        if (hasMore && nextCursor == null) {
            throw new IllegalArgumentException("nextCursor is required when hasMore is true");
        }
        if (!hasMore && nextCursor != null) {
            throw new IllegalArgumentException("nextCursor must be null when hasMore is false");
        }
    }

    /**
     * Create a page result.
     *
     * @param items      items
     * @param nextCursor next cursor
     * @param hasMore    has more
     * @return page result
     */
    public static <T> PageResult<T> of(List<T> items, String nextCursor, boolean hasMore) {
        return new PageResult<>(items, nextCursor, hasMore);
    }

    /**
     * Create an empty page result.
     *
     * @return empty page result
     */
    public static <T> PageResult<T> empty() {
        return new PageResult<>(List.of(), null, false);
    }
}
