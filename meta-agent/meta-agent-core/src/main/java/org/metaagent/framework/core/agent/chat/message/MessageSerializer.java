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

package org.metaagent.framework.core.agent.chat.message;

import com.google.common.collect.Lists;
import org.metaagent.framework.core.agent.chat.message.part.MediaMessagePart;
import org.metaagent.framework.core.agent.chat.message.part.TextMessagePart;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.jsontype.NamedType;
import tools.jackson.databind.module.SimpleModule;
import tools.jackson.datatype.jsr310.JavaTimeModule;

import java.util.List;

public class MessageSerializer {
    private static volatile JsonMapper jsonMapper;
    private static volatile boolean initialized = false;
    private static final List<NamedType> MESSAGE_TYPES = Lists.newArrayList(
            new NamedType(RoleMessage.class, RoleMessage.class.getTypeName())
    );
    private static final List<NamedType> MESSAGE_PART_TYPES = Lists.newArrayList(
            new NamedType(TextMessagePart.class, TextMessagePart.TYPE),
            new NamedType(MediaMessagePart.class, MediaMessagePart.TYPE)
    );

    public static void registerMessageType(String name, Class<? extends Message> messageClass) {
        if (initialized) {
            throw new IllegalStateException("ObjectMapper has already been initialized.");
        }
        MESSAGE_TYPES.add(new NamedType(messageClass, name));
    }

    public static JsonMapper getJsonMapper() {
        if (!initialized) {
            synchronized (MessageSerializer.class) {
                if (!initialized) {
                    jsonMapper = newJsonMapper();
                    initialized = true;
                }
            }
        }
        return jsonMapper;
    }

    private static JsonMapper newJsonMapper() {
        SimpleModule module = new SimpleModule();
        for (NamedType type : MESSAGE_TYPES) {
            module.registerSubtypes(type);
        }
        for (NamedType type : MESSAGE_PART_TYPES) {
            module.registerSubtypes(type);
        }

        return JsonMapper.builder()
                .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                .enable(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT)
                .addModule(new JavaTimeModule())
                .addModule(module)
                .build();
    }

}
