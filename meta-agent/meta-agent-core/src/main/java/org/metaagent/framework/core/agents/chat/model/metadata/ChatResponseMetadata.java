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

package org.metaagent.framework.core.agents.chat.model.metadata;

import org.metaagent.framework.common.metadata.MetadataProvider;
import org.springframework.ai.chat.metadata.RateLimit;
import org.springframework.ai.chat.metadata.Usage;

/**
 * Metadata describing a chat response produced by a chat agent.
 *
 * <p>This interface exposes common information typically returned by chat models
 * such as the model identifier, token usage stats, rate limit information, and
 * the reason the generation finished. Implementations may also provide
 * additional provider-specific metadata via the {@link MetadataProvider}
 * contract.</p>
 *
 * <p>Implementations are expected to be constructed via the provided
 * {@link #builder()} factory method which returns a {@code Builder} to
 * incrementally populate fields.</p>
 */
public interface ChatResponseMetadata extends MetadataProvider {
    /**
     * Create a new builder for {@link ChatResponseMetadata} instances.
     *
     * @return a new {@link DefaultChatResponseMetadata.Builder}
     */
    static DefaultChatResponseMetadata.Builder builder() {
        return new DefaultChatResponseMetadata.Builder();
    }

    /**
     * Return the identifier or name of the model that produced the response.
     *
     * @return the model id (e.g. "gpt-4o") or {@code null} if not available
     */
    String getModel();

    /**
     * Return token usage information for the response.
     *
     * <p>This typically includes counts for prompt tokens, completion tokens and
     * total tokens. May be {@code null} if the provider does not supply usage
     * statistics.</p>
     *
     * @return token usage information or {@code null}
     */
    Usage getTokenUsage();

    /**
     * Return rate limit metadata associated with the response.
     *
     * <p>Provides information about the current rate limiting state (for
     * example limits, remaining quota, and reset times) when available.</p>
     *
     * @return rate limit details or {@code null} if not provided
     */
    RateLimit getRateLimit();

    /**
     * Return the reason the generation finished (for example {@code "stop"},
     * {@code "length"}, or {@code "error"}).
     *
     * @return a short finish reason string or {@code null} when unspecified
     */
    String getFinishReason();

    /**
     * Builder for {@link ChatResponseMetadata} instances.
     *
     * <p>The builder supports fluent population of known fields and arbitrary
     * key/value metadata entries via {@link #metadata(String, Object)}.</p>
     */
    interface Builder {
        /**
         * Set the model identifier.
         *
         * @param model the model id or name
         * @return this builder
         */
        Builder model(String model);

        /**
         * Set token usage statistics.
         *
         * @param tokenUsage the usage information
         * @return this builder
         */
        Builder tokenUsage(Usage tokenUsage);

        /**
         * Set rate limit metadata.
         *
         * @param rateLimit rate limit details
         * @return this builder
         */
        Builder rateLimit(RateLimit rateLimit);

        /**
         * Set the finish reason for the response generation.
         *
         * @param finishReason a short string describing why generation stopped
         * @return this builder
         */
        Builder finishReason(String finishReason);

        /**
         * Add an arbitrary metadata key/value pair. Implementations may store
         * these entries and expose them via the {@link MetadataProvider}
         * contract.
         *
         * @param key   metadata key
         * @param value metadata value
         * @return this builder
         */
        Builder metadata(String key, Object value);

        /**
         * Build the {@link ChatResponseMetadata} instance.
         *
         * @return a constructed, immutable ChatResponseMetadata
         */
        ChatResponseMetadata build();
    }
}
