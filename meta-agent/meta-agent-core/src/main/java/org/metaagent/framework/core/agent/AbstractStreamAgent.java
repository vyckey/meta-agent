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

import com.google.common.collect.Lists;
import org.metaagent.framework.core.agent.input.AgentInput;
import org.metaagent.framework.core.agent.output.AgentOutput;
import org.metaagent.framework.core.agent.output.StreamOutput;
import org.metaagent.framework.core.agent.profile.AgentProfile;
import org.metaagent.framework.core.agent.state.AgentRunStatus;
import org.metaagent.framework.core.agent.state.AgentState;
import org.metaagent.framework.core.agent.state.AgentStepState;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

/**
 * Abstract {@link StreamingAgent} implementation.
 *
 * @author vyckey
 */
public abstract class AbstractStreamAgent<I, O extends StreamOutput<S>, S>
        extends AbstractAgent<I, O> implements StreamingAgent<I, O, S> {
    protected AbstractStreamAgent(String name) {
        super(name);
    }

    protected AbstractStreamAgent(AgentProfile profile) {
        super(profile);
    }

    protected AbstractStreamAgent(AbstractAgentBuilder<?, ?, I, O> builder) {
        super(builder);
    }

    protected Flux<S> doRunStreamComplete(Flux<S> stream) {
        return stream
                .doOnComplete(() -> agentState.setStatus(AgentRunStatus.COMPLETED))
                .doOnError(throwable -> {
                    AgentState agentState = getAgentState();
                    if (throwable instanceof AgentInterruptedException ex) {
                        agentState.setStatus(AgentRunStatus.INTERRUPTED);
                        agentState.getStepState().setLastException(ex);
                        throw ex;
                    } else if (throwable instanceof AgentExecutionException ex) {
                        agentState.setStatus(AgentRunStatus.FAILED);
                        agentState.getStepState().setLastException(ex);
                        throw ex;
                    } else {
                        agentState.setStatus(AgentRunStatus.FAILED);
                        AgentExecutionException ex = new AgentExecutionException("agent execution failed", throwable);
                        agentState.getStepState().setLastException(ex);
                        throw ex;
                    }
                });
    }

    public AgentOutput<O> runStream(AgentInput<I> agentInput) {
        AgentState agentState = getAgentState();
        if (agentState.getStatus() == AgentRunStatus.RUNNING) {
            throw new AgentExecutionException("agent is already running");
        }

        agentState.setStatus(AgentRunStatus.RUNNING);
        agentState.resetStepState();

        MetaAgent<I, O> agent = this;
        AtomicReference<AgentOutput<O>> fullOutputRef = new AtomicReference<>();

        AgentInput<I> input = preprocess(agentInput);
        AgentOutput<O> agentOutput = doRunStream(input, fullOutputRef::set);
        Flux<S> stream = agentOutput.result().stream()
                .doOnSubscribe(sub -> {
                    notifyListeners(runListeners, listener -> listener.onAgentStart(agent, input));
                })
                .onErrorResume(throwable -> {
                    if (throwable instanceof Exception ex) {
                        return fallbackStream(input, ex);
                    }
                    return Flux.error(throwable);
                })
                .doOnComplete(() -> notifyListeners(runListeners, listener -> listener.onAgentOutput(agent, input, fullOutputRef.get())))
                .doOnError(throwable -> {
                    if (throwable instanceof Exception ex) {
                        notifyListeners(runListeners, listener -> listener.onAgentException(agent, input, ex));
                    } else {
                        AgentExecutionException ex = new AgentExecutionException("agent execution failed", throwable);
                        notifyListeners(runListeners, listener -> listener.onAgentException(agent, input, ex));
                    }
                });
        stream = doRunStreamComplete(stream);
        return rebuildOutput(input, agentOutput, stream);
    }

    protected AgentOutput<O> doRunStream(AgentInput<I> input, Consumer<AgentOutput<O>> onStreamComplete) {
        Agent<I, O> agent = this;

        AtomicReference<AgentInput<I>> currentInputRef = new AtomicReference<>(input);
        AtomicReference<AgentOutput<O>> lastOutputRef = new AtomicReference<>();

        Flux<S> stream = Flux
                .defer(() -> stepStream(currentInputRef.get(), lastOutputRef::set).result().stream())
                .repeat(() -> {
                    AgentOutput<O> lastAgentOutput = lastOutputRef.get();
                    AgentInput<I> currentInput = currentInputRef.get();

                    if (getLoopControlStrategy().shouldContinueLoop(agent, currentInput, lastAgentOutput)) {
                        // perform next step
                        AgentStepState stepState = agentState.resetStepState();
                        AgentInput<I> nextInput = buildNextStepInput(currentInput, lastAgentOutput);
                        stepState.getLoopCount().incrementAndGet();
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
                    if (onStreamComplete != null) {
                        onStreamComplete.accept(lastOutput);
                    }
                });
        return rebuildOutput(input, null, stream);
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

    protected Flux<S> fallbackStream(AgentInput<I> input, Exception ex) {
        return Flux.error(ex);
    }

    @Override
    public AgentOutput<O> stepStream(AgentInput<I> input) {
        return stepStream(input, fullOutput -> {
        });
    }

    protected AgentOutput<O> stepStream(AgentInput<I> input, Consumer<AgentOutput<O>> onStreamComplete) {
        MetaAgent<I, O> agent = this;

        AtomicReference<List<S>> streamOutputsRef = new AtomicReference<>(Lists.newArrayList());
        AtomicReference<O> fullOutputRef = new AtomicReference<>();

        StreamOutput.Aggregator<S, O> aggregator = getStreamOutputAggregator();
        AgentOutput<O> agentOutput = doStepStream(input);
        Flux<S> stream = agentOutput.result().stream()
                .doOnSubscribe(sub -> notifyListeners(stepListeners, listener -> listener.onAgentStepStart(agent, input)))
                .doOnNext(output -> streamOutputsRef.get().add(output))
                .doOnComplete(() -> fullOutputRef.set(aggregator.aggregate(streamOutputsRef.get())))
                .doOnComplete(() -> {
                    AgentOutput<O> aggOutput = AgentOutput.create(fullOutputRef.get(), agentOutput.metadata());
                    onStreamComplete.accept(aggOutput);
                    notifyListeners(stepListeners, listener -> listener.onAgentStepFinish(agent, input, aggOutput));
                })
                .doOnError(throwable -> {
                    if (throwable instanceof Exception ex) {
                        notifyListeners(stepListeners, listener -> listener.onAgentStepError(agent, input, ex));
                    } else {
                        AgentExecutionException ex = new AgentExecutionException("agent execution failed", throwable);
                        notifyListeners(stepListeners, listener -> listener.onAgentStepError(agent, input, ex));
                    }
                });
        return rebuildOutput(input, agentOutput, stream);
    }

    protected abstract AgentOutput<O> rebuildOutput(AgentInput<I> input, AgentOutput<O> agentOutput, Flux<S> stream);

    protected abstract AgentOutput<O> doStepStream(AgentInput<I> input);

}
