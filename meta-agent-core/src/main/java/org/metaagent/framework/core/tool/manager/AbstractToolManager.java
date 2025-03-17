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
import lombok.extern.slf4j.Slf4j;
import org.metaagent.framework.core.tool.Tool;

import java.util.List;

/**
 * description is here
 *
 * @author vyckey
 */
@Slf4j
public abstract class AbstractToolManager implements ToolManager {
    protected final List<ToolChangeListener> listeners = Lists.newArrayList();

    @Override
    public boolean hasTool(String name) {
        return getTool(name) != null;
    }

    @Override
    public void removeTool(Tool<?, ?> tool) {
        removeTool(tool.getName());
    }

    @Override
    public void addChangeListener(ToolChangeListener listener) {
        listeners.add(listener);
    }

    @Override
    public void removeChangeListener(ToolChangeListener listener) {
        listeners.remove(listener);
    }

    protected void notifyChangeListeners(Tool<?, ?> tool, ToolChangeListener.EventType eventType) {
        for (ToolChangeListener listener : listeners) {
            try {
                listener.onToolChange(tool, eventType);
            } catch (Exception e) {
                log.error("Failed to notify tool change listener", e);
            }
        }
    }
}
