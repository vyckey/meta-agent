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

package org.metaagent.framework.core.agent.observability;

import org.metaagent.framework.core.agent.MetaAgent;
import org.metaagent.framework.core.agent.input.AgentInput;
import org.metaagent.framework.core.agent.observability.event.AgentEvent;
import org.metaagent.framework.core.agent.observability.event.AgentRunEndEvent;
import org.metaagent.framework.core.agent.observability.event.AgentRunStartEvent;
import org.metaagent.framework.core.agent.output.AgentOutput;

import java.util.Objects;

/**
 * AgentRunEventPublisher is an implementation of {@link AgentRunListener} that publishes events to
 * an {@link AgentEventBus} when an agent starts running, completes running, or encounters an exception.
 *
 * @param <I> The type of input accepted by the agent.
 * @param <O> The type of output produced by the agent.
 * @author vyckey
 */
public class AgentRunEventPublisher<I, O> implements AgentRunListener<I, O> {
    private final AgentEventBus<AgentEvent> agentEventBus;

    public AgentRunEventPublisher(AgentEventBus<AgentEvent> agentEventBus) {
        this.agentEventBus = Objects.requireNonNull(agentEventBus, "agentEventBus is required");
    }

    @Override
    public void onAgentStart(MetaAgent<I, O> agent, AgentInput<I> input) {
        agentEventBus.publish(new AgentRunStartEvent<>(agent, input));
    }

    @Override
    public void onAgentOutput(MetaAgent<I, O> agent, AgentInput<I> input, AgentOutput<O> output) {
        agentEventBus.publish(AgentRunEndEvent.completed(agent, input, output));
    }

    @Override
    public void onAgentException(MetaAgent<I, O> agent, AgentInput<I> input, Exception exception) {
        agentEventBus.publish(AgentRunEndEvent.failed(agent, input, exception));
    }
}
