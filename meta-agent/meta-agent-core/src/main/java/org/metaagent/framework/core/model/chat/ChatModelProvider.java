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

import org.metaagent.framework.core.model.ModelProvider;
import org.metaagent.framework.core.model.chat.metadata.ChatModelMetadata;
import org.springframework.ai.chat.model.ChatModel;

/**
 * Interface for components that provide access to chat models and their associated metadata.
 * Implementations are responsible for supplying both standard and streaming chat model instances
 * along with model-specific configuration and capabilities.
 *
 * @author vyckey
 */
public interface ChatModelProvider extends ModelProvider {
    /**
     * Create a new builder for a {@link ChatModelProvider}.
     *
     * @return A new builder for a {@link ChatModelProvider}.
     */
    static Builder builder() {
        return DefaultChatModelProvider.builder();
    }

    /**
     * Get the metadata associated with the chat model.
     *
     * @return The metadata associated with the chat model.
     */
    @Override
    ChatModelMetadata getModelMetadata();

    /**
     * Get the chat model instance.
     *
     * @return The chat model instance.
     */
    @Override
    ChatModel getModel();

    /**
     * Get the streaming chat model instance.
     *
     * @return The streaming chat model instance.
     */
    @Override
    ChatModel getStreamingModel();


    interface Builder {
        Builder name(String name);

        Builder modelName(String modelName);

        Builder metadata(ChatModelMetadata metadata);

        Builder chatModel(ChatModel chatModel);

        ChatModelProvider build();
    }
}
