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

package org.metaagent.framework.core.agents.chat;

import com.google.common.collect.Lists;
import org.metaagent.framework.common.content.MediaResource;
import org.metaagent.framework.common.metadata.MetadataProvider;
import org.metaagent.framework.core.agent.chat.message.Message;
import org.metaagent.framework.core.agent.chat.message.RoleMessage;
import org.metaagent.framework.core.agent.output.AgentOutput;
import org.metaagent.framework.core.agent.output.AgentStreamOutputAggregator;
import org.metaagent.framework.core.model.chat.message.ToolCallMessage;
import org.metaagent.framework.core.model.chat.message.ToolResponseMessage;

import java.util.List;

public class ChatAgentStreamOutputAggregator implements AgentStreamOutputAggregator<ChatAgentStreamOutput, ChatAgentOutput> {
    public static final ChatAgentStreamOutputAggregator INSTANCE = new ChatAgentStreamOutputAggregator();

    @Override
    public AgentOutput<ChatAgentOutput> aggregate(Iterable<AgentOutput<ChatAgentStreamOutput>> streamOutputs) {
        List<Message> messages = Lists.newArrayList();

        Class<? extends Message> lastMessageClass = null;
        StringBuilder sb = new StringBuilder();
        String lastRole = null;
        List<MediaResource> media = Lists.newArrayList();
        MetadataProvider metadata = MetadataProvider.create();
        for (AgentOutput<ChatAgentStreamOutput> streamOutput : streamOutputs) {
            ChatAgentStreamOutput result = streamOutput.result();
            Message streamMessage = result.message();

            // If the message type changes, or it's a ToolResponseMessage, finalize the previous message
            boolean reset = false;
            if (streamMessage instanceof ToolCallMessage || streamMessage instanceof ToolResponseMessage) {
                messages.add(streamMessage);
                reset = true;
            } else if (lastRole != null && !streamMessage.getRole().equals(lastRole) ||
                    lastMessageClass != null && !lastMessageClass.equals(streamMessage.getClass())) {
                Message newMessage = buildMessage(lastMessageClass, lastRole, sb.toString(), media, metadata);
                messages.add(newMessage);
                reset = true;
            }
            if (reset) {
                sb = new StringBuilder();
                media.clear();
                metadata = MetadataProvider.create();
            }

            // Accumulate content, media, metadata, and tool calls
            if (streamMessage instanceof RoleMessage roleMessage) {
                sb.append(roleMessage.getContent());
                media.addAll(roleMessage.getMedia());
                metadata.merge(roleMessage.getMetadata());
            }
            lastRole = streamMessage.getRole();
            lastMessageClass = streamMessage.getClass();
        }
        return AgentOutput.create(new ChatAgentOutput(messages));
    }

    protected Message buildMessage(Class<? extends Message> messageClass,
                                   String role,
                                   String content,
                                   List<MediaResource> media,
                                   MetadataProvider metadata) {
        if (messageClass.equals(RoleMessage.class)) {
            return new RoleMessage(role, content, media, metadata);
        }
        throw new IllegalArgumentException("Unsupported message class: " + messageClass);
    }
}
