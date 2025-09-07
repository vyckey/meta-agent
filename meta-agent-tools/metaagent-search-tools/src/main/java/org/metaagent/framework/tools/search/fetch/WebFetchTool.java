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

package org.metaagent.framework.tools.search.fetch;

import com.google.common.collect.Lists;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import io.github.furstenheim.CopyDown;
import io.github.furstenheim.OptionsBuilder;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.metaagent.framework.core.tool.Tool;
import org.metaagent.framework.core.tool.ToolContext;
import org.metaagent.framework.core.tool.ToolExecutionException;
import org.metaagent.framework.core.tool.ToolParameterException;
import org.metaagent.framework.core.tool.converter.ToolConverter;
import org.metaagent.framework.core.tool.converter.ToolConverters;
import org.metaagent.framework.core.tool.definition.ToolDefinition;
import org.metaagent.framework.core.util.abort.AbortSignal;
import org.metaagent.framework.tools.search.utils.WebPageUtils;

import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * WebFetchTool is a tool that fetches web pages from the internet.
 *
 * @author vyckey
 */
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

    protected static final OkHttpClient DEFAULT_HTTP_CLIENT = new OkHttpClient.Builder()
            .connectTimeout(Duration.ofSeconds(3))
            .readTimeout(Duration.ofSeconds(5))
            .followRedirects(true)
            .addInterceptor(new RetryInterceptor(3))
            .build();
    protected static final ExecutorService DEFAULT_THREAD_POOL = new ThreadPoolExecutor(
            8, 8,
            1, TimeUnit.SECONDS,
            new ArrayBlockingQueue<>(1000),
            new ThreadFactoryBuilder().setNameFormat("WebFetchTool-%d").build()
    );
    protected static final CopyDown COPY_DOWN = new CopyDown(OptionsBuilder.anOptions().build());

    private final OkHttpClient httpClient;
    private final Executor threadPool;

    public WebFetchTool(OkHttpClient httpClient, Executor threadPool) {
        this.httpClient = httpClient;
        this.threadPool = threadPool;
    }

    public WebFetchTool() {
        this(DEFAULT_HTTP_CLIENT, DEFAULT_THREAD_POOL);
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
            throw new ToolParameterException("urls must be not empty");
        }

        List<WebFetchOutput.URLContent> fetchedContents = fetchWebUrls(fetchInput.urls(), threadPool, toolContext.getAbortSignal());
        return normalizeWebContents(fetchedContents, BooleanUtils.isTrue(fetchInput.raw()), toolContext.getAbortSignal());
    }

    protected WebFetchOutput normalizeWebContents(List<WebFetchOutput.URLContent> fetchedContents, boolean returnRaw, AbortSignal abortSignal) {
        if (abortSignal.isAborted()) {
            return new WebFetchOutput(List.of(), "Fetch URLs aborted");
        }

        // normalize web content if necessary
        int errorCount = 0;
        List<WebFetchOutput.URLContent> normalizedContents = new ArrayList<>(fetchedContents.size());
        for (WebFetchOutput.URLContent fetchedContent : fetchedContents) {
            if (fetchedContent.error() != null) {
                normalizedContents.add(fetchedContent);
                errorCount++;
                continue;
            }

            WebFetchOutput.URLContent.Builder contentBuilder = fetchedContent.toBuilder();
            if (!returnRaw && fetchedContent.contentType().contains("text/html")) {
                String normalizedContent = WebPageUtils.normalizeHtmlText(fetchedContent.content());
                try {
                    normalizedContent = COPY_DOWN.convert(normalizedContent)
                            .replaceAll("\\s+", " ")
                            .replaceAll("\\n{3,}", "\n\n");
                    contentBuilder.contentType("text/plain");
                } catch (Exception e) {
                    // ignore exception
                }
                contentBuilder.content(normalizedContent);
            }
            normalizedContents.add(contentBuilder.build());
        }
        String error = errorCount > 0 ? "Failed to retrieve " + errorCount + " URL(s)" : null;
        return new WebFetchOutput(normalizedContents, error);
    }

    protected List<WebFetchOutput.URLContent> fetchWebUrls(List<String> urls, Executor executor, AbortSignal abortSignal) {
        List<CompletableFuture<WebFetchOutput.URLContent>> futures = Lists.newArrayList();
        for (String url : urls) {
            CompletableFuture<WebFetchOutput.URLContent> future = CompletableFuture.supplyAsync(() -> {
                if (abortSignal.isAborted()) {
                    return WebFetchOutput.URLContent.builder()
                            .url(url)
                            .error("Fetch URL aborted")
                            .build();
                }

                try {
                    return fetchWebUrl(url);
                } catch (IOException e) {
                    return WebFetchOutput.URLContent.builder()
                            .url(url)
                            .error(e.getMessage())
                            .build();
                }
            }, executor);
            futures.add(future);
        }
        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                .thenApply(v -> futures.stream()
                        .map(CompletableFuture::join)
                        .collect(Collectors.toList()))
                .join();
    }

    protected WebFetchOutput.URLContent fetchWebUrl(String url) throws IOException {
        Request request = new Request.Builder()
                .url(url)
                .header("User-Agent", WebPageUtils.randomUserAgent())
                .build();
        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Unexpected code " + response);
            }
            String contentType = response.header("Content-Type");
            if (contentType == null || !(contentType.contains("text/") || contentType.contains("application/"))) {
                throw new IOException("Unexpected content type " + contentType);
            }

            String body = response.body() != null ? response.body().string() : "";
            String title = "";
            if (contentType.contains("text/html")) {
                title = Optional.ofNullable(WebPageUtils.extractHtmlTitle(body)).orElse("");
            }

            return WebFetchOutput.URLContent.builder()
                    .url(url)
                    .title(title)
                    .contentType(contentType)
                    .content(body)
                    .build();
        }
    }

    static class RetryInterceptor implements Interceptor {
        private final int maxRetries;

        RetryInterceptor(int maxRetries) {
            this.maxRetries = maxRetries;
        }

        @Override
        public Response intercept(Chain chain) throws IOException {
            Request request = chain.request();
            Response response = null;
            IOException lastException = null;
            for (int i = 0; i < maxRetries; i++) {
                try {
                    if (response != null) {
                        response.close();
                    }
                    response = chain.proceed(request);
                    if (response.isSuccessful()) {
                        return response;
                    }
                } catch (IOException e) {
                    lastException = e;
                    if (response != null) {
                        response.close();
                        response = null;
                    }
                }
            }
            throw lastException != null ? lastException : new IOException("Exceed HTTP max retries");
        }
    }
}
