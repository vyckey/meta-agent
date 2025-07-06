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

package org.metaagent.framework.tools.search;

import org.apache.commons.collections.CollectionUtils;
import org.junit.jupiter.api.Test;
import org.metaagent.framework.core.tool.DefaultToolContext;
import org.metaagent.framework.core.tool.ToolContext;
import org.metaagent.framework.tools.search.bochaai.BochaaiWebSearchTool;
import org.metaagent.framework.tools.search.common.WebSearchRequest;
import org.metaagent.framework.tools.search.common.WebSearchResponse;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class SearchToolTest {


    @Test
    void bochaaiToolTest() {
        SearchTool searchTool = new BochaaiWebSearchTool();

        WebSearchRequest request = WebSearchRequest.builder()
                .searchTerms("Global hot news today")
                .maxResults(10)
                .build();
        ToolContext toolContext = DefaultToolContext.builder().build();
        WebSearchResponse response = searchTool.run(toolContext, request);

        assertNotNull(response);
        assertFalse(CollectionUtils.isEmpty(response.searchResults()));
    }
}