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

package org.metaagent.framework.tools.search.searchapi;

import com.google.common.collect.Maps;
import org.apache.commons.lang3.StringUtils;
import org.metaagent.framework.core.tool.Tool;
import org.metaagent.framework.core.tool.ToolContext;
import org.metaagent.framework.core.tool.ToolExecutionException;
import org.metaagent.framework.core.tool.converter.ToolConverter;
import org.metaagent.framework.core.tool.converter.ToolConverters;
import org.metaagent.framework.core.tool.definition.ToolDefinition;
import org.metaagent.framework.tools.search.common.WebSearchInformation;
import org.metaagent.framework.tools.search.common.WebSearchRequest;
import org.metaagent.framework.tools.search.common.WebSearchResponse;
import org.metaagent.framework.tools.search.common.WebSearchResult;
import org.metaagent.thirdparty.google.api.searchapi.OrganicResult;
import org.metaagent.thirdparty.google.api.searchapi.SearchApiClient;
import org.metaagent.thirdparty.google.api.searchapi.SearchApiWebSearchRequest;
import org.metaagent.thirdparty.google.api.searchapi.SearchApiWebSearchResponse;

import java.net.URI;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * description is here
 *
 * @author vyckey
 */
public class SearchApiTool implements Tool<WebSearchRequest, WebSearchResponse> {
    private static final String DEFAULT_BASE_URL = "https://www.searchapi.io";
    private static final String DEFAULT_ENGINE = "google";
    private final SearchApiClient client;
    private final String apiKey;
    private final String engine;

    public SearchApiTool() {
        this(
                DEFAULT_BASE_URL,
                Duration.ofSeconds(30),
                Objects.requireNonNull(System.getenv("SEARCH_API_KEY"), "Environment variable SEARCH_API_KEY is not set"),
                DEFAULT_ENGINE
        );
    }

    public SearchApiTool(SearchApiClient client, String apiKey, String engine) {
        this.client = Objects.requireNonNull(client, "client is required");
        this.apiKey = Objects.requireNonNull(apiKey, "SearchAPI API key is required");
        this.engine = StringUtils.isNotEmpty(engine) ? engine : DEFAULT_ENGINE;
    }

    public SearchApiTool(String baseUrl, Duration timeout, String apiKey, String engine) {
        this.apiKey = Objects.requireNonNull(apiKey, "SearchAPI API key is required");
        this.engine = StringUtils.isNotEmpty(engine) ? engine : DEFAULT_ENGINE;
        this.client = SearchApiClient.builder()
                .baseUrl(StringUtils.isNotEmpty(baseUrl) ? baseUrl : DEFAULT_BASE_URL)
                .timeout(timeout != null ? timeout : Duration.ofSeconds(30))
                .build();
    }

    @Override
    public ToolDefinition getDefinition() {
        return ToolDefinition.builder("SearchAPITool")
                .description("Web search tool by SearchAPI")
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
        SearchApiWebSearchRequest searchRequest = SearchApiWebSearchRequest.builder()
                .apiKey(this.apiKey)
                .engine(this.engine)
                .query(webSearchRequest.searchTerms())
                .build();

        SearchApiWebSearchResponse searchResponse = this.client.search(searchRequest);
        WebSearchInformation searchInfo = WebSearchInformation.builder()
                .totalResults(getTotalResults(searchResponse.getSearchInformation()))
                .pageIndex(getCurrentPage(searchResponse.getSearchInformation()))
                .build();
        List<WebSearchResult> searchResults = searchResponse.getOrganicResults().stream()
                .map(this::toSearchResult).toList();
        return WebSearchResponse.builder()
                .searchInfo(searchInfo)
                .searchResults(searchResults)
                .build();
    }

    private static long getTotalResults(Map<String, Object> searchInformation) {
        if (searchInformation != null && searchInformation.containsKey("total_results")) {
            Object totalResults = searchInformation.get("total_results");
            return totalResults instanceof Integer ? ((Integer) totalResults).longValue() : (Long) totalResults;
        } else {
            return 0L;
        }
    }

    private Integer getCurrentPage(Map<String, Object> pagination) {
        return pagination != null && pagination.containsKey("current") ? (Integer) pagination.get("current") : null;
    }

    private WebSearchResult toSearchResult(OrganicResult result) {
        Map<String, Object> metadata = Maps.newHashMapWithExpectedSize(1);
        metadata.put("position", result.getPosition());
        return WebSearchResult.builder()
                .title(result.getTitle())
                .url(URI.create(result.getLink()))
                .snippet(StringUtils.isNotEmpty(result.getSnippet()) ? result.getSnippet() : "")
                .content(null)
                .metadata(metadata)
                .build();
    }
}
