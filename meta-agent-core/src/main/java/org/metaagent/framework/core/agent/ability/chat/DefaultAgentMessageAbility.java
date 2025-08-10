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
import org.metaagent.framework.core.agent.chat.message.MessageListener;
import org.metaagent.framework.core.agent.chat.message.RoleMessage;

import java.util.concurrent.Future;

/**
 * description is here
 *
 * @author vyckey
 */
public class DefaultAgentMessageAbility extends AbstractAgentAbility implements AgentMessageAbility {
    protected final Channel channel;
    protected MessageListener messageListener;

    public DefaultAgentMessageAbility(String name, Channel channel) {
        super(name);
        this.channel = channel;
    }

    public DefaultAgentMessageAbility(Channel channel) {
        this("AgentChatAbility", channel);
    }

    @Override
    public Channel getChannel() {
        return channel;
    }

    @Override
    public void sendMessage(RoleMessage message) {
        checkActivated();
        this.channel.send(message);
    }

    @Override
    public Future<Void> sendMessageAsync(RoleMessage message) {
        checkActivated();
        return this.channel.sendAsync(message);
    }

    @Override
    public void subscribeMessage(MessageListener messageListener) {
        this.channel.remove(message -> {
            if (isActivated()) {
                messageListener.onMessage(message);
            }
        });
    }
}
