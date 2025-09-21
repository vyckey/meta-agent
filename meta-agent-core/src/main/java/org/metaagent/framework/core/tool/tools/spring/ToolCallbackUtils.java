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

package org.metaagent.framework.core.tool.tools.spring;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.apache.commons.collections.CollectionUtils;
import org.metaagent.framework.core.tool.Tool;
import org.metaagent.framework.core.tool.exception.ToolExecutionException;
import org.metaagent.framework.core.tool.executor.BatchToolInputs;
import org.metaagent.framework.core.tool.executor.BatchToolOutputs;
import org.metaagent.framework.core.tool.executor.ToolExecutor;
import org.metaagent.framework.core.tool.executor.ToolExecutorContext;
import org.metaagent.framework.core.tool.manager.ToolManager;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.ToolResponseMessage;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.model.function.FunctionCallback;
import org.springframework.ai.model.tool.DefaultToolCallingChatOptions;
import org.springframework.ai.model.tool.ToolCallingChatOptions;

import java.util.List;
import java.util.Map;

/**
 * Spring ToolCallback utilities.
 *
 * @author vyckey
 */
public abstract class ToolCallbackUtils {
    public static void addToolsToChatOptions(ToolCallingChatOptions chatOptions,
                                             ToolManager toolManager, ToolExecutor toolExecutor) {
        List<FunctionCallback> toolCallbacks = Lists.newArrayList();
        for (String toolName : toolManager.getToolNames()) {
            Tool<Object, Object> tool = toolManager.getTool(toolName);
            toolCallbacks.add(new ToolCallbackDelegate(tool, toolExecutor));
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
                                                        ToolExecutor toolExecutor,
                                                        ToolExecutorContext executorContext,
                                                        Boolean internalToolExecutionEnabled) {
        ToolCallingChatOptions toolCallingChatOptions = null;
        if (chatOptions == null) {
            toolCallingChatOptions = new DefaultToolCallingChatOptions();
        } else if (chatOptions instanceof ToolCallingChatOptions) {
            toolCallingChatOptions = chatOptions.copy();
        }
        if (toolCallingChatOptions != null) {
            ToolManager toolManager = executorContext.getToolManager();
            ToolCallbackUtils.addToolsToChatOptions(toolCallingChatOptions, toolManager, toolExecutor);
            toolCallingChatOptions.setToolContext(Map.of(
                    ToolCallbackDelegate.CONTEXT_KEY, executorContext
            ));
            toolCallingChatOptions.setInternalToolExecutionEnabled(internalToolExecutionEnabled);
            return toolCallingChatOptions;
        }
        return chatOptions;
    }

    public static List<ToolResponseMessage.ToolResponse> callTools(
            ToolExecutor toolExecutor,
            ToolExecutorContext executorContext,
            List<AssistantMessage.ToolCall> toolCalls) throws ToolExecutionException {
        ToolManager toolManager = executorContext.getToolManager();
        List<BatchToolInputs.ToolInput> toolInputs = Lists.newArrayList();
        for (AssistantMessage.ToolCall toolCall : toolCalls) {
            Tool<?, ?> tool = toolManager.getTool(toolCall.name());
            if (tool == null) {
                throw new ToolExecutionException("Tool \"" + toolCall.name() + "\" not found");
            }
            toolInputs.add(new BatchToolInputs.ToolInput(tool.getName(), toolCall.arguments()));
        }

        BatchToolOutputs toolOutputs = toolExecutor.execute(executorContext, new BatchToolInputs(toolInputs));
        return toolOutputs.outputs().stream()
                .map(output -> new ToolResponseMessage.ToolResponse(output.toolName(), output.toolName(), output.output()))
                .toList();
    }
}
