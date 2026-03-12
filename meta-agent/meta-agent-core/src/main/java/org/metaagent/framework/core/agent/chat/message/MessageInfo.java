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

package org.metaagent.framework.core.agent.chat.message;

import org.metaagent.framework.common.metadata.MetadataProvider;
import org.metaagent.framework.core.agent.chat.session.SessionId;

import java.time.Instant;

/**
 * The message info interface represents the basic information of a message.
 *
 * @author vyckey
 */
public interface MessageInfo {
    String ROLE_USER = "user";
    String ROLE_ASSISTANT = "assistant";

    /**
     * Create a new builder.
     *
     * @return a new builder
     */
    static MessageInfo.Builder builder() {
        return RoleMessageInfo.builder();
    }

    /**
     * Create a new builder for a user message.
     *
     * @return a new builder for a user message
     */
    static MessageInfo.Builder user() {
        return builder().role(ROLE_USER);
    }

    /**
     * Create a new builder for an assistant message.
     *
     * @return a new builder for an assistant message
     */
    static MessageInfo.Builder assistant() {
        return builder().role(ROLE_ASSISTANT);
    }

    /**
     * Get the unique identifier of the message.
     *
     * @return the unique identifier of the message
     */
    MessageId id();

    /**
     * Get the unique identifier of the parent message.
     *
     * @return the unique identifier of the parent message, or null if there is no parent message
     */
    MessageId parentId();

    /**
     * Get the unique identifier of the session.
     *
     * @return the unique identifier of the session, or null if there is no session
     */
    SessionId sessionId();

    /**
     * Get the role of the message.
     *
     * @return the role of the message
     */
    String role();

    /**
     * Get the time when the message was created.
     *
     * @return the time when the message was created
     */
    Instant createdAt();

    /**
     * Get the time when the message was last updated.
     *
     * @return the time when the message was last updated
     */
    Instant updatedAt();

    /**
     * Get the metadata of the message.
     *
     * @return the metadata of the message
     */
    MetadataProvider metadata();

    /**
     * Create a new builder.
     *
     * @return a new builder
     */
    Builder toBuilder();

    /**
     * The builder interface for creating a message.
     */
    interface Builder {
        /**
         * Set the unique identifier of the message.
         *
         * @param id the unique identifier of the message
         * @return the builder
         */
        Builder id(MessageId id);

        /**
         * Set the unique identifier of the parent message.
         *
         * @param parentId the unique identifier of the parent message
         * @return the builder
         */
        Builder parentId(MessageId parentId);

        /**
         * Set the unique identifier of the session.
         *
         * @param sessionId the unique identifier of the session
         * @return the builder
         */
        Builder sessionId(SessionId sessionId);

        /**
         * Set the role of the message.
         *
         * @param role the role of the message
         * @return the builder
         */
        Builder role(String role);

        /**
         * Set the time when the message was created.
         *
         * @param createdAt the time when the message was created
         * @return the builder
         */
        Builder createdAt(Instant createdAt);

        /**
         * Set the time when the message was last updated.
         *
         * @param updatedAt the time when the message was last updated
         * @return the builder
         */
        Builder updatedAt(Instant updatedAt);

        /**
         * Set the metadata of the message.
         *
         * @param metadata the metadata of the message
         * @return the builder
         */
        Builder metadata(MetadataProvider metadata);

        /**
         * Build the message info.
         *
         * @return the message info
         */
        MessageInfo build();
    }
}
