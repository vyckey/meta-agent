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

package org.metaagent.framework.core.model.chat;

import org.metaagent.framework.common.metadata.MetadataProvider;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.metadata.ChatResponseMetadata;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.model.tool.ToolCallingChatOptions;
import org.springframework.ai.model.tool.ToolCallingManager;
import org.springframework.ai.model.tool.ToolExecutionResult;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;

/**
 * Utility class for handling tool calls in chat models.
 *
 * @author vyckey
 */
public class ChatModelUtils {
    private static ToolCallingManager getToolCallingManager(ChatModel chatModel) {
        ToolCallingManager toolCallingManager = null;
        Field tcmField = ReflectionUtils.findField(chatModel.getClass(), "toolCallingManager", ToolCallingManager.class);
        if (tcmField != null) {
            tcmField.setAccessible(true);
            toolCallingManager = (ToolCallingManager) ReflectionUtils.getField(tcmField, chatModel);
        }
        if (toolCallingManager == null) {
            throw new IllegalStateException("ToolCallingManager not found in ChatModel");
        }
        return toolCallingManager;
    }

    public static ChatResponse callWithToolCall(ChatModel chatModel, Prompt prompt) {
        return callWithToolCall(chatModel, prompt, null);
    }

    private static ChatResponse callWithToolCall(ChatModel chatModel, Prompt prompt, ChatResponse previousResponse) {
        ChatResponse response = chatModel.call(prompt);
        Generation result = response.getResult();
        AssistantMessage assistantMessage = result.getOutput();
        if (!assistantMessage.hasToolCalls()) {
            return response;
        }
        if (prompt.getOptions() != null && ToolCallingChatOptions.isInternalToolExecutionEnabled(prompt.getOptions())) {
            return response;
        }

        ToolCallingManager toolCallingManager = getToolCallingManager(chatModel);
        ToolExecutionResult toolExecutionResult = toolCallingManager.executeToolCalls(prompt, response);
        if (toolExecutionResult.returnDirect()) {
            return ChatResponse.builder()
                    .from(response)
                    .generations(ToolExecutionResult.buildGenerations(toolExecutionResult))
                    .build();
        } else {
            Prompt newPrompt = new Prompt(toolExecutionResult.conversationHistory(), prompt.getOptions());
            return callWithToolCall(chatModel, newPrompt, response);
        }
    }

    public static MetadataProvider getMetadata(ChatResponse chatResponse) {
        ChatResponseMetadata responseMetadata = chatResponse.getMetadata();
        return MetadataProvider.create()
                .setProperty("id", responseMetadata.getId())
                .setProperty("model", responseMetadata.getModel())
                .setProperty("usage", responseMetadata.getUsage());
    }
}
