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

import lombok.extern.slf4j.Slf4j;
import org.metaagent.framework.core.tool.Tool;
import org.metaagent.framework.core.tool.ToolContext;
import org.metaagent.framework.core.tool.ToolExecutionException;
import org.metaagent.framework.core.tool.converter.ToolConverter;
import org.metaagent.framework.core.tool.listener.ToolExecuteListener;
import org.metaagent.framework.core.tool.tracker.ToolTrackerDelegate;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Default implementation of {@link ToolExecutor}.
 *
 * @author vyckey
 */
@Slf4j
public class DefaultToolExecutor implements ToolExecutor {
    private static final DefaultToolExecutor INSTANCE = new DefaultToolExecutor();

    private final List<ToolExecuteListener> executeListeners = new ArrayList<>();

    public static DefaultToolExecutor getInstance() {
        return INSTANCE;
    }

    public void registerExecuteListener(ToolExecuteListener executeListener) {
        executeListeners.add(executeListener);
    }

    public void unregisterExecuteListener(ToolExecuteListener executeListener) {
        executeListeners.remove(executeListener);
    }

    protected void notifyListeners(Consumer<ToolExecuteListener> consumer) {
        for (ToolExecuteListener listener : executeListeners) {
            try {
                consumer.accept(listener);
            } catch (Exception e) {
                log.error("Fail to invoke tool listener", e);
            }
        }
    }

    @Override
    public <I, O> O execute(ToolContext context, Tool<I, O> tool, I input) throws ToolExecutionException {
        try {
            notifyListeners(listener -> listener.onToolInput(tool, input));

            ToolTrackerDelegate<I, O> delegate = new ToolTrackerDelegate<>(context.getToolCallTracker(), tool);
            O output = delegate.run(context, input);

            notifyListeners(listener -> listener.onToolInput(tool, input));
            return output;
        } catch (ToolExecutionException e) {
            notifyListeners(listener -> listener.onToolException(tool, input, e));
            throw e;
        } catch (Exception e) {
            ToolExecutionException ex = new ToolExecutionException("Call tool " + tool.getName() + " fail", e);
            notifyListeners(listener -> listener.onToolException(tool, input, ex));
            throw ex;
        }
    }

    @Override
    public <I, O> String execute(ToolContext context, Tool<I, O> tool, String input) throws ToolExecutionException {
        ToolConverter<I, O> converter = tool.getConverter();
        try {
            I toolInput = converter.inputConverter().convert(input);
            O toolOutput = execute(context, tool, toolInput);
            return converter.outputConverter().convert(toolOutput);
        } catch (ToolExecutionException e) {
            throw e;
        } catch (Exception e) {
            throw new ToolExecutionException("Call tool " + tool.getName() + " fail", e);
        }
    }
}
