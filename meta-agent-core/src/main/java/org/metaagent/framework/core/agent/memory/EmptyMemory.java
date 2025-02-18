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

package org.metaagent.framework.core.agent.memory;

import org.metaagent.framework.core.agent.memory.fragment.Fragment;

import java.util.Map;
import java.util.Set;

/**
 * description is here
 *
 * @author vyckey
 */
public class EmptyMemory implements ReadOnlyMemory {
    public static final EmptyMemory EMPTY_MEMORY = new EmptyMemory(false);
    public static final EmptyMemory INACCESSIBLE_MEMORY = new EmptyMemory(true);

    private final boolean fastFail;

    private EmptyMemory(boolean fastFail) {
        this.fastFail = fastFail;
    }

    @Override
    public void store(String key, Fragment fragment) {
        if (fastFail) {
            ReadOnlyMemory.super.store(key, fragment);
        }
    }

    @Override
    public void store(Map<String, Fragment> fragments) {
        if (fastFail) {
            ReadOnlyMemory.super.store(fragments);
        }
    }

    @Override
    public Fragment retrieve(String key) {
        if (fastFail) {
            throw new IllegalStateException("Memory is empty");
        }
        return null;
    }

    @Override
    public Map<String, Fragment> retrieve(Set<String> keys) {
        if (fastFail) {
            throw new IllegalStateException("Memory is empty");
        }
        return Map.of();
    }

    @Override
    public Map<String, Fragment> retrieveAll() {
        if (fastFail) {
            throw new IllegalStateException("Memory is empty");
        }
        return Map.of();
    }

    @Override
    public void clear(String key) {
        if (fastFail) {
            ReadOnlyMemory.super.clear(key);
        }
    }

    @Override
    public void clearAll() {
        if (fastFail) {
            ReadOnlyMemory.super.clearAll();
        }
    }
}
