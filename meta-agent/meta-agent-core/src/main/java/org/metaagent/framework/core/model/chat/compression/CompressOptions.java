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

import org.springframework.ai.model.ModelOptions;

/**
 * Context compress options
 *
 * @author vyckey
 */
public interface CompressOptions extends ModelOptions {

    /**
     * Returns max tokens limit for context
     *
     * @return max tokens limit
     */
    int getMaxTokens();

    /**
     * Returns the reserved messages count. When there is a conflict between the max tokens limit
     * and the reserved messages count, the max tokens limit will be used.
     *
     * @return the reserved messages count
     */
    int getReservedMessageCount();

    /**
     * Returns compress prompt which will be appended to the system prompt of the compression model. Default is null.
     *
     * @return the compress prompt
     */
    String getCompressPrompt();
}
