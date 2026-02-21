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

package org.metaagent.framework.core.agents.chat.output;

import com.google.common.collect.Lists;
import org.metaagent.framework.common.metadata.MetadataProvider;
import org.metaagent.framework.core.agent.chat.message.MessageInfo;
import org.metaagent.framework.core.agent.chat.message.RoleMessage;
import org.metaagent.framework.core.agent.chat.message.StreamMessageAggregator;
import org.metaagent.framework.core.agent.chat.message.part.MessagePart;
import org.metaagent.framework.core.agent.output.StreamOutput;

import java.util.List;

public record DefaultChatStreamOutput(MessagePart message, MetadataProvider metadata) implements ChatStreamOutput {
    public static StreamOutput.Aggregator<MessagePart, ChatOutput> aggregator(MessageInfo messageInfo) {
        return streamOutputs -> {
            MetadataProvider metadata = MetadataProvider.create();
            List<MessagePart> streamMessages = Lists.newArrayList();
            for (MessagePart messagePart : streamOutputs) {
                streamMessages.add(messagePart);
            }

            List<MessagePart> aggregatedMessages = StreamMessageAggregator.INSTANCE.aggregate(streamMessages);
            MessageInfo info = messageInfo.toBuilder()
                    .createdAt(aggregatedMessages.isEmpty() ? messageInfo.createdAt() : aggregatedMessages.get(0).createdAt())
                    .updatedAt(aggregatedMessages.isEmpty() ? messageInfo.updatedAt() : aggregatedMessages.get(aggregatedMessages.size() - 1).updatedAt())
                    .build();
            return ChatOutput.builder()
                    .message(new RoleMessage(info, aggregatedMessages))
                    .metadata(metadata)
                    .build();
        };
    }
}
