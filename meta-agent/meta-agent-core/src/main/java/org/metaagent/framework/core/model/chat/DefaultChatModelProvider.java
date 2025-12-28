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

import org.apache.commons.lang3.StringUtils;
import org.metaagent.framework.core.model.chat.metadata.ChatModelMetadata;
import org.springframework.ai.chat.model.ChatModel;

import java.util.Objects;

/**
 * Default implementation of {@link ChatModelProvider}
 *
 * @author vyckey
 */
public record DefaultChatModelProvider(
        String name,
        String modelName,
        ChatModelMetadata metadata,
        ChatModel chatModel
) implements ChatModelProvider {
    public DefaultChatModelProvider(String name, String modelName, ChatModelMetadata metadata, ChatModel chatModel) {
        if (StringUtils.isEmpty(name)) {
            throw new IllegalArgumentException("Model provider name cannot be empty");
        }
        if (StringUtils.isEmpty(modelName)) {
            throw new IllegalArgumentException("Model name cannot be empty");
        }
        this.name = name.trim();
        this.modelName = modelName.trim();
        this.metadata = Objects.requireNonNull(metadata, "Model metadata is required");
        this.chatModel = Objects.requireNonNull(chatModel, "Chat Model is required");
    }

    public static Builder builder() {
        return new Builder();
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getModelName() {
        return modelName;
    }

    @Override
    public ChatModelMetadata getModelMetadata() {
        return metadata;
    }

    @Override
    public ChatModel getModel() {
        return chatModel;
    }

    @Override
    public ChatModel getStreamingModel() {
        return chatModel;
    }

    public static class Builder implements ChatModelProvider.Builder {
        private String name;
        private String modelName;
        private ChatModelMetadata metadata;
        private ChatModel chatModel;

        @Override
        public Builder name(String name) {
            this.name = name;
            return this;
        }

        @Override
        public Builder modelName(String modelName) {
            this.modelName = modelName;
            return this;
        }

        @Override
        public Builder metadata(ChatModelMetadata metadata) {
            this.metadata = metadata;
            return this;
        }

        @Override
        public Builder chatModel(ChatModel chatModel) {
            this.chatModel = chatModel;
            return this;
        }

        @Override
        public DefaultChatModelProvider build() {
            return new DefaultChatModelProvider(name, modelName, metadata, chatModel);
        }
    }
}
