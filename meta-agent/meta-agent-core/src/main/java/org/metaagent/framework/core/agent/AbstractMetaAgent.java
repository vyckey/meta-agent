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
import org.metaagent.framework.core.agent.observability.AgentLogListener;
import org.metaagent.framework.core.agent.observability.AgentLogger;
import org.metaagent.framework.core.agent.observability.AgentRunListener;
import org.metaagent.framework.core.agent.observability.AgentStepListener;
import org.metaagent.framework.core.agent.output.AgentOutput;
import org.metaagent.framework.core.agent.profile.AgentProfile;
import org.metaagent.framework.core.agent.state.AgentRunStatus;
import org.metaagent.framework.core.agent.state.AgentState;
import org.metaagent.framework.core.agent.state.DefaultAgentState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.function.Consumer;

/**
 * Abstract {@link MetaAgent} implementation.
 *
 * @param <I> the type of agent input
 * @param <O> the type of agent output
 * @author vyckey
 */
public abstract class AbstractMetaAgent<I, O> implements MetaAgent<I, O> {

    protected String agentName;
    protected AgentProfile profile;
    protected AgentState agentState = DefaultAgentState.builder().build();
    protected Memory memory = EmptyMemory.INSTANCE;
    protected final List<AgentRunListener<I, O>> runListeners = Lists.newArrayList();
    protected final List<AgentStepListener<I, O>> stepListeners = Lists.newArrayList();
    protected AgentLogger agentLogger;
    protected Logger logger = LoggerFactory.getLogger(getClass());

    protected AbstractMetaAgent(String name) {
        this.agentName = name;
        this.agentLogger = AgentLogger.getLogger(name);
    }

    protected AbstractMetaAgent(AgentProfile profile) {
        this(profile.getName());
        this.profile = profile;
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
        // Register log listener to capture agent logs
        AgentLogListener<I, O> logListener = new AgentLogListener<>(agentState, agentLogger);
        registerRunListener(logListener);
        registerStepListener(logListener);
    }

    public void registerRunListener(AgentRunListener<I, O> listener) {
        runListeners.add(listener);
    }

    public void unregisterRunListener(AgentRunListener<I, O> listener) {
        runListeners.remove(listener);
    }

    public void registerStepListener(AgentStepListener<I, O> listener) {
        stepListeners.add(listener);
    }

    public void unregisterStepListener(AgentStepListener<I, O> listener) {
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

    public AgentFallbackStrategy<I, O> getFallbackStrategy() {
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
