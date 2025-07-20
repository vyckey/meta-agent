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

package org.metaagent.framework.tools.search.bochaai;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
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
import org.metaagent.thirdparty.bochaai.api.BochaaiClient;
import org.metaagent.thirdparty.bochaai.api.BochaaiResponse;
import org.metaagent.thirdparty.bochaai.api.websearch.WebPageValue;
import org.metaagent.thirdparty.bochaai.api.websearch.WebSearchData;
import org.metaagent.thirdparty.bochaai.api.websearch.WebSearchWebPages;

import java.net.URI;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Search tool using Bochaai to perform web searches.
 *
 * @author vyckey
 */
public class BochaaiWebSearchTool implements SearchTool {
    private static final ToolDefinition TOOL_DEFINITION = ToolDefinition.builder("bochaai_web_search")
            .description("Web search tool by Bochaai")
            .inputSchema(WebSearchRequest.class)
            .outputSchema(WebSearchResponse.class)
            .build();
    private static final ToolConverter<WebSearchRequest, WebSearchResponse> TOOL_CONVERTER =
            ToolConverters.jsonConverter(WebSearchRequest.class);
    private final BochaaiClient client;

    public BochaaiWebSearchTool(BochaaiClient client) {
        this.client = Objects.requireNonNull(client, "client is required");
    }

    public BochaaiWebSearchTool(Duration timeout) {
        this(new BochaaiClient(System.getenv("BOCHAAI_API_KEY"), timeout));
    }

    public BochaaiWebSearchTool() {
        this(Duration.ofSeconds(10));
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
    public WebSearchResponse run(ToolContext toolContext, WebSearchRequest webSearchRequest) throws ToolExecutionException {
        org.metaagent.thirdparty.bochaai.api.websearch.WebSearchRequest searchRequest =
                org.metaagent.thirdparty.bochaai.api.websearch.WebSearchRequest.builder()
                        .query(webSearchRequest.searchTerms())
                        .count(webSearchRequest.maxResults())
                        .build();

        BochaaiResponse<WebSearchData> searchResponse = this.client.search(searchRequest);
        WebSearchData searchData = searchResponse.data();
        WebSearchInformation searchInfo = WebSearchInformation.builder()
                .totalResults((long) searchData.webPages().value().size())
                .pageIndex(0)
                .metadata(Map.of(
                        "original_query", searchData.queryContext().originalQuery()
                ))
                .build();
        return WebSearchResponse.builder()
                .searchInfo(searchInfo)
                .searchResults(toSearchResults(searchData.webPages()))
                .build();
    }

    private List<WebSearchResult> toSearchResults(WebSearchWebPages webPages) {
        List<WebSearchResult> searchResults = Lists.newArrayList();
        for (WebPageValue pageValue : webPages.value()) {
            Map<String, Object> metadata = Maps.newHashMapWithExpectedSize(2);
            metadata.put("site", pageValue.siteName());
            metadata.put("published_at", pageValue.datePublished());
            WebSearchResult searchResult = WebSearchResult.builder()
                    .title(pageValue.name())
                    .url(URI.create(pageValue.url()))
                    .snippet(StringUtils.isNotEmpty(pageValue.snippet()) ? pageValue.snippet() : "")
                    .content(null)
                    .metadata(metadata)
                    .build();
            searchResults.add(searchResult);
        }
        return searchResults;
    }
}
