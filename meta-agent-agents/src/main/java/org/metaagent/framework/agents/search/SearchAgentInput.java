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

package org.metaagent.framework.agents.search;

import lombok.Builder;
import org.apache.commons.lang3.StringUtils;

/**
 * {@link SearchAgent} input
 *
 * @param query          the search query
 * @param queryContext   the query context
 * @param forceSearch    whether to force search
 * @param detailIncluded whether to include detailed information in the search
 * @author vyckey
 */
@Builder
public record SearchAgentInput(
        String query,
        String queryContext,
        boolean forceSearch,
        boolean detailIncluded) {

    public SearchAgentInput(String query, String queryContext, boolean forceSearch, boolean detailIncluded) {
        if (StringUtils.isEmpty(query)) {
            throw new IllegalArgumentException("query cannot be empty");
        }
        this.query = query.trim();
        this.queryContext = queryContext != null ? queryContext.trim() : "";
        this.forceSearch = forceSearch;
        this.detailIncluded = detailIncluded;
    }

    public static SearchAgentInput from(String query) {
        return SearchAgentInput.builder().query(query).build();
    }

}
