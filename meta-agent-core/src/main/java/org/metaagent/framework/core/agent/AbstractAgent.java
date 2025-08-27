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

import org.apache.commons.lang3.tuple.Pair;
import org.metaagent.framework.core.agent.input.AgentInput;
import org.metaagent.framework.core.agent.loop.AgentLoopControlStrategy;
import org.metaagent.framework.core.agent.loop.MaxLoopCountAgentLoopControl;
import org.metaagent.framework.core.agent.output.AgentOutput;
import org.metaagent.framework.core.tool.ToolContext;
import org.metaagent.framework.core.tool.executor.ToolExecutorContext;
import org.metaagent.framework.core.tool.manager.ToolManager;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

import java.util.concurrent.atomic.AtomicReference;

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
    protected Flux<AgentOutput<S>> doRunStream(AgentInput<I> input) {
        Agent<I, O, S> agent = this;

        Sinks.Many<AgentOutput<O>> fullOutputSink = Sinks.many().replay().latest();
        AtomicReference<AgentStateHolder<AgentInput<I>, AgentOutput<O>>> holderRef = new AtomicReference<>(
                new AgentStateHolder<>(input, null)
        );

        // perform first step
        Pair<Flux<AgentOutput<S>>, AtomicReference<AgentOutput<O>>> firstStepPair = stepStreamWithOutput(input);
        Flux<AgentOutput<S>> firstStepStream = firstStepPair.getLeft()
                .doOnComplete(() -> fullOutputSink.tryEmitNext(firstStepPair.getRight().get()));

        // perform remaining steps util loop control strategy tells us to stop
        return firstStepStream
                .concatWith(Flux.defer(() -> fullOutputSink.asFlux()
                        .flatMap(fullOutput -> {
                            // update input and output state
                            holderRef.set(new AgentStateHolder<>(holderRef.get().input(), fullOutput));

                            if (getLoopControlStrategy().shouldContinueLoop(agent, input, fullOutput)) {
                                // perform next step
                                AgentInput<I> nextInput = buildNextStepInput(holderRef.get().input(), holderRef.get().fullOutput());
                                holderRef.set(new AgentStateHolder<>(nextInput, null));

                                Pair<Flux<AgentOutput<S>>, AtomicReference<AgentOutput<O>>> stepPair = stepStreamWithOutput(input);
                                return stepPair.getLeft()
                                        .doOnComplete(() -> fullOutputSink.tryEmitNext(firstStepPair.getRight().get()));
                            } else {
                                // stop loop
                                return Flux.empty();
                            }
                        })
                        .takeWhile(output -> getLoopControlStrategy()
                                .shouldContinueLoop(agent, input, holderRef.get().fullOutput())
                        )
                ))
                .doFinally(signal -> fullOutputSink.tryEmitComplete());
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
