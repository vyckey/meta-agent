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

package org.metaagent.framework.core.tool.spring;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.apache.commons.collections.CollectionUtils;
import org.metaagent.framework.core.tool.Tool;
import org.metaagent.framework.core.tool.ToolContext;
import org.metaagent.framework.core.tool.ToolExecutionException;
import org.metaagent.framework.core.tool.manager.ToolManager;
import org.metaagent.framework.core.tool.tracker.ToolCallTracker;
import org.metaagent.framework.core.tool.tracker.ToolTrackerDelegate;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.ToolResponseMessage;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.model.function.FunctionCallback;
import org.springframework.ai.model.tool.ToolCallingChatOptions;

import java.util.List;

/**
 * Spring ToolCallback utilities.
 *
 * @author vyckey
 */
public abstract class ToolCallbackUtils {
    public static void addToolsToChatOptions(ToolCallingChatOptions chatOptions, ToolManager toolManager) {
        addToolsToChatOptions(chatOptions, toolManager, null);
    }

    public static void addToolsToChatOptions(ToolCallingChatOptions chatOptions,
                                             ToolManager toolManager, ToolCallTracker toolCallTracker) {
        List<FunctionCallback> toolCallbacks = Lists.newArrayList();
        for (String toolName : toolManager.getToolNames()) {
            Tool<Object, Object> tool = toolManager.getTool(toolName);
            if (toolCallTracker != null && !(tool instanceof ToolTrackerDelegate)) {
                tool = new ToolTrackerDelegate<>(toolCallTracker, tool);
            }
            toolCallbacks.add(new ToolCallbackDelegate(tool));
        }
        if (CollectionUtils.isEmpty(chatOptions.getToolNames())) {
            chatOptions.setToolNames(toolManager.getToolNames());
        } else {
            chatOptions.setToolNames(Sets.union(chatOptions.getToolNames(), toolManager.getToolNames()));
        }
        if (CollectionUtils.isNotEmpty(chatOptions.getToolCallbacks())) {
            toolCallbacks.addAll(chatOptions.getToolCallbacks());
        }
        chatOptions.setToolCallbacks(toolCallbacks);
    }

    public static ChatOptions buildChatOptionsWithTools(ChatOptions chatOptions,
                                                        ToolManager toolManager, ToolCallTracker toolCallTracker) {
        if (chatOptions instanceof ToolCallingChatOptions) {
            ToolCallingChatOptions toolCallingChatOptions = chatOptions.copy();
            ToolCallbackUtils.addToolsToChatOptions(toolCallingChatOptions, toolManager, toolCallTracker);
            return toolCallingChatOptions;
        }
        return chatOptions;
    }

    public static List<ToolResponseMessage.ToolResponse> callTools(
            ToolManager toolManager, ToolContext toolContext, List<AssistantMessage.ToolCall> toolCalls) {
        List<ToolResponseMessage.ToolResponse> toolResponses = Lists.newArrayList();
        for (AssistantMessage.ToolCall toolCall : toolCalls) {
            Tool<?, ?> tool = toolManager.getTool(toolCall.name());
            if (tool == null) {
                throw new ToolExecutionException("Tool \"" + toolCall.name() + "\" not found");
            }
            String result = new ToolTrackerDelegate<>(toolContext.getToolCallTracker(), tool)
                    .call(toolContext, toolCall.arguments());
            toolResponses.add(new ToolResponseMessage.ToolResponse(toolCall.id(), toolCall.name(), result));
        }
        return toolResponses;
    }
}
