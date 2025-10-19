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

import com.google.common.collect.Lists;
import org.metaagent.framework.core.agent.chat.message.Message;

import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Predicate;

/**
 * Default implementation of the {@link Conversation} interface.
 *
 * @author vyckey
 */
public class DefaultConversation implements Conversation {
    protected final String conversationId;
    protected final List<Message> messages;

    public DefaultConversation(String conversationId, List<Message> messages) {
        this.conversationId = Objects.requireNonNull(conversationId, "historyId is required");
        this.messages = Objects.requireNonNull(messages, "messages is required");
    }

    public DefaultConversation(String conversationId) {
        this(conversationId, Lists.newArrayList());
    }

    public DefaultConversation() {
        this(generateHistoryId());
    }

    public static String generateHistoryId() {
        return UUID.randomUUID().toString().replaceAll("-", "").substring(0, 16);
    }

    @Override
    public String id() {
        return conversationId;
    }

    @Override
    public boolean isEmpty() {
        return messages.isEmpty();
    }

    @Override
    public void appendMessage(Message message) {
        messages.add(message);
    }

    @Override
    public List<Message> findMessages(Predicate<Message> predicate, boolean reverse) {
        Iterable<Message> iterable = reverse ? reverse() : this;
        List<Message> result = Lists.newArrayList();
        for (Message message : iterable) {
            if (predicate.test(message)) {
                result.add(message);
            }
        }
        return result;
    }

    @Override
    public Optional<Message> findMessage(Predicate<Message> predicate, boolean reverse) {
        Iterable<Message> iterable = reverse ? reverse() : this;
        for (Message message : iterable) {
            if (predicate.test(message)) {
                return Optional.of(message);
            }
        }
        return Optional.empty();
    }

    @Override
    public Optional<Message> lastMessage() {
        if (messages.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(messages.get(messages.size() - 1));
    }

    @Override
    public List<Message> lastMessages(int count) {
        int returnCount = Math.min(count, messages.size());
        return messages.subList(messages.size() - returnCount, messages.size());
    }

    @Override
    public void clear() {
        this.messages.clear();
    }

    @Override
    public Iterator<Message> iterator() {
        return Lists.reverse(messages).iterator();
    }

    public String asText(int maxMessageSize, int maxMessageLength) {
        StringBuilder sb = new StringBuilder()
                .append("Message history (").append(conversationId).append("):\n");
        boolean hasMoreMessages = false;
        List<Message> lastMessages = Lists.newArrayList();
        for (Message message : reverse()) {
            if (lastMessages.size() >= maxMessageSize) {
                hasMoreMessages = true;
                break;
            }
            lastMessages.add(message);
        }
        if (hasMoreMessages) {
            sb.append("... (hidden messages)\n");
        }

        if (lastMessages.isEmpty()) {
            sb.append("<empty messages>\n");
        } else {
            appendMessages(sb, Lists.reverse(lastMessages), maxMessageLength);
        }
        return sb.toString();
    }

    private void appendMessages(StringBuilder sb, List<Message> messages, int maxMessageLength) {
        for (Message message : messages) {
            String content = message.getContent();
            if (content.length() > maxMessageLength) {
                content = content.substring(0, maxMessageLength) + "...(truncated)";
            }
            sb.append(message.getRole()).append(": ").append(content);
            sb.append("\n");
        }
    }

    public Iterable<Message> reverse() {
        return messages;
    }

    @Override
    public String toString() {
        return asText(10, 1000);
    }
}
