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

import com.google.common.collect.Maps;
import org.metaagent.framework.core.tool.Tool;
import org.metaagent.framework.core.tool.toolkit.Toolkit;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.ServiceLoader;
import java.util.Set;

/**
 * Default {@link ToolManager} implementation.
 *
 * @author vyckey
 */
public class DefaultToolManager extends AbstractToolManager implements ToolManager {
    protected final Map<String, Tool<?, ?>> tools = Maps.newConcurrentMap();

    public DefaultToolManager() {
    }

    public DefaultToolManager(List<Tool<?, ?>> tools) {
        Objects.requireNonNull(tools, "tools is required");
        tools.forEach(tool -> this.tools.put(tool.getName(), tool));
    }

    public DefaultToolManager fromTools(Tool<?, ?>... tools) {
        return new DefaultToolManager(List.of(tools));
    }

    public static DefaultToolManager fromToolkits(Toolkit... toolkits) {
        List<Tool<?, ?>> tools = Arrays.stream(toolkits).map(Toolkit::listTools)
                .flatMap(List::stream).toList();
        return new DefaultToolManager(tools);
    }

    public void addToolkit(Toolkit toolkit) {
        for (Tool<?, ?> tool : toolkit.listTools()) {
            addTool(tool);
        }
    }

    public void removeToolkit(Toolkit toolkit) {
        for (Tool<?, ?> tool : toolkit.listTools()) {
            removeTool(tool.getName());
        }
    }

    public void loadSpiTools() {
        ServiceLoader.load(Tool.class).forEach(this::addTool);
    }

    @Override
    public Set<String> getToolNames() {
        return Collections.unmodifiableSet(tools.keySet());
    }

    @Override
    public boolean hasTool(String name) {
        return tools.containsKey(name);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <I, O> Tool<I, O> getTool(String name) {
        return (Tool<I, O>) tools.get(name);
    }

    @Override
    public void addTool(Tool<?, ?> tool) {
        Tool<?, ?> oldTool = this.tools.put(tool.getName(), tool);
        if (oldTool != null) {
            notifyChangeListeners(tool, ToolChangeListener.EventType.UPDATED);
        } else {
            notifyChangeListeners(tool, ToolChangeListener.EventType.ADDED);
        }
    }

    @Override
    public void removeTool(String name) {
        Tool<?, ?> tool = this.tools.remove(name);
        if (tool != null) {
            notifyChangeListeners(tool, ToolChangeListener.EventType.REMOVED);
        }
    }
}
