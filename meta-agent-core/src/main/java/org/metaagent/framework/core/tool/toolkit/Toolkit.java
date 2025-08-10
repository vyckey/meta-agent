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

import org.metaagent.framework.core.tool.Tool;
import org.metaagent.framework.core.tool.container.ToolContainer;

import java.util.Arrays;

/**
 * Toolkit is a collection of tools that can be used together to perform a specific task or set of tasks.
 *
 * @author vyckey
 */
public interface Toolkit extends ToolContainer {
    /**
     * Create a new toolkit from the given tools.
     *
     * @param name        the name of the toolkit
     * @param description the description of the toolkit
     * @param tools       the tools to add to the toolkit
     * @return a new toolkit
     */
    static Toolkit fromTools(String name, String description, Tool<?, ?>... tools) {
        return new DefaultToolkit(name, description, Arrays.asList(tools));
    }

    /**
     * Get the name of the toolkit.
     *
     * @return the name of the toolkit
     */
    String getName();

    /**
     * Get the description of the toolkit.
     *
     * @return the description of the toolkit
     */
    String getDescription();

}
