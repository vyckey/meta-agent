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

import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Conversation ID
 *
 * @author vyckey
 */
@FunctionalInterface
public interface ConversationId {
    /**
     * Create a conversation ID
     *
     * @param value Conversation ID value
     * @return Conversation ID
     */
    static ConversationId of(String value) {
        return ConversationIdValue.of(value);
    }

    /**
     * Create a new conversation ID
     *
     * @return Conversation ID
     */
    static ConversationId next() {
        return ConversationIdValue.next();
    }

    /**
     * Conversation ID value
     *
     * @author vyckey
     */
    @JsonValue
    String value();
}
