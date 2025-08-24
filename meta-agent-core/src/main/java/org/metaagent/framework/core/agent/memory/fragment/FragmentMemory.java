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

import org.metaagent.framework.core.agent.memory.Memory;

import java.util.Map;
import java.util.Set;

/**
 * Key-Value memory for storing fragments.
 *
 * @author vyckey
 */
public interface FragmentMemory extends Memory {
    /**
     * Store a fragment in the memory.
     *
     * @param key      the key to store the fragment under
     * @param fragment the fragment to store
     */
    void store(String key, Fragment fragment);

    /**
     * Store multiple fragments in the memory.
     *
     * @param fragments the fragments to store
     */
    void store(Map<String, Fragment> fragments);

    /**
     * Retrieve a fragment from the memory.
     *
     * @param key the key to retrieve the fragment under
     * @return the fragment, or null if not found
     */
    Fragment retrieve(String key);

    /**
     * Retrieve multiple fragments from the memory.
     *
     * @param keys the keys to retrieve the fragments under
     * @return the fragments, or null if not found
     */
    Map<String, Fragment> retrieve(Set<String> keys);

    /**
     * Retrieve all fragments from the memory.
     *
     * @return the all fragments
     */
    Map<String, Fragment> retrieveAll();

    /**
     * Clear the specified fragment from the memory.
     */
    void clear(String key);

}
