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

package org.metaagent.framework.core.agents.llm.message;

import com.google.common.collect.Lists;
import org.apache.commons.lang3.StringUtils;
import org.metaagent.framework.common.metadata.MetadataProvider;
import org.metaagent.framework.core.agent.chat.message.part.MessagePart;
import org.metaagent.framework.core.agent.chat.message.part.TextMessagePart;
import org.metaagent.framework.core.agent.output.aggregator.StreamMessageAggregator;
import org.metaagent.framework.core.agents.llm.message.part.LlmFinishMessagePart;
import org.metaagent.framework.core.agents.llm.message.part.LlmStartMessagePart;
import org.metaagent.framework.core.agents.llm.message.part.ReasoningMessagePart;
import org.metaagent.framework.core.agents.llm.message.part.ToolCallMessagePart;

import java.util.List;

/**
 * Utility class to aggregate stream messages into complete messages.
 *
 * @author vyckey
 */
public class LlmStreamMessageAggregator implements StreamMessageAggregator {
    public static final LlmStreamMessageAggregator INSTANCE = new LlmStreamMessageAggregator();

    @Override
    public List<MessagePart> aggregate(Iterable<MessagePart> streamOutputs) {
        List<MessagePart> outputMessages = Lists.newArrayList();

        List<MessagePart> pendingMessages = Lists.newArrayList();
        for (MessagePart message : streamOutputs) {
            if (canAggregateWith(message, pendingMessages)) {
                pendingMessages.add(message);
            } else {
                outputMessages.add(aggregateMessages(pendingMessages));
                pendingMessages.clear();
                pendingMessages.add(message);
            }
        }
        if (!pendingMessages.isEmpty()) {
            outputMessages.add(aggregateMessages(pendingMessages));
        }
        return outputMessages;
    }

    @Override
    public boolean canAggregateWith(MessagePart message, List<MessagePart> pendingMessages) {
        if (pendingMessages.isEmpty()) {
            return true;
        }

        if (message instanceof LlmStartMessagePart || message instanceof LlmFinishMessagePart) {
            return false;
        }

        MessagePart lastMessagePart = pendingMessages.get(pendingMessages.size() - 1);
        if (lastMessagePart instanceof TextMessagePart) {
            return message instanceof TextMessagePart;
        } else if (lastMessagePart instanceof ReasoningMessagePart) {
            return message instanceof ReasoningMessagePart;
        } else if (lastMessagePart instanceof ToolCallMessagePart lastToolCallMessagePart) {
            if (message instanceof ToolCallMessagePart toolCallMessagePart) {
                return lastToolCallMessagePart.callId().equals(toolCallMessagePart.callId());
            }
        }
        return false;
    }

    protected MessagePart aggregateMessages(List<MessagePart> messageParts) {
        if (messageParts.isEmpty()) {
            throw new IllegalArgumentException("messageParts cannot be empty");
        }
        if (messageParts.size() == 1) {
            return messageParts.get(0);
        }

        final MessagePart firstMessagePart = messageParts.get(0);
        final MessagePart lastMessagePart = messageParts.get(messageParts.size() - 1);
        MetadataProvider metadata = firstMessagePart.metadata().copy().toMutable();
        for (int i = 1; i < messageParts.size(); i++) {
            metadata.merge(messageParts.get(i).metadata());
        }

        MessagePart.Builder builder;
        if (firstMessagePart instanceof TextMessagePart) {
            StringBuilder sb = new StringBuilder();
            for (MessagePart messagePart : messageParts) {
                TextMessagePart textMessagePart = (TextMessagePart) messagePart;
                sb.append(textMessagePart.text());
            }
            builder = TextMessagePart.builder().text(sb.toString());
        } else if (firstMessagePart instanceof ReasoningMessagePart) {
            StringBuilder sb = new StringBuilder();
            ReasoningMessagePart.ReasoningStatus status = null;
            for (MessagePart messagePart : messageParts) {
                ReasoningMessagePart reasoningMessagePart = (ReasoningMessagePart) messagePart;
                status = reasoningMessagePart.status();

                sb.append(reasoningMessagePart.text());
            }
            builder = ReasoningMessagePart.builder().text(sb.toString()).status(status);
        } else if (firstMessagePart instanceof ToolCallMessagePart firstToolCallMessagePart) {
            ToolCallMessagePart.Builder toolCallBuilder = firstToolCallMessagePart.toBuilder();

            for (int i = 1; i < messageParts.size(); i++) {
                ToolCallMessagePart toolCallPart = (ToolCallMessagePart) messageParts.get(i);
                toolCallBuilder.status(toolCallPart.status());
                if (StringUtils.isNotEmpty(toolCallPart.arguments())) {
                    toolCallBuilder.arguments(toolCallPart.arguments());
                }
                if (StringUtils.isNotEmpty(toolCallPart.response())) {
                    toolCallBuilder.response(toolCallPart.response());
                }
            }
            builder = toolCallBuilder;
        } else {
            throw new IllegalStateException("Not support to aggregate message part type: "
                    + firstMessagePart.getClass().getName());
        }
        return builder
                .id(firstMessagePart.id())
                .createdAt(firstMessagePart.createdAt())
                .updatedAt(lastMessagePart.updatedAt())
                .metadata(metadata)
                .build();
    }
}
