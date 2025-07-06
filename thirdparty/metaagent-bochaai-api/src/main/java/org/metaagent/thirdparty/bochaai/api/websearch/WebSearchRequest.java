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

package org.metaagent.thirdparty.bochaai.api.websearch;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.io.Serializable;

@Getter
@Builder
@AllArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public class WebSearchRequest implements Serializable {
    /**
     * The search query. Required.
     */
    private String query;

    /**
     * The freshness of the search results. Optional.
     * Values: noLimit, oneDay, oneWeek, oneMonth, oneYear, YYYY-MM-DD, YYYY-MM-DD..YYYY-MM-DD
     */
    private String freshness;

    /**
     * Whether show a summary of the search results. Optional. Default is false.
     */
    private Boolean summary;

    /**
     * Include specific domains in the search results. Optional. e.g., "example.com|example.org".
     */
    private String include;

    /**
     * Exclude specific domains from the search results. Optional. e.g., "example.com|example.org".
     */
    private String exclude;

    /**
     * The number of search results to return. Optional. Default is 10.
     */
    private Integer count;

    public WebSearchRequest(String query) {
        this.query = query;
    }
}