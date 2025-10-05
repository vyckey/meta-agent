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

import com.google.common.collect.Sets;
import org.metaagent.framework.core.agent.chat.message.Message;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;

/**
 * description is here
 *
 * @author vyckey
 */
public class DefaultGroupChanel extends DefaultChannel implements GroupChannel {
    protected final Set<String> members = Sets.newHashSet();

    public DefaultGroupChanel(String name) {
        super(channelName(name));
    }

    private static String channelName(String name) {
        if (!name.startsWith("#")) {
            throw new IllegalArgumentException("group channel name must start with #");
        }
        return name;
    }

    @Override
    public Set<String> members() {
        return Collections.unmodifiableSet(members);
    }

    @Override
    public void addMembers(Collection<String> members) {
        checkChannelOpen();
        for (String member : members) {
            if (this.members.contains(member)) {
                throw new IllegalArgumentException("member " + member + " is already in channel " + name);
            }
        }
        this.members.addAll(members);
    }

    @Override
    public void removeMembers(Collection<String> members) {
        checkChannelOpen();
        for (String member : members) {
            if (this.members.contains(member)) {
                throw new IllegalArgumentException("member " + member + " is not in channel " + name);
            }
        }
        members.forEach(this.members::remove);
    }

    @Override
    public void send(Message message) {
        if (!members.contains(message.getRole())) {
            throw new IllegalArgumentException("sender " + message.getRole() + " is not in channel " + name);
        }
        super.send(message);
    }
}
