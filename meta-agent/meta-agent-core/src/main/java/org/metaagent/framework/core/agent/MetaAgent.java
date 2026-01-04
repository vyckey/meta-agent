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

import org.apache.commons.lang3.NotImplementedException;
import org.metaagent.framework.core.agent.converter.AgentIOConverter;
import org.metaagent.framework.core.agent.input.AgentInput;
import org.metaagent.framework.core.agent.memory.Memory;
import org.metaagent.framework.core.agent.output.AgentOutput;
import org.metaagent.framework.core.agent.profile.AgentProfile;
import org.metaagent.framework.core.agent.state.AgentState;

import java.util.concurrent.CompletableFuture;

/**
 * Meta Agent, the basic class of all agents.
 *
 * @param <I> the type of agent input
 * @param <O> the type of agent output
 * @author vyckey
 */
public interface MetaAgent<I, O> extends AutoCloseable {

    /**
     * Gets agent name.
     *
     * @return the agent name.
     */
    default String getName() {
        return getAgentProfile().getName();
    }

    /**
     * Gets agent profile.
     *
     * @return the agent profile.
     */
    AgentProfile getAgentProfile();

    /**
     * Gets agent state.
     *
     * @return the agent state.
     */
    AgentState getAgentState();

    /**
     * Gets agent memory.
     *
     * @return the agent memory.
     */
    Memory getMemory();

    /**
     * Gets Input/Output converter.
     *
     * @return the converter.
     */
    default AgentIOConverter<I, O> getIOConverter() {
        throw new NotImplementedException("not implement yet");
    }

    /**
     * Creates a new default execution context.
     *
     * @return the new default execution context.
     */
    default AgentExecutionContext newExecutionContext() {
        return AgentExecutionContext.create();
    }

    /**
     * Runs agent logic.
     *
     * @param input the agent input.
     * @return the agent output.
     */
    default AgentOutput<O> run(AgentInput<I> input) {
        return step(input);
    }

    /**
     * Runs agent with input.
     *
     * @param input the agent input
     * @return the agent output
     */
    default AgentOutput<O> run(I input) {
        return run(AgentInput.builder(input).context(newExecutionContext()).build());
    }

    /**
     * Runs agent synchronously.
     *
     * @param input the agent input
     * @return the agent out.
     */
    default CompletableFuture<AgentOutput<O>> runAsync(AgentInput<I> input) {
        return CompletableFuture.supplyAsync(() -> run(input), input.context().getExecutor());
    }

    /**
     * Runs agent synchronously.
     *
     * @param input the agent input
     * @return the agent out
     */
    default CompletableFuture<AgentOutput<O>> runAsync(I input) {
        return runAsync(AgentInput.builder(input).context(newExecutionContext()).build());
    }

    /**
     * Start an agent step.
     *
     * @param input the agent input.
     * @return the agent output.
     */
    AgentOutput<O> step(AgentInput<I> input);

    /**
     * Reset the agent to initial state.
     */
    void reset();
}
