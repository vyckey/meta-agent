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

package org.metaagent.framework.core.agents.llm.message;

import org.metaagent.framework.core.agent.chat.message.MessageInfo;
import org.metaagent.framework.core.agent.chat.message.part.MessageChunk;
import org.metaagent.framework.core.agent.chat.message.part.MessagePart;

import java.util.Objects;

/**
 * StreamMessageChunk represents a chunk of a message that is being streamed.
 *
 * @param info the message info associated with this chunk
 * @param part the message part that contains the content of this chunk
 * @author vyckey
 * @see MessageChunk
 */
public record StreamMessageChunk(
        MessageInfo info,
        MessagePart part
) implements MessageChunk {
    public StreamMessageChunk {
        Objects.requireNonNull(info, "info cannot be null");
        Objects.requireNonNull(part, "part cannot be null");
    }

    public static StreamMessageChunk from(MessageInfo info, MessagePart part) {
        return new StreamMessageChunk(info, part);
    }
}
