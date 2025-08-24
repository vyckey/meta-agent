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

package org.metaagent.framework.core.agent;

import org.metaagent.framework.core.agent.action.executor.ActionExecutor;
import org.metaagent.framework.core.environment.Environment;
import org.metaagent.framework.core.tool.executor.ToolExecutor;
import org.metaagent.framework.core.tool.listener.ToolExecuteListenerRegistry;
import org.metaagent.framework.core.util.abort.AbortSignal;

import java.util.concurrent.Executor;

/**
 * Builder class for {@link AgentExecutionContext}
 *
 * @author vyckey
 * @see AgentExecutionContext
 */
public interface AgentExecutionContextBuilder {
    /**
     * Sets the environment for the agent execution context
     *
     * @param environment the environment in which the agent operates
     * @return this builder instance for method chaining
     */
    AgentExecutionContextBuilder environment(Environment environment);

    /**
     * Sets the tool executor for the agent execution context
     *
     * @param toolExecutor the executor responsible for executing tools
     * @return this builder instance for method chaining
     */
    AgentExecutionContextBuilder toolExecutor(ToolExecutor toolExecutor);

    /**
     * Sets the tool execute listener registry for the agent execution context
     *
     * @param toolExecuteListenerRegistry the registry for tool execute listeners
     * @return this builder instance for method chaining
     */
    AgentExecutionContextBuilder toolListenerRegistry(ToolExecuteListenerRegistry toolExecuteListenerRegistry);

    /**
     * Sets the action executor for the agent execution context
     *
     * @param actionExecutor the executor responsible for executing actions
     * @return this builder instance for method chaining
     */
    AgentExecutionContextBuilder actionExecutor(ActionExecutor actionExecutor);

    /**
     * Sets the abort signal for the agent execution context
     *
     * @param abortSignal the signal used to abort agent execution
     * @return this builder instance for method chaining
     */
    AgentExecutionContextBuilder abortSignal(AbortSignal abortSignal);

    /**
     * Sets the executor for the agent execution context
     *
     * @param executor the executor service for asynchronous operations
     * @return this builder instance for method chaining
     */
    AgentExecutionContextBuilder executor(Executor executor);

    /**
     * Builds and returns the {@link AgentExecutionContext} instance
     *
     * @return a new {@link AgentExecutionContext} instance with the configured components
     */
    AgentExecutionContext build();
}
