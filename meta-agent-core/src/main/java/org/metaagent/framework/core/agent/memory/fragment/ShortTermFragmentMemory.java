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

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import lombok.Builder;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Short-term fragment memory.
 *
 * @author vyckey
 */
public class ShortTermFragmentMemory implements FragmentMemory {
    protected final Cache<String, Fragment> fragmentCache;

    @Builder
    public ShortTermFragmentMemory(int capacity, int expireAfterWrite, int expireAfterAccess, TimeUnit timeUnit) {
        this.fragmentCache = CacheBuilder.newBuilder()
                .maximumSize(capacity)
                .expireAfterWrite(expireAfterWrite, timeUnit)
                .expireAfterAccess(expireAfterAccess, timeUnit)
                .removalListener(notification -> onFragmentRemoved((Fragment) notification.getValue()))
                .build();
    }

    public ShortTermFragmentMemory(int capacity) {
        this(capacity, 30, 30, TimeUnit.MINUTES);
    }

    protected void onFragmentRemoved(Fragment fragment) {
    }

    @Override
    public void store(String key, Fragment fragment) {
        fragmentCache.put(key, fragment);
    }

    @Override
    public void store(Map<String, Fragment> fragments) {
        fragmentCache.putAll(fragments);
    }

    @Override
    public Fragment retrieve(String key) {
        return fragmentCache.getIfPresent(key);
    }

    @Override
    public Map<String, Fragment> retrieve(Set<String> keys) {
        return fragmentCache.getAllPresent(keys);
    }

    @Override
    public Map<String, Fragment> retrieveAll() {
        return fragmentCache.asMap();
    }

    @Override
    public void clear(String key) {
        fragmentCache.invalidate(key);
    }

    @Override
    public void clear() {
        fragmentCache.invalidateAll();
    }
}
