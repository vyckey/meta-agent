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

import com.google.common.collect.Lists;
import org.metaagent.framework.core.agent.chat.message.Message;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

import static org.metaagent.framework.core.agent.chat.conversation.DefaultConversation.TIME_FORMATTER;

/**
 * DefaultMessageTurn is a default implementation of {@link MessageTurn}.
 *
 * @author vyckey
 */
public class DefaultMessageTurn implements MessageTurn {
    private final List<Message> messages;
    private boolean finished;

    public DefaultMessageTurn(List<Message> messages, boolean finished) {
        this.messages = Objects.requireNonNull(messages, "messages is required");
        this.finished = finished;
    }

    public DefaultMessageTurn() {
        this(Lists.newArrayList(), false);
    }

    public String turnId() {
        return messages.isEmpty() ? String.valueOf(hashCode()) : messages.get(0).getId().value();
    }

    @Override
    public List<Message> messages() {
        return Collections.unmodifiableList(messages);
    }

    @Override
    public void appendMessage(Message message) {
        messages.add(message);
    }

    @Override
    public boolean isFinished() {
        return finished;
    }

    @Override
    public void setFinished(boolean finished) {
        this.finished = finished;
    }

    @Override
    public Iterator<Message> iterator() {
        return messages.iterator();
    }

    public String asText(int maxMessageSize, int maxMessageLength) {
        final int splitter = 80;
        StringBuilder sb = new StringBuilder("Turn ").append(turnId()).append(" (Finished=").append(finished).append("):\n");
        sb.append("=".repeat(splitter)).append('\n');

        List<Message> messageList = messages();
        if (messageList.isEmpty()) {
            sb.append("<empty messages>\n");
        } else {
            List<Message> outputMessages = messageList.subList(0, Math.min(maxMessageSize, messageList.size()));
            appendMessages(sb, outputMessages, maxMessageLength);

            boolean hasMoreMessages = maxMessageSize < messageList.size();
            if (hasMoreMessages) {
                sb.append("... (hidden messages)\n");
            }
        }
        sb.append("=".repeat(splitter)).append('\n');
        return sb.toString();
    }

    private void appendMessages(StringBuilder sb, List<Message> messages, int maxMessageLength) {
        for (Message message : messages) {
            String content = message.getContent();
            if (content.length() > maxMessageLength) {
                content = content.substring(0, maxMessageLength) + "...(truncated)";
            }
            ZonedDateTime createdTime = message.getCreatedAt().atZone(ZoneId.systemDefault());
            sb.append("[").append(createdTime.format(TIME_FORMATTER)).append("] ");
            sb.append(message.getRole()).append(": ").append(content);
            sb.append("\n");
        }
    }

    @Override
    public String toString() {
        return asText(10, 1000);
    }
}
