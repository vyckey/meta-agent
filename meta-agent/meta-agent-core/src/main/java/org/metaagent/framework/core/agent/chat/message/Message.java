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

package org.metaagent.framework.core.agent.chat.message;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import org.metaagent.framework.common.content.TextContent;
import org.metaagent.framework.common.metadata.MetadataProvider;
import org.metaagent.framework.core.agent.chat.message.part.MessagePart;
import org.metaagent.framework.core.agent.chat.message.part.MessagePartId;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

/**
 * The message interface represents a message in a chat.
 *
 * @author vyckey
 */
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "@class",
        defaultImpl = RoleMessage.class
)
public interface Message extends TextContent {
    DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssXXX");

    /**
     * Get the info of the message.
     *
     * @return the info of the message
     */
    MessageInfo info();

    /**
     * Get the content of the message.
     *
     * @return the content of the message
     */
    List<MessagePart> parts();

    /**
     * Get the metadata of the message.
     *
     * @return the metadata of the message
     */
    @Override
    default MetadataProvider metadata() {
        return info().metadata();
    }

    /**
     * Get the part of the message.
     *
     * @param partId the id of the part
     * @return the part of the message
     */
    default Optional<MessagePart> part(MessagePartId partId) {
        return parts().stream().filter(part -> part.id().equals(partId)).findFirst();
    }

    /**
     * Get the builder of the message.
     *
     * @return the builder of the message
     */
    Builder toBuilder();

    /**
     * Copy and set the parent id of the new message.
     *
     * @param parentId the parent id of the message
     * @return the new message
     */
    default Message withParentId(MessageId parentId) {
        MessageInfo newMessageInfo = info().toBuilder().parentId(parentId).build();
        return toBuilder().info(newMessageInfo).build();
    }

    /**
     * The builder interface for building a message.
     *
     * @author vyckey
     */
    interface Builder {
        /**
         * Set the info of the message.
         *
         * @param info the info of the message
         * @return the builder
         */
        Builder info(MessageInfo info);

        /**
         * Set the parts of the message.
         *
         * @param parts the parts of the message
         * @return the builder
         */
        Builder parts(List<MessagePart> parts);

        /**
         * Add a part to the message.
         *
         * @param part the part to add
         * @return the builder
         */
        Builder addPart(MessagePart part);

        /**
         * Build the message.
         *
         * @return the message
         */
        Message build();
    }
}
