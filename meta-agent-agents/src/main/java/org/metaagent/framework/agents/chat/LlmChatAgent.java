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
import org.metaagent.framework.core.agent.chat.message.AssistantMessage;
import org.metaagent.framework.core.agent.chat.message.SystemMessage;
import org.metaagent.framework.core.agent.chat.message.history.DefaultMessageHistory;
import org.metaagent.framework.core.agent.chat.message.history.MessageHistory;
import org.metaagent.framework.core.agents.chat.ChatAgent;
import org.metaagent.framework.core.agents.chat.ChatAgentInput;
import org.metaagent.framework.core.agents.chat.ChatAgentOutput;
import org.metaagent.framework.core.model.parser.OutputParsers;
import org.metaagent.framework.core.model.prompt.PromptTemplate;
import org.metaagent.framework.core.model.prompt.PromptValue;
import org.metaagent.framework.core.model.prompt.StringPromptTemplate;
import org.metaagent.framework.core.model.prompt.registry.PromptRegistry;
import org.springframework.ai.chat.model.ChatModel;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Objects;

/**
 * LLM chat agent.
 *
 * @author vyckey
 */
public class LlmChatAgent extends AbstractAgent<ChatAgentInput, ChatAgentOutput> implements ChatAgent {
    public static final String SYSTEM_PROMPT_ID = "framework:chat_agent_system_prompt";
    private static final boolean DEFAULT_SEARCH_ENABLED = true;
    private static final boolean DEFAULT_DEEP_THINK_ENABLED = false;
    protected MessageHistory messageHistory = new DefaultMessageHistory();
    protected final ChatModel chatModel;
    protected ChatModelClient chatModelClient;
    protected String systemPromptId;

    public LlmChatAgent(String name, ChatModel chatModel, String systemPromptId) {
        super(name);
        this.chatModel = Objects.requireNonNull(chatModel, "chatModel is required");
        this.chatModelClient = new ChatModelClient(chatModel);
        this.systemPromptId = Objects.requireNonNull(systemPromptId, "systemPromptId is required");
    }

    public LlmChatAgent(String name, ChatModel chatModel) {
        this(name, chatModel, SYSTEM_PROMPT_ID);
    }

    static {
        PromptRegistry.global().registerPromptTemplate(SYSTEM_PROMPT_ID,
                StringPromptTemplate.fromFile("agents/prompts/chat_agent_system_prompt.md"));
    }

    @Override
    public MessageHistory getMessageHistory() {
        return messageHistory;
    }

    @Override
    protected ChatAgentOutput doRun(ChatAgentInput input) {
        setSystemPrompt(input);

        chatModelClient.setToolExecutorContext(buildToolExecutorContext(input));
        return super.doRun(input);
    }

    protected void setSystemPrompt(ChatAgentInput input) {
        boolean searchEnabled = input.metadata().getProperty(ChatAgentInput.OPTION_SEARCH_ENABLED,
                Boolean.class, DEFAULT_SEARCH_ENABLED);
        boolean deepThinkEnabled = input.metadata().getProperty(ChatAgentInput.OPTION_DEEP_THINK_ENABLED,
                Boolean.class, DEFAULT_DEEP_THINK_ENABLED);

        PromptTemplate promptTemplate = PromptRegistry.global().getPromptTemplate(systemPromptId);
        PromptValue systemPrompt = promptTemplate.format(Map.of(
                "date", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE),
                "search_enabled", searchEnabled,
                "deep_thinking", deepThinkEnabled
        ));
        chatModelClient.setSystemMessage(new SystemMessage(systemPrompt.toString()));
    }

    @Override
    protected ChatAgentOutput doStep(ChatAgentInput input) {
        AssistantMessage outputMessage = chatModelClient.sendMessage(input.messages().get(0));
        String outputText = outputMessage.getContent();

        String thoughtProcess = null;
        boolean deepThinkEnabled = input.metadata().getProperty(ChatAgentInput.OPTION_DEEP_THINK_ENABLED,
                Boolean.class, DEFAULT_DEEP_THINK_ENABLED);
        if (deepThinkEnabled) {
            thoughtProcess = OutputParsers.htmlTagParser("think", false).parse(outputText);
        }

        chatModelClient.getNewMessages().forEach(messageHistory::appendMessage);
        return ChatAgentOutput.builder().messages(outputMessage)
                .thoughtProcess(thoughtProcess)
                .build();
    }

    @Override
    public void reset() {
        super.reset();
        getMessageHistory().clear();
    }
}
