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

package org.metaagent.framework.core.model.chat.compression;

import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.model.ModelResult;

import java.util.List;

/**
 * Context compression result.
 *
 * @author vyckey
 */
public interface CompressionResult extends ModelResult<Message> {
    /**
     * Returns true if the context was compressed.
     *
     * @return true if the context was compressed.
     */
    boolean isCompressed();

    /**
     * Returns the compression ratio.
     *
     * @return the compression ratio.
     */
    float getCompressionRatio();

    /**
     * Returns the summary message.
     *
     * @return the summary message.
     */
    Message getSummary();

    /**
     * Returns the removed messages.
     *
     * @return the removed messages.
     */
    List<Message> getRemovedMessages();

    /**
     * Returns the retained messages.
     *
     * @return the retained messages.
     */
    List<Message> getRetainedMessages();

    @Override
    default Message getOutput() {
        return getSummary();
    }
}
