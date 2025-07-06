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
import org.metaagent.framework.core.agent.fallback.AgentFallbackStrategy;
import org.metaagent.framework.core.agent.loop.AgentLoopControlStrategy;
import org.metaagent.framework.core.agent.state.AgentRunStatus;
import org.metaagent.framework.core.agent.state.AgentState;

/**
 * The core agent abstraction.
 *
 * @author vyckey
 */
public interface Agent<
        AgentInput extends org.metaagent.framework.core.agent.input.AgentInput,
        AgentOutput extends org.metaagent.framework.core.agent.output.AgentOutput>
        extends MetaAgent<AgentInput, AgentOutput> {

    /**
     * Gets Input/Output converter.
     *
     * @return the converter.
     */
    default AgentIOConverter<AgentInput, AgentOutput> getIOConverter() {
        throw new NotImplementedException("not implement yet");
    }

    /**
     * Gets loop control strategy which controls if the agent will continue to do next loop.
     *
     * @return the loop control strategy.
     */
    AgentLoopControlStrategy<AgentInput, AgentOutput> getLoopControlStrategy();

    /**
     * Gets agent fallback strategy. It will be used to handle unexpected exceptions while running the agent.
     *
     * @return the fallback strategy.
     */
    @Override
    AgentFallbackStrategy<AgentInput, AgentOutput> getFallbackStrategy();

    /**
     * Run with execution context and string input.
     * It will invoke the method {@link #run(AgentInput)}.
     *
     * @param input the agent input.
     * @return the final agent output.
     */
    default AgentOutput run(String input) {
        throw new IllegalArgumentException("string agent input is unsupported");
    }

    /**
     * The agent will execute a loop step until the agent should exit.
     *
     * @param input the agent input.
     * @return the final agent output.
     */
    @Override
    default AgentOutput run(AgentInput input) {
        AgentState agentState = getAgentState();
        AgentOutput output = null;
        while (getLoopControlStrategy().shouldContinueLoop(this, input)) {
            try {
                agentState.setStatus(AgentRunStatus.RUNNING);
                output = step(input);
            } catch (Exception ex) {
                agentState.setLastException(ex);
                output = getFallbackStrategy().fallback(this, input, ex);
            } finally {
                agentState.incrLoopCount();
            }
        }
        if (!agentState.getStatus().isFinished()) {
            agentState.setStatus(AgentRunStatus.COMPLETED);
        }
        return output;
    }

    /**
     * Start an agent step.
     *
     * @param input the agent input.
     * @return the agent output.
     */
    @Override
    AgentOutput step(AgentInput input);

}
