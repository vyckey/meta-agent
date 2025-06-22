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

package org.metaagent.framework.core.common.metadata;

import com.google.common.collect.Maps;

import java.util.Map;
import java.util.Objects;

/**
 * MapMetadataProvider is a simple implementation of MetadataProvider that uses a Map to store metadata.
 * It allows for easy retrieval, setting, and removal of metadata entries.
 *
 * @author vyckey
 */
public class MapMetadataProvider implements MetadataProvider {
    private final Map<String, Object> metadata;

    public MapMetadataProvider(Map<String, Object> metadata) {
        this.metadata = Objects.requireNonNull(metadata);
    }

    public MapMetadataProvider() {
        this.metadata = Maps.newHashMap();
    }

    @Override
    public Map<String, Object> getProperties() {
        return metadata;
    }

    @Override
    public Object getProperty(String key) {
        return metadata.get(key);
    }

    @Override
    public <T> T getProperty(String key, Class<T> type) {
        Object obj = metadata.get(key);
        if (obj == null) {
            return null;
        }
        return type.cast(obj);
    }

    @Override
    public void setProperty(String key, Object value) {
        metadata.put(key, value);
    }

    @Override
    public void removeProperty(String key) {
        metadata.remove(key);
    }

    @Override
    public void merge(MetadataProvider other) {
        this.metadata.putAll(other.getProperties());
    }
}
