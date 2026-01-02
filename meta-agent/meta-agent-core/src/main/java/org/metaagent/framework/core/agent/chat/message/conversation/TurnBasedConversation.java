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
 * A conversation that is based on turns.
 *
 * @author vyckey
 */
public interface TurnBasedConversation extends Conversation {
    /**
     * Returns an iterable of turns.
     * When {@code reverse} is true, the turns will be returned in reverse time order
     * which is useful for iterating from the most recent turn to the oldest.
     *
     * @return an iterable of turns in reverse order
     */
    Iterable<MessageTurn> turns(boolean reverse);

    /**
     * Returns the last turn in the conversation.
     *
     * @return an Optional containing the last turn, or empty if the conversation is empty
     */
    MessageTurn lastTurn();

    /**
     * Returns the last 'count' turns from the conversation.
     *
     * @param count the number of turns to retrieve
     * @return a list of the last 'count' turns
     */
    List<MessageTurn> lastTurns(int count);

    /**
     * Creates a new turn.
     *
     * @return a new turn
     */
    MessageTurn newTurn();

    /**
     * Appends a turn to the conversation.
     *
     * @param turn the turn to append
     */
    void appendTurn(MessageTurn turn);

    /**
     * Appends a message to the conversation.
     * If there are no turns in the conversation, a new turn is created.
     * If the last turn is finished, a new turn is created.
     * Otherwise, the message is appended to the last turn.
     *
     * @param message the message to append
     */
    @Override
    default void appendMessage(Message message) {
        MessageTurn lastTurn = lastTurn();
        if (lastTurn == null || lastTurn.isFinished()) {
            newTurn().appendMessage(message);
        } else {
            lastTurn.appendMessage(message);
        }
    }
}
