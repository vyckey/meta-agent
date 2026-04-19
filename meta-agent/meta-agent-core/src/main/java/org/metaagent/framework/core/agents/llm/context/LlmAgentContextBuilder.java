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

import org.metaagent.framework.core.agent.context.AgentContextBuilder;
import org.metaagent.framework.core.agents.llm.compaction.ContextCompactionService;
import org.metaagent.framework.core.agents.llm.processor.ContextCompactionPostProcessor;
import org.metaagent.framework.core.model.provider.ModelProviderRegistry;
import org.metaagent.framework.core.tool.executor.ToolExecutorContext;
import org.metaagent.framework.core.tool.manager.ToolManager;

/**
 * Builder for {@link LlmAgentContext}.
 *
 * @author vyckey
 */
public interface LlmAgentContextBuilder<Builder extends LlmAgentContextBuilder<Builder>>
        extends AgentContextBuilder<Builder> {
    /**
     * Set the model provider registry.
     *
     * @param modelProviderRegistry the model provider registry
     * @return the builder
     */
    Builder modelProviderRegistry(ModelProviderRegistry modelProviderRegistry);

    /**
     * Set the tool manager.
     *
     * @param toolManager the tool manager
     * @return the builder
     */
    Builder toolManager(ToolManager toolManager);

    /**
     * Set the tool executor context.
     *
     * @param toolExecutorContext the tool executor context
     * @return the builder
     */
    Builder toolExecutorContext(ToolExecutorContext toolExecutorContext);

    /**
     * Set the context compaction service.
     *
     * @param contextCompactionService the context compaction service
     * @return the builder
     */
    Builder contextCompactionService(ContextCompactionService contextCompactionService);

    /**
     * Set the context compaction post processor.
     *
     * @param contextCompactionPostProcessor the context compaction post processor
     * @return the builder
     */
    Builder contextCompactionPostProcessor(ContextCompactionPostProcessor contextCompactionPostProcessor);

    /**
     * Build the agent context.
     *
     * @return the agent context
     */
    @Override
    LlmAgentContext build();
}
