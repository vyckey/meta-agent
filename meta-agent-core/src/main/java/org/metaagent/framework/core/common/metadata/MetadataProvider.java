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

import java.util.Map;
import java.util.Optional;

/**
 * MetadataProvider interface for managing metadata.
 *
 * @author vyckey
 */
public interface MetadataProvider {
    /**
     * Creates a new MetadataProvider instance.
     *
     * @return a new MetadataProvider instance.
     */
    static MetadataProvider create() {
        return new MapMetadataProvider();
    }

    /**
     * Gets all metadata as a map.
     *
     * @return a map containing all metadata.
     */
    Map<String, Object> getProperties();

    /**
     * Gets metadata by key.
     *
     * @param key the metadata key.
     * @return the metadata value associated with the key.
     */
    Object getProperty(String key);

    /**
     * Gets metadata by key and type.
     *
     * @param key  the metadata key.
     * @param type the expected type of the metadata value.
     * @param <T>  the type of the metadata value.
     * @return the metadata value associated with the key, cast to the specified type.
     */
    <T> T getProperty(String key, Class<T> type);

    /**
     * Gets metadata by key and type with default value.
     *
     * @param key          the metadata key.
     * @param type         the expected type of the metadata value.
     * @param defaultValue the default value to return if the metadata key does not exist.
     * @param <T>          the expected type of the metadata value.
     * @return the metadata value associated with the key, cast to the specified type,
     * or the default value if the metadata key does not exist.
     */
    default <T> T getProperty(String key, Class<T> type, T defaultValue) {
        return Optional.ofNullable(getProperty(key, type)).orElse(defaultValue);
    }

    /**
     * Sets metadata with a key-value pair.
     *
     * @param key   the metadata key.
     * @param value the metadata value.
     */
    void setProperty(String key, Object value);

    /**
     * Removes metadata by key.
     *
     * @param key the metadata key to remove.
     */
    void removeProperty(String key);

    /**
     * Merges metadata from another MetadataProvider into this one.
     *
     * @param other the MetadataProvider to merge from.
     */
    void merge(MetadataProvider other);
}
