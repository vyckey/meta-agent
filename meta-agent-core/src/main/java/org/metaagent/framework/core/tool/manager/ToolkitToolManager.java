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
import org.metaagent.framework.core.tool.toolkit.Toolkit;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Toolkit Tool Manager
 *
 * @author vyckey
 */
public class ToolkitToolManager extends DefaultToolManager {
    protected final List<Toolkit> toolkits;

    public ToolkitToolManager(List<Toolkit> toolkits) {
        this.toolkits = toolkits;
    }

    public ToolkitToolManager(Toolkit... toolkits) {
        this.toolkits = List.of(toolkits);
    }

    @Override
    public Set<String> getToolNames() {
        Set<String> toolNames = toolkits.stream().map(Toolkit::listTools)
                .flatMap(List::stream).map(Tool::getName).collect(Collectors.toSet());
        toolNames.addAll(tools.keySet());
        return toolNames;
    }

    @Override
    public boolean hasTool(String name) {
        return getTool(name) != null;
    }

    @Override
    public Tool<?, ?> getTool(String name) {
        Tool<?, ?> tool = super.getTool(name);
        if (tool != null) {
            return tool;
        }
        return findToolFromToolkits(name);
    }

    protected Tool<?, ?> findToolFromToolkits(String name) {
        for (Toolkit toolkit : toolkits) {
            Tool<?, ?> tool = toolkit.getTool(name);
            if (tool != null) {
                return tool;
            }
        }
        return null;
    }

    @Override
    public void addTool(Tool<?, ?> tool) {
        Tool<?, ?> existsTool = findToolFromToolkits(tool.getName());
        if (existsTool != null) {
            throw new IllegalArgumentException("Tool with name " + tool.getName() + " already exists in toolkit");
        }
        super.addTool(tool);
    }

    @Override
    public void removeTool(String name) {
        if (tools.containsKey(name)) {
            super.removeTool(name);
        } else {
            throw new IllegalArgumentException("Tool " + name + " cannot be removed.");
        }
    }

    @Override
    public void removeTool(Tool<?, ?> tool) {
        this.removeTool(tool.getName());
    }
}
