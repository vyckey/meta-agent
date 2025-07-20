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

package org.metaagent.framework.tools.search.tavily;

import com.google.common.collect.Lists;
import org.apache.commons.configuration2.Configuration;
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
import org.metaagent.thirdparty.tavily.api.TavilyApiException;
import org.metaagent.thirdparty.tavily.api.TavilyClient;
import org.metaagent.thirdparty.tavily.api.TavilySearchRequest;
import org.metaagent.thirdparty.tavily.api.TavilySearchResponse;
import org.metaagent.thirdparty.tavily.api.TavilySearchResult;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Tavily Search Tool
 *
 * @author vyckey
 */
public class TavilySearchTool implements SearchTool {
    private static final ToolDefinition TOOL_DEFINITION = ToolDefinition.builder("tavily_search")
            .description("Web search tool by Tavily API")
            .inputSchema(WebSearchRequest.class)
            .outputSchema(WebSearchResponse.class)
            .build();
    private static final ToolConverter<WebSearchRequest, WebSearchResponse> TOOL_CONVERTER =
            ToolConverters.jsonConverter(WebSearchRequest.class);
    private static final String DEFAULT_BASE_URL = "https://api.tavily.com/";
    private final TavilyClient tavilyClient;
    private final String apiKey;
    private final String searchDepth;
    private final Boolean includeAnswer;
    private final Boolean includeRawContent;
    private final List<String> includeDomains;
    private final List<String> excludeDomains;

    public TavilySearchTool(Configuration configuration) {
        String apiKey = configuration.getString("apiKey", System.getenv("TAVILY_API_KEY"));
        if (StringUtils.isEmpty(apiKey)) {
            throw new IllegalArgumentException("Tavily API key is required");
        }

        String baseUrl = configuration.getString("baseUrl", DEFAULT_BASE_URL);
        Duration timeout = Duration.ofSeconds(configuration.getInt("timeout", 10));
        this.tavilyClient = new TavilyClient(baseUrl, timeout);
        this.apiKey = apiKey;
        this.searchDepth = configuration.getString("searchDepth");
        this.includeAnswer = configuration.getBoolean("includeAnswer");
        this.includeRawContent = configuration.getBoolean("includeRawContent");
        this.includeDomains = configuration.getList(String.class, "includeDomains");
        this.excludeDomains = configuration.getList(String.class, "includeDomains");
    }


    @Override
    public ToolDefinition getDefinition() {
        return TOOL_DEFINITION;
    }

    @Override
    public ToolConverter<WebSearchRequest, WebSearchResponse> getConverter() {
        return TOOL_CONVERTER;
    }

    @Override
    public WebSearchResponse run(ToolContext toolContext, WebSearchRequest input) throws ToolExecutionException {
        TavilySearchRequest searchRequest = TavilySearchRequest.builder()
                .apiKey(this.apiKey)
                .query(input.searchTerms())
                .searchDepth(this.searchDepth)
                .includeAnswer(this.includeAnswer)
                .includeRawContent(this.includeRawContent)
                .maxResults(input.maxResults())
                .includeDomains(this.includeDomains)
                .excludeDomains(this.excludeDomains).build();

        TavilySearchResponse searchResponse;
        try {
            searchResponse = tavilyClient.search(searchRequest);
        } catch (TavilyApiException e) {
            throw new ToolExecutionException(e.getMessage(), e);
        }
        WebSearchInformation searchInfo = WebSearchInformation.builder()
                .totalResults((long) searchResponse.getResults().size())
                .build();

        List<WebSearchResult> results = Lists.newArrayList();
        searchResponse.getResults().stream().map(TavilySearchTool::toSearchResult).forEach(results::add);

        if (StringUtils.isNotEmpty(searchResponse.getAnswer())) {
            results.add(0, WebSearchResult.builder()
                    .title("Tavily Search API")
                    .url(URI.create("https://tavily.com/"))
                    .snippet(searchResponse.getAnswer())
                    .build());
        }
        return WebSearchResponse.builder()
                .searchInfo(searchInfo)
                .searchResults(results)
                .build();
    }

    private static WebSearchResult toSearchResult(TavilySearchResult tavilySearchResult) {
        String safeUrlEncoded = URLEncoder.encode(tavilySearchResult.getUrl(), StandardCharsets.UTF_8)
                .replace("+", "%20");
        Map<String, Object> metadata = Collections.singletonMap("score", String.valueOf(tavilySearchResult.getScore()));
        return WebSearchResult.builder()
                .title(tavilySearchResult.getTitle())
                .url(URI.create(safeUrlEncoded))
                .snippet(tavilySearchResult.getContent())
                .content(tavilySearchResult.getRawContent())
                .metadata(metadata)
                .build();
    }
}
