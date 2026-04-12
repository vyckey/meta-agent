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

package org.metaagent.framework.core.tool;

import org.metaagent.framework.common.abort.AbortSignal;
import org.metaagent.framework.core.agent.MetaAgent;
import org.metaagent.framework.core.agent.event.AgentEventBus;
import org.metaagent.framework.core.agent.input.AgentInput;
import org.metaagent.framework.core.agent.output.AgentOutput;
import org.metaagent.framework.core.security.SecurityLevel;
import org.metaagent.framework.core.security.approval.PermissionApproval;
import org.metaagent.framework.core.security.approval.PermissionApprovalManager;
import org.metaagent.framework.core.tool.approval.ToolApprovalRequest;
import org.metaagent.framework.core.tool.config.ToolExecutionConfig;
import org.metaagent.framework.core.tool.listener.ToolExecutionListenerRegistry;
import org.metaagent.framework.core.tool.manager.ToolManager;
import org.metaagent.framework.core.tool.tracker.ToolCallTracker;

import java.nio.file.Path;

/**
 * ToolContext provides the context for tool execution,
 *
 * @author vyckey
 */
public interface ToolContext {
    /**
     * Gets the tool executor for executing tools.
     *
     * @return the tool executor
     */
    static ToolContext create() {
        return DefaultToolContext.builder().build();
    }

    /**
     * Creates a new builder for constructing a ToolContext.
     *
     * @return a new ToolContext builder
     */
    static ToolContextBuilder builder() {
        return DefaultToolContext.builder();
    }

    /**
     * Gets the agent for the tool context.
     *
     * @return the agent
     */
    <I extends AgentInput, O extends AgentOutput>
    MetaAgent<I, O> getAgent();

    /**
     * Gets the agent event bus for managing agent events.
     *
     * @return the agent event bus
     */
    AgentEventBus getAgentEventBus();

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
     * Gets the tool execution configuration.
     *
     * @return the tool configuration
     */
    ToolExecutionConfig getToolExecutionConfig();

    /**
     * Gets the working directory for executing tools.
     *
     * @return the working directory
     */
    default Path getWorkingDirectory() {
        return getToolExecutionConfig().workspaceConfig().currentWorkingDirectory();
    }

    /**
     * Gets the security level for the tool execution.
     *
     * @return the security level
     */
    SecurityLevel getSecurityLevel();

    /**
     * Gets the permission approval manager for managing tool permissions.
     *
     * @return the permission approval manager
     */
    PermissionApprovalManager<ToolApprovalRequest> getApprovalManager();

    /**
     * Initiates a tool permission approval request and wait for approval, it will block until the request is approved or rejected.
     *
     * @param approvalRequest the permission approval request
     * @return the permission approval
     */
    PermissionApproval requestApproval(ToolApprovalRequest approvalRequest);

    /**
     * Gets the abort signal for managing tool execution aborts.
     *
     * @return the abort signal
     */
    AbortSignal getAbortSignal();

    /**
     * Gets the execution id for the current tool call.
     *
     * @return the execution id
     */
    String getExecutionId();

    /**
     * Creates a new builder based on the current ToolContext instance.
     *
     * @return a new ToolContext builder
     */
    ToolContextBuilder toBuilder();

}
