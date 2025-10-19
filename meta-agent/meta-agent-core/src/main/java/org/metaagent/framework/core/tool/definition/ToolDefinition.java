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

package org.metaagent.framework.core.tool.definition;

import org.metaagent.framework.common.metadata.MetadataProvider;

/**
 * ToolDefinition is the interface for tool definition.
 *
 * @author vyckey
 */
public interface ToolDefinition {
    String PROP_IS_CONCURRENCY_SAFE = "isConcurrencySafe";
    String PROP_IS_READ_ONLY = "isReadOnly";

    /**
     * Create a builder for the tool definition.
     *
     * @param name the name of the tool
     * @return the builder
     */
    static DefaultToolDefinition.Builder builder(String name) {
        return DefaultToolDefinition.builder(name);
    }

    /**
     * Get the name of the tool.
     *
     * @return the name of the tool
     */
    String name();

    /**
     * Get the description of the tool.
     *
     * @return the description of the tool
     */
    String description();

    /**
     * Get the input schema of the tool.
     *
     * @return the input schema of the tool
     */
    String inputSchema();

    /**
     * Get the output schema of the tool.
     *
     * @return the output schema of the tool
     */
    String outputSchema();

    /**
     * Get the metadata of the tool.
     *
     * @return the metadata of the tool
     */
    MetadataProvider metadata();

    /**
     * Get whether the tool is concurrency safe.
     *
     * @return whether the tool is concurrency safe
     */
    default boolean isConcurrencySafe() {
        return Boolean.TRUE.equals(metadata().getProperty(PROP_IS_CONCURRENCY_SAFE, Boolean.class));
    }

    /**
     * Get whether the tool is read-only.
     *
     * @return whether the tool is read-only
     */
    default boolean isReadOnly() {
        return Boolean.TRUE.equals(metadata().getProperty(PROP_IS_READ_ONLY, Boolean.class));
    }

}
