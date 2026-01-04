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
import org.springframework.ai.model.Model;

import java.util.List;

/**
 * A model for compressing chat messages.
 *
 * @author vyckey
 */
public interface CompressionModel extends Model<CompressionRequest, CompressionResponse> {
    /**
     * Counts the number of tokens in given messages.
     * Default implementation uses the length of the message text which isn't very accurate, and it's merely a rough estimation.
     */
    int countTokens(List<Message> messages);

    /**
     * Compresses the given chat messages.
     *
     * @param compressionRequest the request containing the chat messages to compress
     * @return the response containing the compressed chat messages
     */
    @Override
    CompressionResponse call(CompressionRequest compressionRequest);
}
