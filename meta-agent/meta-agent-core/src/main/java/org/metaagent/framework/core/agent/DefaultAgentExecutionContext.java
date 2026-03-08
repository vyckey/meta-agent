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

import lombok.Getter;
import org.metaagent.framework.common.abort.AbortController;
import org.metaagent.framework.common.abort.AbortSignal;
import org.metaagent.framework.core.agent.observability.AgentListenerRegistry;
import org.metaagent.framework.core.config.WorkspaceConfig;
import org.metaagent.framework.core.security.SecurityLevel;
import org.metaagent.framework.core.security.approval.AsyncEventPermissionApprovalManager;
import org.metaagent.framework.core.security.approval.PermissionApprovalManager;
import org.metaagent.framework.core.security.approval.PermissionApprover;
import org.metaagent.framework.core.skill.loader.SkillLoaders;
import org.metaagent.framework.core.skill.manager.SkillManager;
import org.metaagent.framework.core.tool.approval.ToolApprovalRequest;
import org.metaagent.framework.core.tool.executor.DefaultToolExecutor;
import org.metaagent.framework.core.tool.executor.ToolExecutor;
import org.metaagent.framework.core.tool.listener.ToolExecutionListenerRegistry;
import org.metaagent.framework.core.tool.manager.ToolManager;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * Default implementation of {@link AgentExecutionContext}.
 *
 * @author vyckey
 */
@Getter
public class DefaultAgentExecutionContext implements AgentExecutionContext {
    private final SecurityLevel securityLevel;
    private final AgentListenerRegistry<?, ?> agentListenerRegistry;
    private final ToolManager toolManager;
    private final ToolExecutor toolExecutor;
    private final ToolExecutionListenerRegistry toolListenerRegistry;
    private final PermissionApprovalManager<ToolApprovalRequest> toolApprovalManager;
    private final SkillManager skillManager;
    private final AbortSignal abortSignal;
    private final Executor executor;
    private final WorkspaceConfig workspaceConfig;

    protected DefaultAgentExecutionContext(Builder builder) {
        this.securityLevel = builder.securityLevel;
        this.agentListenerRegistry = builder.agentListenerRegistry;
        this.toolManager = builder.toolManager;
        this.toolExecutor = builder.toolExecutor;
        this.toolListenerRegistry = builder.toolListenerRegistry;
        this.toolApprovalManager = builder.toolApprovalManager;
        this.skillManager = builder.skillManager;
        this.abortSignal = builder.abortSignal;
        this.executor = builder.executor;
        this.workspaceConfig = builder.workspaceConfig;
    }

    public static Builder builder() {
        return new Builder();
    }

    @Override
    public AgentExecutionContextBuilder toBuilder() {
        return new Builder(this);
    }


    public static final class Builder implements AgentExecutionContextBuilder {
        private SecurityLevel securityLevel;
        private AgentListenerRegistry<?, ?> agentListenerRegistry;
        private ToolManager toolManager;
        private ToolExecutor toolExecutor;
        private ToolExecutionListenerRegistry toolListenerRegistry;
        private PermissionApprovalManager<ToolApprovalRequest> toolApprovalManager;
        private SkillManager skillManager;
        private AbortSignal abortSignal;
        private Executor executor;
        private WorkspaceConfig workspaceConfig;

        private Builder() {
        }

        private Builder(AgentExecutionContext context) {
            this.securityLevel = context.getSecurityLevel();
            this.agentListenerRegistry = context.getAgentListenerRegistry();
            this.toolManager = context.getToolManager();
            this.toolExecutor = context.getToolExecutor();
            this.toolListenerRegistry = context.getToolListenerRegistry();
            this.skillManager = context.getSkillManager();
            this.abortSignal = context.getAbortSignal();
            this.executor = context.getExecutor();
            this.workspaceConfig = context.getWorkspaceConfig();
        }

        @Override
        public AgentExecutionContextBuilder securityLevel(SecurityLevel securityLevel) {
            this.securityLevel = securityLevel;
            return this;
        }

        @Override
        public AgentExecutionContextBuilder agentListenerRegistry(AgentListenerRegistry<?, ?> listenerRegistry) {
            this.agentListenerRegistry = listenerRegistry;
            return this;
        }

        @Override
        public AgentExecutionContextBuilder toolManager(ToolManager toolManager) {
            this.toolManager = toolManager;
            return this;
        }

        @Override
        public Builder toolExecutor(ToolExecutor toolExecutor) {
            this.toolExecutor = toolExecutor;
            return this;
        }

        @Override
        public Builder toolListenerRegistry(ToolExecutionListenerRegistry toolExecutionListenerRegistry) {
            this.toolListenerRegistry = toolExecutionListenerRegistry;
            return this;
        }

        @Override
        public AgentExecutionContextBuilder toolApprovalManager(PermissionApprovalManager<ToolApprovalRequest> toolApprovalManager) {
            this.toolApprovalManager = toolApprovalManager;
            return this;
        }

        @Override
        public AgentExecutionContextBuilder skillManager(SkillManager skillManager) {
            this.skillManager = skillManager;
            return this;
        }

        @Override
        public AgentExecutionContextBuilder abortSignal(AbortSignal abortSignal) {
            this.abortSignal = abortSignal;
            return this;
        }

        @Override
        public Builder executor(Executor executor) {
            this.executor = executor;
            return this;
        }

        @Override
        public AgentExecutionContextBuilder workspaceConfig(WorkspaceConfig workspaceConfig) {
            this.workspaceConfig = workspaceConfig;
            return this;
        }

        private void setDefault() {
            if (agentListenerRegistry == null) {
                agentListenerRegistry = AgentListenerRegistry.create();
            }
            if (toolManager == null) {
                this.toolManager = ToolManager.create();
            }
            if (toolExecutor == null) {
                this.toolExecutor = DefaultToolExecutor.INSTANCE;
            }
            if (toolListenerRegistry == null) {
                this.toolListenerRegistry = ToolExecutionListenerRegistry.create();
            }
            if (toolApprovalManager == null) {
                this.toolApprovalManager = AsyncEventPermissionApprovalManager.assign(
                        PermissionApprover.alwaysApproved(), Executors.newSingleThreadExecutor()
                );
            }
            if (skillManager == null) {
                this.skillManager = SkillManager.create();
                this.skillManager.registerSkillLoader(SkillLoaders.fileSkillLoader());
            }
            if (abortSignal == null) {
                abortSignal = AbortController.global().signal();
            }
            if (executor == null) {
                executor = Executors.newSingleThreadExecutor();
            }
            if (workspaceConfig == null) {
                workspaceConfig = WorkspaceConfig.newDefault();
            }
        }

        @Override
        public DefaultAgentExecutionContext build() {
            setDefault();
            return new DefaultAgentExecutionContext(this);
        }
    }
}
