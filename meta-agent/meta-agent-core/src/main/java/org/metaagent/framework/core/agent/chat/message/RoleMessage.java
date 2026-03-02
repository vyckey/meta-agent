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

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.metaagent.framework.common.metadata.MetadataProvider;
import org.metaagent.framework.core.agent.chat.message.part.MessagePart;
import org.metaagent.framework.core.agent.chat.message.part.TextMessagePart;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * RoleMessage is a message that contains role, content and media resources.
 *
 * @author vyckey
 */
public record RoleMessage(
        @JsonDeserialize(as = RoleMessageInfo.class)
        MessageInfo info,

        List<MessagePart> parts
) implements Message {

    public RoleMessage {
        Objects.requireNonNull(info, "info is required");
        Objects.requireNonNull(parts, "parts is required");
    }

    public RoleMessage(MessageInfo info, MessagePart... parts) {
        this(info, List.of(parts));
    }

    public static Builder builder() {
        return new Builder();
    }

    public static RoleMessage of(String role, List<MessagePart> messageParts, MetadataProvider metadata) {
        return RoleMessage.builder()
                .info(RoleMessageInfo.builder().role(role).metadata(metadata).build())
                .parts(messageParts)
                .build();
    }

    public static RoleMessage of(String role, List<MessagePart> messageParts) {
        return of(role, messageParts, MetadataProvider.empty());
    }

    public static RoleMessage user(MessagePart... messageParts) {
        return of(MessageInfo.ROLE_USER, List.of(messageParts));
    }

    public static RoleMessage user(String content) {
        return user(new TextMessagePart(content));
    }

    public static RoleMessage assistant(MessagePart... messageParts) {
        return of(MessageInfo.ROLE_ASSISTANT, List.of(messageParts));
    }

    public static RoleMessage assistant(String content) {
        return assistant(new TextMessagePart(content));
    }

    @Override
    public String content() {
        return parts().stream().map(MessagePart::content).collect(Collectors.joining("\n"));
    }


    public static class Builder implements Message.Builder {
        private MessageInfo info;
        private List<MessagePart> parts;

        public Builder() {
        }

        @Override
        public Builder info(MessageInfo info) {
            this.info = info;
            return this;
        }

        @Override
        public Builder parts(List<MessagePart> parts) {
            this.parts = parts;
            return this;
        }

        @Override
        public Builder addPart(MessagePart contentPart) {
            if (this.parts == null) {
                this.parts = new ArrayList<>();
            }
            this.parts.add(contentPart);
            return this;
        }

        public RoleMessage build() {
            if (parts == null) {
                parts = List.of();
            }
            return new RoleMessage(info, parts);
        }
    }
}
