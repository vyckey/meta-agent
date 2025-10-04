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
import org.metaagent.framework.core.agent.chat.message.AssistantMessage;
import org.metaagent.framework.core.agent.chat.message.Message;
import org.metaagent.framework.core.agent.chat.message.RoleMessage;
import org.metaagent.framework.core.agent.chat.message.ToolResponseMessage;
import org.metaagent.framework.core.agent.chat.message.UserMessage;
import org.metaagent.framework.core.agent.output.AgentOutput;
import org.metaagent.framework.core.agent.output.AgentStreamOutputAggregator;
import org.metaagent.framework.core.common.media.MediaResource;
import org.metaagent.framework.core.common.metadata.MetadataProvider;

import java.util.List;

public class ChatAgentStreamOutputAggregator implements AgentStreamOutputAggregator<ChatAgentStreamOutput, ChatAgentOutput> {
    public static final ChatAgentStreamOutputAggregator INSTANCE = new ChatAgentStreamOutputAggregator();

    @Override
    public AgentOutput<ChatAgentOutput> aggregate(Iterable<AgentOutput<ChatAgentStreamOutput>> streamOutputs) {
        List<Message> messages = Lists.newArrayList();

        Class<? extends Message> lastMessageClass = null;
        StringBuilder sb = new StringBuilder();
        List<MediaResource> media = Lists.newArrayList();
        MetadataProvider metadata = MetadataProvider.create();
        List<AssistantMessage.ToolCall> toolCalls = Lists.newArrayList();
        for (AgentOutput<ChatAgentStreamOutput> streamOutput : streamOutputs) {
            ChatAgentStreamOutput result = streamOutput.result();
            Message streamMessage = result.message();

            // If the message type changes, or it's a ToolResponseMessage, finalize the previous message
            boolean reset = false;
            if (streamMessage instanceof ToolResponseMessage) {
                messages.add(streamMessage);
                reset = true;
            } else if (lastMessageClass != null && !lastMessageClass.equals(streamMessage.getClass())) {
                Message newMessage = buildMessage(lastMessageClass, sb.toString(), media, toolCalls, metadata);
                messages.add(newMessage);
                reset = true;
            }
            if (reset) {
                sb = new StringBuilder();
                media.clear();
                metadata = MetadataProvider.create();
                toolCalls.clear();
            }

            // Accumulate content, media, metadata, and tool calls
            if (streamMessage instanceof RoleMessage roleMessage) {
                sb.append(roleMessage.getContent());
                media.addAll(roleMessage.getMedia());
                metadata.merge(roleMessage.getMetadata());
                if (streamMessage instanceof AssistantMessage assistantMessage) {
                    toolCalls.addAll(assistantMessage.getToolCalls());
                }
            }
            lastMessageClass = streamMessage.getClass();
        }
        return AgentOutput.create(new ChatAgentOutput(messages));
    }

    protected Message buildMessage(Class<? extends Message> messageClass, String content,
                                   List<MediaResource> media,
                                   List<AssistantMessage.ToolCall> toolCalls,
                                   MetadataProvider metadata) {
        if (messageClass.equals(AssistantMessage.class)) {
            return new AssistantMessage(content, media, toolCalls, metadata);
        } else if (messageClass.equals(UserMessage.class)) {
            return new UserMessage(content, media, metadata);
        }
        throw new IllegalArgumentException("Unsupported message class: " + messageClass);
    }
}
