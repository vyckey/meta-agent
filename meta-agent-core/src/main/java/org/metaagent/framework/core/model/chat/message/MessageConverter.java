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

package org.metaagent.framework.core.model.chat.message;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import org.metaagent.framework.common.content.MediaResource;
import org.metaagent.framework.common.converter.BiConverter;
import org.metaagent.framework.common.metadata.MapMetadataProvider;
import org.metaagent.framework.core.agent.chat.message.Message;
import org.metaagent.framework.core.agent.chat.message.RoleMessage;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.content.Media;

import java.net.URI;
import java.util.List;
import java.util.Optional;

/**
 * description is here
 *
 * @author vyckey
 */
public class MessageConverter implements BiConverter<Message, org.springframework.ai.chat.messages.Message> {
    public static final MessageConverter INSTANCE = new MessageConverter();

    private final boolean cacheEnabled;
    private final BiMap<Message, org.springframework.ai.chat.messages.Message> cache;


    public MessageConverter(boolean cacheEnabled) {
        this.cacheEnabled = cacheEnabled;
        if (cacheEnabled) {
            this.cache = HashBiMap.create();
        } else {
            this.cache = null;
        }
    }

    public MessageConverter() {
        this(false);
    }

    @Override
    public org.springframework.ai.chat.messages.Message convert(Message message) {
        if (cacheEnabled) {
            return cache.computeIfAbsent(message, this::convertTo);
        } else {
            return convertTo(message);
        }
    }

    public org.springframework.ai.chat.messages.Message convertTo(Message message) {
        if (message instanceof RoleMessage roleMessage) {
            List<Media> media = roleMessage.getMedia().stream().map(this::convertMedia).toList();
            if (roleMessage.getRole().equals(RoleMessage.ROLE_ASSISTANT)) {
                return new org.springframework.ai.chat.messages.AssistantMessage(
                        message.getContent(), message.getMetadata().getProperties(), List.of(), media
                );
            }
            return org.springframework.ai.chat.messages.UserMessage.builder()
                    .text(message.getContent())
                    .metadata(message.getMetadata().getProperties())
                    .media(media)
                    .build();
        } else if (message instanceof SystemMessage systemMessage) {
            return new org.springframework.ai.chat.messages.SystemMessage(
                    systemMessage.getContent()
            );
        } else if (message instanceof ToolCallMessage toolCallMessage) {
            List<Media> media = toolCallMessage.getMedia().stream().map(this::convertMedia).toList();
            List<AssistantMessage.ToolCall> toolCalls = toolCallMessage.getToolCalls().stream()
                    .map(call -> new AssistantMessage.ToolCall(call.id(), call.type(), call.name(), call.arguments()))
                    .toList();
            return new org.springframework.ai.chat.messages.AssistantMessage(
                    message.getContent(), message.getMetadata().getProperties(), toolCalls, media
            );
        } else if (message instanceof ToolResponseMessage responseMessage) {
            return new org.springframework.ai.chat.messages.ToolResponseMessage(
                    responseMessage.getToolResponses().stream().map(toolResponse ->
                            new org.springframework.ai.chat.messages.ToolResponseMessage.ToolResponse(
                                    toolResponse.id(), toolResponse.name(), toolResponse.responseData())
                    ).toList(),
                    responseMessage.getMetadata().getProperties()
            );
        } else {
            throw new IllegalArgumentException("message cannot be converted: " + message.getClass().getName());
        }
    }

    protected Media convertMedia(MediaResource resource) {
        return Media.builder().mimeType(resource.mimeType()).name(resource.name()).data(resource.data()).build();
    }

    @Override
    public Message reverse(org.springframework.ai.chat.messages.Message message) {
        if (cacheEnabled) {
            return cache.inverse().computeIfAbsent(message, this::convertTo);
        } else {
            return convertTo(message);
        }
    }

    public Message convertTo(org.springframework.ai.chat.messages.Message message) {
        switch (message.getMessageType()) {
            case USER -> {
                org.springframework.ai.chat.messages.UserMessage userMessage =
                        (org.springframework.ai.chat.messages.UserMessage) message;
                List<MediaResource> media = userMessage.getMedia().stream().map(this::toMedia).toList();
                MapMetadataProvider metadata = new MapMetadataProvider(userMessage.getMetadata());
                return RoleMessage.user(message.getText(), media, metadata);
            }
            case ASSISTANT -> {
                org.springframework.ai.chat.messages.AssistantMessage assistantMessage =
                        (org.springframework.ai.chat.messages.AssistantMessage) message;
                List<MediaResource> media = assistantMessage.getMedia().stream().map(this::toMedia).toList();
                MapMetadataProvider metadata = new MapMetadataProvider(assistantMessage.getMetadata());
                String content = Optional.ofNullable(message.getText()).orElse("");
                if (assistantMessage.hasToolCalls()) {
                    List<ToolCallMessage.ToolCall> toolCalls = assistantMessage.getToolCalls().stream().map(toolCall ->
                            new ToolCallMessage.ToolCall(
                                    toolCall.id(), toolCall.type(), toolCall.name(), toolCall.arguments())
                    ).toList();
                    return new ToolCallMessage(content, media, toolCalls, metadata);
                } else {
                    return RoleMessage.assistant(content, media, metadata);
                }
            }
            case SYSTEM -> {
                return new SystemMessage(message.getText());
            }
            case TOOL -> {
                org.springframework.ai.chat.messages.ToolResponseMessage responseMessage =
                        (org.springframework.ai.chat.messages.ToolResponseMessage) message;
                List<ToolResponseMessage.ToolResponse> toolResponses = responseMessage.getResponses()
                        .stream().map(toolResponse -> new ToolResponseMessage.ToolResponse(
                                toolResponse.id(), toolResponse.name(), toolResponse.responseData()
                        )).toList();
                return new ToolResponseMessage(toolResponses);
            }
            default -> throw new IllegalArgumentException("message cannot be converted");
        }
    }

    protected MediaResource toMedia(Media media) {
        if (media.getData() instanceof URI) {
            return new MediaResource(media.getMimeType(), (URI) media.getData(), media.getName(), null);
        }
        return new MediaResource(media.getMimeType(), null, media.getName(), media.getData());
    }
}
