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

import org.metaagent.framework.common.abort.AbortException;
import org.metaagent.framework.core.agent.context.AgentStepContext;
import org.metaagent.framework.core.agent.exception.AgentExecutionException;
import org.metaagent.framework.core.agent.exception.AgentInterruptedException;
import org.metaagent.framework.core.agent.fallback.AgentFallbackStrategy;
import org.metaagent.framework.core.agent.input.AgentInput;
import org.metaagent.framework.core.agent.output.AgentOutput;

/**
 * The core agent abstraction.
 *
 * @param <I> the type of agent input
 * @param <O> the type of agent output
 * @author vyckey
 */
public interface Agent<I extends AgentInput, O extends AgentOutput, C extends AgentStepContext> extends MetaAgent<I, O> {

    /**
     * Gets agent fallback strategy. It will be used to handle unexpected exceptions while running the agent.
     *
     * @return the fallback strategy.
     */
    AgentFallbackStrategy<Agent<I, O, C>, I, O> getFallbackStrategy();

    /**
     * Determines whether the agent should continue looping.
     *
     * @param input       the input
     * @param output      the output which can be null
     * @param stepContext the agent step context
     * @return true if the agent should continue looping, false otherwise
     */
    boolean shouldContinueLoop(I input, O output, C stepContext);

    /**
     * The agent will execute a loop step until the agent should exit.
     *
     * @param agentInput the agent input.
     * @return the final agent output.
     */
    @Override
    default O run(I agentInput) {
        O agentOutput = null;
        try {
            C stepContext = createStepContext(agentInput);
            while (shouldContinueLoop(agentInput, agentOutput, stepContext)) {
                try {
                    agentOutput = step(agentInput, stepContext);
                } catch (Exception ex) {
                    agentOutput = getFallbackStrategy().fallback(this, agentInput, ex);
                } finally {
                    stepContext.getLoopCounter().incrementAndGet();
                }
            }
        } catch (AbortException ex) {
            throw new AgentInterruptedException("agent is interrupted", ex);
        } catch (AgentExecutionException | AgentInterruptedException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new AgentExecutionException("agent execution failed", ex);
        }
        return agentOutput;
    }

    /**
     * Creates a new step context for the agent execution.
     * This is an internal method and should not be called by users.
     * Subclasses must implement this to provide their specific context.
     *
     * @param agentInput the agent input
     * @return a new step context instance
     */
    private C createStepContext(I agentInput) {
        throw new UnsupportedOperationException("Not implemented");
    }

    /**
     * Performs an agent step execution.
     *
     * @param agentInput  the agent input.
     * @param stepContext the agent step context.
     * @return the agent output.
     */
    O step(I agentInput, C stepContext);

}
