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
import org.apache.commons.lang3.StringUtils;
import org.metaagent.framework.core.tool.Tool;
import org.metaagent.framework.core.tool.container.ToolContainerImpl;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Default implementation of a toolkit.
 *
 * @author vyckey
 */
public class DefaultToolkit extends ToolContainerImpl implements Toolkit {
    protected final String name;
    protected final String description;

    public DefaultToolkit(String name, String description, Map<String, Tool<?, ?>> tools) {
        super(tools);
        if (StringUtils.isEmpty(name)) {
            throw new IllegalArgumentException("Toolkit name is required");
        }
        this.name = name;
        this.description = description;
    }

    public DefaultToolkit(String name, String description, List<Tool<?, ?>> tools) {
        this(name, description, tools.stream().collect(Collectors.toMap(Tool::getName, Function.identity())));
    }

    public DefaultToolkit(String name, String description) {
        this(name, description, Maps.newHashMap());
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getDescription() {
        return description;
    }

    public void loadTools() {
    }

    @Override
    public String toString() {
        return "Toolkit{name=\"" + name + "\"}";
    }
}
