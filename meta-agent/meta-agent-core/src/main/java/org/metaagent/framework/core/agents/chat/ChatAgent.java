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

import org.metaagent.framework.core.agent.StreamingAgent;
import org.metaagent.framework.core.agent.chat.conversation.Conversation;
import org.metaagent.framework.core.agent.chat.message.Message;
import org.metaagent.framework.core.agent.chat.message.RoleMessage;
import org.metaagent.framework.core.agent.output.AgentOutput;
import org.metaagent.framework.core.agent.output.AgentStreamOutput;
import org.metaagent.framework.core.agents.chat.input.ChatInput;
import org.metaagent.framework.core.agents.chat.output.ChatOutput;
import org.metaagent.framework.core.agents.chat.output.ChatStreamOutput;

/**
 * ChatAgent is an agent that has an ability to chat with message history.
 *
 * @author vyckey
 */
public interface ChatAgent<I extends ChatInput, O extends ChatOutput, S extends ChatStreamOutput>
        extends StreamingAgent<I, O, S> {
    String PROPERTY_CONTEXT_COMPRESSION_RATIO = "contextCompressionRatio";
    String PROPERTY_COMPRESSION_RESERVED_MESSAGES_COUNT = "compressionReservedMessageCount";

    /**
     * Get the conversation of this agent.
     *
     * @return the conversation of this agent
     */
    Conversation getConversation();

    /**
     * Run the agent with the given input.
     *
     * @param input the input to run the agent with
     * @return the output of the agent
     */
    @Override
    default AgentOutput<O> run(String input) {
        return run(RoleMessage.user(input));
    }

    /**
     * Run chat agent with messages.
     *
     * @param messages the messages to run this agent with
     * @return the output of this agent
     */
    AgentOutput<O> run(Message... messages);

    /**
     * Run the agent with the given input.
     *
     * @param input the input to run the agent with
     * @return the output of the agent
     */
    default AgentStreamOutput<O, S> runStream(String input) {
        return runStream(RoleMessage.user(input));
    }

    /**
     * Run chat agent with messages.
     *
     * @param messages the messages to run this agent with
     * @return the output of this agent
     */
    AgentStreamOutput<O, S> runStream(Message... messages);

}
