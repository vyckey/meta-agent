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

import com.google.common.collect.Maps;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.metaagent.framework.common.content.MediaResource;
import org.metaagent.framework.common.converter.Converter;
import org.metaagent.framework.common.metadata.MapMetadataProvider;
import org.metaagent.framework.common.metadata.MetadataProvider;
import org.metaagent.framework.core.agent.chat.message.Message;
import org.metaagent.framework.core.agent.chat.message.MessageId;
import org.metaagent.framework.core.agent.chat.message.MessageInfo;
import org.metaagent.framework.core.agent.chat.message.part.MediaMessagePart;
import org.metaagent.framework.core.agent.chat.message.part.MessagePart;
import org.metaagent.framework.core.agent.chat.message.part.MessagePartId;
import org.metaagent.framework.core.agent.chat.message.part.TextMessagePart;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.ToolResponseMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.content.Media;

import java.net.URI;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;

import static org.metaagent.framework.core.agent.chat.message.MessageMetadataKeys.KEY_CREATED_AT;
import static org.metaagent.framework.core.agent.chat.message.MessageMetadataKeys.KEY_MESSAGE_ID;
import static org.metaagent.framework.core.agent.chat.message.MessageMetadataKeys.KEY_ROLE;
import static org.metaagent.framework.core.agent.chat.message.MessageMetadataKeys.KEY_UPDATED_AT;

/**
 * MessageConverter for converting between {@link Message} and {@link org.springframework.ai.chat.messages.Message}.
 *
 * @author vyckey
 */
public class MessageConverter implements Converter<Message, List<org.springframework.ai.chat.messages.Message>> {
    private final Supplier<MessagePartId> messagePartIdProvider;
    private final boolean cacheEnabled;
    private final Map<org.springframework.ai.chat.messages.Message, List<MessagePart>> messagePartCache;
    private final Map<Message, List<org.springframework.ai.chat.messages.Message>> springMessageCache;

    public MessageConverter(Supplier<MessagePartId> messagePartIdProvider,
                            boolean cacheEnabled) {
        this.messagePartIdProvider = Objects.requireNonNull(messagePartIdProvider, "messagePartIdProvider is required");
        this.cacheEnabled = cacheEnabled;
        this.messagePartCache = cacheEnabled ? Maps.newConcurrentMap() : null;
        this.springMessageCache = cacheEnabled ? Maps.newConcurrentMap() : null;
    }

    public MessageConverter(boolean cacheEnabled) {
        this(MessagePartId::next, cacheEnabled);
    }

    @Override
    public List<org.springframework.ai.chat.messages.Message> convert(Message message) {
        if (cacheEnabled) {
            return springMessageCache.computeIfAbsent(message, this::doConvert);
        }
        return doConvert(message);
    }

    private List<org.springframework.ai.chat.messages.Message> doConvert(Message message) {
        List<org.springframework.ai.chat.messages.Message> result = new ArrayList<>();
        List<MessagePart> groupParts = new ArrayList<>();
        for (MessagePart messagePart : message.parts()) {
            if (groupParts.isEmpty()) {
                groupParts.add(messagePart);
            } else if (messagePart instanceof ToolCallMessagePart && groupParts.get(0) instanceof ToolCallMessagePart) {
                groupParts.add(messagePart);
            } else if (messagePart instanceof MediaMessagePart && groupParts.get(0) instanceof TextMessagePart) {
                groupParts.add(messagePart);
            } else {
                result.addAll(convertGroupParts(message.info().id(), message.info().role(), groupParts));
                groupParts.clear();
                groupParts.add(messagePart);
            }
        }
        if (!groupParts.isEmpty()) {
            result.addAll(convertGroupParts(message.info().id(), message.info().role(), groupParts));
            groupParts.clear();
        }
        return result;
    }

    private Media convertMedia(MediaMessagePart part) {
        MediaResource mediaResource = part.media();
        return Media.builder()
                .mimeType(mediaResource.mimeType())
                .data(mediaResource.data())
                .build();
    }

    private List<org.springframework.ai.chat.messages.Message> convertGroupParts(
            MessageId messageId, String role, List<MessagePart> messageParts) {
        MessagePart firstMessagePart = messageParts.get(0);
        MapMetadataProvider extendMetadata = MetadataProvider.builder()
                .setProperty(KEY_MESSAGE_ID, messageId.value())
                .setProperty(KEY_CREATED_AT, firstMessagePart.createdAt().toEpochMilli())
                .setProperty(KEY_UPDATED_AT, messageParts.get(messageParts.size() - 1).updatedAt().toEpochMilli())
                .build();

        List<org.springframework.ai.chat.messages.Message> messages = new ArrayList<>();
        if (firstMessagePart instanceof TextMessagePart textMessagePart) {
            List<Media> media = messageParts.subList(1, messageParts.size()).stream()
                    .map(MediaMessagePart.class::cast).map(this::convertMedia).toList();
            if (MessageInfo.ROLE_ASSISTANT.equals(role)) {
                messages.add(org.springframework.ai.chat.messages.AssistantMessage.builder()
                        .content(textMessagePart.text())
                        .media(media)
                        .properties(textMessagePart.metadata().union(extendMetadata).getProperties())
                        .toolCalls(List.of())
                        .build());
            } else {
                messages.add(org.springframework.ai.chat.messages.UserMessage.builder()
                        .text(textMessagePart.text())
                        .metadata(textMessagePart.metadata().union(extendMetadata).getProperties())
                        .media(media)
                        .build());
            }
            return messages;
        } else if (firstMessagePart instanceof ToolCallMessagePart) {
            List<AssistantMessage.ToolCall> toolCalls = new ArrayList<>();
            List<ToolResponseMessage.ToolResponse> toolResponses = new ArrayList<>();
            MetadataProvider toolCallMetadata = MetadataProvider.create();
            MetadataProvider toolResponseMetadata = MetadataProvider.create();

            for (MessagePart messagePart : messageParts) {
                ToolCallMessagePart toolCallMessagePart = (ToolCallMessagePart) messagePart;
                if (toolCallMessagePart.arguments() != null) {
                    toolCallMetadata.merge(toolCallMessagePart.metadata());

                    toolCalls.add(new AssistantMessage.ToolCall(
                            toolCallMessagePart.callId(),
                            "",
                            toolCallMessagePart.toolName(),
                            toolCallMessagePart.arguments()
                    ));
                }
                if (toolCallMessagePart.status() != ToolCallMessagePart.ToolCallStatus.START) {
                    toolResponseMetadata.merge(toolCallMessagePart.metadata());
                    toolResponses.add(new ToolResponseMessage.ToolResponse(
                            toolCallMessagePart.callId(),
                            toolCallMessagePart.toolName(),
                            toolCallMessagePart.response()
                    ));
                }
            }

            messages.add(org.springframework.ai.chat.messages.AssistantMessage.builder()
                    .content("")
                    .properties(toolCallMetadata.getProperties())
                    .toolCalls(toolCalls)
                    .build());

            if (toolResponses.isEmpty()) {
                messages.add(org.springframework.ai.chat.messages.ToolResponseMessage.builder()
                        .responses(toolResponses)
                        .metadata(toolResponseMetadata.getProperties())
                        .build());
            }
            return messages;
        } else if (firstMessagePart instanceof LlmStartMessagePart || firstMessagePart instanceof LlmFinishMessagePart) {
            return messages;
        } else {
            throw new IllegalArgumentException("messageParts cannot be converted");
        }
    }

    private Media convertMedia(MediaResource resource) {
        return Media.builder().mimeType(resource.mimeType()).name(resource.name()).data(resource.data()).build();
    }

    public List<MessagePart> convert(List<org.springframework.ai.chat.messages.Message> messages) {
        return messages.stream().map(this::convert).flatMap(List::stream).toList();
    }

    public List<MessagePart> convert(org.springframework.ai.chat.messages.Message message) {
        if (cacheEnabled) {
            return messagePartCache.computeIfAbsent(message, this::doConvert);
        }
        return doConvert(message);
    }

    private List<MessagePart> doConvert(org.springframework.ai.chat.messages.Message message) {
        final Instant createdAt = MapUtils.getLong(message.getMetadata(), KEY_CREATED_AT) != null
                ? Instant.ofEpochMilli(MapUtils.getLong(message.getMetadata(), KEY_CREATED_AT)) : Instant.now();
        final Instant updatedAt = MapUtils.getLong(message.getMetadata(), KEY_UPDATED_AT) != null
                ? Instant.ofEpochMilli(MapUtils.getLong(message.getMetadata(), KEY_UPDATED_AT)) : Instant.now();

        List<MessagePart> parts = new ArrayList<>();
        if (message instanceof UserMessage userMessage) {
            TextMessagePart textMessagePart = TextMessagePart.builder()
                    .id(messagePartIdProvider.get())
                    .text(userMessage.getText())
                    .metadata(MetadataProvider.builder()
                            .setProperties(userMessage.getMetadata())
                            .setProperty(KEY_ROLE, MessageInfo.ROLE_USER)
                            .build()
                    )
                    .createdAt(createdAt)
                    .updatedAt(updatedAt)
                    .build();
            parts.add(textMessagePart);
            for (Media media : userMessage.getMedia()) {
                parts.add(convertToMedia(media, MetadataProvider.from(userMessage.getMetadata()), createdAt, updatedAt));
            }
        } else if (message instanceof AssistantMessage assistantMessage) {
            // Always add text content first if present
            if (StringUtils.isNotEmpty(assistantMessage.getText())) {
                TextMessagePart textMessagePart = TextMessagePart.builder()
                        .id(messagePartIdProvider.get())
                        .text(assistantMessage.getText())
                        .metadata(MetadataProvider.builder()
                                .setProperties(assistantMessage.getMetadata())
                                .setProperty(KEY_ROLE, MessageInfo.ROLE_ASSISTANT)
                                .build()
                        )
                        .createdAt(createdAt)
                        .updatedAt(updatedAt)
                        .build();
                parts.add(textMessagePart);
            }

            // Add tool calls if present
            if (assistantMessage.hasToolCalls()) {
                for (AssistantMessage.ToolCall toolCall : assistantMessage.getToolCalls()) {
                    ToolCallMessagePart toolCallMessagePart = ToolCallMessagePart.builder()
                            .id(messagePartIdProvider.get())
                            .callId(toolCall.id())
                            .toolName(toolCall.name())
                            .arguments(toolCall.arguments())
                            .status(ToolCallMessagePart.ToolCallStatus.START)
                            .metadata(MetadataProvider.from(message.getMetadata()))
                            .createdAt(createdAt)
                            .updatedAt(updatedAt)
                            .build();
                    parts.add(toolCallMessagePart);
                }
            }

            for (Media media : assistantMessage.getMedia()) {
                parts.add(convertToMedia(media, MetadataProvider.from(message.getMetadata()), createdAt, updatedAt));
            }
        } else if (message instanceof ToolResponseMessage toolResponseMessage) {
            for (ToolResponseMessage.ToolResponse toolResponse : toolResponseMessage.getResponses()) {
                ToolCallMessagePart toolCallResultMessagePart = ToolCallMessagePart.builder()
                        .id(messagePartIdProvider.get())
                        .callId(toolResponse.id())
                        .toolName(toolResponse.name())
                        .response(toolResponse.responseData())
                        .metadata(MetadataProvider.from(message.getMetadata()))
                        .createdAt(createdAt)
                        .updatedAt(updatedAt)
                        .build();
                parts.add(toolCallResultMessagePart);
            }
        } else {
            throw new IllegalArgumentException("message cannot be converted: " + message.getClass());
        }
        return parts;
    }

    private MediaMessagePart convertToMedia(Media media, MetadataProvider metadata, Instant createdAt, Instant updatedAt) {
        MediaResource resource;
        if (media.getData() instanceof URI) {
            resource = new MediaResource(media.getMimeType(), (URI) media.getData(), media.getName(), null);
        } else {
            resource = new MediaResource(media.getMimeType(), null, media.getName(), media.getData());
        }
        return MediaMessagePart.builder()
                .id(messagePartIdProvider.get())
                .media(resource)
                .metadata(metadata)
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .build();
    }
}
