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

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import lombok.Builder;
import org.apache.commons.lang3.StringUtils;
import org.metaagent.framework.core.tool.schema.ToolDisplayable;

import java.util.List;

/**
 * Output of the {@link WebFetchTool}.
 *
 * @author vyckey
 */
public record WebFetchOutput(
        @JsonPropertyDescription("URL contents")
        List<URLContent> urlContents,

        @JsonPropertyDescription("Error message while failing to fetch URLs")
        String error
) implements ToolDisplayable {

    @Override
    public String display() {
        long errorCount = urlContents.stream().map(URLContent::error).filter(StringUtils::isNotEmpty).count();

        final int maxDisplayUrls = 5;
        StringBuilder sb = new StringBuilder("Fetched ").append(urlContents.size()).append(" URL contents")
                .append(" (").append(errorCount).append(" failed)\n");
        for (int i = 0; i < urlContents.size(); i++) {
            URLContent urlContent = urlContents.get(i);

            String status = "SUCCESS";
            if (urlContent.error() != null) {
                status = urlContent.error().replace("\n", " ");
                status = status.length() > 30 ? status.substring(0, 30) + "..." : status;
            }
            sb.append("- ").append(urlContent.url()).append(" : ").append(status).append("\n");
            if (i > maxDisplayUrls) {
                sb.append("- ...(").append(urlContents.size() - i).append(" more)");
                break;
            }
        }
        return sb.toString();
    }

    @Builder(builderClassName = "Builder", toBuilder = true)
    public record URLContent(
            @JsonProperty(required = true)
            @JsonPropertyDescription("The target URL")
            String url,

            @JsonPropertyDescription("The title of URL")
            String title,

            @JsonIgnore
            String contentType,

            @JsonPropertyDescription("The content of URL")
            String content,

            @JsonPropertyDescription("The error message if the URL cannot be fetched")
            String error) {

    }
}
