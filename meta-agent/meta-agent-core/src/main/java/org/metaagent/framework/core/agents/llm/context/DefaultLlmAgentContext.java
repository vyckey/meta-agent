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

package org.metaagent.framework.core.agents.llm.context;

import org.metaagent.framework.common.abort.AbortSignal;
import org.metaagent.framework.core.agent.event.AgentEventBus;
import org.metaagent.framework.core.agents.llm.LlmAgent;
import org.metaagent.framework.core.agents.llm.LlmStreamingAgent;
import org.metaagent.framework.core.model.provider.ModelProviderRegistry;
import org.metaagent.framework.core.tool.executor.ToolExecutorContext;
import org.metaagent.framework.core.tool.manager.ToolManager;

import java.util.Objects;
import java.util.concurrent.Executor;

/**
 * LlmAgentContext is the context for {@link LlmStreamingAgent} or {@link LlmAgent}.
 *
 * @author vyckey
 */
public record DefaultLlmAgentContext(
        AgentEventBus agentEventBus,
        AbortSignal abortSignal,
        Executor executor,
        ModelProviderRegistry modelProviderRegistry,
        ToolManager toolManager,
        ToolExecutorContext toolExecutorContext
) implements LlmAgentContext {
    public DefaultLlmAgentContext {
        Objects.requireNonNull(agentEventBus, "agentEventBus is required");
        Objects.requireNonNull(abortSignal, "abortSignal is required");
        Objects.requireNonNull(executor, "executor is required");
        Objects.requireNonNull(modelProviderRegistry, "modelProviderRegistry is required");
        Objects.requireNonNull(toolManager, "toolManager is required");
        Objects.requireNonNull(toolExecutorContext, "toolExecutorContext is required");
    }

    public static Builder builder() {
        return new Builder();
    }

    @Override
    public LlmAgentContextBuilder<?> toBuilder() {
        return new Builder(this);
    }


    public static class Builder extends AbstractLlmAgentContextBuilder<Builder> {
        private Builder() {
        }

        public Builder(LlmAgentContext context) {
            super(context);
        }

        @Override
        protected Builder self() {
            return this;
        }

        @Override
        public DefaultLlmAgentContext build() {
            withDefaults();
            return new DefaultLlmAgentContext(agentEventBus, abortSignal, executor,
                    modelProviderRegistry, toolManager, toolExecutorContext);
        }
    }
}
