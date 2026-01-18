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
import org.metaagent.framework.core.agent.fallback.AgentFallbackStrategy;
import org.metaagent.framework.core.agent.fallback.FastFailAgentFallbackStrategy;
import org.metaagent.framework.core.agent.input.AgentInput;
import org.metaagent.framework.core.agent.memory.EmptyMemory;
import org.metaagent.framework.core.agent.memory.Memory;
import org.metaagent.framework.core.agent.observability.AgentEventBus;
import org.metaagent.framework.core.agent.observability.AgentEventListener;
import org.metaagent.framework.core.agent.observability.AgentListenerRegistry;
import org.metaagent.framework.core.agent.observability.AgentLogListener;
import org.metaagent.framework.core.agent.observability.AgentLogger;
import org.metaagent.framework.core.agent.observability.AgentRunEventPublisher;
import org.metaagent.framework.core.agent.observability.AgentRunListener;
import org.metaagent.framework.core.agent.observability.AgentStepListener;
import org.metaagent.framework.core.agent.observability.event.AgentEvent;
import org.metaagent.framework.core.agent.output.AgentOutput;
import org.metaagent.framework.core.agent.profile.AgentProfile;
import org.metaagent.framework.core.agent.state.AgentRunStatus;
import org.metaagent.framework.core.agent.state.AgentState;
import org.metaagent.framework.core.agent.state.AgentStepState;
import org.metaagent.framework.core.agent.state.DefaultAgentState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.function.Consumer;

/**
 * Abstract {@link MetaAgent} implementation.
 *
 * @param <I> the type of agent input
 * @param <O> the type of agent output
 * @author vyckey
 */
public abstract class AbstractMetaAgent<I, O> implements AgentListenerRegistry<I, O>, MetaAgent<I, O> {

    protected String agentName;
    protected AgentProfile profile;
    protected AgentState agentState;
    protected Memory memory;
    protected AgentEventBus<AgentEvent> eventBus;
    protected final List<AgentRunListener<I, O>> runListeners = Lists.newCopyOnWriteArrayList();
    protected final List<AgentStepListener<I, O>> stepListeners = Lists.newCopyOnWriteArrayList();
    protected boolean initialized = false;
    protected AgentLogger agentLogger;
    protected Logger logger = LoggerFactory.getLogger(getClass());

    protected AbstractMetaAgent(String name) {
        this.agentName = name;
        this.agentState = DefaultAgentState.builder().build();
        this.memory = EmptyMemory.INSTANCE;
        this.eventBus = AgentEventBus.global();
        this.agentLogger = AgentLogger.getLogger(name);
    }

    protected AbstractMetaAgent(AgentProfile profile) {
        this(profile.getName());
        this.profile = profile;
        this.eventBus = AgentEventBus.global();
    }

    protected AbstractMetaAgent(AbstractAgentBuilder<?, ?, I, O> builder) {
        this.profile = Objects.requireNonNull(builder.profile, "agent profile is required");
        this.agentName = builder.profile.getName();
        this.agentLogger = AgentLogger.getLogger(this.agentName);
        this.agentState = builder.agentState != null ? builder.agentState : DefaultAgentState.builder().build();
        this.memory = builder.memory != null ? builder.memory : EmptyMemory.INSTANCE;
        this.eventBus = builder.agentEventBus != null ? builder.agentEventBus : AgentEventBus.global();
    }

    @Override
    public final String getName() {
        return agentName;
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

    public void initialize() {
        if (initialized) {
            return;
        }

        // Register log listener to capture agent logs
        AgentLogListener<I, O> logListener = new AgentLogListener<>(agentState, agentLogger);
        registerRunListener(logListener);
        registerStepListener(logListener);
        // Register agent run event publisher
        registerRunListener(new AgentRunEventPublisher<>(eventBus));
        initialized = true;
    }

    @Override
    public void registerRunListener(AgentRunListener<I, O> listener) {
        runListeners.add(listener);
    }

    @Override
    public void unregisterRunListener(AgentRunListener<I, O> listener) {
        runListeners.remove(listener);
    }

    @Override
    public void unregisterRunListeners() {
        runListeners.clear();
    }

    @Override
    public void registerStepListener(AgentStepListener<I, O> listener) {
        stepListeners.add(listener);
    }

    @Override
    public void unregisterStepListener(AgentStepListener<I, O> listener) {
        stepListeners.remove(listener);
    }

    @Override
    public void unregisterStepListeners() {
        stepListeners.clear();
    }

    @Override
    public void registerEventListener(AgentEventListener<AgentEvent> listener) {
        eventBus.subscribe(listener);
    }

    @Override
    public void unregisterEventListener(AgentEventListener<AgentEvent> listener) {
        eventBus.unsubscribe(listener);
    }

    @Override
    public void unregisterEventListeners() {
        eventBus.unsubscribeAll();
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

    public AgentFallbackStrategy<I, O> getFallbackStrategy() {
        return new FastFailAgentFallbackStrategy<>();
    }

    /**
     * Performs the agent run.
     *
     * @param callable the callable to execute
     * @param <V>      the type of the callable result
     * @return the callable result
     */
    protected <V> V performRun(Callable<V> callable) {
        AgentState agentState = getAgentState();
        if (agentState.getStatus() == AgentRunStatus.RUNNING) {
            throw new AgentExecutionException("agent is already running");
        }

        agentState.setStatus(AgentRunStatus.RUNNING);
        AgentStepState stepState = agentState.resetStepState();
        try {
            return callable.call();
        } catch (AgentInterruptedException ex) {
            agentState.setStatus(AgentRunStatus.INTERRUPTED);
            stepState.setLastException(ex);
            throw ex;
        } catch (AgentExecutionException ex) {
            agentState.setStatus(AgentRunStatus.FAILED);
            stepState.setLastException(ex);
            throw ex;
        } catch (Exception ex) {
            agentState.setStatus(AgentRunStatus.FAILED);
            AgentExecutionException exception = new AgentExecutionException("agent execution failed", ex);
            stepState.setLastException(exception);
            throw exception;
        } finally {
            if (!agentState.getStatus().isFinished()) {
                agentState.setStatus(AgentRunStatus.COMPLETED);
            }
        }
    }

    /**
     * Preprocess agent input before running.
     *
     * @param input the agent input
     * @return the preprocessed agent input
     */
    protected AgentInput<I> preprocess(AgentInput<I> input) {
        if (!initialized) {
            initialize();
        }
        return input;
    }

    @Override
    public AgentOutput<O> run(AgentInput<I> agentInput) {
        return performRun(() -> {
            AgentInput<I> input = preprocess(agentInput);
            try {
                notifyListeners(runListeners, listener -> listener.onAgentStart(this, input));

                AgentOutput<O> output = doRun(input);

                notifyListeners(runListeners, listener -> listener.onAgentOutput(this, input, output));
                return output;
            } catch (Exception ex) {
                notifyListeners(runListeners, listener -> listener.onAgentException(this, input, ex));
                throw ex;
            }
        });
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
            getAgentState().getStepState().setLastException(ex);
            notifyListeners(stepListeners, listener -> listener.onAgentStepError(this, input, ex));
            throw ex;
        }
    }

    protected abstract AgentOutput<O> doStep(AgentInput<I> input);

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
