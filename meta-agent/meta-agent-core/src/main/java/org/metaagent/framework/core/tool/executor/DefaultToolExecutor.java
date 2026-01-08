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

import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.metaagent.framework.core.tool.Tool;
import org.metaagent.framework.core.tool.ToolContext;
import org.metaagent.framework.core.tool.exception.ToolExecutionError;
import org.metaagent.framework.core.tool.exception.ToolExecutionException;
import org.metaagent.framework.core.tool.listener.ToolExecuteListener;
import org.metaagent.framework.core.tool.listener.ToolExecuteListenerRegistry;
import org.metaagent.framework.core.tool.manager.ToolManager;
import org.metaagent.framework.core.tool.tools.WrapExceptionTool;
import org.metaagent.framework.core.tool.tracker.ToolTrackerDelegate;

import java.util.List;
import java.util.function.Consumer;

/**
 * Default implementation of {@link ToolExecutor}.
 *
 * @author vyckey
 */
@Slf4j
public class DefaultToolExecutor implements ToolExecutor {
    public static final DefaultToolExecutor INSTANCE = new DefaultToolExecutor();

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
    public <I, O> O execute(ToolExecutorContext executorContext, Tool<I, O> tool, I input) throws ToolExecutionException {
        ToolExecuteListenerRegistry listenerRegistry = executorContext.getToolListenerRegistry();
        try {
            notifyListeners(listenerRegistry, listener -> listener.onToolInput(tool, input));

            O output = tool.run(executorContext.getToolContext(), input);

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
    public <I, O> String execute(ToolExecutorContext executorContext, Tool<I, O> tool, String input) throws ToolExecutionException {
        ToolExecuteListenerRegistry listenerRegistry = executorContext.getToolListenerRegistry();
        ToolTrackerDelegate<I, O> trackerDelegate = new ToolTrackerDelegate<>(executorContext.getToolCallTracker(), tool) {
            @Override
            public O run(ToolContext context, I input) throws ToolExecutionException {
                return execute(executorContext, tool, input);
            }

            @Override
            public String call(ToolContext context, String input) throws ToolExecutionException {
                notifyListeners(listenerRegistry, listener -> listener.onToolInputRequest(tool, input));

                // perform tool call
                String response = super.call(context, input);

                notifyListeners(listenerRegistry, listener -> listener.onToolResponse(tool, input, response));
                return response;
            }
        };
        Tool<I, O> delegate = new WrapExceptionTool<>(trackerDelegate);
        return delegate.call(executorContext.getToolContext(), input);
    }

    @Override
    public BatchToolOutputs execute(ToolExecutorContext executorContext, BatchToolInputs toolInputs) throws ToolExecutionException {
        ToolManager toolManager = executorContext.getToolManager();

        List<BatchToolOutputs.ToolOutput> outputs = Lists.newArrayList();
        for (BatchToolInputs.ToolInput toolInput : toolInputs.inputs()) {
            Tool<?, ?> tool = toolManager.getTool(toolInput.toolName());
            String output = execute(executorContext, tool, toolInput.input());
            outputs.add(new BatchToolOutputs.ToolOutput(toolInput.toolName(), output));
        }
        return new BatchToolOutputs(outputs);
    }
}
