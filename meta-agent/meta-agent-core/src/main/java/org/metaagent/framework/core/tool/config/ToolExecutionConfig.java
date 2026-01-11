/*
 * MIT License
 *
 * Copyright (c) 2026 MetaAgent
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

package org.metaagent.framework.core.tool.config;

import java.util.List;
import java.util.Set;

/**
 * Tool execution configuration interface.
 *
 * @author vyckey
 */
public interface ToolExecutionConfig {

    /**
     * Return the set of allowed tools.
     *
     * @return a set of allowed command strings
     */
    Set<ToolPattern> allowedTools();

    /**
     * Return the set of allowed tools for a specific tool name.
     *
     * @param toolName the name of the tool
     * @return a list of allowed command strings for the specified tool
     */
    List<ToolPattern> allowedTools(String toolName);

    /**
     * Return the set of disallowed tools.
     *
     * @return a set of disallowed command strings
     */
    Set<ToolPattern> disallowedTools();

    /**
     * Return the set of disallowed tools for a specific tool name.
     *
     * @param toolName the name of the tool
     * @return a list of disallowed command strings for the specified tool
     */
    List<ToolPattern> disallowedTools(String toolName);

}
