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

package org.metaagent.framework.core.agent.chat.message.conversation;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import org.metaagent.framework.core.agent.chat.message.Message;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Objects;

/**
 * description is here
 *
 * @author vyckey
 */
public class ConversationFileStorage implements ConversationStorage {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper()
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
            .enable(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT);
    private final String filePathFormat;

    public ConversationFileStorage(String filePathFormat) {
        this.filePathFormat = Objects.requireNonNull(filePathFormat);
    }

    public String getFilePath(String historyId) {
        return String.format(filePathFormat, historyId);
    }

    @Override
    public void save(Conversation conversation) {
        String filePath = getFilePath(conversation.id());
        File file = new File(filePath);
        if (!file.exists()) {
            file.getParentFile().mkdirs();
        }
        try {
            OBJECT_MAPPER.writeValue(file, Lists.newArrayList(conversation.reverse()));
        } catch (IOException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    @Override
    public void load(Conversation conversation) {
        String filePath = getFilePath(conversation.id());
        File file = new File(filePath);
        if (file.exists()) {
            try {
                List<Message> messages = OBJECT_MAPPER.readValue(file, new TypeReference<>() {
                });
                messages.forEach(conversation::appendMessage);
            } catch (IOException e) {
                throw new IllegalStateException(e.getMessage(), e);
            }
        }
    }

    @Override
    public void clear(String conversationId) {
        String filePath = getFilePath(conversationId);
        File file = new File(filePath);
        if (file.exists()) {
            file.delete();
        }
    }
}
