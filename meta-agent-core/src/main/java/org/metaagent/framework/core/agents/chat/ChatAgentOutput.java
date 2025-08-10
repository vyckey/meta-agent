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

import org.metaagent.framework.core.agent.chat.message.Message;
import org.metaagent.framework.core.agent.output.AgentOutput;

import java.util.List;

/**
 * {@link ChatAgent} output interface.
 *
 * @author vyckey
 */
public interface ChatAgentOutput extends AgentOutput {
    /**
     * Builder for {@link ChatAgentOutput}.
     *
     * @return a builder
     */
    static DefaultChatAgentOutput.Builder builder() {
        return DefaultChatAgentOutput.builder();
    }

    /**
     * Gets output messages.
     *
     * @return output messages
     */
    List<Message> messages();

    /**
     * Gets the first output message.
     *
     * @return the first output message
     */
    default Message message() {
        return messages().isEmpty() ? null : messages().get(0);
    }

    /**
     * Gets the thought process.
     *
     * @return the thought process
     */
    String thoughtProcess();

}
