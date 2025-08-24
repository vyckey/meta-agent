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

package org.metaagent.framework.core.agent.memory.fragment;

import com.google.common.collect.Maps;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Default implementation of {@link FragmentMemory}.
 *
 * @author vyckey
 */
public class DefaultFragmentMemory implements FragmentMemory {
    protected final Map<String, Fragment> memory;

    public DefaultFragmentMemory(Map<String, Fragment> memory) {
        this.memory = Objects.requireNonNull(memory, "memory cannot be null");
    }

    public DefaultFragmentMemory() {
        this.memory = Maps.newHashMap();
    }

    @Override
    public void store(String key, Fragment fragment) {
        memory.put(key, fragment);
    }

    @Override
    public void store(Map<String, Fragment> fragments) {
        memory.putAll(fragments);
    }

    @Override
    public Fragment retrieve(String key) {
        return memory.get(key);
    }

    @Override
    public Map<String, Fragment> retrieve(Set<String> keys) {
        Map<String, Fragment> result = Maps.newHashMap();
        for (String key : keys) {
            if (memory.containsKey(key)) {
                result.put(key, memory.get(key));
            }
        }
        return result;
    }

    @Override
    public Map<String, Fragment> retrieveAll() {
        return Collections.unmodifiableMap(memory);
    }

    @Override
    public void clear(String key) {
        memory.remove(key);
    }

    @Override
    public void clear() {
        memory.clear();
    }
}
