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

import org.metaagent.framework.core.agent.input.AgentInput;
import org.metaagent.framework.core.agent.loop.AgentLoopControlStrategy;
import org.metaagent.framework.core.agent.loop.MaxLoopCountAgentLoopControl;
import org.metaagent.framework.core.agent.output.AgentOutput;
import org.metaagent.framework.core.agent.profile.AgentProfile;
import org.metaagent.framework.core.tool.ToolContext;
import org.metaagent.framework.core.tool.executor.ToolExecutorContext;
import org.metaagent.framework.core.tool.manager.ToolManager;

/**
 * Abstract {@link Agent} implementation.
 *
 * @param <I> the type of agent input
 * @param <O> the type of agent output
 * @author vyckey
 */
public abstract class AbstractAgent<I, O>
        extends AbstractMetaAgent<I, O> implements Agent<I, O> {
    protected ToolManager toolManager = ToolManager.create();

    protected AbstractAgent(String name) {
        super(name);
    }

    protected AbstractAgent(AgentProfile profile) {
        super(profile);
    }

    @Override
    public ToolManager getToolManager() {
        return toolManager;
    }

    @Override
    public AgentLoopControlStrategy<I, O> getLoopControlStrategy() {
        return new MaxLoopCountAgentLoopControl<>(1);
    }

    @Override
    protected AgentOutput<O> doRun(AgentInput<I> input) {
        return Agent.super.run(input);
    }

    protected ToolExecutorContext buildToolExecutorContext(AgentInput<I> input) {
        AgentExecutionContext agentContext = input.context();
        return ToolExecutorContext.builder()
                .toolManager(getToolManager())
                .toolListenerRegistry(agentContext.getToolListenerRegistry())
                .toolCallTracker(agentState.getToolCallTracker())
                .toolContext(ToolContext.builder()
                        .abortSignal(agentContext.getAbortSignal())
                        .build())
                .build();
    }
}
