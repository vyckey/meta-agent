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
import reactor.core.publisher.Flux;

import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

/**
 * Abstract {@link Agent} implementation.
 *
 * @param <I> the type of agent input
 * @param <O> the type of agent output
 * @param <S> the type of agent stream output
 * @author vyckey
 */
public abstract class AbstractAgent<I, O, S>
        extends AbstractMetaAgent<I, O, S> implements Agent<I, O, S> {
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
    public AgentLoopControlStrategy<I, O, S> getLoopControlStrategy() {
        return new MaxLoopCountAgentLoopControl<>(1);
    }

    @Override
    protected AgentOutput<O> doRun(AgentInput<I> input) {
        return Agent.super.run(input);
    }

    @Override
    protected Flux<AgentOutput<S>> doRunStream(AgentInput<I> input, Consumer<AgentOutput<O>> onRunStreamComplete) {
        Agent<I, O, S> agent = this;

        AtomicReference<AgentInput<I>> currentInputRef = new AtomicReference<>(input);
        AtomicReference<AgentOutput<O>> lastOutputRef = new AtomicReference<>();

        return Flux.defer(() -> stepStream(currentInputRef.get(), lastOutputRef::set))
                .repeat(() -> {
                    AgentOutput<O> lastAgentOutput = lastOutputRef.get();
                    AgentInput<I> currentInput = currentInputRef.get();

                    if (getLoopControlStrategy().shouldContinueLoop(agent, currentInput, lastAgentOutput)) {
                        // perform next step
                        AgentInput<I> nextInput = buildNextStepInput(currentInput, lastAgentOutput);
                        agentState.incrLoopCount();
                        currentInputRef.set(nextInput);
                        return true;
                    } else {
                        // stop loop
                        return false;
                    }
                })
                .doOnComplete(() -> {
                    AgentOutput<O> lastOutput = lastOutputRef.get();
                    System.out.println("Invoking onRunStreamComplete callback...");
                    if (onRunStreamComplete != null) {
                        onRunStreamComplete.accept(lastOutput);
                    }
                });
    }

    /**
     * Builds the next step input for the streaming agent.
     *
     * @param input  The current input for the agent.
     * @param output The output from the current step.
     * @return The next step input for the agent.
     */
    protected AgentInput<I> buildNextStepInput(AgentInput<I> input, AgentOutput<O> output) {
        return input;
    }

    record AgentStateHolder<I, O>(I input, O fullOutput) {
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
