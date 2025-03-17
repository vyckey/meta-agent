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
import com.google.common.collect.Sets;
import org.metaagent.framework.core.tool.Tool;

import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Mixed tool manager.
 *
 * @author vyckey
 */
public class MixedToolManager extends AbstractToolManager implements ToolManager {
    protected final Set<ToolManager> toolManagers;
    protected final Map<String, ToolManager> toolManagerMapping;
    protected final Map<ToolManager, ToolChangeListener> listenerMapping;

    protected MixedToolManager(Set<ToolManager> toolManagers, Map<String, ToolManager> toolManagerMapping) {
        this.toolManagers = Objects.requireNonNull(toolManagers, "toolManagers is required");
        this.toolManagerMapping = Objects.requireNonNull(toolManagerMapping, "toolManagerMapping is required");
        this.listenerMapping = Maps.newHashMap();
    }

    public MixedToolManager(ToolManager... toolManagers) {
        this(Sets.newHashSet(), Maps.newHashMap());
        for (ToolManager toolManager : toolManagers) {
            addToolManager(toolManager);
        }
    }

    public void addToolManager(ToolManager toolManager) {
        if (toolManagers.contains(toolManager)) {
            throw new IllegalArgumentException("toolManager is already added");
        }
        toolManagers.add(toolManager);
        for (String toolName : toolManager.getToolNames()) {
            toolManagerMapping.put(toolName, toolManager);
        }
        ChangeListener listener = new ChangeListener(toolManager);
        listenerMapping.put(toolManager, listener);
        toolManager.addChangeListener(listener);
    }

    public void removeToolManager(ToolManager toolManager) {
        if (toolManagers.contains(toolManager)) {
            ToolChangeListener listener = listenerMapping.get(toolManager);
            toolManager.removeChangeListener(listener);

            toolManagerMapping.entrySet().removeIf(entry -> Objects.equals(entry.getValue(), toolManager));
            toolManagers.remove(toolManager);
        }
    }

    @Override
    public Set<String> getToolNames() {
        return toolManagerMapping.keySet();
    }

    @Override
    public boolean hasTool(String name) {
        return toolManagerMapping.containsKey(name);
    }

    @Override
    public Tool<?, ?> getTool(String name) {
        ToolManager toolManager = toolManagerMapping.get(name);
        return toolManager != null ? toolManager.getTool(name) : null;
    }

    @Override
    public void addTool(Tool<?, ?> tool) {
        throw new UnsupportedOperationException("Unsupported operation for mixed tool manager.");
    }

    @Override
    public void removeTool(String name) {
        if (toolManagerMapping.containsKey(name)) {
            ToolManager toolManager = toolManagerMapping.get(name);
            toolManager.removeTool(name);
        }
    }

    public void clear() {
        listenerMapping.forEach(ToolManager::removeChangeListener);
        listenerMapping.clear();
        toolManagerMapping.clear();
        toolManagers.clear();
    }

    class ChangeListener implements ToolChangeListener {
        ToolManager toolManager;

        ChangeListener(ToolManager toolManager) {
            this.toolManager = toolManager;
        }

        @Override
        public void onToolChange(Tool<?, ?> tool, EventType eventType) {
            if (eventType == EventType.ADDED) {
                toolManagerMapping.put(tool.getName(), toolManager);
            } else if (eventType == EventType.REMOVED) {
                toolManagerMapping.remove(tool.getName());
            }
        }
    }
}
