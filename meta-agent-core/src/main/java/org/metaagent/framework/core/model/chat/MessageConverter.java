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

package org.metaagent.framework.core.model.chat;

import org.metaagent.framework.core.agent.chat.message.Message;
import org.metaagent.framework.core.agent.chat.message.RoleMessage;
import org.metaagent.framework.core.converter.BiConverter;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.MessageType;

/**
 * description is here
 *
 * @author vyckey
 */
public class MessageConverter implements BiConverter<Message, org.springframework.ai.chat.messages.Message> {
    public static final MessageConverter INSTANCE = new MessageConverter();

    @Override
    public org.springframework.ai.chat.messages.Message convert(Message message) {
        return switch (MessageType.fromValue(message.getRole())) {
            case ASSISTANT -> new AssistantMessage(message.getContent(), message.getMetadata().getProperties());
            case USER -> new org.springframework.ai.chat.messages.UserMessage(message.getContent());
            default -> throw new IllegalArgumentException("message cannot be converted");
        };
    }

    @Override
    public Message reverse(org.springframework.ai.chat.messages.Message message) {
        switch (message.getMessageType()) {
            case USER, ASSISTANT -> {
                String role = message.getMessageType().getValue();
                RoleMessage roleMessage = RoleMessage.create(role, message.getText());
                message.getMetadata().forEach(roleMessage.getMetadata()::setProperty);
                return roleMessage;
            }
            default -> throw new IllegalArgumentException("message cannot be converted");
        }
    }
}
