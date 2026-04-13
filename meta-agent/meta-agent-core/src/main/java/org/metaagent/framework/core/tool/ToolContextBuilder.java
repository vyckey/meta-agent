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

package org.metaagent.framework.core.tool;

import org.metaagent.framework.common.abort.AbortSignal;
import org.metaagent.framework.core.agent.MetaAgent;
import org.metaagent.framework.core.agent.event.AgentEventBus;
import org.metaagent.framework.core.security.SecurityLevel;
import org.metaagent.framework.core.security.approval.PermissionApprovalManager;
import org.metaagent.framework.core.tool.approval.ToolApprovalRequest;
import org.metaagent.framework.core.tool.config.ToolExecutionConfig;
import org.metaagent.framework.core.tool.listener.ToolExecutionListenerRegistry;
import org.metaagent.framework.core.tool.manager.ToolManager;
import org.metaagent.framework.core.tool.tracker.ToolCallTracker;

/**
 * The {@code ToolContextBuilder} interface defines methods for building a {@link ToolContext}.
 *
 * @author MetaAgent
 */
public interface ToolContextBuilder {
    /**
     * Sets the agent for the tool context.
     *
     * @param agent the agent
     * @return the builder instance
     */
    ToolContextBuilder agent(MetaAgent<?, ?> agent);

    /**
     * Sets the agent event bus for the tool context.
     *
     * @param agentEventBus the agent event bus
     * @return the builder instance
     */
    ToolContextBuilder agentEventBus(AgentEventBus agentEventBus);

    /**
     * Set the {@link ToolManager} for the tool context.
     *
     * @param toolManager the {@link ToolManager} to use
     * @return this builder
     */
    ToolContextBuilder toolManager(ToolManager toolManager);

    /**
     * Set the execution listener registry for the tool context.
     *
     * @param toolListenerRegistry the execution listener registry to use
     * @return this builder
     */
    ToolContextBuilder toolListenerRegistry(ToolExecutionListenerRegistry toolListenerRegistry);

    /**
     * Set the {@link ToolCallTracker} for the tool context.
     *
     * @param toolCallTracker the {@link ToolCallTracker} to use
     * @return this builder
     */
    ToolContextBuilder toolCallTracker(ToolCallTracker toolCallTracker);

    /**
     * Sets the tool execution configuration for the tool context.
     *
     * @param toolExecutionConfig the tool configuration
     * @return the builder instance
     */
    ToolContextBuilder toolExecutionConfig(ToolExecutionConfig toolExecutionConfig);

    /**
     * Sets the security level for the tool context.
     *
     * @param securityLevel the security level
     * @return the builder instance
     */
    ToolContextBuilder securityLevel(SecurityLevel securityLevel);

    /**
     * Sets the permission approval manager for the tool context.
     *
     * @param approvalManager the permission approval manager
     * @return the builder instance
     */
    ToolContextBuilder approvalManager(PermissionApprovalManager<ToolApprovalRequest> approvalManager);

    /**
     * Sets the abort signal for the tool context.
     *
     * @param abortSignal the abort signal
     * @return the builder instance
     */
    ToolContextBuilder abortSignal(AbortSignal abortSignal);

    /**
     * Sets the execution ID for the tool context.
     *
     * @param executionId the execution ID
     * @return the builder instance
     */
    ToolContextBuilder executionId(String executionId);

    /**
     * Builds the tool context.
     *
     * @return the tool context
     */
    ToolContext build();
}
