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

import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.metaagent.framework.common.content.MediaResource;
import org.metaagent.framework.common.metadata.MetadataProvider;
import org.metaagent.framework.core.agent.chat.message.AbstractMessage;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Represents a tool call message in a chat model.
 *
 * @author vyckey
 */
@Getter
@EqualsAndHashCode(callSuper = true)
public class ToolCallMessage extends AbstractMessage {
    public static final String ROLE_TOOL_CALL = "tool_call";
    private final String content;
    private final List<MediaResource> media;
    private final List<ToolCall> toolCalls;

    public ToolCallMessage(String content, List<MediaResource> media, List<ToolCall> toolCalls, MetadataProvider metadata) {
        this.content = Objects.requireNonNull(content, "content is required");
        this.media = Objects.requireNonNull(media, "media is required");
        this.toolCalls = Objects.requireNonNull(toolCalls, "toolCalls is required");
        this.metadata = Objects.requireNonNull(metadata, "metadata is required");
    }

    public ToolCallMessage(String content, List<MediaResource> media, List<ToolCall> toolCalls) {
        this(content, media, toolCalls, MetadataProvider.create());
    }

    public ToolCallMessage(String content, List<ToolCall> toolCalls) {
        this(content, List.of(), toolCalls, MetadataProvider.create());
    }

    @Override
    public String getRole() {
        return ROLE_TOOL_CALL;
    }

    public boolean hasToolCalls() {
        return toolCalls != null && !toolCalls.isEmpty();
    }

    @Override
    public String toString() {
        String toolNames = toolCalls.stream()
                .map(toolCall -> "- " + toolCall.name() + ": " + toolCall.arguments())
                .collect(Collectors.joining("\n"));
        return getRole() + ":\n" + toolNames;
    }

    public record ToolCall(String id, String type, String name, String arguments) {
    }
}
