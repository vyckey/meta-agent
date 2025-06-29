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
import org.metaagent.framework.core.agent.AbstractAgent;
import org.metaagent.framework.core.agent.AgentExecutionContext;
import org.metaagent.framework.core.agent.DefaultAgentExecutionContext;
import org.metaagent.framework.core.agent.fallback.AgentFallbackStrategy;
import org.metaagent.framework.core.agent.fallback.RetryAgentFallbackStrategy;
import org.metaagent.framework.core.agent.input.AgentInput;
import org.metaagent.framework.core.agent.output.AgentOutput;
import org.metaagent.framework.core.model.chat.ChatModelUtils;
import org.metaagent.framework.core.model.parser.JsonOutputParser;
import org.metaagent.framework.core.model.prompt.PromptValue;
import org.metaagent.framework.core.model.prompt.StringPromptValue;
import org.metaagent.framework.core.tool.manager.DefaultToolManager;
import org.metaagent.framework.core.tool.manager.ToolManager;
import org.metaagent.framework.core.tool.spring.ToolCallbackUtils;
import org.metaagent.framework.tools.search.searchapi.SearchApiTool;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.Prompt;

import java.util.List;

/**
 * SearchAgent is an agent that performs web searches using a chat model and tools.
 *
 * @author vyckey
 */
public class SearchAgent extends AbstractAgent {
    protected final ChatModel chatModel;
    protected final ChatOptions chatOptions;
    protected PromptValue promptValue = StringPromptValue.fromFile("agents/prompts/search_agent_system_prompt.md");

    public SearchAgent(String name, ChatModel chatModel, ChatOptions chatOptions) {
        super(name);
        this.chatModel = chatModel;
        this.chatOptions = chatOptions;
    }

    @Override
    public AgentExecutionContext createContext() {
        SearchApiTool searchApiTool = new SearchApiTool();
        ToolManager toolManager = DefaultToolManager.fromTools(searchApiTool);
        return DefaultAgentExecutionContext.builder()
                .toolManager(toolManager)
                .build();
    }

    @Override
    public AgentFallbackStrategy getFallbackStrategy() {
        return new RetryAgentFallbackStrategy(2);
    }

    @Override
    protected AgentOutput doStep(AgentExecutionContext context, AgentInput input) {
        SearchAgentInput agentInput = (SearchAgentInput) input;

        Prompt prompt = buildPrompt(context, agentInput);
        ChatResponse response = ChatModelUtils.callWithToolCall(chatModel, prompt);
        AssistantMessage assistantMessage = response.getResult().getOutput();
        return parseOutput(assistantMessage);
    }

    protected Prompt buildPrompt(AgentExecutionContext context, SearchAgentInput agentInput) {
        ChatOptions options = ToolCallbackUtils.buildChatOptionsWithTools(this.chatOptions,
                context.getToolManager(), getAgentState().getToolCallTracker());

        List<Message> messages = List.of(
                new SystemMessage(promptValue.toString()),
                new UserMessage(agentInput.query())
        );
        return new Prompt(messages, options);
    }

    protected SearchAgentOutput parseOutput(AssistantMessage message) {
        String text = message.getText();
        SearchAgentOutput output = new JsonOutputParser<>(SearchAgentOutput.class).parse(text);
        if (CollectionUtils.isEmpty(output.queryTerms()) && CollectionUtils.isEmpty(output.sources())) {
            return SearchAgentOutput.fromAnswer(output.answer());
        }
        return new SearchAgentOutput(output.answer(), true,
                output.queryTerms(), output.sources(), output.explanation()
        );
    }

}
