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


import com.google.common.collect.Maps;

import java.util.Map;

public class DefaultToolkitRegistry implements ToolkitRegistry {
    public static volatile DefaultToolkitRegistry globalRegistry;

    public static DefaultToolkitRegistry global() {
        if (globalRegistry == null) {
            synchronized (DefaultToolkitRegistry.class) {
                if (globalRegistry == null) {
                    DefaultToolkitRegistry registry = new DefaultToolkitRegistry(Maps.newHashMap());
                    registry.addToolkit(SpiToolkit.INSTANCE);
                    globalRegistry = registry;
                }
            }
        }
        return globalRegistry;
    }

    protected final Map<String, Toolkit> toolkits;

    public DefaultToolkitRegistry(Map<String, Toolkit> toolkits) {
        this.toolkits = toolkits;
    }

    public DefaultToolkitRegistry() {
        this(Maps.newConcurrentMap());
    }

    @Override
    public boolean hasToolkit(String toolkitName) {
        return toolkits.containsKey(toolkitName);
    }

    @Override
    public Toolkit getToolkit(String toolkitName) {
        return toolkits.get(toolkitName);
    }

    @Override
    public void addToolkit(Toolkit toolkit) {
        if (!toolkit.equals(toolkits.putIfAbsent(toolkit.getName(), toolkit))) {
            throw new IllegalArgumentException("Toolkit already exists: " + toolkit.getName());
        }
    }

    @Override
    public void removeToolkit(String toolkitName) {
        if (toolkits.remove(toolkitName) == null) {
            throw new IllegalArgumentException("Toolkit does not exist: " + toolkitName);
        }
    }

    @Override
    public String toString() {
        return "ToolkitRegistry";
    }
}
