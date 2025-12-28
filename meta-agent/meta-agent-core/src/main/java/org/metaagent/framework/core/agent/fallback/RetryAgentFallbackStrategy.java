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

package org.metaagent.framework.core.agent.fallback;

import lombok.extern.slf4j.Slf4j;
import org.metaagent.framework.core.agent.MetaAgent;
import org.metaagent.framework.core.agent.input.AgentInput;
import org.metaagent.framework.core.agent.output.AgentOutput;
import org.metaagent.framework.core.agent.state.AgentState;

/**
 * Retry Agent Fallback Strategy
 *
 * @author vyckey
 */
@Slf4j
public class RetryAgentFallbackStrategy<I, O> implements AgentFallbackStrategy<I, O> {
    private final int maxRetries;

    public RetryAgentFallbackStrategy(int maxRetries) {
        this.maxRetries = maxRetries;
    }

    @Override
    public AgentOutput<O> fallback(MetaAgent<I, O> agent, AgentInput<I> input, Exception exception) {
        AgentState agentState = agent.getAgentState();
        if (agentState.getRetryCount() < maxRetries) {
            agentState.incrRetryCount();
            return agent.step(input);
        }
        log.warn("Failed to retry agent after {} retries", maxRetries);
        return new FastFailAgentFallbackStrategy<I, O>().fallback(agent, input, exception);
    }
}
