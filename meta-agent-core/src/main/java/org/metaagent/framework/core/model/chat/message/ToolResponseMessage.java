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
import org.metaagent.framework.common.metadata.MetadataProvider;
import org.metaagent.framework.core.agent.chat.message.AbstractMessage;

import java.util.List;
import java.util.Objects;

/**
 * Represents a message containing tool responses in a chat context.
 * This message is used to convey the results of tool executions back to the user.
 *
 * @author vyckey
 */
@EqualsAndHashCode(callSuper = true)
public class ToolResponseMessage extends AbstractMessage {
    public static final String ROLE_TOOL_RESPONSE = "tool_response";
    private final List<ToolResponse> toolResponses;

    public ToolResponseMessage(List<ToolResponse> toolResponses, MetadataProvider metadata) {
        this.toolResponses = Objects.requireNonNull(toolResponses);
        this.metadata = metadata;
    }

    public ToolResponseMessage(List<ToolResponse> toolResponses) {
        this(toolResponses, MetadataProvider.create());
    }

    public ToolResponseMessage(ToolResponse... toolResponses) {
        this.toolResponses = List.of(toolResponses);
    }

    @Override
    public String getRole() {
        return ROLE_TOOL_RESPONSE;
    }

    public List<ToolResponse> getToolResponses() {
        return toolResponses;
    }

    @Override
    public String getContent() {
        return "";
    }

    public record ToolResponse(String id, String name, String responseData) {
    }
}
