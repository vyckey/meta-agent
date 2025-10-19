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

package org.metaagent.framework.core.agent.chat.channel;

import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.metaagent.framework.core.agent.chat.message.Message;
import org.metaagent.framework.core.agent.chat.message.MessageListener;
import org.metaagent.framework.core.agent.chat.message.conversation.Conversation;
import org.metaagent.framework.core.agent.chat.message.conversation.DefaultConversation;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * description is here
 *
 * @author vyckey
 */
@Slf4j
public class DefaultChannel implements Channel {
    protected final String name;
    protected final List<MessageListener> messageListeners = Lists.newArrayList();
    protected Conversation conversation = new DefaultConversation();
    protected boolean open = true;

    public DefaultChannel(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public boolean isOpen() {
        return open;
    }

    protected void checkChannelOpen() {
        if (!open) {
            throw new IllegalStateException("channel " + name + " is closed");
        }
    }

    @Override
    public void close() throws IOException {
        this.open = false;
    }

    @Override
    public void send(Message message) {
        checkChannelOpen();
        conversation.appendMessage(message);
        notifyReceiver(message);
    }

    @Override
    public CompletableFuture<Void> sendAsync(Message message) {
        return CompletableFuture.runAsync(() -> send(message));
    }

    protected void notifyReceiver(Message message) {
        for (MessageListener messageListener : messageListeners) {
            try {
                messageListener.onMessage(message);
            } catch (Exception e) {
                log.error("notify message receiver error", e);
            }
        }
    }

    @Override
    public void receive(MessageListener messageListener) {
        messageListeners.add(messageListener);
    }

    @Override
    public void remove(MessageListener messageListener) {
        messageListeners.remove(messageListener);
    }

    @Override
    public Conversation conversation() {
        return conversation;
    }

    @Override
    public String toString() {
        return "Channel{name=" + getName() + "}";
    }
}
