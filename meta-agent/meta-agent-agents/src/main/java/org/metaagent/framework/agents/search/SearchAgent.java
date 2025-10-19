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

package org.metaagent.framework.agents.search;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.metaagent.framework.core.agent.AbstractAgent;
import org.metaagent.framework.core.agent.fallback.AgentFallbackStrategy;
import org.metaagent.framework.core.agent.fallback.RetryAgentFallbackStrategy;
import org.metaagent.framework.core.agent.input.AgentInput;
import org.metaagent.framework.core.agent.output.AgentOutput;
import org.metaagent.framework.core.model.chat.ChatModelUtils;
import org.metaagent.framework.core.model.parser.OutputParsers;
import org.metaagent.framework.core.model.prompt.PromptTemplate;
import org.metaagent.framework.core.model.prompt.PromptValue;
import org.metaagent.framework.core.model.prompt.StringPromptTemplate;
import org.metaagent.framework.core.model.prompt.registry.PromptRegistry;
import org.metaagent.framework.core.tool.executor.ToolExecutorContext;
import org.metaagent.framework.core.tool.tools.spring.ToolCallbackUtils;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.Prompt;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

/**
 * SearchAgent is an agent that performs web searches using a chat model and tools.
 *
 * @author vyckey
 */
public class SearchAgent extends AbstractAgent<SearchAgentInput, SearchAgentOutput, SearchAgentOutput> {
    protected final ChatModel chatModel;
    protected final ChatOptions chatOptions;

    static {
        PromptRegistry.global().registerPromptTemplate("framework:search_agent_system_prompt",
                StringPromptTemplate.fromFile("jinja2", "agents/prompts/search_agent_system_prompt.md")
        );
    }

    public SearchAgent(String name, ChatModel chatModel, ChatOptions chatOptions) {
        super(name);
        this.chatModel = chatModel;
        this.chatOptions = chatOptions;
    }

    @Override
    public AgentFallbackStrategy<SearchAgentInput, SearchAgentOutput, SearchAgentOutput> getFallbackStrategy() {
        return new RetryAgentFallbackStrategy<>(2);
    }

    @Override
    public AgentOutput<SearchAgentOutput> run(String input) {
        AgentInput<SearchAgentInput> agentInput = AgentInput.builder(SearchAgentInput.from(input)).build();
        return run(agentInput);
    }

    @Override
    protected AgentOutput<SearchAgentOutput> doStep(AgentInput<SearchAgentInput> input) {
        Prompt prompt = buildPrompt(input);
        ChatResponse response = ChatModelUtils.callWithToolCall(chatModel, prompt);
        AssistantMessage assistantMessage = response.getResult().getOutput();
        SearchAgentOutput output = parseOutput(assistantMessage);
        return AgentOutput.create(output);
    }

    protected Prompt buildPrompt(AgentInput<SearchAgentInput> agentInput) {
        ToolExecutorContext toolExecutorContext = buildToolExecutorContext(agentInput);
        ChatOptions options = ToolCallbackUtils.buildChatOptionsWithTools(this.chatOptions,
                agentInput.context().getToolExecutor(), toolExecutorContext, true);

        SearchAgentInput searchInput = agentInput.input();
        PromptTemplate promptTemplate = PromptRegistry.global().getPromptTemplate("framework:search_agent_system_prompt");
        PromptValue promptValue = promptTemplate.format(Map.of(
                "date", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE),
                "force_search", searchInput.forceSearch()
        ));
        String message = searchInput.query();
        if (StringUtils.isNotEmpty(searchInput.queryContext())) {
            message = "## Context\n" + searchInput.queryContext() + "\n##Query\n" + message;
        }
        List<Message> messages = List.of(
                new SystemMessage(promptValue.toString()),
                new UserMessage(message)
        );
        return new Prompt(messages, options);
    }

    protected SearchAgentOutput parseOutput(AssistantMessage message) {
        String text = message.getText();
        SearchAgentOutput output = OutputParsers.jsonParser(SearchAgentOutput.class).parse(text);
        if (CollectionUtils.isEmpty(output.queryTerms()) && CollectionUtils.isEmpty(output.sources())) {
            return SearchAgentOutput.fromAnswer(output.answer());
        }
        return SearchAgentOutput.builder()
                .answer(output.answer())
                .searched(true)
                .queryTerms(output.queryTerms())
                .sources(output.sources())
                .explanation(output.explanation())
                .valuable(output.valuable())
                .build();
    }

}
