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

import org.metaagent.framework.common.abort.AbortController;
import org.metaagent.framework.common.abort.AbortSignal;
import org.metaagent.framework.core.agent.MetaAgent;
import org.metaagent.framework.core.agent.event.AgentEventBus;
import org.metaagent.framework.core.agent.input.AgentInput;
import org.metaagent.framework.core.agent.output.AgentOutput;
import org.metaagent.framework.core.security.SecurityLevel;
import org.metaagent.framework.core.security.approval.AsyncEventPermissionApprovalManager;
import org.metaagent.framework.core.security.approval.PermissionApproval;
import org.metaagent.framework.core.security.approval.PermissionApprovalManager;
import org.metaagent.framework.core.security.approval.PermissionApprover;
import org.metaagent.framework.core.tool.approval.ToolApprovalRequest;
import org.metaagent.framework.core.tool.config.DefaultToolExecutionConfig;
import org.metaagent.framework.core.tool.config.ToolExecutionConfig;
import org.metaagent.framework.core.tool.exception.ToolRejectException;
import org.metaagent.framework.core.tool.listener.ToolExecutionListenerRegistry;
import org.metaagent.framework.core.tool.manager.ToolManager;
import org.metaagent.framework.core.tool.tracker.ToolCallTracker;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;

/**
 * Default implementation of {@link ToolContext} interface.
 *
 * @author vyckey
 */
public record DefaultToolContext(
        MetaAgent<?, ?> agent,
        AgentEventBus agentEventBus,
        ToolManager toolManager,
        ToolExecutionListenerRegistry toolListenerRegistry,
        ToolCallTracker toolCallTracker,
        ToolExecutionConfig toolExecutionConfig,
        SecurityLevel securityLevel,
        PermissionApprovalManager<ToolApprovalRequest> approvalManager,
        AbortSignal abortSignal,
        String executionId
) implements ToolContext {

    public DefaultToolContext {
        Objects.requireNonNull(agent, "agent is required");
        Objects.requireNonNull(agentEventBus, "agentEventBus is required");
        Objects.requireNonNull(toolManager, "toolManager is required");
        Objects.requireNonNull(toolListenerRegistry, "toolListenerRegistry is required");
        Objects.requireNonNull(toolCallTracker, "toolCallTracker is required");
        Objects.requireNonNull(toolExecutionConfig, "toolExecutionConfig is required");
        Objects.requireNonNull(securityLevel, "securityLevel is required");
        Objects.requireNonNull(approvalManager, "approvalManager is required");
        Objects.requireNonNull(abortSignal, "abortSignal is required");
    }

    public static Builder builder() {
        return new Builder();
    }

    public <I extends AgentInput, O extends AgentOutput> MetaAgent<I, O> getAgent() {
        //noinspection unchecked
        return (MetaAgent<I, O>) agent;
    }

    @Override
    public AgentEventBus getAgentEventBus() {
        return agentEventBus;
    }

    @Override
    public ToolManager getToolManager() {
        return toolManager;
    }

    @Override
    public ToolExecutionListenerRegistry getToolListenerRegistry() {
        return toolListenerRegistry;
    }

    @Override
    public ToolCallTracker getToolCallTracker() {
        return toolCallTracker;
    }

    @Override
    public ToolExecutionConfig getToolExecutionConfig() {
        return toolExecutionConfig;
    }

    @Override
    public SecurityLevel getSecurityLevel() {
        return securityLevel;
    }

    @Override
    public PermissionApprovalManager<ToolApprovalRequest> getApprovalManager() {
        return approvalManager;
    }

    public AbortSignal getAbortSignal() {
        return abortSignal;
    }

    @Override
    public String getExecutionId() {
        return executionId;
    }

    @Override
    public PermissionApproval requestApproval(ToolApprovalRequest approvalRequest) throws ToolRejectException {
        try {
            CompletableFuture<PermissionApproval> approvalFuture = getApprovalManager().initiateApproval(approvalRequest);
            return approvalFuture.get();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ToolRejectException(approvalRequest.toolName(), "approval process was interrupted");
        } catch (ExecutionException e) {
            Throwable reason = e.getCause();
            throw new ToolRejectException(approvalRequest.toolName(), "approval process occurred exception:" + reason.getMessage(), reason);
        }
    }

    @Override
    public ToolContextBuilder toBuilder() {
        return new Builder(this);
    }

    public static class Builder implements ToolContextBuilder {
        private MetaAgent<?, ?> agent;
        private AgentEventBus agentEventBus;
        private ToolManager toolManager;
        private ToolExecutionListenerRegistry toolListenerRegistry;
        private ToolCallTracker toolCallTracker;
        private ToolExecutionConfig toolExecutionConfig;
        private SecurityLevel securityLevel;
        private PermissionApprovalManager<ToolApprovalRequest> approvalManager;
        private AbortSignal abortSignal;
        private String executionId;

        public Builder() {
        }

        private Builder(ToolContext toolContext) {
            this.agent = toolContext.getAgent();
            this.agentEventBus = toolContext.getAgentEventBus();
            this.toolManager = toolContext.getToolManager();
            this.toolListenerRegistry = toolContext.getToolListenerRegistry();
            this.toolCallTracker = toolContext.getToolCallTracker();
            this.toolExecutionConfig = toolContext.getToolExecutionConfig();
            this.securityLevel = toolContext.getSecurityLevel();
            this.approvalManager = toolContext.getApprovalManager();
            this.abortSignal = toolContext.getAbortSignal();
            this.executionId = toolContext.getExecutionId();
        }

        @Override
        public ToolContextBuilder agent(MetaAgent<?, ?> agent) {
            this.agent = agent;
            return this;
        }

        @Override
        public ToolContextBuilder agentEventBus(AgentEventBus agentEventBus) {
            this.agentEventBus = agentEventBus;
            return this;
        }

        @Override
        public ToolContextBuilder toolManager(ToolManager toolManager) {
            this.toolManager = toolManager;
            return this;
        }

        @Override
        public ToolContextBuilder toolCallTracker(ToolCallTracker toolCallTracker) {
            this.toolCallTracker = toolCallTracker;
            return this;
        }

        @Override
        public ToolContextBuilder toolListenerRegistry(ToolExecutionListenerRegistry toolListenerRegistry) {
            this.toolListenerRegistry = toolListenerRegistry;
            return this;
        }

        @Override
        public ToolContextBuilder toolExecutionConfig(ToolExecutionConfig toolExecutionConfig) {
            this.toolExecutionConfig = toolExecutionConfig;
            return this;
        }

        @Override
        public ToolContextBuilder securityLevel(SecurityLevel securityLevel) {
            this.securityLevel = securityLevel;
            return this;
        }

        @Override
        public ToolContextBuilder approvalManager(PermissionApprovalManager<ToolApprovalRequest> approvalManager) {
            this.approvalManager = approvalManager;
            return this;
        }

        @Override
        public ToolContextBuilder abortSignal(AbortSignal abortSignal) {
            this.abortSignal = abortSignal;
            return this;
        }

        @Override
        public ToolContextBuilder executionId(String executionId) {
            this.executionId = executionId;
            return this;
        }

        protected ToolContextBuilder withDefaults() {
            if (agentEventBus == null) {
                this.agentEventBus = AgentEventBus.create();
            }
            if (toolManager == null) {
                this.toolManager = ToolManager.create();
            }
            if (toolCallTracker == null) {
                this.toolCallTracker = ToolCallTracker.empty();
            }
            if (toolListenerRegistry == null) {
                this.toolListenerRegistry = ToolExecutionListenerRegistry.create();
            }
            if (toolExecutionConfig == null) {
                toolExecutionConfig = DefaultToolExecutionConfig.builder().build();
            }
            if (securityLevel == null) {
                this.securityLevel = SecurityLevel.RESTRICTED_DEFAULT_SALE;
            }
            if (approvalManager == null) {
                this.approvalManager = AsyncEventPermissionApprovalManager.assign(
                        PermissionApprover.alwaysApproved(),
                        ForkJoinPool.commonPool()
                );
            }
            if (abortSignal == null) {
                this.abortSignal = AbortController.global().signal();
            }
            return this;
        }

        @Override
        public DefaultToolContext build() {
            withDefaults();
            return new DefaultToolContext(
                    agent,
                    agentEventBus,
                    toolManager,
                    toolListenerRegistry,
                    toolCallTracker,
                    toolExecutionConfig,
                    securityLevel,
                    approvalManager,
                    abortSignal,
                    executionId
            );
        }
    }
}
