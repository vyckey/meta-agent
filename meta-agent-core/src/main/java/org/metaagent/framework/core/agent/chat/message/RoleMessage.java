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

import org.metaagent.framework.core.common.media.MediaResource;

import java.util.List;

/**
 * A message that has a role associated with it, such as "user" or "assistant".
 *
 * @author vyckey
 */
public interface RoleMessage extends Message {
    String ROLE_USER = "user";
    String ROLE_ASSISTANT = "assistant";

    /**
     * Creates a new RoleMessage with the specified role, content, and media resources.
     *
     * @param role    the role of the message
     * @param content the content of the message
     * @param media   optional media resources associated with the message
     * @return a new RoleMessage instance
     */
    static RoleMessage create(String role, String content, MediaResource... media) {
        return new DefaultRoleMessage(role, content, List.of(media));
    }

    /**
     * Creates a new RoleMessage with the user role.
     *
     * @param content the content of the message
     * @param media   optional media resources associated with the message
     * @return a new RoleMessage instance with the user role
     */
    static RoleMessage user(String content, MediaResource... media) {
        return new UserMessage(content, List.of(media));
    }

    /**
     * Creates a new RoleMessage with the assistant role.
     *
     * @param content the content of the message
     * @param media   optional media resources associated with the message
     * @return a new RoleMessage instance with the assistant role
     */
    static AssistantMessage assistant(String content, MediaResource... media) {
        return new AssistantMessage(content, List.of(media));
    }

    /**
     * Get the role of the message.
     *
     * @return the role of the message
     */
    String getRole();

    /**
     * Get the media resources associated with the message.
     *
     * @return a list of media resources
     */
    List<MediaResource> getMedia();
}
