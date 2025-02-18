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

package org.metaagent.framework.core.agent.agents;

import com.google.common.collect.Lists;
import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.ImmutableConfiguration;
import org.metaagent.framework.core.agent.AgentExecutionContext;
import org.metaagent.framework.core.agent.state.AgentState;
import org.metaagent.framework.core.agent.MetaAgent;
import org.metaagent.framework.core.agent.fallback.AgentFallbackStrategy;
import org.metaagent.framework.core.agent.fallback.FastFailAgentFallbackStrategy;
import org.metaagent.framework.core.agent.listener.AgentExecutionListener;
import org.metaagent.framework.core.agent.listener.AgentRunListener;
import org.metaagent.framework.core.agent.memory.EmptyMemory;
import org.metaagent.framework.core.agent.memory.Memory;
import org.metaagent.framework.core.agent.output.AgentOutput;
import org.slf4j.Logger;

import java.util.List;
import java.util.function.Consumer;

/**
 * description is here
 *
 * @author vyckey
 */
public abstract class AbstractMetaAgent implements MetaAgent {
    protected final String name;
    protected Configuration configuration;
    protected Memory memory = EmptyMemory.EMPTY_MEMORY;
    protected final List<AgentRunListener> runListeners = Lists.newArrayList();
    protected final List<AgentExecutionListener> executionListeners = Lists.newArrayList();
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

    public void registerRunListener(AgentRunListener listener) {
        runListeners.add(listener);
    }

    public void unregisterRunListener(AgentRunListener listener) {
        runListeners.remove(listener);
    }

    public void registerExecutionListener(AgentExecutionListener listener) {
        executionListeners.add(listener);
    }

    public void unregisterExecutionListener(AgentExecutionListener listener) {
        executionListeners.remove(listener);
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

    @Override
    public AgentFallbackStrategy getAgentFallbackStrategy() {
        return FastFailAgentFallbackStrategy.INSTANCE;
    }

    @Override
    public AgentOutput run(AgentExecutionContext context) {
        AgentState agentState = context.getAgentState();
        if (agentState.getStatus().isFinished()) {
            agentLogger.info("Agent {} has been run, will exit.", getName());
            return agentState.getAgentOutput();
        }

        agentLogger.info("Agent {} is ready to run...", getName());
        notifyListeners(runListeners, listener -> listener.beforeAgentRun(context));
        AgentOutput output = MetaAgent.super.run(context);
        notifyListeners(runListeners, listener -> listener.postAgentRun(context));
        agentLogger.info("Agent {} run finished.", getName());
        return output;
    }

    @Override
    public AgentOutput execute(AgentExecutionContext context) {
        int turn = context.getAgentState().getLoopCount() + 1;
        agentLogger.info("Agent {} is ready to execute... (Turn#{})", getName(), turn);
        notifyListeners(executionListeners, listener -> listener.beforeAgentExecute(context));
        AgentOutput output = doExecute(context);
        notifyListeners(executionListeners, listener -> listener.postAgentExecute(context));
        agentLogger.info("Agent {} executes finished. (Turn#{})", getName(), turn);
        return output;
    }

    protected abstract AgentOutput doExecute(AgentExecutionContext context);

    @Override
    public String toString() {
        return getName();
    }
}
