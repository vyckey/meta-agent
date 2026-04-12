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

package org.metaagent.framework.agents.chat.context;

import org.metaagent.framework.common.abort.AbortSignal;
import org.metaagent.framework.core.agent.event.AgentEventBus;
import org.metaagent.framework.core.agents.llm.context.AbstractLlmAgentContextBuilder;
import org.metaagent.framework.core.agents.llm.context.LlmAgentContext;
import org.metaagent.framework.core.agents.llm.context.LlmAgentContextBuilder;
import org.metaagent.framework.core.config.WorkspaceConfig;
import org.metaagent.framework.core.model.provider.ModelProviderRegistry;
import org.metaagent.framework.core.security.approval.AsyncEventPermissionApprovalManager;
import org.metaagent.framework.core.security.approval.PermissionApprovalManager;
import org.metaagent.framework.core.security.approval.PermissionApprover;
import org.metaagent.framework.core.skill.loader.SkillLoaders;
import org.metaagent.framework.core.skill.manager.SkillManager;
import org.metaagent.framework.core.tool.approval.ToolApprovalRequest;
import org.metaagent.framework.core.tool.executor.ToolExecutor;
import org.metaagent.framework.core.tool.executor.ToolExecutorContext;
import org.metaagent.framework.core.tool.manager.ToolManager;

import java.util.Objects;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * ChatAgentContext is the context for the ChatAgent.
 *
 * @author vyckey
 */
public record ChatAgentContext(
        AgentEventBus agentEventBus,
        AbortSignal abortSignal,
        Executor executor,
        ModelProviderRegistry modelProviderRegistry,
        ToolManager toolManager,
        ToolExecutor toolExecutor,
        ToolExecutorContext toolExecutorContext,
        SkillManager skillManager,
        PermissionApprovalManager<ToolApprovalRequest> toolApprovalManager,
        WorkspaceConfig workspaceConfig
) implements LlmAgentContext {
    public ChatAgentContext {
        Objects.requireNonNull(agentEventBus, "agentEventBus is required");
        Objects.requireNonNull(abortSignal, "abortSignal is required");
        Objects.requireNonNull(executor, "executor is required");
        Objects.requireNonNull(modelProviderRegistry, "modelProviderRegistry is required");
        Objects.requireNonNull(toolManager, "toolManager is required");
        Objects.requireNonNull(toolExecutor, "toolExecutor is required");
        Objects.requireNonNull(toolExecutorContext, "toolExecutorContext is required");
        Objects.requireNonNull(skillManager, "skillManager is required");
        Objects.requireNonNull(toolApprovalManager, "toolApprovalManager is required");
        Objects.requireNonNull(workspaceConfig, "workspaceConfig is required");
    }

    public static ChatAgentContext create() {
        return builder().build();
    }

    public static Builder builder() {
        return new Builder();
    }

    @Override
    public LlmAgentContextBuilder<?> toBuilder() {
        return new Builder(this);
    }


    public static class Builder extends AbstractLlmAgentContextBuilder<Builder> {
        private SkillManager skillManager;
        private PermissionApprovalManager<ToolApprovalRequest> toolApprovalManager;
        private WorkspaceConfig workspaceConfig;

        private Builder() {
        }

        protected Builder(ChatAgentContext context) {
            super(context);
            this.skillManager = context.skillManager;
            this.toolApprovalManager = context.toolApprovalManager;
            this.workspaceConfig = context.workspaceConfig;
        }

        @Override
        protected Builder self() {
            return this;
        }

        public Builder skillManager(SkillManager skillManager) {
            this.skillManager = skillManager;
            return this;
        }

        public Builder workspaceConfig(WorkspaceConfig workspaceConfig) {
            this.workspaceConfig = workspaceConfig;
            return this;
        }

        @Override
        protected Builder withDefaults() {
            super.withDefaults();
            if (toolApprovalManager == null) {
                this.toolApprovalManager = AsyncEventPermissionApprovalManager.assign(
                        PermissionApprover.alwaysApproved(), Executors.newSingleThreadExecutor()
                );
            }
            if (skillManager == null) {
                this.skillManager = SkillManager.create();
                this.skillManager.registerSkillLoader(SkillLoaders.fileSkillLoader());
            }
            if (workspaceConfig == null) {
                workspaceConfig = WorkspaceConfig.newDefault();
            }
            return self();
        }

        @Override
        public ChatAgentContext build() {
            withDefaults();
            return new ChatAgentContext(
                    agentEventBus,
                    abortSignal,
                    executor,
                    modelProviderRegistry,
                    toolManager,
                    toolExecutor,
                    toolExecutorContext,
                    skillManager,
                    toolApprovalManager,
                    workspaceConfig
            );
        }
    }

}
