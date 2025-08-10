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

import org.apache.commons.lang3.StringUtils;
import org.metaagent.framework.core.tool.Tool;
import org.metaagent.framework.core.tool.container.ImmutableToolContainer;
import org.metaagent.framework.core.tool.container.ImmutableToolContainerImpl;

import java.util.Map;

/**
 * Immutable Toolkit.
 *
 * @author vyckey
 */
public class ImmutableToolkit extends ImmutableToolContainerImpl implements Toolkit, ImmutableToolContainer {
    protected final String name;
    protected final String description;

    public ImmutableToolkit(String name, String description, Map<String, Tool<?, ?>> tools) {
        super(tools);
        if (StringUtils.isEmpty(name)) {
            throw new IllegalArgumentException("Toolkit name cannot be empty");
        }
        this.name = name;
        this.description = description != null ? description : "";
    }

    public ImmutableToolkit(String name, Map<String, Tool<?, ?>> tools) {
        this(name, "", tools);
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
    public String toString() {
        return "Toolkit{name=\"" + name + "\"}";
    }
}
