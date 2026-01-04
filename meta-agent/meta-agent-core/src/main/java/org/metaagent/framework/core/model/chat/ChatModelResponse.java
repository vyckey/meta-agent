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

import com.google.common.collect.Lists;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.metadata.ChatResponseMetadata;
import org.springframework.ai.chat.metadata.EmptyUsage;
import org.springframework.ai.chat.metadata.Usage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.support.UsageCalculator;

import java.util.List;
import java.util.Map;

public class ChatModelResponse extends ChatResponse {
    private final List<ChatResponse> allChatResponses;

    public ChatModelResponse(List<Generation> generations) {
        this(generations, ChatResponseMetadata.builder().build());
    }

    public ChatModelResponse(List<Generation> generations, ChatResponseMetadata responseMetadata) {
        super(generations, responseMetadata);
        this.allChatResponses = List.of();
    }

    public ChatModelResponse(List<Generation> generations, ChatResponseMetadata responseMetadata, List<ChatResponse> allChatResponses) {
        super(generations, responseMetadata);
        this.allChatResponses = List.copyOf(allChatResponses);
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public AssistantMessage getOutput() {
        return getResult().getOutput();
    }

    public List<Generation> getAllGenerations() {
        return allChatResponses.stream().map(ChatResponse::getResults).flatMap(List::stream).toList();
    }

    public List<AssistantMessage> getAllOutputs() {
        return allChatResponses.stream().map(ChatResponse::getResult).map(Generation::getOutput).toList();
    }

    public String toString() {
        return "ChatModelResponse{metadata=" + getMetadata() + ", generations=" + getResults() + "}";
    }


    public static final class Builder {
        private List<Generation> generations;
        private final ChatResponseMetadata.Builder responseMetadataBuilder = ChatResponseMetadata.builder();
        private Usage currentUsage = new EmptyUsage();
        private final List<ChatResponse> allChatResponses = Lists.newArrayList();

        private Builder() {
        }

        public ChatModelResponse.Builder merge(ChatResponse other) {
            if (other == null) {
                return this;
            }

            Usage accumulatedUsage = UsageCalculator.getCumulativeUsage(currentUsage, other);
            generations(other.getResults()).metadata(other.getMetadata());
            currentUsage = accumulatedUsage;
            responseMetadataBuilder.usage(accumulatedUsage);

            if (other instanceof ChatModelResponse chatModelResponse) {
                this.allChatResponses.addAll(chatModelResponse.allChatResponses);
            } else {
                this.allChatResponses.add(other);
            }
            return this;
        }

        public ChatModelResponse.Builder metadata(ChatResponseMetadata other) {
            this.currentUsage = other.getUsage();
            this.responseMetadataBuilder.model(other.getModel());
            this.responseMetadataBuilder.id(other.getId());
            this.responseMetadataBuilder.rateLimit(other.getRateLimit());
            this.responseMetadataBuilder.usage(other.getUsage());
            this.responseMetadataBuilder.promptMetadata(other.getPromptMetadata());

            for (Map.Entry<String, Object> entry : other.entrySet()) {
                this.responseMetadataBuilder.keyValue(entry.getKey(), entry.getValue());
            }
            return this;
        }

        public ChatModelResponse.Builder metadata(String key, Object value) {
            this.responseMetadataBuilder.keyValue(key, value);
            return this;
        }

        public ChatModelResponse.Builder generations(List<Generation> generations) {
            this.generations = generations;
            return this;
        }

        public ChatModelResponse.Builder addChatResponse(ChatResponse chatResponse) {
            this.allChatResponses.add(chatResponse);
            return this;
        }

        public ChatModelResponse build() {
            return new ChatModelResponse(this.generations, this.responseMetadataBuilder.build(), allChatResponses);
        }
    }
}