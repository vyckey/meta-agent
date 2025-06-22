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

package org.metaagent.framework.core.agent.observability;

import org.metaagent.framework.core.agent.AgentContext;
import org.metaagent.framework.core.agent.input.AgentInput;
import org.metaagent.framework.core.agent.output.AgentOutput;
import org.metaagent.framework.core.agent.state.AgentState;

/**
 * AgentLogListener is a listener for logging agent runs and steps.
 * It implements both AgentRunListener and AgentStepListener interfaces.
 * This class can be extended to provide custom logging behavior.
 *
 * @author vyckey
 * @see AgentRunListener
 * @see AgentStepListener
 */
public class AgentLogListener implements AgentRunListener, AgentStepListener {
    private final AgentState agentState;
    private final AgentLogger agentLogger;

    public AgentLogListener(AgentState agentState, AgentLogger agentLogger) {
        this.agentState = agentState;
        this.agentLogger = agentLogger;
    }

    @Override
    public void onAgentStart(AgentContext context, AgentInput input) {
        agentLogger.debug("Agent is ready to run...");
    }

    @Override
    public void onAgentOutput(AgentContext context, AgentInput input, AgentOutput output) {
        agentLogger.debug("Agent run finished.");
    }

    @Override
    public void onAgentException(AgentContext context, AgentInput input, Exception exception) {
        agentLogger.error("Agent run exception.", exception);
    }

    @Override
    public void onAgentNextLoop(AgentContext context) {
        AgentStepListener.super.onAgentNextLoop(context);
    }

    @Override
    public void onAgentStepStart(AgentContext context, AgentInput input) {
        int turn = agentState.getLoopCount() + 1;
        agentLogger.debug("Agent is ready to execute... (Turn#{})", turn);
    }

    @Override
    public void onAgentStepFinish(AgentContext context, AgentInput input, AgentOutput output) {
        int turn = agentState.getLoopCount() + 1;
        agentLogger.debug("Agent executes finished. (Turn#{})", turn);
    }

    @Override
    public void onAgentStepError(AgentContext context, AgentInput input, Exception exception) {
        int turn = agentState.getLoopCount() + 1;
        agentLogger.error("Agent executes occurs error. (Turn#{})", turn, exception);
    }
}
