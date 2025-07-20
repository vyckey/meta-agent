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

import org.metaagent.framework.core.common.metadata.MetadataProvider;

import java.time.Instant;

/**
 * The message interface represents a message in a chat.
 *
 * @author vyckey
 */
public interface Message {
    /**
     * Get the metadata of the message.
     *
     * @return the metadata of the message
     */
    MetadataProvider getMetadata();

    /**
     * Get the role of the message.
     *
     * @return the role of the message
     */
    String getRole();

    /**
     * Get the text content of the message.
     *
     * @return the content of the message
     */
    String getContent();

    /**
     * Get the time when the message was created.
     *
     * @return the time when the message was created
     */
    Instant getCreatedAt();
}
