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

package org.metaagent.framework.tools.search.fetch;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import org.metaagent.framework.core.tool.schema.ToolDisplayable;

import java.util.List;

/**
 * Input of the {@link WebFetchTool}.
 */
public record WebFetchInput(
        @JsonProperty(required = true)
        @JsonPropertyDescription("The URLs to fetch")
        List<String> urls,

        @JsonPropertyDescription("Whether to return the raw content or simplified content which is usually a Markdown format text"
                + " with useless text removed. Optional, default is false.")
        Boolean raw
) implements ToolDisplayable {
    public WebFetchInput(List<String> urls) {
        this(urls, false);
    }

    @Override
    public String display() {
        int displaySize = Math.min(urls.size(), 5);
        StringBuilder sb = new StringBuilder("Fetch URLs:\n");
        for (int i = 0; i < displaySize; i++) {
            sb.append("- ").append(urls.get(i)).append("\n");
        }
        if (displaySize < urls.size()) {
            sb.append("- ...(").append(urls.size() - displaySize).append(" more)");
        }
        return sb.toString();
    }
}
