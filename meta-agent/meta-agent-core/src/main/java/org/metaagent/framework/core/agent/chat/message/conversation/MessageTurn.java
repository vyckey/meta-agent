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

package org.metaagent.framework.core.agent.chat.message.conversation;

import org.metaagent.framework.core.agent.chat.message.Message;

import java.util.List;

/**
 * Interface representing a turn in a conversation.
 * A turn is a sequence of messages exchanged between two different roles.
 *
 * @author vyckey
 */
public interface MessageTurn extends Iterable<Message> {
    /**
     * Returns the messages in this turn.
     *
     * @return a list of messages
     */
    List<Message> messages();

    /**
     * Returns the last message in this turn.
     *
     * @return the last message, or null if there are no messages
     */
    default Message lastMessage() {
        List<Message> messages = messages();
        return messages.isEmpty() ? null : messages.get(messages.size() - 1);
    }

    /**
     * Appends a message to this turn.
     *
     * @param message the message to append
     */
    void appendMessage(Message message);

    /**
     * Returns whether this turn is finished.
     *
     * @return true if the turn is finished, false otherwise
     */
    boolean isFinished();

    /**
     * Sets whether this turn is finished.
     *
     * @param finished true if the turn is finished, false otherwise
     */
    void setFinished(boolean finished);
}
