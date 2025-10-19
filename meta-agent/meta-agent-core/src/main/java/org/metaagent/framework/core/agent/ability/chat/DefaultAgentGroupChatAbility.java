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

package org.metaagent.framework.core.agent.ability.chat;

import org.metaagent.framework.core.agent.ability.AbstractAgentAbility;
import org.metaagent.framework.core.agent.chat.channel.Channel;
import org.metaagent.framework.core.agent.chat.channel.ChannelManager;
import org.metaagent.framework.core.agent.chat.message.Message;
import org.metaagent.framework.core.agent.chat.message.MessageListener;

import java.util.concurrent.CompletableFuture;

/**
 * description is here
 *
 * @author vyckey
 */
public class DefaultAgentGroupChatAbility extends AbstractAgentAbility implements AgentGroupChatAbility {
    protected final ChannelManager channelManager;

    public DefaultAgentGroupChatAbility(String name, ChannelManager channelManager) {
        super(name);
        this.channelManager = channelManager;
    }

    public DefaultAgentGroupChatAbility(ChannelManager channelManager) {
        this("AgentGroupChatAbility", channelManager);
    }

    @Override
    public ChannelManager getChannelManager() {
        return channelManager;
    }

    private Channel getChannel(String channelName) {
        Channel channel = channelManager.getChannel(channelName);
        if (channel == null) {
            throw new IllegalStateException("Channel not found: " + channelName);
        }
        return channel;
    }

    @Override
    public void sendMessage(String channelName, Message message) {
        checkActivated();
        getChannel(channelName).send(message);
    }

    @Override
    public CompletableFuture<Void> sendMessageAsync(String channelName, Message message) {
        checkActivated();
        return getChannel(channelName).sendAsync(message);
    }

    @Override
    public void subscribeMessage(String channelName, MessageListener messageListener) {
        getChannel(channelName).receive(message -> {
            if (isActivated()) {
                messageListener.onMessage(message);
            }
        });
    }
}
