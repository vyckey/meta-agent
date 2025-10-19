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
import org.metaagent.framework.core.tool.container.ToolContainer;
import org.metaagent.framework.core.tool.toolkit.Toolkit;

/**
 * ToolManager is responsible for managing the lifecycle and access to various tools.
 * It provides methods to add, remove, and retrieve tools by their names.
 * It also allows checking the existence of a tool by its name.
 *
 * @author vyckey
 */
public interface ToolManager extends ToolContainer {
    /**
     * Create a new instance of ToolManager.
     *
     * @return a new instance of ToolManager
     */
    static ToolManager create() {
        return new DefaultToolManager();
    }

    /**
     * Add a tool to the tool manager.
     *
     * @param tools the tools to add
     * @return the tool manager
     */
    static ToolManager fromTools(Tool<?, ?>... tools) {
        ToolManager toolManager = create();
        toolManager.addTools(tools);
        return toolManager;
    }

    /**
     * Create a tool manager from toolkits.
     *
     * @param toolkits the toolkits to add
     * @return the tool manager
     */
    static ToolManager fromToolkits(Toolkit... toolkits) {
        ToolManager toolManager = create();
        for (Toolkit toolkit : toolkits) {
            toolManager.addToolkit(toolkit);
        }
        return toolManager;
    }

    /**
     * Add toolkit to the tool manager.
     *
     * @param toolkit the toolkit to add
     * @return the tool manager
     */
    ToolManager addToolkit(Toolkit toolkit);

    /**
     * Add toolkit to the tool manager with specific tool names.
     *
     * @param toolkit   the toolkit
     * @param toolNames the specific tool names to add
     * @return the tool manager
     */
    ToolManager addToolkit(Toolkit toolkit, String... toolNames);

    /**
     * Remove toolkit from the tool manager.
     * The namespace is used to identify the tools from the tool manager.
     * The names of tools in the toolkit will have a prefix of the namespace.
     * e.g. if the namespace is "file", the tool name "read_file" in the toolkit will be "file.read_file".
     *
     * @param namespace the namespace of the toolkit
     * @param toolkit   the toolkit
     * @param toolNames the specific tool names to add
     * @return the tool manager
     */
    ToolManager addToolkit(String namespace, Toolkit toolkit, String... toolNames);

    /**
     * Remove toolkit from the tool manager.
     *
     * @param toolkitName the toolkit name to remove
     */
    void removeToolkit(String toolkitName);

    /**
     * Add a tool change listener.
     *
     * @param listener the tool change listener.
     */
    void addToolChangeListener(ToolChangeListener listener);

    /**
     * Remove a tool change listener.
     *
     * @param listener the tool change listener.
     */
    void removeToolChangeListener(ToolChangeListener listener);
}