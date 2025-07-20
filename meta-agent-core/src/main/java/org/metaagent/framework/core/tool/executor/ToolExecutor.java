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

package org.metaagent.framework.core.tool.executor;

import org.metaagent.framework.core.tool.Tool;
import org.metaagent.framework.core.tool.ToolContext;
import org.metaagent.framework.core.tool.ToolExecutionException;

/**
 * ToolExecutor is responsible for executing tools within a given context.
 * It provides methods to execute tools with different input types.
 *
 * @author vyckey
 */
public interface ToolExecutor {
    /**
     * Executes the given tool with the provided context.
     *
     * @param context the context in which the tool is executed
     * @param tool    the tool to be executed
     * @return the result of the tool execution
     * @throws ToolExecutionException if an error occurs during execution
     */
    <I, O> O execute(ToolContext context, Tool<I, O> tool, I input) throws ToolExecutionException;

    /**
     * Executes the given tool with the provided context and input.
     *
     * @param context the context in which the tool is executed
     * @param tool    the tool to be executed
     * @param input   the input for the tool
     * @return the result of the tool execution
     * @throws ToolExecutionException if an error occurs during execution
     */
    <I, O> String execute(ToolContext context, Tool<I, O> tool, String input) throws ToolExecutionException;
}
