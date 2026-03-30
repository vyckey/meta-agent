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
import com.google.common.collect.ImmutableMap;
import lombok.EqualsAndHashCode;

import java.util.Map;
import java.util.Objects;

/**
 * ImmutableMapMetadataProvider is an immutable implementation of MetadataProvider that uses an ImmutableMap to store metadata.
 *
 * @author vyckey
 */
@EqualsAndHashCode(of = "properties")
public class ImmutableMapMetadataProvider implements ImmutableMetadataProvider {
    private final Map<String, Object> properties;

    @JsonCreator
    public ImmutableMapMetadataProvider(ImmutableMap<String, Object> properties) {
        this.properties = Objects.requireNonNull(properties, "properties cannot be null");
    }

    public ImmutableMapMetadataProvider() {
        this(ImmutableMap.of());
    }

    public static Builder builder() {
        return new Builder();
    }

    @Override
    public boolean isEmpty() {
        return properties.isEmpty();
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
    public MetadataProvider union(MetadataProvider other) {
        return new Builder(this)
                .setProperties(other.getProperties())
                .build();
    }

    @Override
    public ImmutableMapMetadataProvider copy() {
        return this;
    }


    public record Builder(ImmutableMap.Builder<String, Object> metadataBuilder) {
        private Builder() {
            this(ImmutableMap.builder());
        }

        public Builder(MetadataProvider metadata) {
            this();
            setProperties(metadata.getProperties());
        }

        public Builder setProperties(Map<String, Object> metadata) {
            this.metadataBuilder.putAll(metadata);
            return this;
        }

        public Builder setProperties(Iterable<? extends Map.Entry<String, Object>> properties) {
            metadataBuilder.putAll(properties);
            return this;
        }

        public Builder setProperty(String key, Object value) {
            metadataBuilder.put(key, value);
            return this;
        }

        public ImmutableMapMetadataProvider build() {
            return new ImmutableMapMetadataProvider(metadataBuilder.build());
        }
    }
}
