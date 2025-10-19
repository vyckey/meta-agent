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

package org.metaagent.framework.core.tool.listener;

import com.google.common.collect.Lists;

import java.util.List;
import java.util.Objects;

/**
 * Default implementation of the ToolExecuteListenerRegistry interface.
 * This class provides a registry for tool execution listeners, allowing
 * registration, unregistration, and retrieval of listeners.
 *
 * @author vyckey
 */
public class DefaultToolExecuteListenerRegistry implements ToolExecuteListenerRegistry {
    public static volatile DefaultToolExecuteListenerRegistry instance;
    private final List<ToolExecuteListener> listeners;

    public DefaultToolExecuteListenerRegistry(List<ToolExecuteListener> listeners) {
        this.listeners = Objects.requireNonNull(listeners, "listeners cannot be null");
    }

    public DefaultToolExecuteListenerRegistry() {
        this.listeners = Lists.newArrayList();
    }

    public static DefaultToolExecuteListenerRegistry getInstance() {
        if (instance == null) {
            synchronized (DefaultToolExecuteListenerRegistry.class) {
                if (instance == null) {
                    instance = new DefaultToolExecuteListenerRegistry();
                    instance.registerDefaultListeners();
                }
            }
        }
        return instance;
    }

    public void registerDefaultListeners() {
        registerListener(ToolExecuteDisplayListener.INSTANCE);
    }

    @Override
    public List<ToolExecuteListener> getListeners() {
        return listeners;
    }

    @Override
    public void registerListener(ToolExecuteListener listener) {
        listeners.add(listener);
    }

    @Override
    public void unregisterListener(ToolExecuteListener listener) {
        listeners.remove(listener);
    }

    @Override
    public void removeAllListeners() {
        listeners.clear();
    }
}
