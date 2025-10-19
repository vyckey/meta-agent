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
import org.metaagent.framework.core.tool.listener.ToolExecuteListenerRegistry;
import org.metaagent.framework.core.tool.manager.ToolManager;
import org.metaagent.framework.core.tool.tracker.ToolCallTracker;

import java.util.concurrent.Executor;

/**
 * Builder for {@link ToolExecutorContext}.
 */
public interface ToolExecutorContextBuilder {
    /**
     * Set the {@link ToolManager} to use.
     *
     * @param toolManager the {@link ToolManager} to use
     * @return this builder
     */
    ToolExecutorContextBuilder toolManager(ToolManager toolManager);

    /**
     * Set the execution listener registry to use.
     *
     * @param toolListenerRegistry the execution listener registry to use
     * @return this builder
     */
    ToolExecutorContextBuilder toolListenerRegistry(ToolExecuteListenerRegistry toolListenerRegistry);

    /**
     * Set the {@link ToolCallTracker} to use.
     *
     * @param toolCallTracker the {@link ToolCallTracker} to use
     * @return this builder
     */
    ToolExecutorContextBuilder toolCallTracker(ToolCallTracker toolCallTracker);

    /**
     * Set the {@link ToolContext} to use.
     *
     * @param toolContext the {@link ToolContext} to use
     * @return this builder
     */
    ToolExecutorContextBuilder toolContext(ToolContext toolContext);

    /**
     * Set the {@link Executor} to use.
     *
     * @param executor the {@link Executor} to use
     * @return this builder
     */
    ToolExecutorContextBuilder executor(Executor executor);

    /**
     * Build the {@link ToolExecutorContext}.
     *
     * @return the {@link ToolExecutorContext}
     */
    ToolExecutorContext build();
}
