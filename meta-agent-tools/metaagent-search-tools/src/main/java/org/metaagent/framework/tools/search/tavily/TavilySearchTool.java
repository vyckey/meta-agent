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
import org.metaagent.framework.core.tool.Tool;
import org.metaagent.framework.core.tool.ToolContext;
import org.metaagent.framework.core.tool.ToolExecutionException;
import org.metaagent.framework.core.tool.converter.ToolConverter;
import org.metaagent.framework.core.tool.converter.ToolConverters;
import org.metaagent.framework.core.tool.definition.ToolDefinition;
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
 * description is here
 *
 * @author vyckey
 */
public class TavilySearchTool implements Tool<TavilySearchInput, TavilySearchOutput> {
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
        return ToolDefinition.builder("tavily-search")
                .description("Tavily search tool")
                .inputSchema(TavilySearchInput.class)
                .outputSchema(TavilySearchOutput.class)
                .build();
    }

    @Override
    public ToolConverter<TavilySearchInput, TavilySearchOutput> getConverter() {
        return ToolConverters.jsonConverter(TavilySearchInput.class);
    }

    @Override
    public TavilySearchOutput run(ToolContext toolContext, TavilySearchInput input) throws ToolExecutionException {
        TavilySearchRequest searchRequest = TavilySearchRequest.builder()
                .apiKey(this.apiKey)
                .query(input.getSearchTerms())
                .searchDepth(this.searchDepth)
                .includeAnswer(this.includeAnswer)
                .includeRawContent(this.includeRawContent)
                .maxResults(input.getMaxResults())
                .includeDomains(this.includeDomains)
                .excludeDomains(this.excludeDomains).build();

        TavilySearchResponse searchResponse;
        try {
            searchResponse = tavilyClient.search(searchRequest);
        } catch (TavilyApiException e) {
            throw new ToolExecutionException(e.getMessage(), e);
        }
        List<SearchOrganicResult> results = Lists.newArrayList();
        searchResponse.getResults().stream().map(TavilySearchTool::toOrganicResult).forEach(results::add);
        return TavilySearchOutput.builder()
                .totalResults((long) results.size())
                .results(results)
                .build();
    }

    private static SearchOrganicResult toOrganicResult(TavilySearchResult tavilySearchResult) {
        String safeUrlEncoded = URLEncoder.encode(tavilySearchResult.getUrl(), StandardCharsets.UTF_8)
                .replace("+", "%20");
        Map<String, Object> metadata = Collections.singletonMap("score", String.valueOf(tavilySearchResult.getScore()));
        return SearchOrganicResult.builder()
                .title(tavilySearchResult.getTitle())
                .url(URI.create(safeUrlEncoded))
                .snippet(tavilySearchResult.getContent())
                .content(tavilySearchResult.getRawContent())
                .metadata(metadata)
                .build();
    }
}
