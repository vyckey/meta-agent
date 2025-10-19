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

package org.metaagent.framework.core.tool.toolkit;

/**
 * ToolkitRegistry which manages Toolkit instances.
 *
 * @author vyckey
 */
public interface ToolkitRegistry {
    /**
     * Returns the global ToolkitRegistry instance.
     *
     * @return the global ToolkitRegistry instance
     */
    static ToolkitRegistry global() {
        return DefaultToolkitRegistry.global();
    }

    /**
     * Check if a Toolkit instance exists in the registry.
     *
     * @param toolkitName the name of the Toolkit instance
     * @return true if the Toolkit instance exists, false otherwise
     */
    default boolean hasToolkit(String toolkitName) {
        return getToolkit(toolkitName) != null;
    }

    /**
     * Get a Toolkit instance by name
     *
     * @param toolkitName the name of the Toolkit
     * @return the Toolkit instance
     */
    Toolkit getToolkit(String toolkitName);

    /**
     * Add a Toolkit instance
     *
     * @param toolkit the Toolkit instance
     */
    void addToolkit(Toolkit toolkit);

    /**
     * Add Toolkit instances
     *
     * @param toolkits the Toolkit instances
     */
    default void addToolkits(Toolkit... toolkits) {
        for (Toolkit toolkit : toolkits) {
            addToolkit(toolkit);
        }
    }

    /**
     * Remove a Toolkit instance
     *
     * @param toolkit the Toolkit instance
     */
    default void removeToolkit(Toolkit toolkit) {
        removeToolkit(toolkit.getName());
    }

    /**
     * Remove a Toolkit instance
     *
     * @param toolkitName the name of the Toolkit instance
     */
    void removeToolkit(String toolkitName);

}
