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

package org.metaagent.framework.tools.search.common;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import lombok.Builder;
import org.metaagent.framework.core.tool.schema.ToolDisplayable;

import java.util.Map;

/**
 * Web search request
 *
 * @author vyckey
 */
@Builder
public record WebSearchRequest(
        @JsonProperty(required = true)
        @JsonPropertyDescription("Search terms to query the web with.")
        String searchTerms,

        @JsonPropertyDescription("Search engine to use for the query.")
        String searchEngine,

        @JsonPropertyDescription("Maximum number of results to return. Optional.")
        Integer maxResults,

        @JsonPropertyDescription("Language to search in. Optional, default is based on user query.")
        String language,

        @JsonPropertyDescription("Geolocation to use for the search. Optional, default is based on user information.")
        String geoLocation,

        @JsonPropertyDescription("Start page for pagination. Optional, default is 1.")
        Integer startPage,

        @JsonPropertyDescription("Index to start returning results from. Optional, default is 0.")
        Integer startIndex,

        @JsonPropertyDescription("Additional parameters for the search request. Optional.")
        Map<String, Object> additionalParams) implements ToolDisplayable {

    @Override
    public String display() {
        StringBuilder sb = new StringBuilder("Web Search [").append(searchTerms).append(']');
        if (searchEngine != null) {
            sb.append(" by ").append(searchEngine);
        }
        return sb.toString();
    }
}
