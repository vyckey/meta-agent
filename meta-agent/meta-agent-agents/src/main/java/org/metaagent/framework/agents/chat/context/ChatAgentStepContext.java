/*
 * MIT License
 *
 * Copyright (c) 2026 MetaAgent
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

package org.metaagent.framework.agents.chat.context;

import org.metaagent.framework.core.agent.chat.message.MessageId;
import org.metaagent.framework.core.agent.chat.message.MessageInfo;
import org.metaagent.framework.core.agent.chat.session.Session;
import org.metaagent.framework.core.agent.context.AgentStepContext;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

public class ChatAgentStepContext implements AgentStepContext {
    private final AtomicInteger loopCounter = new AtomicInteger();
    private final Session session;
    private MessageInfo outputMessageInfo;

    private ChatAgentStepContext(Builder builder) {
        this.session = builder.session;
        this.outputMessageInfo = builder.outputMessageInfo;
    }

    public static Builder builder() {
        return new Builder();
    }

    @Override
    public AtomicInteger getLoopCounter() {
        return loopCounter;
    }

    public Session getSession() {
        return session;
    }

    public MessageInfo getOutputMessageInfo() {
        return outputMessageInfo;
    }

    public void setOutputMessageInfo(MessageInfo outputMessageInfo) {
        this.outputMessageInfo = outputMessageInfo;
    }

    public MessageId getOutputMessageId() {
        return Objects.requireNonNull(outputMessageInfo).id();
    }

    @Override
    public void reset() {
        loopCounter.set(0);
    }

    public static class Builder {
        private Session session;
        private MessageInfo outputMessageInfo;

        public Builder session(Session session) {
            this.session = session;
            return this;
        }

        public Builder outputMessageInfo(MessageInfo outputMessageInfo) {
            this.outputMessageInfo = outputMessageInfo;
            return this;
        }

        public ChatAgentStepContext build() {
            return new ChatAgentStepContext(this);
        }
    }
}
