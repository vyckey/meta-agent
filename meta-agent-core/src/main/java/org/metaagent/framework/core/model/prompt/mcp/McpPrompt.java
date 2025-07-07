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

package org.metaagent.framework.core.model.prompt.mcp;

import com.google.common.collect.Lists;
import io.modelcontextprotocol.spec.McpSchema;
import lombok.Getter;
import org.apache.commons.lang3.NotImplementedException;
import org.apache.commons.lang3.StringUtils;
import org.metaagent.framework.core.agent.chat.message.Message;
import org.metaagent.framework.core.agent.chat.message.MessageFactory;
import org.metaagent.framework.core.mcp.client.UnifiedMcpClient;
import org.metaagent.framework.core.model.prompt.ChatPromptValue;
import org.metaagent.framework.core.model.prompt.PromptFormatException;
import org.metaagent.framework.core.model.prompt.PromptTemplate;
import org.metaagent.framework.core.model.prompt.PromptValue;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * McpPrompt is a prompt template that retrieves prompts from a Model Context Protocol (MCP) server.
 * It formats the prompt based on the provided arguments and returns a ChatPromptValue containing the messages.
 *
 * @author vyckey
 */
@Getter
public class McpPrompt implements PromptTemplate {
    protected final String name;
    protected final UnifiedMcpClient mcpClient;

    public McpPrompt(String name, UnifiedMcpClient mcpClient) {
        if (StringUtils.isBlank(name)) {
            throw new IllegalArgumentException("Prompt name cannot be null or empty");
        }
        this.name = name;
        this.mcpClient = Objects.requireNonNull(mcpClient, "MCP client cannot be null");
    }

    @Override
    public PromptValue format(Object... args) {
        if (args.length == 0) {
            return format(Map.of());
        }
        throw new PromptFormatException("Only support key-value pairs for arguments");
    }

    @Override
    public PromptValue format(Map<String, Object> args) {
        McpSchema.GetPromptRequest request = new McpSchema.GetPromptRequest(this.name, args);
        McpSchema.GetPromptResult promptResult;
        if (mcpClient.isSync()) {
            promptResult = mcpClient.sync().getPrompt(request);
        } else {
            promptResult = mcpClient.async().getPrompt(request).block();
        }
        List<Message> messages = Lists.newArrayList();
        for (McpSchema.PromptMessage promptMessage : promptResult.messages()) {
            messages.add(newMessage(promptMessage));
        }
        return new ChatPromptValue(messages);
    }

    static Message newMessage(McpSchema.PromptMessage promptMessage) {
        String contentType = promptMessage.content().type();
        Message message;
        switch (contentType) {
            case "text" -> {
                McpSchema.TextContent textContent = (McpSchema.TextContent) promptMessage.content();
                message = MessageFactory.textMessage(promptMessage.role().name(), textContent.text());
            }
            case "image" -> {
                McpSchema.ImageContent imageContent = (McpSchema.ImageContent) promptMessage.content();
                throw new NotImplementedException("Not implemented");
            }
            case "resource" -> {
                McpSchema.EmbeddedResource resourceContent = (McpSchema.EmbeddedResource) promptMessage.content();
                McpSchema.ResourceContents contents = resourceContent.resource();
                if (contents instanceof McpSchema.TextResourceContents) {
                    throw new NotImplementedException("Not implemented");
                } else if (contents instanceof McpSchema.BlobResourceContents) {
                    throw new NotImplementedException("Not implemented");
                } else {
                    throw new PromptFormatException("Unsupported resource content type: " + contents.getClass().getName());
                }
            }
            default -> throw new PromptFormatException("Unsupported content type: " + contentType);
        }
        return message;
    }
}
