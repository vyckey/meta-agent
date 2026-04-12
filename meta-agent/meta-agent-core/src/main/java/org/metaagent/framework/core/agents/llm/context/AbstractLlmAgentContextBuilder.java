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

import org.metaagent.framework.core.agent.context.AbstractAgentContextBuilder;
import org.metaagent.framework.core.model.provider.ModelProviderRegistry;
import org.metaagent.framework.core.tool.executor.DefaultToolExecutor;
import org.metaagent.framework.core.tool.executor.ToolExecutor;
import org.metaagent.framework.core.tool.executor.ToolExecutorContext;
import org.metaagent.framework.core.tool.manager.ToolManager;

/**
 * AbstractLlmAgentContextBuilder is an abstract class that provides a common implementation for building
 * {@link LlmAgentContext} instances.
 *
 * @param <Builder> The type of the builder.
 * @author vyckey
 */
public abstract class AbstractLlmAgentContextBuilder<Builder extends AbstractLlmAgentContextBuilder<Builder>>
        extends AbstractAgentContextBuilder<Builder> implements LlmAgentContextBuilder<Builder> {
    protected ModelProviderRegistry modelProviderRegistry;
    protected ToolManager toolManager;
    protected ToolExecutor toolExecutor;
    protected ToolExecutorContext toolExecutorContext;

    protected AbstractLlmAgentContextBuilder() {
    }

    protected AbstractLlmAgentContextBuilder(LlmAgentContext context) {
        super(context);
        this.modelProviderRegistry = context.modelProviderRegistry();
        this.toolManager = context.toolManager();
        this.toolExecutor = context.toolExecutor();
        this.toolExecutorContext = context.toolExecutorContext();
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
}
