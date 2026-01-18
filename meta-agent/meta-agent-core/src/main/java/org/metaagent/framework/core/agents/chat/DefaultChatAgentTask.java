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

package org.metaagent.framework.core.agents.chat;

import org.apache.commons.lang3.StringUtils;
import org.metaagent.framework.core.agent.input.AgentInput;
import org.metaagent.framework.core.agent.output.AgentOutput;
import org.metaagent.framework.core.agents.chat.input.ChatInput;
import org.metaagent.framework.core.agents.chat.output.ChatOutput;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Default implementation of {@link ChatAgentTask}.
 *
 * @author vyckey
 */
public record DefaultChatAgentTask(
        String taskId,
        AgentInput<ChatInput> input,
        CompletableFuture<AgentOutput<ChatOutput>> outputFuture,
        Instant startTime
) implements ChatAgentTask {
    public DefaultChatAgentTask {
        if (StringUtils.isEmpty(taskId)) {
            throw new IllegalArgumentException("taskId cannot be empty");
        }
        Objects.requireNonNull(input, "agent input is required");
        Objects.requireNonNull(outputFuture, "agent output future is required");
        if (startTime == null) {
            startTime = Instant.now();
        }
    }

    public DefaultChatAgentTask(AgentInput<ChatInput> input) {
        this(generateTaskId(), input, new CompletableFuture<>(), Instant.now());
    }

    public static String generateTaskId() {
        return UUID.randomUUID().toString().replaceAll("-", "").substring(16);
    }
}
