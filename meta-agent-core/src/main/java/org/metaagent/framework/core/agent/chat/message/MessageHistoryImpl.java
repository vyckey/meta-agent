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

package org.metaagent.framework.core.agent.chat.message;

import com.google.common.collect.Lists;

import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;

/**
 * description is here
 *
 * @author vyckey
 */
public class MessageHistoryImpl implements MessageHistory {
    protected final List<Message> messages;

    public MessageHistoryImpl(List<Message> messages) {
        this.messages = Objects.requireNonNull(messages);
    }

    public MessageHistoryImpl() {
        this.messages = Lists.newArrayList();
    }

    @Override
    public boolean isEmpty() {
        return messages.isEmpty();
    }

    @Override
    public Iterable<Message> reverse() {
        return Lists.reverse(messages);
    }

    @Override
    public void appendMessage(Message message) {
        messages.add(message);
    }

    @Override
    public List<Message> findMessages(Predicate<Message> predicate, boolean reverse) {
        if (reverse) {
            return Lists.reverse(messages).stream().filter(predicate).toList();
        }
        return messages.stream().filter(predicate).toList();
    }

    @Override
    public Optional<Message> findMessage(Predicate<Message> predicate, boolean reverse) {
        if (reverse) {
            for (Message message : reverse()) {
                if (predicate.test(message)) {
                    return Optional.of(message);
                }
            }
            return Optional.empty();
        }
        return messages.stream().filter(predicate).findFirst();
    }

    @Override
    public void clear() {
        this.messages.clear();
    }

    @Override
    public Iterator<Message> iterator() {
        return messages.iterator();
    }

    protected String toText(int maxMessageSize) {
        StringBuilder sb = new StringBuilder();
        int i = 0;
        for (Message message : reverse()) {
            if (++i > maxMessageSize) {
                sb.insert(0, "...\n");
                break;
            }
            sb.insert(0, message.getSender() + ": " + message.getContent() + "\n");
        }
        sb.insert(0, "Message history:\n");
        return sb.toString();
    }

    @Override
    public String toString() {
        return toText(10);
    }
}
