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
import org.apache.commons.lang3.tuple.Pair;
import org.metaagent.framework.core.agent.ability.AgentAbilityManager;
import org.metaagent.framework.core.agent.ability.DefaultAgentAbilityManager;
import org.metaagent.framework.core.agent.fallback.AgentFallbackStrategy;
import org.metaagent.framework.core.agent.fallback.FastFailAgentFallbackStrategy;
import org.metaagent.framework.core.agent.input.AgentInput;
import org.metaagent.framework.core.agent.memory.EmptyMemory;
import org.metaagent.framework.core.agent.memory.Memory;
import org.metaagent.framework.core.agent.observability.AgentLogListener;
import org.metaagent.framework.core.agent.observability.AgentLogger;
import org.metaagent.framework.core.agent.observability.AgentRunListener;
import org.metaagent.framework.core.agent.observability.AgentStepListener;
import org.metaagent.framework.core.agent.output.AgentOutput;
import org.metaagent.framework.core.agent.output.AgentStreamOutputAggregator;
import org.metaagent.framework.core.agent.profile.AgentProfile;
import org.metaagent.framework.core.agent.profile.DefaultAgentProfile;
import org.metaagent.framework.core.agent.state.AgentRunStatus;
import org.metaagent.framework.core.agent.state.AgentState;
import org.metaagent.framework.core.agent.state.DefaultAgentState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

/**
 * Abstract {@link MetaAgent} implementation.
 *
 * @param <I> the type of agent input
 * @param <O> the type of agent output
 * @param <S> the type of agent stream output
 * @author vyckey
 */
public abstract class AbstractMetaAgent<I, O, S> implements MetaAgent<I, O, S> {

    protected AgentProfile profile;
    protected AgentState agentState = DefaultAgentState.builder().build();
    protected Memory memory = EmptyMemory.INSTANCE;
    protected AgentAbilityManager abilityManager = new DefaultAgentAbilityManager();
    protected final List<AgentRunListener<I, O, S>> runListeners = Lists.newArrayList();
    protected final List<AgentStepListener<I, O, S>> stepListeners = Lists.newArrayList();
    protected AgentLogger agentLogger;
    protected Logger logger = LoggerFactory.getLogger(getClass());

    protected AbstractMetaAgent(AgentProfile profile) {
        this.profile = profile;
        this.agentLogger = AgentLogger.getLogger(profile.getName());
    }

    protected AbstractMetaAgent(String name) {
        this(new DefaultAgentProfile(name));
    }

    @Override
    public final String getName() {
        return profile.getName();
    }

    @Override
    public AgentProfile getAgentProfile() {
        return profile;
    }

    @Override
    public AgentState getAgentState() {
        return agentState;
    }

    @Override
    public Memory getMemory() {
        return memory;
    }

    @Override
    public AgentAbilityManager getAbilityManager() {
        return abilityManager;
    }

    public void initialize() {
        // Register log listener to capture agent logs
        AgentLogListener<I, O, S> logListener = new AgentLogListener<>(agentState, agentLogger);
        registerRunListener(logListener);
        registerStepListener(logListener);
    }

    public void registerRunListener(AgentRunListener<I, O, S> listener) {
        runListeners.add(listener);
    }

    public void unregisterRunListener(AgentRunListener<I, O, S> listener) {
        runListeners.remove(listener);
    }

    public void registerStepListener(AgentStepListener<I, O, S> listener) {
        stepListeners.add(listener);
    }

    public void unregisterStepListener(AgentStepListener<I, O, S> listener) {
        stepListeners.remove(listener);
    }

    protected <T> void notifyListeners(Iterable<T> listeners, Consumer<T> consumer) {
        for (T listener : listeners) {
            try {
                consumer.accept(listener);
            } catch (Exception e) {
                logger.error("Fail to invoke listener", e);
            }
        }
    }

    public AgentFallbackStrategy<I, O, S> getFallbackStrategy() {
        return new FastFailAgentFallbackStrategy<>();
    }

    /**
     * This method is called before the agent runs. It also checks the agent state and the agent input.
     *
     * @param input the agent input
     */
    protected void beforeRun(AgentInput<I> input) {
        if (agentState.getStatus().isFinished()) {
            throw new AgentExecutionException("Agent " + getName() + " has run finished. Please reset it before running again.");
        }
    }

    @Override
    public AgentOutput<O> run(AgentInput<I> input) {
        beforeRun(input);

        agentState.setStatus(AgentRunStatus.RUNNING);
        try {
            notifyListeners(runListeners, listener -> listener.onAgentStart(this, input));
            AgentOutput<O> output = doRun(input);
            notifyListeners(runListeners, listener -> listener.onAgentOutput(this, input, output));

            agentState.setStatus(AgentRunStatus.COMPLETED);
            return output;
        } catch (AgentExecutionException ex) {
            agentState.setLastException(ex);
            notifyListeners(runListeners, listener -> listener.onAgentException(this, input, ex));
            throw ex;
        } catch (Exception ex) {
            agentState.setLastException(ex);
            notifyListeners(runListeners, listener -> listener.onAgentException(this, input, ex));
            throw new AgentExecutionException(ex);
        }
    }

    protected AgentOutput<O> doRun(AgentInput<I> input) {
        try {
            return step(input);
        } catch (Exception ex) {
            return getFallbackStrategy().fallback(this, input, ex);
        }
    }

    @Override
    public AgentOutput<O> step(AgentInput<I> input) {
        try {
            notifyListeners(stepListeners, listener -> listener.onAgentStepStart(this, input));
            AgentOutput<O> output = doStep(input);
            notifyListeners(stepListeners, listener -> listener.onAgentStepFinish(this, input, output));
            return output;
        } catch (Exception ex) {
            agentState.setLastException(ex);
            notifyListeners(stepListeners, listener -> listener.onAgentStepError(this, input, ex));
            throw ex;
        }
    }

    protected abstract AgentOutput<O> doStep(AgentInput<I> input);

    @Override
    public Flux<AgentOutput<S>> runStream(AgentInput<I> input) {
        MetaAgent<I, O, S> agent = this;
        return doRunStream(input)
                .doOnSubscribe(sub -> {
                    beforeRun(input);
                    agentState.setStatus(AgentRunStatus.RUNNING);
                    notifyListeners(runListeners, listener -> listener.onAgentStart(agent, input));
                })
                .doOnNext(output -> notifyListeners(runListeners, listener -> listener.onAgentOutput(agent, input, null)))
                .onErrorResume(throwable -> {
                    if (throwable instanceof Exception ex) {
                        return fallbackStream(input, ex);
                    }
                    return Flux.error(throwable);
                })
                .doOnComplete(() -> agentState.setStatus(AgentRunStatus.COMPLETED))
                .doOnError(throwable -> {
                    Exception ex = wrapException(throwable);
                    agentState.setLastException(ex);
                    agentState.setStatus(AgentRunStatus.FAILED);
                    notifyListeners(runListeners, listener -> listener.onAgentException(agent, input, ex));
                });
    }

    protected Flux<AgentOutput<S>> doRunStream(AgentInput<I> input) {
        return stepStream(input);
    }

    @Override
    public Flux<AgentOutput<S>> stepStream(AgentInput<I> input) {
        return stepStreamWithOutput(input).getLeft();
    }

    protected Pair<Flux<AgentOutput<S>>, AtomicReference<AgentOutput<O>>> stepStreamWithOutput(AgentInput<I> input) {
        MetaAgent<I, O, S> agent = this;

        AgentStreamOutputAggregator<S, O> aggregator = getStreamOutputAggregator();
        AtomicReference<AgentOutput<O>> fullOutputRef = new AtomicReference<>(aggregator.initialState());
        Flux<AgentOutput<S>> stream = doStepStream(input)
                .doOnSubscribe(sub -> notifyListeners(stepListeners, listener -> listener.onAgentStepStart(agent, input)))
                .doOnNext(output -> fullOutputRef.set(aggregator.aggregate(fullOutputRef.get(), output)))
                .doOnComplete(() -> notifyListeners(stepListeners, listener -> listener.onAgentStepFinish(agent, input, fullOutputRef.get())))
                .doOnError(throwable -> {
                    Exception ex = wrapException(throwable);
                    agentState.setLastException(ex);
                    notifyListeners(stepListeners, listener -> listener.onAgentStepError(agent, input, ex));
                });
        return Pair.of(stream, fullOutputRef);
    }

    protected Flux<AgentOutput<S>> doStepStream(AgentInput<I> input) {
        throw new UnsupportedOperationException("Streaming step not supported");
    }

    protected Flux<AgentOutput<S>> fallbackStream(AgentInput<I> input, Exception ex) {
        return Flux.error(ex);
    }

    protected Exception wrapException(Throwable throwable) {
        if (throwable instanceof Exception e) {
            return e;
        } else {
            return new AgentExecutionException(throwable);
        }
    }

    @Override
    public void reset() {
        this.agentState.reset();
        this.memory.clear();
    }

    @Override
    public String toString() {
        return getName();
    }
}
