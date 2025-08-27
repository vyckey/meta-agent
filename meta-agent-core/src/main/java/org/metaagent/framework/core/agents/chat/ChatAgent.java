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

package org.metaagent.framework.core.agents.chat;

import org.metaagent.framework.core.agent.Agent;
import org.metaagent.framework.core.agent.AgentExecutionContext;
import org.metaagent.framework.core.agent.chat.message.Message;
import org.metaagent.framework.core.agent.chat.message.RoleMessage;
import org.metaagent.framework.core.agent.chat.message.history.MessageHistory;
import org.metaagent.framework.core.agent.input.AgentInput;
import org.metaagent.framework.core.agent.output.AgentOutput;
import reactor.core.publisher.Flux;

/**
 * ChatAgent is an agent that has an ability to chat with message history.
 *
 * @author vyckey
 */
public interface ChatAgent extends Agent<ChatAgentInput, ChatAgentOutput, ChatAgentStreamOutput> {
    /**
     * Get the message history of this agent.
     *
     * @return the message history of this agent
     */
    MessageHistory getMessageHistory();

    /**
     * Run the agent with the given input.
     *
     * @param input the input to run the agent with
     * @return the output of the agent
     */
    @Override
    default AgentOutput<ChatAgentOutput> run(String input) {
        return run(RoleMessage.user(input));
    }

    /**
     * Run chat agent with a message.
     *
     * @param message the message to run this agent with
     * @return the output of this agent
     */
    default AgentOutput<ChatAgentOutput> run(Message message) {
        AgentInput<ChatAgentInput> agentInput = AgentInput
                .builder(new ChatAgentInput(message))
                .context(AgentExecutionContext.create())
                .build();
        return run(agentInput);
    }

    /**
     * Runs chat streaming agent with a message.
     *
     * @param message the message to run this agent with
     * @return the output of this agent
     */
    default Flux<AgentOutput<ChatAgentStreamOutput>> runStream(Message message) {
        AgentInput<ChatAgentInput> agentInput = AgentInput
                .builder(new ChatAgentInput(message))
                .context(AgentExecutionContext.create())
                .build();
        return runStream(agentInput);
    }
}
