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

import java.util.Collections;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.Set;

/**
 * description is here
 *
 * @author vyckey
 */
public class DefaultToolManager extends AbstractToolManager implements ToolManager {
    private static volatile ToolManager instance;
    protected final Map<String, Tool<?, ?>> tools = Maps.newConcurrentMap();

    protected DefaultToolManager() {
    }

    public static ToolManager getInstance() {
        if (instance == null) {
            synchronized (DefaultToolManager.class) {
                if (instance == null) {
                    DefaultToolManager manager = new DefaultToolManager();
                    ServiceLoader.load(Tool.class).forEach(manager::addTool);
                    instance = manager;
                }
            }
        }
        return instance;
    }

    @Override
    public Set<String> getToolNames() {
        return Collections.unmodifiableSet(tools.keySet());
    }

    @Override
    public boolean hasTool(String name) {
        return tools.containsKey(name);
    }

    @Override
    public Tool<?, ?> getTool(String name) {
        return tools.get(name);
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
