/*
 * MIT License
 *
 * Copyright (c) 2026 MetaAgent
 *
 * Permission is hereby granted, free of charge, to any person obtaining MetaAgent<I,O> copy
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
 * FITNESS FOR MetaAgent<I,O> PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package org.metaagent.framework.core.agent.listener;

import org.metaagent.framework.core.agent.AgentLogger;
import org.metaagent.framework.core.agent.MetaAgent;
import org.metaagent.framework.core.agent.context.AgentStepContext;
import org.metaagent.framework.core.agent.input.AgentInput;
import org.metaagent.framework.core.agent.output.AgentOutput;

import java.util.Objects;

/**
 * AgentLogListener is MetaAgent<I,O> listener for logging agent runs and steps.
 * It implements both AgentRunListener and AgentStepListener interfaces.
 * This class can be extended to provide custom logging behavior.
 *
 * @author vyckey
 * @see AgentRunListener
 * @see AgentStepListener
 */
public class AgentLogListener<I extends AgentInput, O extends AgentOutput, C extends AgentStepContext>
        implements AgentRunListener<I, O>, AgentStepListener<I, O, C> {
    private final AgentLogger agentLogger;

    public AgentLogListener(AgentLogger agentLogger) {
        this.agentLogger = Objects.requireNonNull(agentLogger, "agentLogger is required");
    }

    @Override
    public void onAgentStart(MetaAgent<I, O> agent, I input) {
        agentLogger.debug("Agent is ready to run...");
    }

    @Override
    public void onAgentOutput(MetaAgent<I, O> agent, I input, O output) {
        agentLogger.debug("Agent run finished.");
    }

    @Override
    public void onAgentException(MetaAgent<I, O> agent, I input, Exception exception) {
        agentLogger.error("Agent run exception.", exception);
    }

    @Override
    public void onAgentStepStart(MetaAgent<I, O> agent, I input, C stepContext) {
        int step = stepContext.getLoopCounter().get() + 1;
        agentLogger.debug("Agent is ready to execute... (Step#{})", step);
    }

    @Override
    public void onAgentStepFinish(MetaAgent<I, O> agent, I input, O output, C stepContext) {
        int step = stepContext.getLoopCounter().get() + 1;
        agentLogger.debug("Agent executes finished. (Step#{})", step);
    }

    @Override
    public void onAgentStepError(MetaAgent<I, O> agent, I input, Exception exception, C stepContext) {
        int step = stepContext.getLoopCounter().get() + 1;
        agentLogger.error("Agent executes occurs error. (Step#{})", step, exception);
    }
}
