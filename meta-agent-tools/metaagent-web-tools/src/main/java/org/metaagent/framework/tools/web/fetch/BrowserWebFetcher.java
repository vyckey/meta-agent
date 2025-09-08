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

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.options.LoadState;
import org.metaagent.framework.core.util.abort.AbortException;
import org.metaagent.framework.core.util.abort.AbortSignal;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

/**
 * BrowserWebFetcher is a WebFetcher implementation that uses a browser to fetch web pages.
 *
 * @author vyckey
 */
public class BrowserWebFetcher implements WebFetcher {
    protected static final ThreadLocal<Playwright> PLAYWRIGHT_LOCAL = ThreadLocal.withInitial(Playwright::create);

    private final BrowserType.LaunchOptions browserLaunchOptions;
    private final Page.WaitForLoadStateOptions pageLoadStateOptions;

    public BrowserWebFetcher(BrowserType.LaunchOptions browserLaunchOptions, Page.WaitForLoadStateOptions pageLoadStateOptions) {
        this.browserLaunchOptions = Objects.requireNonNull(browserLaunchOptions, "Browser launch options cannot be null");
        this.pageLoadStateOptions = Objects.requireNonNull(pageLoadStateOptions, "Page load state options cannot be null");
    }

    public BrowserWebFetcher() {
        this(new BrowserType.LaunchOptions().setTimeout(30000), new Page.WaitForLoadStateOptions().setTimeout(5000));
    }

    @Override
    public List<WebFetchOutput.URLContent> fetchUrls(List<String> urls, Executor executor, AbortSignal abortSignal) {
        try (Browser browser = PLAYWRIGHT_LOCAL.get().chromium().launch(browserLaunchOptions);
             BrowserContext context = browser.newContext()) {

            List<CompletableFuture<WebFetchOutput.URLContent>> futures = urls.stream()
                    .map(url -> CompletableFuture.supplyAsync(() -> {
                        try (Page page = context.newPage()) {
                            if (abortSignal.isAborted()) {
                                throw new AbortException("Fetch URL aborted");
                            }

                            page.navigate(url);
                            page.waitForLoadState(LoadState.DOMCONTENTLOADED, pageLoadStateOptions);

                            return WebFetchOutput.URLContent.builder()
                                    .url(url)
                                    .title(page.title())
                                    .contentType("text/html")
                                    .content(page.content())
                                    .build();
                        } catch (Exception e) {
                            return WebFetchOutput.URLContent.builder()
                                    .url(url)
                                    .error(e.getMessage())
                                    .build();
                        }
                    }, executor)).toList();

            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
            return futures.stream().map(future -> future.getNow(null)).toList();
        }
    }
}
