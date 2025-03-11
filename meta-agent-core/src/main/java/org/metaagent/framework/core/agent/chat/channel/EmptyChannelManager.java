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

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * description is here
 *
 * @author vyckey
 */
public final class EmptyChannelManager implements ChannelManager {
    public static final EmptyChannelManager INSTANCE = new EmptyChannelManager();

    private EmptyChannelManager() {
    }

    @Override
    public Set<String> getChannelNames() {
        return Collections.emptySet();
    }

    @Override
    public Channel getChannel(String channelName) {
        return null;
    }

    @Override
    public void addChannel(Channel channel) {
        throw new UnsupportedOperationException("Empty ChannelManager does not support addChannel");
    }

    @Override
    public void removeChannel(String channelName) {
        throw new UnsupportedOperationException("Empty ChannelManager does not support removeChannel");
    }

    @Override
    public Iterator<Channel> iterator() {
        return new Iterator<Channel>() {
            @Override
            public boolean hasNext() {
                return false;
            }

            @Override
            public Channel next() {
                throw new IllegalStateException();
            }
        };
    }
}
