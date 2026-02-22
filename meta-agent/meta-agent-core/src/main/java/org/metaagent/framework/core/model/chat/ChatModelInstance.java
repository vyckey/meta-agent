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

import org.metaagent.framework.core.model.ModelInstance;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;

/**
 * Model instance specialized for chat models.
 * <p>
 * Provides chat model metadata and the runtime used to execute chat requests.
 * Implementations should document nullability and thread-safety as needed.
 */
public interface ChatModelInstance extends ModelInstance<Prompt, ChatResponse> {

    /**
     * Return metadata for this chat model.
     *
     * @return the {@link ChatModelInfo} describing the model (non-null)
     */
    @Override
    ChatModelInfo getInfo();

    /**
     * Return the runtime used to execute chat requests.
     *
     * @return the {@link ChatModel} runtime (non-null)
     * @throws IllegalStateException if the runtime is unavailable
     */
    @Override
    ChatModel getRuntime();
}
