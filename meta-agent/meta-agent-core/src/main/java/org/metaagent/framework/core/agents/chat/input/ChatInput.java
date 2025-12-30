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

package org.metaagent.framework.core.agents.chat.input;

import org.metaagent.framework.core.agent.chat.message.Message;
import org.metaagent.framework.core.agents.chat.ChatAgent;

import java.util.List;

/**
 * {@link ChatAgent} input
 *
 * @author vyckey
 */
public interface ChatInput {
    /**
     * Get the messages of this input.
     *
     * @return the messages of this input
     */
    List<Message> messages();

    /**
     * Check whether the agent is thinking enabled. It's determined by the agent's implementation if not present.
     *
     * @return true if the agent is thinking enabled, false otherwise
     */
    Boolean isThinkingEnabled();

    /**
     * Check whether the agent is streaming enabled.  It's determined by the agent's implementation if not present.
     *
     * @return true if the agent is streaming enabled, false otherwise
     */
    Boolean isStreamingEnabled();
}
