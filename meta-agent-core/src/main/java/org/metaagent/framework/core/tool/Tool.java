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

package org.metaagent.framework.core.tool;

import org.metaagent.framework.core.tool.converter.ToolConverter;
import org.metaagent.framework.core.tool.definition.ToolDefinition;
import org.metaagent.framework.core.tool.exception.ToolExecutionError;
import org.metaagent.framework.core.tool.exception.ToolExecutionException;

/**
 * Tool represents a generic tool that can be managed by ToolManager.
 * It defines methods to get the tool's name, introduction, and to run the tool with a given input.
 *
 * @param <I> the type of input the tool accepts.
 * @param <O> the type of output the tool produces.
 * @author vyckey
 */
public interface Tool<I, O> {
    /**
     * Gets the tool name.
     *
     * @return the tool name.
     */
    default String getName() {
        return getDefinition().name();
    }

    /**
     * Gets the definition of the tool.
     *
     * @return the definition of the tool.
     */
    ToolDefinition getDefinition();

    /**
     * Gets the input and output converter of the tool.
     *
     * @return the input and output converter of the tool.
     */
    ToolConverter<I, O> getConverter();

    /**
     * Runs the tool with the given input.
     *
     * @param context the context in which the tool is executed
     * @param input   the input to the tool
     * @return the output of the tool
     * @throws ToolExecutionException if the tool execution fails
     */
    O run(ToolContext context, I input) throws ToolExecutionException;

    /**
     * Runs the tool with the given input.
     *
     * @param context the context in which the tool is executed
     * @param input   the input to the tool
     * @return the output of the tool
     * @throws ToolExecutionException if the tool execution fails
     */
    default String call(ToolContext context, String input) throws ToolExecutionException {
        try {
            I toolInput = getConverter().inputConverter().convert(input);
            O toolOutput = run(context, toolInput);
            return getConverter().outputConverter().convert(toolOutput);
        } catch (ToolExecutionException e) {
            throw e;
        } catch (Exception e) {
            throw new ToolExecutionError("Call tool " + getDefinition().name() + " error", e);
        }
    }
}