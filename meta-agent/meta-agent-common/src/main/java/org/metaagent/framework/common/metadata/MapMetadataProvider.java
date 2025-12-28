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

package org.metaagent.framework.common.metadata;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.google.common.collect.Maps;
import lombok.EqualsAndHashCode;

import java.util.Map;
import java.util.Objects;

/**
 * MapMetadataProvider is a simple implementation of MetadataProvider that uses a Map to store metadata.
 * It allows for easy retrieval, setting, and removal of metadata entries.
 *
 * @author vyckey
 */
@EqualsAndHashCode(of = "properties")
public class MapMetadataProvider implements MetadataProvider {
    private final Map<String, Object> properties;

    @JsonCreator
    public MapMetadataProvider(Map<String, Object> properties) {
        this.properties = Objects.requireNonNull(properties);
    }

    public MapMetadataProvider() {
        this(Maps.newHashMap());
    }

    public static Builder builder() {
        return new Builder();
    }

    @JsonValue
    @Override
    public Map<String, Object> getProperties() {
        return properties;
    }

    @Override
    public Object getProperty(String key) {
        return properties.get(key);
    }

    @Override
    public <T> T getProperty(String key, Class<T> type) {
        Object obj = properties.get(key);
        if (obj == null) {
            return null;
        }
        return type.cast(obj);
    }

    @Override
    public MetadataProvider setProperty(String key, Object value) {
        properties.put(key, value);
        return this;
    }

    @Override
    public void removeProperty(String key) {
        properties.remove(key);
    }

    @Override
    public void clear() {
        properties.clear();
    }

    @Override
    public void merge(MetadataProvider other) {
        this.properties.putAll(other.getProperties());
    }

    @Override
    public MetadataProvider union(MetadataProvider other) {
        return new Builder(getProperties())
                .setProperties(other.getProperties())
                .build();
    }

    public ImmutableMetadataProvider immutable() {
        return new ImmutableMetadataWrapper(this);
    }


    public record Builder(Map<String, Object> metadata) {
        private Builder() {
            this(Maps.newHashMap());
        }

        public Builder(Map<String, Object> metadata) {
            this.metadata = Objects.requireNonNull(metadata);
        }

        public Builder setProperties(Map<String, Object> metadata) {
            metadata.putAll(metadata);
            return this;
        }

        public Builder setProperty(String key, Object value) {
            metadata.put(key, value);
            return this;
        }

        public Builder removeProperty(String key, Object value) {
            metadata.remove(key);
            return this;
        }

        public MapMetadataProvider build() {
            return new MapMetadataProvider(metadata);
        }
    }
}
