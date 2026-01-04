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
import org.junit.jupiter.api.Test;
import org.metaagent.framework.core.agent.chat.conversation.DefaultMessageTurn;
import org.metaagent.framework.core.agent.chat.conversation.DefaultTurnBasedConversation;
import org.metaagent.framework.core.agent.chat.conversation.MessageTurn;
import org.metaagent.framework.core.agent.chat.conversation.TurnBasedConversation;
import org.metaagent.framework.core.agent.chat.message.Message;
import org.metaagent.framework.core.agent.chat.message.MessageId;
import org.metaagent.framework.core.agent.chat.message.RoleMessage;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TurnBasedConversationTest {

    @Test
    void testConversationId() {
        String id = "test-id";
        TurnBasedConversation conversation = new DefaultTurnBasedConversation(id);
        assertEquals(id, conversation.id());
    }

    @Test
    void testDefaultConversationId() {
        TurnBasedConversation conversation = new DefaultTurnBasedConversation();
        assertNotNull(conversation.id());
        assertFalse(conversation.id().isEmpty());
    }

    @Test
    void testEmptyConversation() {
        TurnBasedConversation conversation = new DefaultTurnBasedConversation();
        assertTrue(conversation.isEmpty());
        assertNull(conversation.lastTurn());
    }

    @Test
    void testLastTurnWithEmptyConversation() {
        TurnBasedConversation conversation = new DefaultTurnBasedConversation();
        assertNull(conversation.lastTurn());
    }

    @Test
    void testLastTurnAfterAddingTurn() {
        TurnBasedConversation conversation = new DefaultTurnBasedConversation();
        MessageTurn turn = conversation.newTurn();
        assertEquals(turn, conversation.lastTurn());
    }

    @Test
    void testLastTurnsWithMultipleTurns() {
        TurnBasedConversation conversation = new DefaultTurnBasedConversation();
        MessageTurn turn1 = conversation.newTurn();
        MessageTurn turn2 = conversation.newTurn();
        List<MessageTurn> lastTwo = conversation.lastTurns(2);
        assertEquals(2, lastTwo.size());
        assertEquals(turn1, lastTwo.get(0));
        assertEquals(turn2, lastTwo.get(1));
    }

    @Test
    void testNewTurnMarksPreviousTurnAsFinished() {
        TurnBasedConversation conversation = new DefaultTurnBasedConversation();
        MessageTurn firstTurn = conversation.newTurn();
        assertFalse(firstTurn.isFinished());
        conversation.newTurn();
        assertTrue(firstTurn.isFinished());
    }

    @Test
    void testAppendTurnAddsTurn() {
        TurnBasedConversation conversation = new DefaultTurnBasedConversation();
        MessageTurn turn = new DefaultMessageTurn();
        conversation.appendTurn(turn);
        assertEquals(turn, conversation.lastTurn());
    }

    @Test
    void testFindMessagesWithPredicate() {
        TurnBasedConversation conversation = new DefaultTurnBasedConversation();
        conversation.appendMessage(RoleMessage.user("What's the weather like today?"));
        conversation.appendMessage(RoleMessage.assistant("The weather is sunny with a high of 75°F."));
        conversation.appendMessage(RoleMessage.user("Thanks for the info"));

        List<Message> found = conversation.findMessages(m -> m.getContent().contains("weather"), false);
        assertEquals(2, found.size());
        assertTrue(found.stream().anyMatch(m -> Objects.equals(m.getRole(), RoleMessage.ROLE_USER) && m.getContent().contains("weather")));
        assertTrue(found.stream().anyMatch(m -> Objects.equals(m.getRole(), RoleMessage.ROLE_ASSISTANT) && m.getContent().contains("weather")));
    }

    @Test
    void testLastMessage() {
        TurnBasedConversation conversation = new DefaultTurnBasedConversation();
        conversation.appendMessage(RoleMessage.user("How do I reset my password?"));
        conversation.appendMessage(RoleMessage.assistant("You can reset your password by clicking the 'Forgot Password' link on the login page."));
        assertEquals("You can reset your password by clicking the 'Forgot Password' link on the login page.", conversation.lastMessage().get().getContent());
        assertEquals(RoleMessage.ROLE_ASSISTANT, conversation.lastMessage().get().getRole());
    }

    @Test
    void testLastMessages() {
        TurnBasedConversation conversation = new DefaultTurnBasedConversation();
        conversation.appendMessage(RoleMessage.user("What time does the store open?"));
        conversation.appendMessage(RoleMessage.assistant("The store opens at 9 AM."));
        conversation.appendMessage(RoleMessage.user("Is it open on Sundays?"));

        List<Message> lastTwo = conversation.lastMessages(2);
        assertEquals(2, lastTwo.size());
        assertEquals("The store opens at 9 AM.", lastTwo.get(0).getContent());
        assertEquals(RoleMessage.ROLE_ASSISTANT, lastTwo.get(0).getRole());
        assertEquals("Is it open on Sundays?", lastTwo.get(1).getContent());
        assertEquals(RoleMessage.ROLE_USER, lastTwo.get(1).getRole());
    }

    @Test
    void testResetAfterInclusive() {
        TurnBasedConversation conversation = new DefaultTurnBasedConversation();
        conversation.appendMessage(RoleMessage.user("Can you help me with my order?"));
        conversation.appendMessage(RoleMessage.assistant("Sure, what's your order number?"));
        MessageId targetId = conversation.lastMessage().get().getId();
        conversation.appendMessage(RoleMessage.user("My order number is #12345"));
        conversation.appendMessage(RoleMessage.assistant("Let me check the status for you..."));

        conversation.resetAfter(targetId, true);

        List<Message> messages = conversation.lastMessages(10);
        assertEquals(2, messages.size());
        assertEquals("Can you help me with my order?", messages.get(0).getContent());
        assertEquals("Sure, what's your order number?", messages.get(1).getContent());
    }

    @Test
    void testResetAfterExclusive() {
        TurnBasedConversation conversation = new DefaultTurnBasedConversation();
        conversation.appendMessage(RoleMessage.user("Can you help me with my order?"));
        conversation.appendMessage(RoleMessage.assistant("Sure, what's your order number?"));
        MessageId targetId = conversation.lastMessage().get().getId();
        conversation.appendMessage(RoleMessage.user("My order number is #12345"));
        conversation.appendMessage(RoleMessage.assistant("Let me check the status for you..."));

        conversation.resetAfter(targetId, false);

        List<Message> messages = conversation.lastMessages(10);
        assertEquals(1, messages.size());
        assertEquals("Can you help me with my order?", messages.get(0).getContent());
    }

    @Test
    void testResetAfterLastMessage() {
        TurnBasedConversation conversation = new DefaultTurnBasedConversation();
        conversation.appendMessage(RoleMessage.user("What's the return policy?"));
        conversation.appendMessage(RoleMessage.assistant("Returns are accepted within 30 days with a receipt."));
        MessageId targetId = conversation.lastMessage().get().getId();

        conversation.resetAfter(targetId, true);

        assertEquals(2, conversation.lastMessages(2).size());
        assertEquals("What's the return policy?", conversation.lastMessages(2).get(0).getContent());
        assertEquals("Returns are accepted within 30 days with a receipt.", conversation.lastMessages(2).get(1).getContent());
    }

    @Test
    void testIterationOrder() {
        TurnBasedConversation conversation = new DefaultTurnBasedConversation();
        conversation.appendMessage(RoleMessage.user("Hello"));
        conversation.appendMessage(RoleMessage.assistant("Hi there! How can I help?"));

        // append an empty turn
        conversation.newTurn();

        conversation.newTurn();
        conversation.appendMessage(RoleMessage.user("I need help with my account"));
        conversation.appendMessage(RoleMessage.assistant("What seems to be the problem?"));

        List<Message> messages = new ArrayList<>();
        conversation.iterator().forEachRemaining(messages::add);
        assertEquals(4, messages.size());
        assertEquals("Hello", messages.get(0).getContent());
        assertEquals(RoleMessage.ROLE_USER, messages.get(0).getRole());
        assertEquals("Hi there! How can I help?", messages.get(1).getContent());
        assertEquals(RoleMessage.ROLE_ASSISTANT, messages.get(1).getRole());
        assertEquals("I need help with my account", messages.get(2).getContent());
        assertEquals(RoleMessage.ROLE_USER, messages.get(2).getRole());
        assertEquals("What seems to be the problem?", messages.get(3).getContent());
        assertEquals(RoleMessage.ROLE_ASSISTANT, messages.get(3).getRole());
    }

    @Test
    void testReverseIteration() {
        TurnBasedConversation conversation = new DefaultTurnBasedConversation();
        conversation.appendMessage(RoleMessage.user("Hello"));
        conversation.appendMessage(RoleMessage.assistant("Hi there! How can I help?"));

        // append an empty turn
        conversation.newTurn();

        conversation.newTurn();
        conversation.appendMessage(RoleMessage.user("I need help with my account"));
        conversation.appendMessage(RoleMessage.assistant("What seems to be the problem?"));

        List<Message> messages = Lists.newArrayList(conversation.reverse());
        assertEquals(4, messages.size());
        assertEquals("What seems to be the problem?", messages.get(0).getContent());
        assertEquals(RoleMessage.ROLE_ASSISTANT, messages.get(0).getRole());
        assertEquals("I need help with my account", messages.get(1).getContent());
        assertEquals(RoleMessage.ROLE_USER, messages.get(1).getRole());
        assertEquals("Hi there! How can I help?", messages.get(2).getContent());
        assertEquals(RoleMessage.ROLE_ASSISTANT, messages.get(2).getRole());
        assertEquals("Hello", messages.get(3).getContent());
        assertEquals(RoleMessage.ROLE_USER, messages.get(3).getRole());
    }
}