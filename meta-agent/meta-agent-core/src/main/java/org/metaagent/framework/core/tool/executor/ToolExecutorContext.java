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

import org.metaagent.framework.core.tool.ToolContext;
import org.metaagent.framework.core.tool.listener.ToolExecutionListenerRegistry;
import org.metaagent.framework.core.tool.manager.ToolManager;
import org.metaagent.framework.core.tool.tracker.ToolCallTracker;

import java.util.concurrent.Executor;

/**
 * ToolExecutorContext is a context object that is passed to the ToolExecutor.
 *
 * @author vyckey
 */
public interface ToolExecutorContext {
    /**
     * Create a new tool executor context.
     *
     * @return a new tool executor context
     */
    static ToolExecutorContext create() {
        return DefaultToolExecutorContext.builder().build();
    }

    /**
     * Create a new tool executor context builder.
     *
     * @return a executor context builder
     */
    static ToolExecutorContextBuilder builder() {
        return DefaultToolExecutorContext.builder();
    }

    /**
     * Gets the tool executor for executing tools.
     *
     * @return the tool executor
     */
    ToolManager getToolManager();

    /**
     * Gets the tool listener registry for managing tool execution listeners.
     *
     * @return the tool listener registry
     */
    ToolExecutionListenerRegistry getToolListenerRegistry();

    /**
     * Gets the tool executor for executing tools.
     *
     * @return the tool executor
     */
    ToolCallTracker getToolCallTracker();

    /**
     * Gets the tool context.
     *
     * @return the tool context
     */
    ToolContext getToolContext();

    /**
     * Gets the executor.
     *
     * @return the executor
     */
    Executor getExecutor();

}
