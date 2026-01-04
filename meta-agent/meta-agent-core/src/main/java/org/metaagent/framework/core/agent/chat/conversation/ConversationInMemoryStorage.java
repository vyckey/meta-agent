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

package org.metaagent.framework.core.agent.chat.conversation;

import org.metaagent.framework.core.agent.chat.message.Message;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory implementation of ConversationStorage.
 *
 * @author vyckey
 */
public class ConversationInMemoryStorage implements ConversationStorage {
    public static final ConversationInMemoryStorage INSTANCE = new ConversationInMemoryStorage();
    private final Map<String, Conversation> conversations = new ConcurrentHashMap<>();

    private ConversationInMemoryStorage() {
    }

    @Override
    public void store(Conversation conversation) {
        conversations.put(conversation.id(), conversation);
    }

    @Override
    public void load(Conversation conversation) {
        Conversation stored = conversations.get(conversation.id());
        if (stored != null && conversation != stored) {
            if (stored instanceof TurnBasedConversation storedConversation
                    && conversation instanceof TurnBasedConversation targetConversation) {
                for (MessageTurn messageTurn : storedConversation.turns(false)) {
                    targetConversation.appendTurn(messageTurn);
                }
            } else {
                for (Message message : conversation) {
                    conversation.appendMessage(message);
                }
            }
        }
    }

    @Override
    public void clear(String conversationId) {
        conversations.remove(conversationId);
    }

    @Override
    public void close() {
        conversations.clear();
    }
}
