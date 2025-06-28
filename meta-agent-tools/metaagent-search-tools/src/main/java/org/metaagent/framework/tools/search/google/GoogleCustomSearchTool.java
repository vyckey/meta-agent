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

package org.metaagent.framework.tools.search.google;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.apache.ApacheHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.customsearch.v1.Customsearch;
import com.google.api.services.customsearch.v1.model.Result;
import com.google.api.services.customsearch.v1.model.Search;
import com.google.common.collect.Lists;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.metaagent.framework.core.tool.ToolContext;
import org.metaagent.framework.core.tool.ToolExecutionException;
import org.metaagent.framework.core.tool.converter.ToolConverter;
import org.metaagent.framework.core.tool.converter.ToolConverters;
import org.metaagent.framework.core.tool.definition.ToolDefinition;
import org.metaagent.framework.tools.search.SearchTool;
import org.metaagent.framework.tools.search.common.WebSearchInformation;
import org.metaagent.framework.tools.search.common.WebSearchRequest;
import org.metaagent.framework.tools.search.common.WebSearchResponse;
import org.metaagent.framework.tools.search.common.WebSearchResult;

import java.io.IOException;
import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * <a href="https://developers.google.com/custom-search/v1/overview">Google Custom Search v1</a>
 *
 * @author vyckey
 */
public class GoogleCustomSearchTool implements SearchTool {
    private static final String DEFAULT_ENGINE = "google";
    private static final String DEFAULT_LANGUAGE = "en";
    private final Customsearch customsearch;
    private final String apiKey;

    public GoogleCustomSearchTool(Customsearch customsearch, String apiKey) {
        if (StringUtils.isEmpty(apiKey)) {
            throw new IllegalArgumentException("Customsearch API key is required");
        }
        this.customsearch = Objects.requireNonNull(customsearch, "customsearch is required");
        this.apiKey = apiKey;
    }

    public GoogleCustomSearchTool(Customsearch customsearch) {
        this(customsearch, Objects.requireNonNull(System.getenv("CUSTOM_SEARCH_API_KEY"),
                "Environment variable CUSTOM_SEARCH_API_KEY is required"));
    }

    public GoogleCustomSearchTool() {
        this(new Customsearch.Builder(new ApacheHttpTransport(), new JacksonFactory(), new GoogleCredential())
                .build());
    }

    @Override
    public ToolDefinition getDefinition() {
        return ToolDefinition.builder("google_custom_search")
                .description("Web search tool by Google Custom Search API")
                .inputSchema(WebSearchRequest.class)
                .outputSchema(WebSearchResponse.class)
                .build();
    }

    @Override
    public ToolConverter<WebSearchRequest, WebSearchResponse> getConverter() {
        return ToolConverters.jsonConverter(WebSearchRequest.class);
    }

    @Override
    public WebSearchResponse run(ToolContext toolContext, WebSearchRequest webSearchRequest) throws ToolExecutionException {
        try {
            HttpRequest httpRequest = customsearch.cse().list()
                    .setKey(apiKey)
                    .setCx(Optional.ofNullable(webSearchRequest.searchEngine()).orElse(DEFAULT_ENGINE))
                    .setQ(webSearchRequest.searchTerms())
                    .setLr(Optional.ofNullable(webSearchRequest.language()).orElse(DEFAULT_LANGUAGE))
                    .setStart(1L)
                    .setNum(webSearchRequest.maxResults())
                    .buildHttpRequest();
            HttpResponse httpResponse = httpRequest.execute();
            Search searchResult = httpResponse.parseAs(Search.class);

            List<WebSearchResult> searchResults = toSearchResults(searchResult.getItems());
            WebSearchInformation searchInfo = WebSearchInformation.builder()
                    .totalResults((long) searchResults.size())
                    .build();

            return WebSearchResponse.builder()
                    .searchInfo(searchInfo)
                    .searchResults(searchResults)
                    .build();
        } catch (IOException e) {
            throw new ToolExecutionException("request customsearch fail", e);
        }
    }

    @SuppressWarnings("unchecked")
    private List<WebSearchResult> toSearchResults(List<Result> results) {
        List<WebSearchResult> searchResults = Lists.newArrayListWithCapacity(results.size());
        for (Result result : results) {
            Map<String, Object> pagemap = result.getPagemap();
            if (pagemap == null || !pagemap.containsKey("metatags")) {
                continue;
            }

            Map metatags = MapUtils.getMap(pagemap, "metatags", Collections.emptyMap());
            String content = metatags.getOrDefault("og:description", "N/A").toString();
            WebSearchResult searchResult = WebSearchResult.builder()
                    .title(result.getTitle())
                    .url(URI.create(result.getLink()))
                    .snippet(result.getSnippet())
                    .content(content)
                    .metadata(Collections.emptyMap())
                    .build();
            searchResults.add(searchResult);
        }
        return searchResults;
    }
}
