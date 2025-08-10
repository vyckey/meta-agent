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

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.apache.commons.lang3.NotImplementedException;
import org.metaagent.framework.core.tool.Tool;
import org.metaagent.framework.core.tool.container.ToolContainerImpl;
import org.metaagent.framework.core.tool.toolkit.Toolkit;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Default implementation of {@link ToolManager}.
 *
 * @author vyckey
 */
public class DefaultToolManager extends ToolContainerImpl implements ToolManager {
    protected final Map<String, ToolkitWrapper> toolkitMap;

    public DefaultToolManager(Map<String, Tool<?, ?>> tools) {
        super(tools);
        this.toolkitMap = Maps.newHashMap();
    }

    public DefaultToolManager() {
        this(Maps.newHashMap());
    }

    @Override
    public Set<String> getToolNames() {
        Set<String> toolNames = Sets.newHashSet(tools.keySet());
        for (ToolkitWrapper toolkitWrapper : toolkitMap.values()) {
            toolNames.addAll(toolkitWrapper.availableToolNames());
        }
        return toolNames;
    }

    @Override
    public List<Tool<?, ?>> listTools() {
        List<Tool<?, ?>> toolList = Lists.newArrayList(tools.values());
        for (ToolkitWrapper toolkitWrapper : toolkitMap.values()) {
            toolList.addAll(toolkitWrapper.toolkit.listTools());
        }
        return toolList.stream().filter(Objects::nonNull).toList();
    }

    @Override
    public boolean hasTool(String name) {
        if (tools.containsKey(name)) {
            return true;
        }
        for (ToolkitWrapper toolkitWrapper : toolkitMap.values()) {
            if (toolkitWrapper.availableToolNames().contains(name)) {
                return true;
            }
        }
        return false;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <I, O> Tool<I, O> getTool(String name) {
        if (tools.containsKey(name)) {
            return (Tool<I, O>) tools.get(name);
        }
        for (ToolkitWrapper wrapper : toolkitMap.values()) {
            if (wrapper.availableToolNames().contains(name)) {
                return wrapper.getTool(name);
            }
        }
        return null;
    }

    @Override
    public void addTool(Tool<?, ?> tool) {
        throw new UnsupportedOperationException("Unsupported operation for mixed tool manager.");
    }

    @Override
    public void removeTool(String name) {
        tools.remove(name);
    }

    @Override
    public ToolManager addToolkit(Toolkit toolkit) {
        return addToolkit(toolkit.getName(), toolkit);
    }

    @Override
    public ToolManager addToolkit(Toolkit toolkit, String... toolNames) {
        String toolkitName = Objects.requireNonNull(toolkit).getName();
        ToolkitWrapper wrapper = new ToolkitWrapper(toolkit, Lists.newArrayList(toolNames));
        if (toolkitMap.putIfAbsent(toolkitName, wrapper) != null) {
            throw new IllegalArgumentException("Toolkit already exists: " + toolkitName);
        }
        return this;
    }

    @Override
    public ToolManager addToolkit(String namespace, Toolkit toolkit, String... toolNames) {
        String toolkitName = Objects.requireNonNull(toolkit).getName();
        ToolkitWrapper wrapper = new ToolkitWrapper(toolkit, Lists.newArrayList(toolNames), namespace);
        if (toolkitMap.putIfAbsent(toolkitName, wrapper) != null) {
            throw new IllegalArgumentException("Toolkit already exists: " + toolkitName);
        }
        return this;
    }

    @Override
    public void removeToolkit(String toolkitName) {
        this.toolkitMap.remove(toolkitName);
    }

    record ToolkitWrapper(Toolkit toolkit, List<String> toolNames, String namespace) {
        ToolkitWrapper(Toolkit toolkit, List<String> toolNames) {
            this(toolkit, toolNames, null);
        }

        ToolkitWrapper(Toolkit toolkit) {
            this(toolkit, null);
        }

        String getToolName(String namespace, String toolName) {
            return namespace != null ? namespace + "." + toolName : toolName;
        }

        Collection<String> availableToolNames() {
            Collection<String> availableToolNames = toolNames.isEmpty() ? toolkit.getToolNames() : toolNames;
            if (namespace == null) {
                return availableToolNames;
            }
            return availableToolNames.stream().map(name -> getToolName(namespace, name)).toList();
        }

        <I, O> Tool<I, O> getTool(String name) {
            Tool<I, O> tool = toolkit.getTool(name);
            if (tool != null && namespace != null) {
                // create a tool proxy and modify getName
                throw new NotImplementedException();
            }
            return tool;
        }
    }

}
