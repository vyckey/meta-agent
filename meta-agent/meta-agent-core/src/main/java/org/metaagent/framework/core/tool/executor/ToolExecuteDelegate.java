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

package org.metaagent.framework.core.tool.executor;

import lombok.extern.slf4j.Slf4j;
import org.metaagent.framework.core.tool.Tool;
import org.metaagent.framework.core.tool.ToolContext;
import org.metaagent.framework.core.tool.ToolDelegate;
import org.metaagent.framework.core.tool.exception.ToolExecutionError;
import org.metaagent.framework.core.tool.exception.ToolExecutionException;
import org.metaagent.framework.core.tool.listener.ToolExecuteListener;
import org.metaagent.framework.core.tool.listener.ToolExecuteListenerRegistry;
import org.metaagent.framework.core.tool.tracker.DefaultToolCallRecord;
import org.metaagent.framework.core.tool.tracker.ToolCallRecord;

import java.util.Objects;
import java.util.function.Consumer;

/**
 * Tool execute delegate to handle tool execution with listeners and tracking.
 *
 * @param <I> Input type
 * @param <O> Output type
 * @author vyckey
 */
@Slf4j
public class ToolExecuteDelegate<I, O> extends ToolDelegate<I, O> {
    protected final ToolExecutorContext executorContext;

    public ToolExecuteDelegate(Tool<I, O> delegateTool, ToolExecutorContext executorContext) {
        super(delegateTool);
        this.executorContext = Objects.requireNonNull(executorContext, "executorContext is required");
    }

    protected void notifyListeners(ToolExecuteListenerRegistry listenerRegistry,
                                   Consumer<ToolExecuteListener> consumer) {
        for (ToolExecuteListener listener : listenerRegistry.getListeners()) {
            try {
                consumer.accept(listener);
            } catch (Exception e) {
                log.error("Fail to invoke tool listener", e);
            }
        }
    }

    @Override
    public O run(ToolContext context, I input) throws ToolExecutionException {
        Tool<I, O> tool = getDelegateTool();
        ToolExecuteListenerRegistry listenerRegistry = executorContext.getToolListenerRegistry();
        try {
            notifyListeners(listenerRegistry, listener -> listener.onToolInput(tool, input));

            O output = getDelegateTool().run(executorContext.getToolContext(), input);

            notifyListeners(listenerRegistry, listener -> listener.onToolOutput(tool, input, output));
            return output;
        } catch (ToolExecutionException e) {
            notifyListeners(listenerRegistry, listener -> listener.onToolException(tool, input, e));
            throw e;
        } catch (Exception e) {
            ToolExecutionError ex = new ToolExecutionError("Call tool " + tool.getName() + " error", e);
            notifyListeners(listenerRegistry, listener -> listener.onToolException(tool, input, ex));
            throw ex;
        }
    }

    @Override
    public String call(ToolContext context, String input) throws ToolExecutionException {
        String toolName = getDefinition().name();
        Tool<I, O> tool = getDelegateTool();
        ToolExecuteListenerRegistry listenerRegistry = executorContext.getToolListenerRegistry();

        DefaultToolCallRecord.Builder builder = DefaultToolCallRecord.builder()
                .id(context.getExecutionId())
                .toolName(toolName)
                .toolInput(input);
        I toolInput = null;
        try {
            notifyListeners(listenerRegistry, listener -> listener.onToolInputRequest(tool, input));

            toolInput = getConverter().inputConverter().convert(input);
            O toolOutput = run(context, toolInput);
            String output = getConverter().outputConverter().convert(toolOutput);
            builder.toolOutput(output);

            notifyListeners(listenerRegistry, listener -> listener.onToolResponse(tool, input, output));
            return output;
        } catch (ToolExecutionException ex) {
            final I finalInput = toolInput;
            notifyListeners(listenerRegistry, listener -> listener.onToolException(tool, finalInput, ex));
            builder.exception(ex);
            throw ex;
        } catch (Exception e) {
            final I finalInput = toolInput;
            ToolExecutionException ex = new ToolExecutionError("Call tool " + toolName + " fail", e);
            notifyListeners(listenerRegistry, listener -> listener.onToolException(tool, finalInput, ex));
            builder.exception(ex);
            throw ex;
        } finally {
            ToolCallRecord callRecord = builder.build();
            executorContext.getToolCallTracker().track(callRecord);
        }
    }
}
