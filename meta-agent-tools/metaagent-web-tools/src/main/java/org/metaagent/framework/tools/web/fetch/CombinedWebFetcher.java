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
import org.apache.commons.lang3.StringUtils;
import org.metaagent.framework.core.util.abort.AbortSignal;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Executor;

/**
 * CombinedWebFetcher is a WebFetcher that combines the functionalities of HttpWebFetcher, BrowserWebFetcher or others.
 *
 * @author vyckey
 */
public class CombinedWebFetcher implements WebFetcher {
    private final List<WebFetcher> webFetchers;

    public CombinedWebFetcher(List<WebFetcher> webFetchers) {
        this.webFetchers = Objects.requireNonNull(webFetchers, "webFetchers cannot be null");
    }

    public CombinedWebFetcher(WebFetcher... webFetchers) {
        this(List.of(webFetchers));
    }

    @Override
    public List<WebFetchOutput.URLContent> fetchUrls(List<String> urls, Executor executor, AbortSignal abortSignal) {
        Map<String, WebFetchOutput.URLContent> urlContentMap = Maps.newHashMapWithExpectedSize(urls.size());
        List<String> urlsToFetch = Lists.newArrayList(urls);
        for (WebFetcher webFetcher : webFetchers) {
            if (urlsToFetch.isEmpty()) {
                break;
            }

            List<WebFetchOutput.URLContent> urlContents = webFetcher.fetchUrls(urlsToFetch, executor, abortSignal);

            urlsToFetch.clear();
            for (WebFetchOutput.URLContent urlContent : urlContents) {
                urlContentMap.put(urlContent.url(), urlContent);
                if (StringUtils.isNotEmpty(urlContent.error())) {
                    urlsToFetch.add(urlContent.url());
                }
            }
        }
        return urls.stream().map(urlContentMap::get).toList();
    }
}
