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

package org.metaagent.framework.tools.file.find;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import jakarta.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;
import org.metaagent.framework.core.tool.schema.ToolDisplayable;

import java.util.Objects;
import java.util.Optional;
import java.util.regex.Pattern;

/**
 * Input parameters for the GrepFile tool.
 *
 * @author vyckey
 */
@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class GrepFileInput implements ToolDisplayable {
    @NotBlank(message = "Grep pattern must be specified")
    @JsonProperty(required = true)
    @JsonPropertyDescription("The regular expression pattern to search for in file contents " +
            "(e.g., 'function\\s+testFunc', 'import\\\\s+\\\\{.*\\\\}\\\\s+from\\\\s+.*')")
    private Pattern pattern;

    @JsonPropertyDescription("The directory to search in. Optional, defaults to current working directory")
    private String directory;

    @JsonPropertyDescription("The pattern to filter which files to search in (e.g., '*.js', '*.{ts,tsx}', 'src/**'). Optional, defaults to all files")
    private String include;

    @JsonCreator
    public GrepFileInput(@JsonProperty("directory") String directory, @JsonProperty("pattern") Pattern pattern) {
        this.directory = directory;
        this.pattern = Objects.requireNonNull(pattern, "Pattern is required");
    }

    @Override
    public String display() {
        String dir = directory;
        if (StringUtils.isEmpty(dir)) {
            dir = Optional.ofNullable(System.getenv("CWD")).orElse(".");
        }
        StringBuilder sb = new StringBuilder("Search content in directory '")
                .append(dir).append('\'');
        if (StringUtils.isNotEmpty(include)) {
            sb.append('(').append(include).append(')');
        }
        sb.append(": ").append(pattern.pattern());
        return sb.toString();
    }

}
