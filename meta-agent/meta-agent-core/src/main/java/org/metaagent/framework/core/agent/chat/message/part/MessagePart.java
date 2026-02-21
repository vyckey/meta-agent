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

package org.metaagent.framework.core.agent.chat.message.part;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import org.metaagent.framework.common.content.TextContent;
import org.metaagent.framework.common.metadata.MetadataProvider;

import java.time.Instant;

/**
 * Represents a part of a message.
 *
 * @author vyckey
 * @see TextMessagePart
 * @see MediaMessagePart
 */
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "type"
)
public interface MessagePart extends TextContent {
    /**
     * Returns the unique identifier of the message part.
     *
     * @return the unique identifier of the message part
     */
    MessagePartId id();

    /**
     * Returns the type of the message part.
     *
     * @return the type of the message part
     */
    String type();

    /**
     * Returns the creation time of the message part.
     *
     * @return the creation time of the message part
     */
    Instant createdAt();

    /**
     * Returns the update time of the message part.
     *
     * @return the update time of the message part
     */
    Instant updatedAt();

    /**
     * Returns the metadata of the message part.
     *
     * @return the metadata of the message part
     */
    @Override
    MetadataProvider metadata();

    /**
     * Builder interface for creating message parts.
     */
    interface Builder {
        /**
         * Sets the unique identifier of the message part.
         *
         * @param id the unique identifier of the message part
         * @return the builder
         */
        Builder id(MessagePartId id);

        /**
         * Sets the creation time of the message part.
         *
         * @param createdAt the creation time of the message part
         * @return the builder
         */
        Builder createdAt(Instant createdAt);

        /**
         * Sets the update time of the message part.
         *
         * @param updatedAt the update time of the message part
         * @return the builder
         */
        Builder updatedAt(Instant updatedAt);

        /**
         * Sets the metadata of the message part.
         *
         * @param metadata the metadata of the message part
         * @return the builder
         */
        Builder metadata(MetadataProvider metadata);

        /**
         * Builds the message part.
         *
         * @return the built message part
         */
        MessagePart build();
    }
}
