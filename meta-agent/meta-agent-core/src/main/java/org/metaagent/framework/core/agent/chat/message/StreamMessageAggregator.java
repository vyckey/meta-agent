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
import org.apache.commons.lang3.StringUtils;
import org.metaagent.framework.common.metadata.MetadataProvider;
import org.metaagent.framework.core.agent.chat.message.part.MessagePart;
import org.metaagent.framework.core.agent.chat.message.part.TextMessagePart;
import org.metaagent.framework.core.agent.chat.message.part.ToolCallMessagePart;
import org.metaagent.framework.core.agent.chat.message.part.ToolResponseMessagePart;

import java.util.List;

/**
 * Utility class to aggregate stream messages into complete messages.
 *
 * @author vyckey
 */
public class StreamMessageAggregator {
    public static final StreamMessageAggregator INSTANCE = new StreamMessageAggregator();

    public List<MessagePart> aggregate(Iterable<MessagePart> streamMessages) {
        List<List<MessagePart>> streamMessageGroup = Lists.newArrayList();
        for (MessagePart streamMessage : streamMessages) {
            boolean appendToLastGroup = false;
            if (!streamMessageGroup.isEmpty()) {
                List<MessagePart> lastGroup = streamMessageGroup.get(streamMessageGroup.size() - 1);
                MessagePart lastMessagePart = lastGroup.get(lastGroup.size() - 1);
                if (lastMessagePart.getClass().equals(streamMessage.getClass())) {
                    if (streamMessage instanceof TextMessagePart) {
                        appendToLastGroup = true;
                    } else if (streamMessage instanceof ToolCallMessagePart toolCallMessagePart
                            && ((ToolCallMessagePart) lastMessagePart).toolCall().name().equals(toolCallMessagePart.toolCall().name())) {
                        appendToLastGroup = true;
                    } else if (streamMessage instanceof ToolResponseMessagePart toolRespMessagePart
                            && ((ToolResponseMessagePart) lastMessagePart).toolResponse().name().equals(toolRespMessagePart.toolResponse().name())) {
                        appendToLastGroup = true;
                    }
                }
            }
            if (appendToLastGroup) {
                streamMessageGroup.get(streamMessageGroup.size() - 1).add(streamMessage);
            } else {
                streamMessageGroup.add(Lists.newArrayList(streamMessage));
            }
        }

        List<MessagePart> aggregatedMessages = Lists.newArrayList();
        for (List<MessagePart> messageParts : streamMessageGroup) {
            aggregatedMessages.add(aggregate(messageParts));
        }
        return aggregatedMessages;
    }

    protected MessagePart aggregate(List<MessagePart> messageParts) {
        if (messageParts.isEmpty()) {
            throw new IllegalStateException("No message part to aggregate");
        }
        if (messageParts.size() == 1) {
            return messageParts.get(0);
        }

        final MessagePart firstMessagePart = messageParts.get(0);
        final MessagePart lastMessagePart = messageParts.get(messageParts.size() - 1);
        MetadataProvider metadata = MetadataProvider.builder()
                .setProperties(firstMessagePart.metadata().getProperties()).build();
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
        } else if (firstMessagePart instanceof ToolCallMessagePart toolCallMessagePart) {
            StringBuilder arguments = new StringBuilder();
            for (MessagePart messagePart : messageParts) {
                ToolCallMessagePart toolCallPart = (ToolCallMessagePart) messagePart;
                String args = toolCallPart.toolCall().arguments();
                arguments.append(StringUtils.isEmpty(args) ? "" : args);
            }

            ToolCallMessagePart.ToolCall toolCall = new ToolCallMessagePart.ToolCall(
                    toolCallMessagePart.toolCall().id(),
                    toolCallMessagePart.toolCall().type(),
                    toolCallMessagePart.toolCall().name(),
                    arguments.toString()
            );
            builder = ToolCallMessagePart.builder()
                    .toolCall(toolCall);
        } else if (firstMessagePart instanceof ToolResponseMessagePart toolResponseMessagePart) {
            StringBuilder responseData = new StringBuilder();
            for (MessagePart messagePart : messageParts) {
                ToolResponseMessagePart toolRespPart = (ToolResponseMessagePart) messagePart;
                String respData = toolRespPart.toolResponse().responseData();
                responseData.append(StringUtils.isEmpty(respData) ? "" : respData);
            }

            ToolResponseMessagePart.ToolResponse toolResponse = new ToolResponseMessagePart.ToolResponse(
                    toolResponseMessagePart.toolResponse().id(),
                    toolResponseMessagePart.toolResponse().name(),
                    responseData.toString()
            );
            builder = ToolResponseMessagePart.builder()
                    .toolResponse(toolResponse);
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
