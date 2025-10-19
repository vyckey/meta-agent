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

import com.google.common.collect.Lists;
import org.metaagent.framework.common.content.MediaResource;
import org.metaagent.framework.common.metadata.MetadataProvider;

import java.util.List;

/**
 * Utility class to aggregate stream messages into complete messages.
 *
 * @author vyckey
 */
public class StreamMessageAggregator {
    public static final StreamMessageAggregator INSTANCE = new StreamMessageAggregator();

    public List<Message> aggregate(Iterable<Message> streamMessages) {
        List<List<Message>> streamMessageGroup = Lists.newArrayList();
        for (Message streamMessage : streamMessages) {
            if (streamMessageGroup.isEmpty()) {
                streamMessageGroup.add(Lists.newArrayList(streamMessage));
            } else {
                List<Message> lastGroup = streamMessageGroup.get(streamMessageGroup.size() - 1);
                Message lastMessage = lastGroup.get(lastGroup.size() - 1);
                if (streamMessage instanceof RoleMessage && lastMessage instanceof RoleMessage
                        && streamMessage.getRole().equals(lastMessage.getRole())
                        && streamMessage.getClass().equals(lastMessage.getClass())) {
                    lastGroup.add(streamMessage);
                } else {
                    streamMessageGroup.add(Lists.newArrayList(streamMessage));
                }
            }
        }

        List<Message> aggregatedMessages = Lists.newArrayList();
        for (List<Message> messageGroup : streamMessageGroup) {
            aggregate(aggregatedMessages, messageGroup);
        }
        return aggregatedMessages;
    }

    protected void aggregate(List<Message> aggregatedMessages, List<Message> messageGroup) {
        if (messageGroup.get(0) instanceof RoleMessage) {
            String role = messageGroup.get(0).getRole();
            StringBuilder sb = new StringBuilder();
            List<MediaResource> media = Lists.newArrayList();
            MetadataProvider metadata = MetadataProvider.create();
            for (Message message : messageGroup) {
                sb.append(message.getContent());
                media.addAll(message.getMedia());
                metadata.merge(message.getMetadata());
            }
            aggregatedMessages.add(new RoleMessage(role, sb.toString(), media, metadata));
        } else {
            aggregatedMessages.addAll(messageGroup);
        }
    }
}
