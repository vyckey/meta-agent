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
import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.ImmutableConfiguration;
import org.metaagent.framework.core.agent.ability.AgentAbilityManager;
import org.metaagent.framework.core.agent.ability.DefaultAgentAbilityManager;
import org.metaagent.framework.core.agent.fallback.AgentFallbackStrategy;
import org.metaagent.framework.core.agent.fallback.FastFailAgentFallbackStrategy;
import org.metaagent.framework.core.agent.input.AgentInput;
import org.metaagent.framework.core.agent.memory.EmptyMemory;
import org.metaagent.framework.core.agent.memory.Memory;
import org.metaagent.framework.core.agent.observability.AgentRunListener;
import org.metaagent.framework.core.agent.observability.AgentStepListener;
import org.metaagent.framework.core.agent.output.AgentOutput;
import org.metaagent.framework.core.agent.state.AgentState;
import org.slf4j.Logger;

import java.util.List;
import java.util.function.Consumer;

/**
 * Abstract {@link MetaAgent} implementation.
 *
 * @author vyckey
 */
public abstract class AbstractMetaAgent implements MetaAgent {
    protected final String name;
    protected Configuration configuration;
    protected Memory memory = EmptyMemory.EMPTY_MEMORY;
    protected AgentAbilityManager abilityManager = new DefaultAgentAbilityManager();
    protected final List<AgentRunListener> runListeners = Lists.newArrayList();
    protected final List<AgentStepListener> stepListeners = Lists.newArrayList();
    protected Logger agentLogger;
    protected Logger logger;

    protected AbstractMetaAgent(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public ImmutableConfiguration getConfiguration() {
        return configuration;
    }

    @Override
    public Memory getMemory() {
        return memory;
    }

    @Override
    public AgentAbilityManager getAbilityManager() {
        return abilityManager;
    }

    public void registerRunListener(AgentRunListener listener) {
        runListeners.add(listener);
    }

    public void unregisterRunListener(AgentRunListener listener) {
        runListeners.remove(listener);
    }

    public void registerStepListener(AgentStepListener listener) {
        stepListeners.add(listener);
    }

    public void unregisterStepListener(AgentStepListener listener) {
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

    public AgentFallbackStrategy getFallbackStrategy() {
        return FastFailAgentFallbackStrategy.INSTANCE;
    }

    protected void checkAgentInput(AgentInput input) {
    }

    @Override
    public AgentOutput run(AgentExecutionContext context, AgentInput input) {
        checkAgentInput(input);

        AgentState agentState = context.getAgentState();
        if (agentState.getStatus().isFinished()) {
            agentLogger.info("Agent {} has been run, will exit.", getName());
            return agentState.getAgentOutput();
        }

        try {
            agentLogger.debug("Agent {} is ready to run...", getName());
            notifyListeners(runListeners, listener -> listener.onAgentStart(context, input));
            AgentOutput output = doRun(context, input);
            agentLogger.debug("Agent {} run finished.", getName());
            notifyListeners(runListeners, listener -> listener.onAgentOutput(context, input, output));
            return output;
        } catch (AgentExecutionException ex) {
            agentLogger.error("Agent {} run exception.", getName(), ex);
            notifyListeners(runListeners, listener -> listener.onAgentException(context, input, ex));
            throw ex;
        } catch (Exception ex) {
            agentLogger.error("Agent {} run exception.", getName(), ex);
            notifyListeners(runListeners, listener -> listener.onAgentException(context, input, ex));
            throw new AgentExecutionException(ex);
        }
    }

    protected AgentOutput doRun(AgentExecutionContext context, AgentInput input) {
        return MetaAgent.super.run(context, input);
    }

    @Override
    public AgentOutput step(AgentExecutionContext context, AgentInput input) {
        int turn = context.getAgentState().getLoopCount() + 1;
        try {
            agentLogger.debug("Agent {} is ready to execute... (Turn#{})", getName(), turn);
            notifyListeners(stepListeners, listener -> listener.onAgentStepStart(context, input));
            AgentOutput output = doStep(context, input);
            agentLogger.debug("Agent {} executes finished. (Turn#{})", getName(), turn);
            notifyListeners(stepListeners, listener -> listener.onAgentStepFinish(context, input, output));
            return output;
        } catch (Exception ex) {
            agentLogger.error("Agent {} executes occurs error. (Turn#{})", getName(), turn, ex);
            notifyListeners(stepListeners, listener -> listener.onAgentStepError(context, input, ex));
            throw ex;
        }
    }

    protected abstract AgentOutput doStep(AgentExecutionContext context, AgentInput input);

    @Override
    public String toString() {
        return getName();
    }
}
