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

package org.metaagent.framework.core.agents.llm;

import org.metaagent.framework.common.abort.AbortSignal;
import org.metaagent.framework.core.agent.context.AbstractAgentContextBuilder;
import org.metaagent.framework.core.agent.context.AgentContext;
import org.metaagent.framework.core.agent.context.AgentContextBuilder;
import org.metaagent.framework.core.agent.event.AgentEventBus;
import org.metaagent.framework.core.model.ModelId;
import org.metaagent.framework.core.model.chat.ChatModelInstance;
import org.metaagent.framework.core.model.provider.ModelProvider;
import org.metaagent.framework.core.model.provider.ModelProviderRegistry;
import org.metaagent.framework.core.tool.executor.DefaultToolExecutor;
import org.metaagent.framework.core.tool.executor.ToolExecutor;
import org.metaagent.framework.core.tool.executor.ToolExecutorContext;
import org.metaagent.framework.core.tool.manager.ToolManager;

import java.util.concurrent.Executor;

/**
 * LlmAgentContext is the context for {@link LlmAgent}.
 *
 * @author vyckey
 */
public record LlmAgentContext(
        AgentEventBus agentEventBus,
        AbortSignal abortSignal,
        Executor executor,
        ModelProviderRegistry modelProviderRegistry,
        ToolManager toolManager,
        ToolExecutor toolExecutor,
        ToolExecutorContext toolExecutorContext
) implements AgentContext {

    public static Builder builder() {
        return new Builder();
    }

    @Override
    public AgentEventBus getAgentEventBus() {
        return agentEventBus;
    }

    @Override
    public AbortSignal getAbortSignal() {
        return abortSignal;
    }

    @Override
    public Executor getExecutor() {
        return executor;
    }

    public ModelProviderRegistry getModelProviderRegistry() {
        return modelProviderRegistry;
    }

    public ChatModelInstance getChatModelInstance(ModelId modelId) {
        ModelProvider modelProvider = modelProviderRegistry.getProvider(modelId.providerId());
        return modelProvider.getModel(modelId, ChatModelInstance.class);
    }

    public ToolManager getToolManager() {
        return toolManager;
    }

    public ToolExecutor getToolExecutor() {
        return toolExecutor;
    }

    public ToolExecutorContext getToolExecutorContext() {
        return toolExecutorContext;
    }

    @Override
    public <B extends AgentContextBuilder<B>> B toBuilder() {
        return null;
    }

    public static class Builder extends AbstractAgentContextBuilder<Builder> {
        private ModelProviderRegistry modelProviderRegistry;
        private ToolManager toolManager;
        private ToolExecutor toolExecutor;
        private ToolExecutorContext toolExecutorContext;

        @Override
        protected Builder self() {
            return this;
        }

        public Builder modelProviderRegistry(ModelProviderRegistry modelProviderRegistry) {
            this.modelProviderRegistry = modelProviderRegistry;
            return self();
        }

        public Builder toolManager(ToolManager toolManager) {
            this.toolManager = toolManager;
            return self();
        }

        public Builder toolExecutor(ToolExecutor toolExecutor) {
            this.toolExecutor = toolExecutor;
            return self();
        }

        public Builder toolExecutorContext(ToolExecutorContext toolExecutorContext) {
            this.toolExecutorContext = toolExecutorContext;
            return self();
        }

        @Override
        protected Builder withDefaults() {
            super.withDefaults();
            if (toolManager == null) {
                this.toolManager = ToolManager.create();
            }
            if (toolExecutor == null) {
                this.toolExecutor = DefaultToolExecutor.INSTANCE;
            }
            return self();
        }

        @Override
        public LlmAgentContext build() {
            withDefaults();
            return new LlmAgentContext(agentEventBus, abortSignal, executor,
                    modelProviderRegistry, toolManager, toolExecutor, toolExecutorContext);
        }
    }
}
