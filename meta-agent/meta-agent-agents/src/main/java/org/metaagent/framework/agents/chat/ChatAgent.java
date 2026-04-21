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

package org.metaagent.framework.agents.chat;

import com.google.common.collect.Lists;
import org.metaagent.framework.agents.chat.context.ChatAgentStepContext;
import org.metaagent.framework.agents.chat.input.ChatAgentInput;
import org.metaagent.framework.agents.chat.output.ChatAgentOutput;
import org.metaagent.framework.core.agent.AbstractStreamAgent;
import org.metaagent.framework.core.agent.chat.conversation.Conversation;
import org.metaagent.framework.core.agent.chat.message.Message;
import org.metaagent.framework.core.agent.chat.message.MessageId;
import org.metaagent.framework.core.agent.chat.message.MessageInfo;
import org.metaagent.framework.core.agent.chat.message.RoleMessage;
import org.metaagent.framework.core.agent.chat.session.Session;
import org.metaagent.framework.core.agent.event.AgentEventBus;
import org.metaagent.framework.core.agent.event.AgentMessageEvent;
import org.metaagent.framework.core.agent.exception.AgentExecutionException;
import org.metaagent.framework.core.agents.chat.SessionService;
import org.metaagent.framework.core.agents.llm.LlmStreamingAgent;
import org.metaagent.framework.core.agents.llm.context.DefaultLlmAgentContext;
import org.metaagent.framework.core.agents.llm.context.LlmAgentContext;
import org.metaagent.framework.core.agents.llm.input.LlmAgentInput;
import org.metaagent.framework.core.agents.llm.message.StreamMessageChunk;
import org.metaagent.framework.core.agents.llm.output.LlmAgentStreamOutput;
import reactor.core.publisher.Flux;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * ChatAgent is an agent that has an ability to chat with message history.
 *
 * @author vyckey
 */
public class ChatAgent extends AbstractStreamAgent<ChatAgentInput, ChatAgentOutput, ChatAgentStepContext, StreamMessageChunk> {
    private final SessionService sessionService = null;
    private final LlmStreamingAgent llmAgent = new LlmStreamingAgent("llm-agent");

    public ChatAgent(String name) {
        super(name);
    }

    @Override
    protected ChatAgentInput preprocess(ChatAgentInput input) {
        ChatAgentInput agentInput = super.preprocess(input);
        Session session = sessionService.getSession(agentInput.sessionId());

        Conversation conversation = session.conversation();
        try {
            // Validate input messages IDs
            Set<MessageId> newMessageIds = input.messages().stream().map(Message::info).map(MessageInfo::id).collect(Collectors.toSet());
            List<Message> invalidMessages = conversation.findMessages(message -> newMessageIds.contains(message.info().id()), false);
            if (!invalidMessages.isEmpty()) {
                String invalidMessageIds = invalidMessages.stream().map(Message::info)
                        .map(MessageInfo::id).map(MessageId::value)
                        .collect(Collectors.joining(", "));
                throw new AgentExecutionException("Invalid input messages with IDs: " + invalidMessageIds);
            }

            // Append input messages to conversation
            for (Message message : input.messages()) {
                conversation.appendMessage(message);
            }
            conversation.flush();
        } catch (IOException e) {
            throw new AgentExecutionException("Failed to append messages to conversation", e);
        }

        List<Message> allMessages = Lists.newArrayList(conversation.iterator());
        return agentInput.toBuilder()
                .messages(allMessages)
                .build();
    }

    @Override
    public ChatAgentStepContext createStepContext(ChatAgentInput agentInput) {
        Session session = sessionService.getSession(agentInput.sessionId());
        return ChatAgentStepContext.builder().session(session).build();
    }

    @Override
    protected ChatAgentOutput buildAgentOutput(ChatAgentInput agentInput, ChatAgentStepContext stepContext, Flux<StreamMessageChunk> stream) {
        return ChatAgentOutput.builder()
                .messageInfo(stepContext.getOutputMessageInfo())
                .stream(stream)
                .build();
    }

    @Override
    protected boolean shouldContinueLoop(ChatAgentInput agentInput, ChatAgentStepContext stepContext) {
        return false;
    }

    @Override
    protected Flux<StreamMessageChunk> doStepStream(ChatAgentInput agentInput, ChatAgentStepContext stepContext) {
        AgentEventBus llmAgentEventBus = AgentEventBus.create();
        llmAgentEventBus.subscribe(event -> {
            if (event instanceof AgentMessageEvent messageEvent) {
                handleLlmAgentMessageEvent(messageEvent, stepContext);
            }
        });

        LlmAgentContext llmAgentContext = new DefaultLlmAgentContext.Builder(agentInput.context())
                .agentEventBus(llmAgentEventBus)
                .build();

        LlmAgentInput llmAgentInput = LlmAgentInput.builder()
                .modelId(agentInput.modelId())
                .systemPrompts(agentInput.systemPrompts())
                .messages(agentInput.messages())
                .thinking(agentInput.thinking())
                .messageIdGenerator(sessionService.messageIdGenerator())
                .messagePartIdGenerator(sessionService.messagePartIdGenerator())
                .context(llmAgentContext)
                .build();
        LlmAgentStreamOutput llmAgentOutput = llmAgent.run(llmAgentInput);

        MessageInfo messageInfo = llmAgentOutput.messageInfo()
                .toBuilder()
                .sessionId(agentInput.sessionId())
                .build();
        stepContext.setOutputMessageInfo(messageInfo);
        return llmAgentOutput.stream();
    }

    private void handleLlmAgentMessageEvent(AgentMessageEvent messageEvent, ChatAgentStepContext stepContext) {
        Session session = stepContext.getSession();
        Conversation conversation = session.conversation();

        MessageId outputMessageId = stepContext.getOutputMessageId();
        Optional<Message> messageOptional = conversation.getMessage(outputMessageId);
        Message outputMessage;
        if (messageOptional.isPresent()) {
            outputMessage = messageOptional.get().toBuilder()
                    .addPart(messageEvent.messagePart())
                    .build();
        } else {
            outputMessage = RoleMessage.builder()
                    .info(stepContext.getOutputMessageInfo())
                    .addPart(messageEvent.messagePart())
                    .build();
        }

        if (messageOptional.isPresent()) {
            conversation.updateMessage(outputMessage);
        } else {
            conversation.appendMessage(outputMessage);
        }
    }

    @Override
    public void close() {
        llmAgent.close();
    }
}
