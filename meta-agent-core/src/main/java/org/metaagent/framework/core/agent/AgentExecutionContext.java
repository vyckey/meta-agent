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
import org.metaagent.framework.core.tool.manager.ToolManager;

import java.util.concurrent.Executor;

/**
 * Agent execution context which won't contain agent state.
 *
 * @author vyckey
 */
public interface AgentExecutionContext {
    /**
     * Create a new agent execution context with default settings.
     *
     * @return a new AgentExecutionContext instance
     */
    static AgentExecutionContext create() {
        return builder().build();
    }

    /**
     * Create a new agent execution context builder.
     *
     * @return a DefaultAgentExecutionContext builder instance
     */
    static DefaultAgentExecutionContext.Builder builder() {
        return DefaultAgentExecutionContext.builder();
    }

    /**
     * Get the environment associated with this agent execution context.
     *
     * @return the environment
     */
    Environment getEnvironment();

    /**
     * Get the tool manager associated with this agent execution context.
     *
     * @return the tool manager
     */
    ToolManager getToolManager();

    /**
     * Get the action executor associated with this agent execution context.
     *
     * @return the action executor
     */
    ActionExecutor getActionExecutor();

    /**
     * Get the executor associated with this agent execution context.
     *
     * @return the executor
     */
    Executor getExecutor();

}
