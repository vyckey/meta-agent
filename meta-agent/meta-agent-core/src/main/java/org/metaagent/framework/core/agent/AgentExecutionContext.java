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

import org.metaagent.framework.common.abort.AbortSignal;
import org.metaagent.framework.core.agent.observability.AgentListenerRegistry;
import org.metaagent.framework.core.config.WorkspaceConfig;
import org.metaagent.framework.core.security.SecurityLevel;
import org.metaagent.framework.core.security.approval.PermissionApprovalManager;
import org.metaagent.framework.core.skill.manager.SkillManager;
import org.metaagent.framework.core.tool.ToolContext;
import org.metaagent.framework.core.tool.approval.ToolApprovalRequest;
import org.metaagent.framework.core.tool.executor.ToolExecutor;
import org.metaagent.framework.core.tool.listener.ToolExecutionListenerRegistry;
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
    static AgentExecutionContextBuilder builder() {
        return DefaultAgentExecutionContext.builder();
    }

    /**
     * Get the agent listener registry associated with this agent execution context.
     *
     * @return the agent listener registry
     */
    <I, O> AgentListenerRegistry<I, O> getAgentListenerRegistry();

    /**
     * Get the agent security level.
     *
     * @return the agent security level
     */
    SecurityLevel getSecurityLevel();

    /**
     * Gets the agent tool manager.
     *
     * @return the tool manager.
     */
    ToolManager getToolManager();

    /**
     * Get the tool executor associated with this agent execution context.
     *
     * @return the tool executor
     */
    ToolExecutor getToolExecutor();

    /**
     * Gets the tool listener registry for managing tool execution listeners.
     *
     * @return the tool listener registry
     */
    ToolExecutionListenerRegistry getToolListenerRegistry();

    /**
     * Gets the permission approval manager for managing tool permissions.
     *
     * @return the permission approval manager
     */
    PermissionApprovalManager<ToolApprovalRequest> getToolApprovalManager();

    /**
     * Gets skill manager.
     *
     * @return the skill manager.
     */
    SkillManager getSkillManager();

    /**
     * Gets the abort signal for managing agent execution aborts.
     *
     * @return the abort signal
     */
    AbortSignal getAbortSignal();

    /**
     * Get the executor associated with this agent execution context.
     *
     * @return the executor
     */
    Executor getExecutor();

    /**
     * Get the workspace configuration associated with this agent execution context.
     *
     * @return the workspace configuration
     */
    WorkspaceConfig getWorkspaceConfig();

    /**
     * Create a new builder instance based on this agent execution context.
     *
     * @return a new builder instance
     */
    AgentExecutionContextBuilder toBuilder();

}
