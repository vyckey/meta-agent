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

package org.metaagent.framework.core.agent.chat.message.conversation;

import org.metaagent.framework.core.agent.chat.message.Message;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

/**
 * Interface representing a conversation of messages in a chat session.
 * The last message in the conversation is considered the most recent one.
 *
 * @author vyckey
 */
public interface Conversation extends Iterable<Message> {

    /**
     * Returns the unique identifier for this message conversation.
     *
     * @return the conversation ID
     */
    String id();

    /**
     * Returns whether this message conversation is empty.
     *
     * @return true if there are no messages in the conversation, false otherwise
     */
    boolean isEmpty();

    /**
     * Appends a message to the conversation.
     *
     * @param message the message to append
     */
    void appendMessage(Message message);

    /**
     * Finds all messages that match the given predicate.
     *
     * @param predicate the predicate to match messages against
     * @param reverse   if true, search in reverse order
     * @return a list of messages that match the predicate
     */
    List<Message> findMessages(Predicate<Message> predicate, boolean reverse);

    /**
     * Finds the first message that matches the given predicate.
     *
     * @param predicate the predicate to match the message against
     * @param reverse   if true, search in reverse order
     * @return an Optional containing the first matching message, or empty if no match is found
     */
    Optional<Message> findMessage(Predicate<Message> predicate, boolean reverse);

    /**
     * Returns the most recent message in the conversation.
     *
     * @return an Optional containing the most recent message, or empty if the conversation is empty
     */
    Optional<Message> lastMessage();

    /**
     * Returns the last 'count' messages from the conversation.
     *
     * @param count the number of messages to retrieve
     * @return a list of the last 'count' messages
     */
    List<Message> lastMessages(int count);

    /**
     * Clears all messages from the conversation.
     * This operation is irreversible and will remove all messages.
     */
    void clear();

    /**
     * Returns an iterable of messages in reverse order.
     * This is useful for iterating from the most recent message to the oldest.
     *
     * @return an iterable of messages in reverse order
     */
    Iterable<Message> reverse();
}
