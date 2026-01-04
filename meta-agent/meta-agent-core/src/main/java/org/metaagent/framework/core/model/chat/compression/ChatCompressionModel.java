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

package org.metaagent.framework.core.model.chat.compression;

import com.google.common.collect.Lists;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.metaagent.framework.core.model.chat.message.ToolCallMessage;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.ToolResponseMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.DefaultChatOptions;
import org.springframework.ai.chat.prompt.Prompt;

import java.util.List;
import java.util.Objects;

/**
 * ChatCompressionModel is a model that compresses a list of messages using a chat model.
 *
 * @author vyckey
 */
public class ChatCompressionModel implements CompressionModel {
    private final ChatModel chatModel;
    private final ChatOptions chatOptions;
    private final SystemMessage systemPrompt;

    public ChatCompressionModel(ChatModel chatModel, ChatOptions chatOptions, SystemMessage systemPrompt) {
        this.chatModel = Objects.requireNonNull(chatModel, "chatModel is required");
        this.chatOptions = chatOptions;
        this.systemPrompt = Objects.requireNonNull(systemPrompt, "systemPrompt is required");
    }

    public ChatCompressionModel(ChatModel chatModel, SystemMessage systemPrompt) {
        this(chatModel, new DefaultChatOptions(), systemPrompt);
    }

    /**
     * Counts the number of tokens in a list of messages.
     * Default implementation uses the length of the message text which isn't very accurate, and it's merely a rough estimation.
     *
     * @param messages the list of messages to count tokens for
     * @return the total number of tokens in the messages
     */
    @Override
    public int countTokens(List<Message> messages) {
        int totalTokenCount = 0;
        for (Message message : messages) {
            if (message instanceof ToolCallMessage toolCallMessage) {
                for (ToolCallMessage.ToolCall toolCall : toolCallMessage.getToolCalls()) {
                    totalTokenCount += toolCall.name().length();
                    totalTokenCount += toolCall.arguments().length();
                }
            } else if (message instanceof ToolResponseMessage toolResponseMessage) {
                for (ToolResponseMessage.ToolResponse toolResponse : toolResponseMessage.getResponses()) {
                    totalTokenCount += toolResponse.responseData().length();
                }
            }
            totalTokenCount += Objects.requireNonNull(message.getText()).length();
        }
        return totalTokenCount;
    }

    @Override
    public CompressionResponse call(CompressionRequest compressionRequest) {
        List<Message> messages = compressionRequest.getInstructions();
        CompressOptions compressOptions = compressionRequest.getOptions();

        int totalTokenCount = countTokens(messages);
        // if the token count is less than the max tokens, no compression is needed
        if (totalTokenCount < compressOptions.getMaxTokens()) {
            CompressionResult compressionResult = DefaultCompressionResult.builder().compressed(false).retainedMessages(messages).build();
            return new CompressionResponse(List.of(compressionResult));
        }

        // find the last message that needs to be compressed
        int lastCompressionIndex = findLastCompressionIndex(messages, compressOptions);
        List<Message> removedMessages = messages.subList(0, lastCompressionIndex + 1);
        List<Message> retainedMessages = messages.subList(lastCompressionIndex, messages.size());

        // build the compress prompt
        List<Message> promptMessages = Lists.newArrayListWithCapacity(lastCompressionIndex + 2);
        if (StringUtils.isNotEmpty(compressOptions.getCompressPrompt())) {
            promptMessages.add(new SystemMessage(this.systemPrompt.getText() + "\n\n" + compressOptions.getCompressPrompt().trim()));
        } else {
            promptMessages.add(this.systemPrompt);
        }
        promptMessages.addAll(preprocessingMessagesToCompress(removedMessages));
        Prompt compressPrompt = Prompt.builder()
                .messages(promptMessages)
                .chatOptions(chatOptions)
                .build();

        ChatResponse chatResponse = chatModel.call(compressPrompt);
        DefaultCompressionResult.Builder resultBuilder = DefaultCompressionResult.builder();

        // build the compression results
        List<CompressionResult> compressionResults = Lists.newArrayListWithCapacity(chatResponse.getResults().size());
        for (Generation generation : chatResponse.getResults()) {
            float compressionRatio = countTokens(List.of(generation.getOutput())) * 1.0f / totalTokenCount;
            CompressionResult compressionResult = resultBuilder.compressed(true)
                    .summary(generation.getOutput())
                    .compressionRatio(compressionRatio)
                    .removedMessages(removedMessages)
                    .retainedMessages(retainedMessages)
                    .build();
            compressionResults.add(compressionResult);
        }
        return new CompressionResponse(compressionResults);
    }

    protected int findLastCompressionIndex(List<Message> messages, CompressOptions compressOptions) {
        if (compressOptions.getReservedMessageCount() <= 0) {
            return messages.size() - 1;
        }

        final int reservedMessageCount = Math.min(compressOptions.getReservedMessageCount(), messages.size());
        int totalTokenCount = 0;
        for (int i = messages.size() - 1; i >= messages.size() - reservedMessageCount; i--) {
            if (totalTokenCount >= compressOptions.getMaxTokens()) {
                return i + 1;
            }
            totalTokenCount += countTokens(List.of(messages.get(i)));
        }
        return messages.size() - reservedMessageCount - 1;
    }

    protected List<Message> preprocessingMessagesToCompress(List<Message> messages) {
        List<Message> preprocessedMessages = Lists.newArrayListWithCapacity(messages.size());
        for (Message message : messages) {
            Message processedMessage = message;
            if (message instanceof AssistantMessage assistantMessage && CollectionUtils.isNotEmpty(assistantMessage.getToolCalls())) {
                List<AssistantMessage.ToolCall> toolCalls = assistantMessage.getToolCalls().stream()
                        .map(ChatCompressionModel::trimToolCall).toList();
                processedMessage = AssistantMessage.builder()
                        .content(assistantMessage.getText() != null ? assistantMessage.getText() : "")
                        .toolCalls(toolCalls)
                        .properties(assistantMessage.getMetadata())
                        .build();
            } else if (message instanceof ToolResponseMessage toolResponseMessage) {
                List<ToolResponseMessage.ToolResponse> toolResponses = toolResponseMessage.getResponses().stream()
                        .map(ChatCompressionModel::trimToolResponse).toList();
                processedMessage = ToolResponseMessage.builder()
                        .responses(toolResponses).metadata(toolResponseMessage.getMetadata()).build();
            }
            preprocessedMessages.add(processedMessage);
        }
        return preprocessedMessages;
    }

    private static AssistantMessage.ToolCall trimToolCall(AssistantMessage.ToolCall toolCall) {
        final int maxArgumentsLength = 60;
        if (toolCall.arguments().length() < maxArgumentsLength) {
            return toolCall;
        } else {
            return new AssistantMessage.ToolCall(
                    toolCall.id(), toolCall.type(), toolCall.name(), toolCall.arguments().substring(0, maxArgumentsLength) + "..."
            );
        }
    }

    private static ToolResponseMessage.ToolResponse trimToolResponse(ToolResponseMessage.ToolResponse toolResponse) {
        final int maxArgumentsLength = 100;
        if (toolResponse.responseData().length() < maxArgumentsLength) {
            return toolResponse;
        }
        return new ToolResponseMessage.ToolResponse(
                toolResponse.id(), toolResponse.name(), toolResponse.responseData().substring(0, maxArgumentsLength) + "..."
        );
    }

}
