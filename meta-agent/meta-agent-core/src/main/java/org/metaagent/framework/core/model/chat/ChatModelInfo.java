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

import org.metaagent.framework.core.model.ModelInfo;
import org.metaagent.framework.core.model.chat.config.ModelApiConfig;

import java.time.Instant;

/**
 * Chat-specific extension of {@link ModelInfo} describing conversational model
 * characteristics and configuration required by chat clients.
 *
 * <p>Implementations expose the API configuration needed to talk to the model,
 * the context window size (number of tokens or turns supported) and the model's
 * knowledge cut-off date.</p>
 */
public interface ChatModelInfo extends ModelInfo {

    /**
     * Create a new builder for {@link ChatModelInfo} implementations.
     *
     * <p>This convenience factory returns a {@link DefaultChatModelInfo.Builder}
     * which can be used to fluently construct a {@link DefaultChatModelInfo}
     * instance. Callers may use this when a lightweight, immutable default
     * implementation is sufficient.</p>
     *
     * @return a new {@link DefaultChatModelInfo.Builder}
     */
    static DefaultChatModelInfo.Builder builder() {
        return DefaultChatModelInfo.builder();
    }

    /**
     * Return the API configuration used to interact with this chat model.
     *
     * @return a {@link ModelApiConfig} containing endpoint and credential details; may be null
     */
    ModelApiConfig getApiConfig();

    /**
     * Return the model's context capacity. This represents how much history the
     * model can consider (for example measured in tokens or message turns). The
     * unit is provider-specific and should be documented by implementations.
     *
     * @return the context size (non-negative integer)
     */
    int getContextSize();

    /**
     * Gets the model's knowledge cut-off date. This indicates the latest point
     * in time the model was trained on or aware of; callers can use this to
     * determine whether the model may be unaware of very recent events.
     *
     * @return an {@link Instant} representing the cut-off date, or null if unknown
     */
    Instant getCutOffDate();
}
