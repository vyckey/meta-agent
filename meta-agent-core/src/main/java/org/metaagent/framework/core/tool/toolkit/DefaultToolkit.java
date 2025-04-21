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

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import org.apache.commons.lang3.StringUtils;
import org.metaagent.framework.core.tool.Tool;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * description is here
 *
 * @author vyckey
 */
public class DefaultToolkit implements Toolkit {
    protected final String name;
    protected String description;
    protected final Map<String, Tool<?, ?>> tools;

    protected DefaultToolkit(String name, String description, Map<String, Tool<?, ?>> tools) {
        if (StringUtils.isEmpty(name)) {
            throw new IllegalArgumentException("Toolkit name is required");
        }
        this.name = name;
        this.description = description;
        this.tools = Objects.requireNonNull(tools, "Tools is required");
    }

    public DefaultToolkit(String name, String description) {
        this(name, description, Maps.newHashMap());
    }

    public DefaultToolkit(String name) {
        this(name, "");
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public List<Tool<?, ?>> listTools() {
        return ImmutableList.copyOf(tools.values());
    }

    @Override
    public Tool<?, ?> getTool(String name) {
        return tools.get(name);
    }

    @Override
    public String toString() {
        return "Toolkit{name=\"" + name + "\"}";
    }
}
