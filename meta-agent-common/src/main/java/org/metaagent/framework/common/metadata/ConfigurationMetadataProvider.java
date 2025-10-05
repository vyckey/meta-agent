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

import org.apache.commons.configuration2.BaseConfiguration;
import org.apache.commons.configuration2.Configuration;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;

/**
 * Provides metadata from a {@link Configuration} object.
 * <p>
 * This class implements the {@link MetadataProvider} interface and retrieves metadata
 * from the provided configuration, allowing for dynamic access to configuration properties.
 * It supports getting all metadata, specific metadata by key, and setting or removing metadata.
 *
 * @author vyckey
 */
public class ConfigurationMetadataProvider implements MetadataProvider {
    private final Configuration configuration;

    public ConfigurationMetadataProvider(Configuration configuration) {
        this.configuration = Objects.requireNonNull(configuration, "Configuration cannot be null");
    }

    public ConfigurationMetadataProvider() {
        this(new BaseConfiguration());
    }

    public Configuration getConfiguration() {
        return configuration;
    }

    @Override
    public Map<String, Object> getProperties() {
        Map<String, Object> metadata = new HashMap<>();
        Iterator<String> iterator = configuration.getKeys();
        while (iterator.hasNext()) {
            String key = iterator.next();
            metadata.put(key, configuration.getProperty(key));
        }
        return metadata;
    }

    @Override
    public Object getProperty(String key) {
        return configuration.getProperty(key);
    }

    @Override
    public <T> T getProperty(String key, Class<T> type) {
        Object value = configuration.getProperty(key);
        if (value == null) {
            return null;
        } else if (type.isAssignableFrom(value.getClass())) {
            return type.cast(value);
        }
        throw new IllegalArgumentException("Unsupported property type: " + type.getName());
    }

    @Override
    public MetadataProvider setProperty(String key, Object value) {
        configuration.setProperty(key, value);
        return this;
    }

    @Override
    public void removeProperty(String key) {
        configuration.clearProperty(key);
    }

    @Override
    public void merge(MetadataProvider other) {
        other.getProperties().forEach(this::setProperty);
    }
}
