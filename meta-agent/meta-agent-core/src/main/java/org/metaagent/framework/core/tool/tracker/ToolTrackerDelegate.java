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

package org.metaagent.framework.core.tool.tracker;

import org.metaagent.framework.core.tool.Tool;
import org.metaagent.framework.core.tool.ToolContext;
import org.metaagent.framework.core.tool.converter.ToolConverter;
import org.metaagent.framework.core.tool.definition.ToolDefinition;
import org.metaagent.framework.core.tool.exception.ToolExecutionException;

import java.util.Objects;

/**
 * description is here
 *
 * @author vyckey
 */
public class ToolTrackerDelegate<I, O> implements Tool<I, O> {
    private final ToolCallTracker toolCallTracker;
    private final Tool<I, O> tool;

    public ToolTrackerDelegate(ToolCallTracker toolCallTracker, Tool<I, O> tool) {
        this.toolCallTracker = Objects.requireNonNull(toolCallTracker, "toolCallTracker is required");
        this.tool = Objects.requireNonNull(tool, "tool is required");
    }

    @Override
    public ToolDefinition getDefinition() {
        return tool.getDefinition();
    }

    @Override
    public ToolConverter<I, O> getConverter() {
        return tool.getConverter();
    }

    @Override
    public O run(ToolContext context, I input) throws ToolExecutionException {
        return tool.run(context, input);
    }

    @Override
    public String call(ToolContext context, String input) throws ToolExecutionException {
        String toolName = getDefinition().name();
        DefaultToolCallRecord.Builder builder = DefaultToolCallRecord.builder().id().toolName(toolName)
                .toolInput(input);
        try {
            I toolInput = getConverter().inputConverter().convert(input);
            O toolOutput = run(context, toolInput);
            String output = getConverter().outputConverter().convert(toolOutput);
            builder.toolOutput(output);
            return output;
        } catch (ToolExecutionException ex) {
            builder.exception(ex);
            throw ex;
        } catch (Exception e) {
            ToolExecutionException ex = new ToolExecutionException("Call tool " + toolName + " fail", e);
            builder.exception(ex);
            throw ex;
        } finally {
            ToolCallRecord callRecord = builder.build();
            toolCallTracker.track(callRecord);
        }
    }

    @Override
    public String toString() {
        return tool.toString();
    }
}
