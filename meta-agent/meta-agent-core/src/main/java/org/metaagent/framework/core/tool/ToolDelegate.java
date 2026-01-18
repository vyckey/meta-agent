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

package org.metaagent.framework.core.tool;

import org.metaagent.framework.core.tool.converter.ToolConverter;
import org.metaagent.framework.core.tool.definition.ToolDefinition;
import org.metaagent.framework.core.tool.exception.ToolExecutionException;

import java.util.Objects;

/**
 * ToolDelegate is a tool delegate that wraps another tool and handles exceptions.
 *
 * @param <I> the type of input the tool accepts.
 * @param <O> the type of output the tool produces.
 * @author vyckey
 */
public class ToolDelegate<I, O> implements Tool<I, O> {
    private final Tool<I, O> delegateTool;

    public ToolDelegate(Tool<I, O> delegateTool) {
        this.delegateTool = Objects.requireNonNull(delegateTool, "delegateTool is required");
    }

    protected Tool<I, O> getDelegateTool() {
        return delegateTool;
    }

    @Override
    public String getName() {
        return delegateTool.getName();
    }

    @Override
    public ToolDefinition getDefinition() {
        return delegateTool.getDefinition();
    }

    @Override
    public ToolConverter<I, O> getConverter() {
        return delegateTool.getConverter();
    }

    @Override
    public O run(ToolContext context, I input) throws ToolExecutionException {
        try {
            return delegateTool.run(context, input);
        } catch (ToolExecutionException e) {
            throw e;
        } catch (Exception e) {
            throw new ToolExecutionException("Error executing tool: " + getName(), e);
        }
    }

    @Override
    public String call(ToolContext context, String input) throws ToolExecutionException {
        try {
            return delegateTool.call(context, input);
        } catch (ToolExecutionException e) {
            throw e;
        } catch (Exception e) {
            throw new ToolExecutionException("Error executing tool: " + getName(), e);
        }
    }

    @Override
    public String toString() {
        return "ToolDelegate{tool=" + delegateTool + '}';
    }
}
