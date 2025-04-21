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

import com.google.common.collect.Lists;
import org.metaagent.framework.core.agent.Agent;
import org.metaagent.framework.core.agent.AgentExecutionContext;
import org.metaagent.framework.core.agent.chat.message.Message;
import org.metaagent.framework.core.agent.chat.message.TextMessage;
import org.metaagent.framework.core.agent.chat.message.history.MessageHistory;
import org.metaagent.framework.core.agent.input.AgentInput;
import org.metaagent.framework.core.agent.input.message.AgentMessageInput;
import org.metaagent.framework.core.agent.output.AgentOutput;
import org.metaagent.framework.core.agent.output.message.AgentMessageOutput;

/**
 * description is here
 *
 * @author vyckey
 */
public interface ChatAgent extends Agent {
    MessageHistory getMessageHistory();

    default AgentOutput run(String input) {
        return run(createContext(), new TextMessage(input));
    }

    @Override
    default AgentOutput run(AgentExecutionContext context, String input) {
        return run(context, new TextMessage(input));
    }

    default AgentMessageOutput run(AgentExecutionContext context, Message message) {
        MessageHistory messageHistory = getMessageHistory();
        messageHistory.appendMessage(message);

        AgentMessageInput messageInput = AgentMessageInput.with(Lists.newArrayList(messageHistory));
        return run(context, messageInput);
    }

    default AgentMessageOutput run(AgentExecutionContext context, AgentMessageInput input) {
        return (AgentMessageOutput) run(context, (AgentInput) input);
    }

}
