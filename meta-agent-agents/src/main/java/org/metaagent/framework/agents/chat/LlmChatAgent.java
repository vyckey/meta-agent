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

package org.metaagent.framework.agents.chat;

import org.metaagent.framework.core.agent.AbstractAgent;
import org.metaagent.framework.core.agent.chat.message.Message;
import org.metaagent.framework.core.agent.chat.message.history.DefaultMessageHistory;
import org.metaagent.framework.core.agent.chat.message.history.MessageHistory;
import org.metaagent.framework.core.agent.input.AgentInput;
import org.metaagent.framework.core.agent.output.AgentOutput;
import org.metaagent.framework.core.agent.output.AgentStreamOutputAggregator;
import org.metaagent.framework.core.agents.chat.ChatAgent;
import org.metaagent.framework.core.agents.chat.ChatAgentInput;
import org.metaagent.framework.core.agents.chat.ChatAgentOutput;
import org.metaagent.framework.core.agents.chat.ChatAgentStreamOutput;
import org.metaagent.framework.core.agents.chat.ChatAgentStreamOutputAggregator;
import org.metaagent.framework.core.model.chat.ChatModelClient;
import org.metaagent.framework.core.model.prompt.PromptTemplate;
import org.metaagent.framework.core.model.prompt.PromptValue;
import org.metaagent.framework.core.model.prompt.StringPromptTemplate;
import org.metaagent.framework.core.model.prompt.registry.PromptRegistry;
import org.springframework.ai.chat.model.ChatModel;
import reactor.core.publisher.Flux;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * {@link ChatAgent} implementation with Large Language Model (LLM).
 *
 * @author vyckey
 */
public class LlmChatAgent extends AbstractAgent<ChatAgentInput, ChatAgentOutput, ChatAgentStreamOutput> implements ChatAgent {
    public static final String DEFAULT_SYSTEM_PROMPT_ID = "framework:chat_agent_system_prompt";
    protected MessageHistory messageHistory = new DefaultMessageHistory();
    protected final ChatModel chatModel;
    protected ChatModelClient chatModelClient;
    protected PromptTemplate systemPromptTemplate;

    public LlmChatAgent(String name, ChatModel chatModel, PromptTemplate systemPromptTemplate) {
        super(name);
        this.chatModel = Objects.requireNonNull(chatModel, "chatModel is required");
        this.chatModelClient = new ChatModelClient(chatModel);
        this.systemPromptTemplate = Objects.requireNonNull(systemPromptTemplate, "systemPrompt is required");
    }

    public LlmChatAgent(String name, ChatModel chatModel) {
        this(name, chatModel, PromptRegistry.global().getPromptTemplate(DEFAULT_SYSTEM_PROMPT_ID));
    }

    static {
        PromptRegistry.global().registerPromptTemplate(DEFAULT_SYSTEM_PROMPT_ID,
                StringPromptTemplate.fromFile("agents/prompts/chat_agent_system_prompt.md"));
    }

    @Override
    public MessageHistory getMessageHistory() {
        return messageHistory;
    }

    @Override
    protected void beforeRun(AgentInput<ChatAgentInput> input) {
        // set system prompt
        String modelCutoffDate = input.metadata().getProperty("model_cutoff_date", String.class, "Unknown");
        PromptValue systemPrompt = systemPromptTemplate.format(Map.of(
                "name", getName(),
                "current_date", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE),
                "model_cutoff_date", modelCutoffDate
        ));
        chatModelClient.setSystemPrompt(systemPrompt);

        chatModelClient.setToolContext(buildToolExecutorContext(input), input.context().getToolExecutor());
    }

    @Override
    protected AgentOutput<ChatAgentOutput> doStep(AgentInput<ChatAgentInput> agentInput) {
        ChatAgentInput input = agentInput.input();
        Message outputMessage = chatModelClient.sendMessage(input.messages());

        chatModelClient.getNewMessages().forEach(messageHistory::appendMessage);
        ChatAgentOutput chatOutput = new ChatAgentOutput(List.of(outputMessage));
        return AgentOutput.create(chatOutput);
    }

    @Override
    protected Flux<AgentOutput<ChatAgentStreamOutput>> doStepStream(AgentInput<ChatAgentInput> agentInput) {
        ChatAgentInput input = agentInput.input();
        Flux<Message> messageFlux = chatModelClient.sendMessageStream(input.messages());
        return messageFlux.map(output -> AgentOutput.create(new ChatAgentStreamOutput(output)));
    }

    @Override
    public AgentStreamOutputAggregator<ChatAgentStreamOutput, ChatAgentOutput> getStreamOutputAggregator() {
        return ChatAgentStreamOutputAggregator.INSTANCE;
    }

    @Override
    public void reset() {
        super.reset();
        chatModelClient.reset();
        getMessageHistory().clear();
    }
}
