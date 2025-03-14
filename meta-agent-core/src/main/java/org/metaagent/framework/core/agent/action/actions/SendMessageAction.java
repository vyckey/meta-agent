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

package org.metaagent.framework.core.agent.action.actions;

import lombok.Getter;
import org.metaagent.framework.core.agent.action.ActionExecuteContext;
import org.metaagent.framework.core.agent.action.result.ActionResult;
import org.metaagent.framework.core.agent.action.result.DefaultActionResult;
import org.metaagent.framework.core.agent.chat.channel.Channel;
import org.metaagent.framework.core.agent.chat.channel.ChannelManager;
import org.metaagent.framework.core.agent.chat.message.Message;

import java.util.Objects;

/**
 * description is here
 *
 * @author vyckey
 */
@Getter
public class SendMessageAction extends AbstractAction {
    public static final String NAME = "SendMessage";
    private final Channel channel;
    private final Message message;

    public SendMessageAction(Channel channel, Message message) {
        super(NAME);
        this.channel = Objects.requireNonNull(channel, "channel is required");
        this.message = Objects.requireNonNull(message, "message is required");
        this.description = message.getContent();
    }

    public SendMessageAction(ChannelManager channelManager, String channelName, Message message) {
        this(channelManager.getChannel(channelName), message);
    }

    @Override
    protected ActionResult doExecute(ActionExecuteContext context) {
        channel.send(message);
        return DefaultActionResult.success(true);
    }
}
