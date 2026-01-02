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

package org.metaagent.framework.core.model.chat;

import com.google.common.collect.Lists;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;

import java.util.List;

/**
 * ChatResponse Utils.
 *
 * @author vyckey
 */
public abstract class ChatResponseUtils {
    /**
     * Aggregate tool calls with the same id.
     *
     * @param toolCalls tool calls
     * @return aggregated tool calls
     */
    public static List<AssistantMessage.ToolCall> aggregateToolCalls(List<AssistantMessage.ToolCall> toolCalls) {
        if (CollectionUtils.isEmpty(toolCalls)) {
            return toolCalls;
        }
        List<AssistantMessage.ToolCall> aggregatedToolCalls = Lists.newArrayListWithExpectedSize(toolCalls.size());
        for (AssistantMessage.ToolCall toolCall : toolCalls) {
            if (aggregatedToolCalls.isEmpty()){
                aggregatedToolCalls.add(toolCall);
                continue;
            }
            AssistantMessage.ToolCall lastToolCall = aggregatedToolCalls.get(aggregatedToolCalls.size() - 1);
            if (toolCall.id().equals(lastToolCall.id())) {
                AssistantMessage.ToolCall aggregatedToolCall = new AssistantMessage.ToolCall(
                        toolCall.id(),
                        StringUtils.isEmpty(toolCall.type()) ? lastToolCall.type() : toolCall.type(),
                        StringUtils.isEmpty(toolCall.name()) ? lastToolCall.name() : toolCall.name(),
                        lastToolCall.arguments() + toolCall.arguments()
                );
                aggregatedToolCalls.set(aggregatedToolCalls.size() - 1, aggregatedToolCall);
            } else {
                aggregatedToolCalls.add(toolCall);
            }
        }
        return aggregatedToolCalls;
    }

    public static ChatResponse rebuildResponseIfRequired(ChatResponse chatResponse) {
        if (chatResponse.getResult() == null) {
            return ChatResponse.builder().from(chatResponse)
                    .generations(List.of(new Generation(new AssistantMessage(""))))
                    .build();
        }
        if (!chatResponse.hasToolCalls()) {
            return chatResponse;
        }

        List<Generation> generations = Lists.newArrayListWithCapacity(chatResponse.getResults().size());
        for (Generation generation : chatResponse.getResults()) {
            if (generation.getOutput().hasToolCalls()) {
                // Aggregate tool calls
                AssistantMessage outputMessage = generation.getOutput();
                List<AssistantMessage.ToolCall> toolCalls = aggregateToolCalls(outputMessage.getToolCalls());
                AssistantMessage message = AssistantMessage.builder()
                        .content(outputMessage.getText() != null ? outputMessage.getText() : "")
                        .properties(outputMessage.getMetadata())
                        .media(outputMessage.getMedia())
                        .toolCalls(toolCalls)
                        .build();
                generations.add(new Generation(message, generation.getMetadata()));
            } else {
                generations.add(generation);
            }
        }
        return ChatResponse.builder().from(chatResponse).generations(generations).build();
    }
}
