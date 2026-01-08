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

package org.metaagent.framework.tools.web.fetch;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import io.github.furstenheim.CopyDown;
import io.github.furstenheim.OptionsBuilder;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.metaagent.framework.common.abort.AbortSignal;
import org.metaagent.framework.core.model.parser.OutputParsers;
import org.metaagent.framework.core.model.parser.RegexGroupOutputParser;
import org.metaagent.framework.core.model.prompt.PromptValue;
import org.metaagent.framework.core.model.prompt.StringPromptValue;
import org.metaagent.framework.core.model.prompt.registry.PromptRegistry;
import org.metaagent.framework.core.tool.Tool;
import org.metaagent.framework.core.tool.ToolContext;
import org.metaagent.framework.core.tool.converter.ToolConverter;
import org.metaagent.framework.core.tool.converter.ToolConverters;
import org.metaagent.framework.core.tool.definition.ToolDefinition;
import org.metaagent.framework.core.tool.exception.ToolExecutionException;
import org.metaagent.framework.core.tool.exception.ToolArgumentException;
import org.metaagent.framework.tools.web.utils.WebPageUtils;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * WebFetchTool is a tool that fetches web pages from the internet.
 *
 * @author vyckey
 */
@Slf4j
public class WebFetchTool implements Tool<WebFetchInput, WebFetchOutput> {
    private static final ToolDefinition TOOL_DEFINITION = ToolDefinition.builder("fetch_web_urls")
            .description("""
                    Fetches URLs from the internet and optionally extracts its contents as markdown.
                    """)
            .inputSchema(WebFetchInput.class)
            .outputSchema(WebFetchOutput.class)
            .isConcurrencySafe(true)
            .isReadOnly(true)
            .build();
    private static final ToolConverter<WebFetchInput, WebFetchOutput> TOOL_CONVERTER =
            ToolConverters.jsonConverter(WebFetchInput.class);

    public static final String SYSTEM_PROMPT_ID = "tools:fetch_web_urls_system_prompt";
    private static volatile ExecutorService DEFAULT_THREAD_POOL = null;
    protected static final String CONTENT_SEPARATOR = "=========================";
    protected static final CopyDown COPY_DOWN = new CopyDown(OptionsBuilder.anOptions().build());
    protected static final RegexGroupOutputParser OUTPUT_PARSER = OutputParsers
            .regexGroupParser("<url id=\"(?<id>\\d+)\">(?<content>[\\s\\S]*?)</url>", "id", "content");
    private final Executor threadPool;

    private final WebFetcher webFetcher;
    private final ChatModel chatModel;

    public WebFetchTool(WebFetcher webFetcher, Executor threadPool, ChatModel chatModel) {
        this.threadPool = Objects.requireNonNull(threadPool, "threadPool is required");
        this.webFetcher = Objects.requireNonNull(webFetcher, "webFetcher is required");
        this.chatModel = Objects.requireNonNull(chatModel, "chatModel is required");
    }

    public WebFetchTool(ChatModel chatModel) {
        this(
                new CombinedWebFetcher(new HttpWebFetcher(), new BrowserWebFetcher()),
                defaultThreadPool(),
                chatModel
        );
    }

    static {
        PromptRegistry.global().registerPrompt(
                SYSTEM_PROMPT_ID, StringPromptValue.fromFile("agents/prompts/tool_fetch_web_urls_system_prompt.md")
        );
    }

    protected static ExecutorService defaultThreadPool() {
        if (DEFAULT_THREAD_POOL == null) {
            synchronized (WebFetchTool.class) {
                if (DEFAULT_THREAD_POOL == null) {
                    DEFAULT_THREAD_POOL = new ThreadPoolExecutor(
                            8, 8,
                            1, TimeUnit.SECONDS,
                            new ArrayBlockingQueue<>(1000),
                            new ThreadFactoryBuilder().setNameFormat("WebFetchTool-%d").build()
                    );
                }
            }
        }
        return DEFAULT_THREAD_POOL;
    }

    public static void close() {
        if (DEFAULT_THREAD_POOL != null) {
            DEFAULT_THREAD_POOL.shutdown();
        }
    }

    @Override
    public ToolDefinition getDefinition() {
        return TOOL_DEFINITION;
    }

    @Override
    public ToolConverter<WebFetchInput, WebFetchOutput> getConverter() {
        return TOOL_CONVERTER;
    }

    @Override
    public WebFetchOutput run(ToolContext toolContext, WebFetchInput fetchInput) throws ToolExecutionException {
        if (CollectionUtils.isEmpty(fetchInput.urls())) {
            throw new ToolArgumentException("urls must be not empty");
        }
        if (fetchInput.prompt() == null || fetchInput.prompt().trim().isEmpty()) {
            throw new ToolArgumentException("prompt must be not empty");
        }

        // 1. Fetch URLs contents
        List<WebFetchOutput.URLContent> urlContents = webFetcher.fetchUrls(fetchInput.urls(), threadPool, toolContext.getAbortSignal());
        List<WebFetchOutput.URLContent> fetchedContents = urlContents.stream().filter(content -> !content.hasError()).toList();

        List<WebFetchOutput.URLContent> finalContents;
        if (BooleanUtils.isTrue(fetchInput.returnRaw()) || fetchedContents.isEmpty()) {
            finalContents = urlContents;
        } else {
            // 2. Normalize URL contents
            List<WebFetchOutput.URLContent> normalizedContents = normalizeWebContents(fetchedContents, toolContext.getAbortSignal());

            // 3. Process prompt
            List<WebFetchOutput.URLContent> proceedContents = processPrompt(normalizedContents, fetchInput.prompt(), toolContext.getAbortSignal());

            // merge to final contents
            finalContents = Lists.newArrayList(urlContents);
            for (WebFetchOutput.URLContent urlContent : proceedContents) {
                for (int i = 0; i < finalContents.size(); i++) {
                    if (urlContent.url().equals(finalContents.get(i).url())) {
                        finalContents.set(i, urlContent);
                    }
                }
            }
        }

        long errorCount = finalContents.stream().filter(WebFetchOutput.URLContent::hasError).count();
        String error = errorCount > 0 ? "Fetched " + finalContents.size() + " URLs (" + errorCount + " failed)" : "";
        return new WebFetchOutput(finalContents, error);
    }

    protected List<WebFetchOutput.URLContent> normalizeWebContents(List<WebFetchOutput.URLContent> fetchedContents, AbortSignal abortSignal) {
        if (abortSignal.isAborted()) {
            return fetchedContents;
        }

        List<WebFetchOutput.URLContent> normalizedContents = Lists.newArrayListWithCapacity(fetchedContents.size());
        for (WebFetchOutput.URLContent fetchedContent : fetchedContents) {
            // normalize html web content
            if (fetchedContent.contentType().contains("text/html")) {
                WebFetchOutput.URLContent.Builder contentBuilder = fetchedContent.toBuilder();
                String normalizedContent = WebPageUtils.normalizeHtmlText(fetchedContent.content());
                try {
                    normalizedContent = COPY_DOWN.convert(normalizedContent)
                            .replaceAll("\\s+", " ")
                            .replaceAll("\\n{3,}", "\n\n");
                    contentBuilder.contentType("text/plain");
                } catch (Exception e) {
                    log.error("normalizeWebContents error for url {}: {}", fetchedContent.url(), e.getMessage(), e);
                }
                contentBuilder.content(normalizedContent);
                normalizedContents.add(contentBuilder.build());
            } else {
                normalizedContents.add(fetchedContent);
            }
        }
        return normalizedContents;
    }

    protected List<WebFetchOutput.URLContent> processPrompt(List<WebFetchOutput.URLContent> fetchedContents,
                                                            String prompt, AbortSignal abortSignal) {
        PromptValue systemPrompt = PromptRegistry.global().getPrompt(SYSTEM_PROMPT_ID);

        StringBuilder contentBuilder = new StringBuilder();
        for (int i = 0; i < fetchedContents.size(); i++) {
            String content = fetchedContents.get(i).content();
            contentBuilder.append("## URL [").append(i + 1).append("] content\n")
                    .append(content)
                    .append("\n\n")
                    .append(CONTENT_SEPARATOR)
                    .append("\n\n");
        }

        List<Message> messages = List.of(
                new SystemMessage(systemPrompt.toString()),
                new UserMessage(contentBuilder.toString()),
                new UserMessage(prompt)
        );
        try {
            String outputText = chatModel.call(messages.toArray(Message[]::new));
            parseAndUpdateContents(fetchedContents, outputText);
            return fetchedContents;
        } catch (Exception e) {
            log.error("Failed to call model in processPrompt: {}", e.getMessage(), e);
            return fetchedContents;
        }
    }

    protected void parseAndUpdateContents(List<WebFetchOutput.URLContent> urlContents, String modelOutput) {
        List<RegexGroupOutputParser.GroupResult> groupResults = OUTPUT_PARSER.parseAll(modelOutput);
        Map<Integer, String> idToContent = Maps.newHashMap();
        for (RegexGroupOutputParser.GroupResult groupResult : groupResults) {
            String idStr = groupResult.groups().get("id");
            String content = groupResult.groups().get("content");
            if (idStr != null && content != null) {
                try {
                    int id = Integer.parseInt(idStr);
                    idToContent.put(id, content.trim());
                } catch (NumberFormatException e) {
                    // ignore invalid id
                }
            }
        }
        for (int i = 0; i < urlContents.size(); i++) {
            WebFetchOutput.URLContent urlContent = urlContents.get(i);
            if (idToContent.containsKey(i + 1)) {
                String newContent = idToContent.get(i + 1);
                urlContents.set(i, urlContent.toBuilder().content(newContent).build());
            }
        }
    }

}
