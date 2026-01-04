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

import org.junit.jupiter.api.Test;
import org.metaagent.framework.common.content.MediaResource;
import org.metaagent.framework.common.metadata.MetadataProvider;
import org.metaagent.framework.core.agent.chat.message.RoleMessage;
import org.springframework.util.MimeType;

import java.net.URI;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * description is here
 *
 * @author vyckey
 */
class ConversationStorageTest {
    @Test
    void fileStorageTest() throws Exception {
        Conversation conversation = new DefaultConversation();
        conversation.appendMessage(RoleMessage.user("Who are you?"));
        conversation.appendMessage(RoleMessage.assistant("I am your assistant."));

        MediaResource media = MediaResource.builder().mimeType(MimeType.valueOf("image/*")).name("img").uri(URI.create("images/hello.jpg")).build();
        RoleMessage message = RoleMessage.user("How is today?", List.of(), MetadataProvider.from(Map.of(
                "k1", 1,
                "k2", "v2"
        )));
        conversation.appendMessage(message);

        try (ConversationStorage conversationStorage = ConversationFileStorage.fromPattern("data/chat/session_%s.json")) {
            conversationStorage.store(conversation);

            DefaultConversation conversation2 = new DefaultConversation(conversation.id());
            conversationStorage.load(conversation2);
            assertEquals(conversation.toString(), conversation2.toString());

            conversationStorage.clear(conversation.id());
        }
    }
}

