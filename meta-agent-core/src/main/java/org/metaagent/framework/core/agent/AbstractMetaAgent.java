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
import org.metaagent.framework.core.agent.ability.AgentAbilityManager;
import org.metaagent.framework.core.agent.ability.DefaultAgentAbilityManager;
import org.metaagent.framework.core.agent.fallback.AgentFallbackStrategy;
import org.metaagent.framework.core.agent.fallback.FastFailAgentFallbackStrategy;
import org.metaagent.framework.core.agent.memory.EmptyMemory;
import org.metaagent.framework.core.agent.memory.Memory;
import org.metaagent.framework.core.agent.observability.AgentLogListener;
import org.metaagent.framework.core.agent.observability.AgentLogger;
import org.metaagent.framework.core.agent.observability.AgentRunListener;
import org.metaagent.framework.core.agent.observability.AgentStepListener;
import org.metaagent.framework.core.agent.profile.AgentProfile;
import org.metaagent.framework.core.agent.profile.DefaultAgentProfile;
import org.metaagent.framework.core.agent.state.AgentRunStatus;
import org.metaagent.framework.core.agent.state.AgentState;
import org.metaagent.framework.core.agent.state.DefaultAgentState;
import org.metaagent.framework.core.tool.executor.ToolExecutorContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.function.Consumer;

/**
 * Abstract {@link MetaAgent} implementation.
 *
 * @author vyckey
 */
public abstract class AbstractMetaAgent<
        AgentInput extends org.metaagent.framework.core.agent.input.AgentInput,
        AgentOutput extends org.metaagent.framework.core.agent.output.AgentOutput>
        implements MetaAgent<AgentInput, AgentOutput> {

    protected AgentProfile profile;
    protected AgentState agentState = DefaultAgentState.builder().build();
    protected Memory memory = EmptyMemory.EMPTY_MEMORY;
    protected AgentAbilityManager abilityManager = new DefaultAgentAbilityManager();
    protected final List<AgentRunListener<AgentInput, AgentOutput>> runListeners = Lists.newArrayList();
    protected final List<AgentStepListener<AgentInput, AgentOutput>> stepListeners = Lists.newArrayList();
    protected AgentLogger agentLogger;
    protected Logger logger = LoggerFactory.getLogger(getClass());

    protected AbstractMetaAgent(String name) {
        this.profile = new DefaultAgentProfile(name);
        this.agentLogger = AgentLogger.getLogger(name);
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
        AgentLogListener<AgentInput, AgentOutput> logListener = new AgentLogListener<>(agentState, agentLogger);
        registerRunListener(logListener);
        registerStepListener(logListener);
    }

    public void registerRunListener(AgentRunListener<AgentInput, AgentOutput> listener) {
        runListeners.add(listener);
    }

    public void unregisterRunListener(AgentRunListener<AgentInput, AgentOutput> listener) {
        runListeners.remove(listener);
    }

    public void registerStepListener(AgentStepListener<AgentInput, AgentOutput> listener) {
        stepListeners.add(listener);
    }

    public void unregisterStepListener(AgentStepListener<AgentInput, AgentOutput> listener) {
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

    public AgentFallbackStrategy<AgentInput, AgentOutput> getFallbackStrategy() {
        return new FastFailAgentFallbackStrategy<>();
    }

    /**
     * This method is called before the agent runs. It also checks the agent state and the agent input.
     *
     * @param input the agent input
     */
    protected void beforeRun(AgentInput input) {
        if (agentState.getStatus().isFinished()) {
            throw new AgentExecutionException("Agent " + getName() + " has run finished. Please reset it before running again.");
        }
    }

    @Override
    public AgentOutput run(AgentInput input) {
        beforeRun(input);

        agentState.setStatus(AgentRunStatus.RUNNING);
        try {
            notifyListeners(runListeners, listener -> listener.onAgentStart(this, input));
            AgentOutput output = doRun(input);
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

    protected AgentOutput doRun(AgentInput input) {
        return MetaAgent.super.run(input);
    }

    @Override
    public AgentOutput step(AgentInput input) {
        try {
            notifyListeners(stepListeners, listener -> listener.onAgentStepStart(this, input));
            AgentOutput output = doStep(input);
            notifyListeners(stepListeners, listener -> listener.onAgentStepFinish(this, input, output));
            return output;
        } catch (Exception ex) {
            agentState.setLastException(ex);
            notifyListeners(stepListeners, listener -> listener.onAgentStepError(this, input, ex));
            throw ex;
        }
    }

    protected abstract AgentOutput doStep(AgentInput input);

    protected ToolExecutorContext buildToolExecutorContext(AgentInput input) {
        AgentExecutionContext context = input.context();
        return ToolExecutorContext.builder()
                .toolManager(context.getToolManager())
                .build();
    }

    @Override
    public void reset() {
        this.agentState.reset();
        this.memory.clearAll();
    }

    @Override
    public String toString() {
        return getName();
    }
}
