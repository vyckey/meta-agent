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

import org.apache.commons.lang3.StringUtils;
import org.metaagent.framework.core.agent.chat.message.Message;
import org.springframework.ai.chat.messages.AssistantMessage;

import static org.metaagent.framework.core.agent.chat.message.MessageMetadataKeys.KEY_REASONING_CONTENT;

/**
 * MessageHelper
 *
 * @author vyckey
 */
public abstract class MessageHelper {

    /**
     * Check if the message is a reasoning message
     *
     * @param message the message
     * @return true if the message is a reasoning message, false otherwise
     */
    public static boolean isReasoningMessage(org.springframework.ai.chat.messages.Message message) {
        if (message instanceof AssistantMessage assistantMessage) {
            return StringUtils.isEmpty(assistantMessage.getText())
                    && assistantMessage.getMetadata().containsKey(KEY_REASONING_CONTENT);
        }
        return false;
    }

    /**
     * Check if the message has a compact message part
     *
     * @param message the message
     * @return true if the message has a compact message part, false otherwise
     */
    public static boolean hasCompactMessagePart(Message message) {
        return message.findPart(part -> CompactionMessagePart.TYPE.equals(part.type())).isPresent();
    }

}
