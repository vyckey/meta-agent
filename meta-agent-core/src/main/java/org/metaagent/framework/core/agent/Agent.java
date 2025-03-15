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

import org.metaagent.framework.core.agent.fallback.AgentFallbackStrategy;
import org.metaagent.framework.core.agent.input.AgentInput;
import org.metaagent.framework.core.agent.loop.AgentLoopControlStrategy;
import org.metaagent.framework.core.agent.output.AgentOutput;
import org.metaagent.framework.core.agent.state.AgentRunStatus;
import org.metaagent.framework.core.agent.state.AgentState;

/**
 * The core agent abstraction.
 *
 * @author vyckey
 */
public interface Agent extends MetaAgent {

    /**
     * Gets loop control strategy which controls if the agent will continue to do next loop.
     *
     * @return the loop control strategy.
     */
    AgentLoopControlStrategy getLoopControlStrategy();

    /**
     * Gets agent fallback strategy. It will be used to handle unexpected exceptions while running the agent.
     *
     * @return the fallback strategy.
     */
    @Override
    AgentFallbackStrategy getFallbackStrategy();

    /**
     * The agent will execute a loop step until the agent should exit.
     *
     * @param context the agent execution context.
     * @param input   the agent input.
     * @return the final agent output.
     */
    @Override
    default AgentOutput run(AgentExecutionContext context, AgentInput input) {
        AgentState agentState = context.getAgentState();
        if (agentState.getStatus().isFinished()) {
            return agentState.getAgentOutput();
        }

        agentState.setStatus(AgentRunStatus.RUNNING);
        while (getLoopControlStrategy().shouldContinueLoop(context, input)) {
            AgentOutput output = null;
            try {
                output = step(context, input);
            } catch (Exception ex) {
                output = getFallbackStrategy().fallback(this, context, input, ex);
            } finally {
                agentState.incrLoopCount();
                agentState.setAgentOutput(output);
            }
        }
        agentState.setStatus(AgentRunStatus.COMPLETED);
        return agentState.getAgentOutput();
    }

    /**
     * Start an agent step.
     *
     * @param context the agent execution context.
     * @param input   the agent input.
     * @return the agent output.
     */
    @Override
    AgentOutput step(AgentExecutionContext context, AgentInput input);
}
