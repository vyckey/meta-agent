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

package org.metaagent.framework.core.agent.chat.conversation;

import com.google.common.collect.Lists;
import org.metaagent.framework.core.agent.chat.message.Message;
import org.metaagent.framework.core.agent.chat.message.MessageId;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;

/**
 * Default implementation of {@link TurnBasedConversation} that stores
 * messages in a list of {@link MessageTurn}s.
 *
 * @author vyckey
 */
public class DefaultTurnBasedConversation implements TurnBasedConversation {
    protected final String conversationId;
    protected final List<MessageTurn> turns;

    public DefaultTurnBasedConversation(String conversationId, List<MessageTurn> turns) {
        this.conversationId = Objects.requireNonNull(conversationId, "conversationId is required");
        this.turns = Objects.requireNonNull(turns, "turns is required");
    }

    public DefaultTurnBasedConversation(String conversationId) {
        this(conversationId, Lists.newArrayList());
    }

    public DefaultTurnBasedConversation() {
        this(DefaultConversation.generateConversationId());
    }

    @Override
    public String id() {
        return conversationId;
    }

    @Override
    public Iterable<MessageTurn> turns(boolean reverse) {
        return reverse ? Lists.reverse(turns) : turns;
    }

    @Override
    public MessageTurn lastTurn() {
        return turns.isEmpty() ? null : turns.get(turns.size() - 1);
    }

    @Override
    public List<MessageTurn> lastTurns(int count) {
        List<MessageTurn> lastTurns = Lists.newArrayListWithExpectedSize(count);
        for (MessageTurn turn : turns(true)) {
            if (lastTurns.size() >= count) {
                break;
            }
            lastTurns.add(turn);
        }
        return Lists.reverse(lastTurns);
    }

    @Override
    public MessageTurn newTurn() {
        MessageTurn lastTurn = lastTurn();
        if (lastTurn != null && !lastTurn.isFinished()) {
            lastTurn.setFinished(true);
        }
        DefaultMessageTurn turn = new DefaultMessageTurn();
        appendTurn(turn);
        return turn;
    }

    @Override
    public void appendTurn(MessageTurn turn) {
        this.turns.add(turn);
    }

    @Override
    public boolean isEmpty() {
        return turns.isEmpty() || turns.size() == 1 && turns.get(0).messages().isEmpty();
    }

    @Override
    public List<Message> findMessages(Predicate<Message> predicate, boolean reverse) {
        List<Message> foundMessages = turns.stream().map(MessageTurn::messages).flatMap(List::stream).filter(predicate).toList();
        return reverse ? Lists.reverse(foundMessages) : foundMessages;
    }

    @Override
    public Optional<Message> findMessage(Predicate<Message> predicate, boolean reverse) {
        Iterator<Message> messageIterator = reverse ? reverse().iterator() : iterator();
        while (messageIterator.hasNext()) {
            Message message = messageIterator.next();
            if (predicate.test(message)) {
                return Optional.of(message);
            }
        }
        return Optional.empty();
    }

    @Override
    public Optional<Message> lastMessage() {
        for (Message message : reverse()) {
            return Optional.of(message);
        }
        return Optional.empty();
    }

    @Override
    public List<Message> lastMessages(int count) {
        List<Message> lastMessages = Lists.newArrayListWithExpectedSize(count);
        for (Message message : reverse()) {
            if (lastMessages.size() >= count) {
                break;
            }
            lastMessages.add(message);
        }
        return Lists.reverse(lastMessages);
    }

    @Override
    public void resetAfter(MessageId messageId, boolean inclusive) {
        Iterator<MessageTurn> turnIterator = turns(true).iterator();
        while (turnIterator.hasNext()) {
            MessageTurn turn = turnIterator.next();
            int index = -1;
            for (int i = 0; i < turn.messages().size(); i++) {
                if (turn.messages().get(i).getId().equals(messageId)) {
                    index = i;
                    break;
                }
            }
            if (index < 0) {
                turnIterator.remove();
            } else if (!inclusive && index + 1 == turn.messages().size()) {
                // no turn needed to remove
                break;
            } else {
                List<Message> messages = turn.messages().subList(0, inclusive ? index + 1 : index);
                turnIterator.remove();
                appendTurn(new DefaultMessageTurn(messages, true));
                break;
            }
        }
    }

    @Override
    public void clear() {
        this.turns.clear();
    }

    @Override
    public Iterable<Message> reverse() {
        return () -> new Iterator<>() {
            private final Iterator<MessageTurn> turnIterator = turns(true).iterator();
            private Iterator<Message> messageIterator = turnIterator.hasNext()
                    ? Lists.reverse(turnIterator.next().messages()).iterator()
                    : Collections.emptyIterator();

            @Override
            public boolean hasNext() {
                while (!messageIterator.hasNext() && turnIterator.hasNext()) {
                    messageIterator = Lists.reverse(turnIterator.next().messages()).iterator();
                }
                return messageIterator.hasNext();
            }

            @Override
            public Message next() {
                if (!hasNext()) throw new NoSuchElementException();
                return messageIterator.next();
            }
        };
    }

    @Override
    public Iterator<Message> iterator() {
        return new Iterator<>() {
            private final Iterator<MessageTurn> turnIterator = turns(false).iterator();
            private Iterator<Message> messageIterator = turnIterator.hasNext() ? turnIterator.next().iterator()
                    : Collections.emptyIterator();

            @Override
            public boolean hasNext() {
                while (!messageIterator.hasNext() && turnIterator.hasNext()) {
                    messageIterator = turnIterator.next().iterator();
                }
                return messageIterator.hasNext();
            }

            @Override
            public Message next() {
                if (!hasNext()) throw new NoSuchElementException();
                return messageIterator.next();
            }
        };
    }

    public String asText(int maxTurnSize) {
        int maxOutputTurnSize = Math.min(maxTurnSize, turns.size());
        List<MessageTurn> outputTurns = turns.subList(turns.size() - maxOutputTurnSize, turns.size());

        StringBuilder sb = new StringBuilder()
                .append("Conversation history (ID=").append(conversationId).append("):\n");
        if (outputTurns.isEmpty()) {
            sb.append("<empty messages>\n");
        }
        for (MessageTurn turn : outputTurns) {
            sb.append(turn).append("\n");
        }
        return sb.toString();
    }

    @Override
    public String toString() {
        return asText(4);
    }
}
