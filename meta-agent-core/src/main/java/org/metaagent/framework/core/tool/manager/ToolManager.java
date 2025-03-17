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

package org.metaagent.framework.core.tool.manager;

import org.metaagent.framework.core.tool.Tool;

import java.util.Set;

/**
 * ToolManager is responsible for managing the lifecycle and access to various tools.
 * It provides methods to add, remove, and retrieve tools by their names.
 * It also allows checking the existence of a tool by its name.
 *
 * @author vyckey
 */
public interface ToolManager {
    /**
     * Retrieves the names of all registered tools.
     *
     * @return a set of tool names.
     */
    Set<String> getToolNames();

    /**
     * Checks if a tool with the specified name exists.
     *
     * @param name the name of the tool.
     * @return true if the tool exists, false otherwise.
     */
    boolean hasTool(String name);

    /**
     * Retrieves a tool by its name.
     *
     * @param name the name of the tool.
     * @return the tool instance, or null if no tool with the specified name exists.
     */
    Tool<?, ?> getTool(String name);

    /**
     * Adds a new tool to the manager.
     *
     * @param tool the tool to be added.
     */
    void addTool(Tool<?, ?> tool);

    /**
     * Removes a tool from the manager.
     *
     * @param tool the tool to be removed.
     */
    void removeTool(Tool<?, ?> tool);

    /**
     * Removes a tool from the manager by its name.
     *
     * @param name the name of the tool to be removed.
     */
    void removeTool(String name);

    /**
     * Add a tool change listener.
     *
     * @param listener the tool change listener.
     */
    void addChangeListener(ToolChangeListener listener);

    /**
     * Remove a tool change listener.
     *
     * @param listener the tool change listener.
     */
    void removeChangeListener(ToolChangeListener listener);
}