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

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Sets;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.metaagent.framework.common.content.MediaResource;
import org.metaagent.framework.common.converter.Converter;
import org.metaagent.framework.common.metadata.MapMetadataProvider;
import org.metaagent.framework.common.metadata.MetadataProvider;
import org.metaagent.framework.common.util.IdGenerator;
import org.metaagent.framework.core.agent.chat.message.Message;
import org.metaagent.framework.core.agent.chat.message.MessageId;
import org.metaagent.framework.core.agent.chat.message.MessageInfo;
import org.metaagent.framework.core.agent.chat.message.part.MediaMessagePart;
import org.metaagent.framework.core.agent.chat.message.part.MessagePart;
import org.metaagent.framework.core.agent.chat.message.part.MessagePartId;
import org.metaagent.framework.core.agent.chat.message.part.TextMessagePart;
import org.metaagent.framework.core.agents.llm.message.part.LlmFinishMessagePart;
import org.metaagent.framework.core.agents.llm.message.part.LlmStartMessagePart;
import org.metaagent.framework.core.agents.llm.message.part.ReasoningMessagePart;
import org.metaagent.framework.core.agents.llm.message.part.SystemMessagePart;
import org.metaagent.framework.core.agents.llm.message.part.ToolCallMessagePart;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.ToolResponseMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.content.Media;

import java.net.URI;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.metaagent.framework.core.agent.chat.message.MessageMetadataKeys.KEY_CREATED_AT;
import static org.metaagent.framework.core.agent.chat.message.MessageMetadataKeys.KEY_MESSAGE_ID;
import static org.metaagent.framework.core.agent.chat.message.MessageMetadataKeys.KEY_REASONING_CONTENT;
import static org.metaagent.framework.core.agent.chat.message.MessageMetadataKeys.KEY_ROLE;
import static org.metaagent.framework.core.agent.chat.message.MessageMetadataKeys.KEY_TOOL_CALL_ARGS;
import static org.metaagent.framework.core.agent.chat.message.MessageMetadataKeys.KEY_TOOL_CALL_HAS_ERROR;
import static org.metaagent.framework.core.agent.chat.message.MessageMetadataKeys.KEY_UPDATED_AT;

/**
 * MessageConverter for converting between {@link Message} and {@link org.springframework.ai.chat.messages.Message}.
 *
 * @author vyckey
 */
public class MessageConverter implements Converter<Message, List<org.springframework.ai.chat.messages.Message>> {

    @Override
    public List<org.springframework.ai.chat.messages.Message> convert(Message message) {
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
        if (firstMessagePart instanceof SystemMessagePart systemMessagePart) {
            messages.add(SystemMessage.builder()
                    .text(systemMessagePart.text())
                    .metadata(systemMessagePart.metadata().union(extendMetadata).getProperties())
                    .build());
        } else if (firstMessagePart instanceof ReasoningMessagePart) {
            for (MessagePart messagePart : messageParts) {
                if (messagePart instanceof ReasoningMessagePart reasoningPart) {
                    Map<String, Object> properties = ImmutableMap.<String, Object>builder()
                            .putAll(reasoningPart.metadata().getProperties())
                            .putAll(extendMetadata.getProperties())
                            .put(KEY_REASONING_CONTENT, reasoningPart.text())
                            .build();

                    messages.add(org.springframework.ai.chat.messages.AssistantMessage.builder()
                            .content("")
                            .media(List.of())
                            .toolCalls(List.of())
                            .properties(properties)
                            .build());
                }
            }
        } else if (firstMessagePart instanceof TextMessagePart textMessagePart) {
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
        } else if (firstMessagePart instanceof ToolCallMessagePart) {
            List<AssistantMessage.ToolCall> toolCalls = new ArrayList<>();
            List<ToolResponseMessage.ToolResponse> toolResponses = new ArrayList<>();
            MetadataProvider toolCallMetadata = MetadataProvider.create();
            MetadataProvider toolResponseMetadata = MetadataProvider.create();

            Set<String> addedToolCallIds = Sets.newHashSet();
            for (MessagePart messagePart : messageParts) {
                ToolCallMessagePart toolCallMessagePart = (ToolCallMessagePart) messagePart;
                if (toolCallMessagePart.arguments() != null && !addedToolCallIds.contains(toolCallMessagePart.callId())) {
                    toolCallMetadata.merge(toolCallMessagePart.metadata());

                    toolCalls.add(new AssistantMessage.ToolCall(
                            toolCallMessagePart.callId(),
                            "",
                            toolCallMessagePart.toolName(),
                            toolCallMessagePart.arguments()
                    ));
                    addedToolCallIds.add(toolCallMessagePart.callId());
                }
                if (toolCallMessagePart.status() != ToolCallMessagePart.ToolCallStatus.START) {
                    toolResponseMetadata.merge(toolCallMessagePart.metadata());
                    toolResponseMetadata.setProperty(KEY_TOOL_CALL_ARGS, toolCallMessagePart.arguments());
                    if (toolCallMessagePart.status() == ToolCallMessagePart.ToolCallStatus.ERROR) {
                        toolResponseMetadata.setProperty(KEY_TOOL_CALL_HAS_ERROR, true);
                    }
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

            if (!toolResponses.isEmpty()) {
                messages.add(org.springframework.ai.chat.messages.ToolResponseMessage.builder()
                        .responses(toolResponses)
                        .metadata(toolResponseMetadata.getProperties())
                        .build());
            }
        } else if (firstMessagePart instanceof LlmStartMessagePart || firstMessagePart instanceof LlmFinishMessagePart) {
            return messages;
        } else {
            throw new IllegalArgumentException("messageParts cannot be converted");
        }
        return messages;
    }

    private Media convertMedia(MediaResource resource) {
        return Media.builder().mimeType(resource.mimeType()).name(resource.name()).data(resource.data()).build();
    }

    public List<MessagePart> convert(IdGenerator<MessagePartId> partIdGenerator,
                                     List<org.springframework.ai.chat.messages.Message> messages,
                                     Map<String, Object> extendMetadata) {
        return messages.stream()
                .map(message -> convert(partIdGenerator, messages, extendMetadata))
                .flatMap(List::stream).toList();
    }

    public List<MessagePart> convert(IdGenerator<MessagePartId> partIdGenerator,
                                     org.springframework.ai.chat.messages.Message message,
                                     Map<String, Object> extendMetadata) {
        final Instant createdAt = MapUtils.getLong(message.getMetadata(), KEY_CREATED_AT) != null
                ? Instant.ofEpochMilli(MapUtils.getLong(message.getMetadata(), KEY_CREATED_AT)) : Instant.now();
        final Instant updatedAt = MapUtils.getLong(message.getMetadata(), KEY_UPDATED_AT) != null
                ? Instant.ofEpochMilli(MapUtils.getLong(message.getMetadata(), KEY_UPDATED_AT)) : Instant.now();

        MapMetadataProvider.Builder metadataBuilder = MetadataProvider.builder()
                .setProperties(message.getMetadata())
                .setProperties(extendMetadata);

        List<MessagePart> parts = new ArrayList<>();
        if (message instanceof UserMessage userMessage) {
            TextMessagePart textMessagePart = TextMessagePart.builder()
                    .id(partIdGenerator.nextId())
                    .text(userMessage.getText())
                    .metadata(metadataBuilder.setProperty(KEY_ROLE, MessageInfo.ROLE_USER).build())
                    .createdAt(createdAt)
                    .updatedAt(updatedAt)
                    .build();
            parts.add(textMessagePart);
            for (Media media : userMessage.getMedia()) {
                MediaMessagePart mediaMessagePart = MediaMessagePart.builder()
                        .id(partIdGenerator.nextId())
                        .media(toMediaResource(media))
                        .metadata(metadataBuilder.setProperty(KEY_ROLE, MessageInfo.ROLE_USER).build())
                        .createdAt(createdAt)
                        .updatedAt(updatedAt)
                        .build();
                parts.add(mediaMessagePart);
            }
        } else if (message instanceof AssistantMessage assistantMessage) {
            // Always add text content first if present
            if (StringUtils.isNotEmpty(assistantMessage.getText())) {
                TextMessagePart textMessagePart = TextMessagePart.builder()
                        .id(partIdGenerator.nextId())
                        .text(assistantMessage.getText())
                        .metadata(metadataBuilder.setProperty(KEY_ROLE, MessageInfo.ROLE_ASSISTANT).build())
                        .createdAt(createdAt)
                        .updatedAt(updatedAt)
                        .build();
                parts.add(textMessagePart);
            }

            // Add tool calls if present
            if (assistantMessage.hasToolCalls()) {
                for (AssistantMessage.ToolCall toolCall : assistantMessage.getToolCalls()) {
                    ToolCallMessagePart toolCallMessagePart = ToolCallMessagePart.builder()
                            .id(partIdGenerator.nextId())
                            .callId(toolCall.id())
                            .toolName(toolCall.name())
                            .arguments(toolCall.arguments())
                            .status(ToolCallMessagePart.ToolCallStatus.START)
                            .metadata(metadataBuilder.setProperty(KEY_ROLE, MessageInfo.ROLE_ASSISTANT).build())
                            .createdAt(createdAt)
                            .updatedAt(updatedAt)
                            .build();
                    parts.add(toolCallMessagePart);
                }
            }

            for (Media media : assistantMessage.getMedia()) {
                MediaMessagePart mediaMessagePart = MediaMessagePart.builder()
                        .id(partIdGenerator.nextId())
                        .media(toMediaResource(media))
                        .metadata(metadataBuilder.setProperty(KEY_ROLE, MessageInfo.ROLE_ASSISTANT).build())
                        .createdAt(createdAt)
                        .updatedAt(updatedAt)
                        .build();
                parts.add(mediaMessagePart);
            }
        } else if (message instanceof ToolResponseMessage toolResponseMessage) {
            for (ToolResponseMessage.ToolResponse toolResponse : toolResponseMessage.getResponses()) {
                ToolCallMessagePart toolCallResultMessagePart = ToolCallMessagePart.builder()
                        .id(partIdGenerator.nextId())
                        .callId(toolResponse.id())
                        .toolName(toolResponse.name())
                        .arguments(MapUtils.getString(message.getMetadata(), KEY_TOOL_CALL_ARGS, ""))
                        .response(toolResponse.responseData())
                        .status(MapUtils.getBoolean(message.getMetadata(), KEY_TOOL_CALL_HAS_ERROR, false)
                                ? ToolCallMessagePart.ToolCallStatus.ERROR : ToolCallMessagePart.ToolCallStatus.END)
                        .metadata(metadataBuilder.build())
                        .createdAt(createdAt)
                        .updatedAt(updatedAt)
                        .build();
                parts.add(toolCallResultMessagePart);
            }
        } else if (message instanceof SystemMessage systemMessage) {
            SystemMessagePart systemMessagePart = SystemMessagePart.builder()
                    .id(partIdGenerator.nextId())
                    .text(systemMessage.getText())
                    .metadata(metadataBuilder.build())
                    .createdAt(createdAt)
                    .build();
            parts.add(systemMessagePart);
        } else {
            throw new IllegalArgumentException("message cannot be converted: " + message.getClass());
        }
        return parts;
    }

    private MediaResource toMediaResource(Media media) {
        MediaResource resource;
        if (media.getData() instanceof URI) {
            resource = new MediaResource(media.getMimeType(), (URI) media.getData(), media.getName(), null);
        } else {
            resource = new MediaResource(media.getMimeType(), null, media.getName(), media.getData());
        }
        return resource;
    }
}
