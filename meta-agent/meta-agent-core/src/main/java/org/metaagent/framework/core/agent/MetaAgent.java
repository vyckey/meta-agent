/*
 * MIT License
 *
 * Copyright (c) 2026 MetaAgent
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

import org.metaagent.framework.core.agent.exception.AgentExecutionException;
import org.metaagent.framework.core.agent.exception.AgentInterruptedException;
import org.metaagent.framework.core.agent.input.AgentInput;
import org.metaagent.framework.core.agent.output.AgentOutput;
import org.metaagent.framework.core.agent.profile.AgentProfile;

import java.util.concurrent.CompletableFuture;

/**
 * Meta Agent, the basic class of all agents.
 *
 * @param <I> the type of agent input
 * @param <O> the type of agent output
 * @author vyckey
 */
public interface MetaAgent<I extends AgentInput, O extends AgentOutput> extends AutoCloseable {
    /**
     * Gets agent name.
     *
     * @return the agent name.
     */
    default String name() {
        return profile().getName();
    }

    /**
     * Gets agent profile.
     *
     * @return the agent profile.
     */
    AgentProfile profile();

    /**
     * Runs agent with input.
     * <p>
     * The default implementation is as follows:
     * <pre>
     * public O run(I input) {
     *     try {
     *         return step(input);
     *     } catch (AbortException ex) {
     *         throw new AgentInterruptedException("agent is interrupted", ex);
     *     } catch (AgentExecutionException | AgentInterruptedException ex) {
     *         throw ex;
     *     } catch (Exception ex) {
     *         throw new AgentExecutionException("agent execution failed", ex);
     *     }
     * }
     * </pre>
     *
     * @param agentInput the agent input
     * @return the agent output
     * @throws AgentExecutionException   if agent execution failed
     * @throws AgentInterruptedException if agent is interrupted
     */
    O run(I agentInput);

    /**
     * Runs agent synchronously.
     *
     * @param agentInput the agent input
     * @return the agent out
     */
    default CompletableFuture<O> runAsync(I agentInput) {
        return CompletableFuture.supplyAsync(() -> run(agentInput), agentInput.context().getExecutor());
    }

    /**
     * Reset the agent to initial state.
     */
    void reset();

    /**
     * Cleans up resources and stop the agent.
     */
    @Override
    void close();
}
