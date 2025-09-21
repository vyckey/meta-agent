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
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.metaagent.framework.core.util.abort.AbortSignal;
import org.metaagent.framework.tools.web.utils.WebPageUtils;

import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;

/**
 * A simple web fetcher that uses OkHttp to fetch web pages.
 *
 * @author vyckey
 */
public class HttpWebFetcher implements WebFetcher {
    private final OkHttpClient httpClient;

    public HttpWebFetcher(OkHttpClient httpClient) {
        this.httpClient = Objects.requireNonNull(httpClient, "httpClient is required");
    }

    public HttpWebFetcher() {
        this(new OkHttpClient.Builder()
                .connectTimeout(Duration.ofSeconds(3))
                .readTimeout(Duration.ofSeconds(5))
                .followRedirects(true)
                .addInterceptor(new RetryInterceptor(3))
                .build());
    }

    @Override
    public List<WebFetchOutput.URLContent> fetchUrls(List<String> urls, Executor executor, AbortSignal abortSignal) {
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
                    return fetchUrlByHttp(url);
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

    protected WebFetchOutput.URLContent fetchUrlByHttp(String url) throws IOException {
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
