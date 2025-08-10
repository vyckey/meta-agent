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

import org.metaagent.framework.core.tool.Tool;
import org.metaagent.framework.core.tool.ToolExecutionException;

/**
 * Listener interface for tool execution events.
 * Provides methods to handle input, output, and exceptions during tool execution.
 *
 * @author vyckey
 */
public interface ToolExecuteListener {
    /**
     * Called when a tool receives input.
     *
     * @param tool  the tool that received the input
     * @param input the input provided to the tool
     * @param <I>   the type of the input
     */
    <I> void onToolInput(Tool<I, ?> tool, I input);

    /**
     * Called when a tool produces output.
     *
     * @param tool   the tool that produced the output
     * @param input  the input provided to the tool
     * @param output the output produced by the tool
     * @param <I>    the type of the input
     * @param <O>    the type of the output
     */
    <I, O> void onToolOutput(Tool<I, O> tool, I input, O output);

    /**
     * Called when a tool execution results in an exception.
     *
     * @param tool      the tool that encountered the exception
     * @param input     the input provided to the tool
     * @param exception the exception thrown during tool execution
     * @param <I>       the type of the input
     */
    <I> void onToolException(Tool<I, ?> tool, I input, ToolExecutionException exception);
}
