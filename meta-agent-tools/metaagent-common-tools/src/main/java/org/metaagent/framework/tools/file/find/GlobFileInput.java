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

import java.util.List;
import java.util.Optional;

/**
 * Input for the GlobFile tool.
 * This class represents the input parameters required to perform a file search using glob patterns.
 *
 * @author vyckey
 */
@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class GlobFileInput implements ToolDisplayable {
    @JsonPropertyDescription("The directory to search in. Optional, defaults to current working directory")
    private String directory;

    @NotBlank(message = "pattern must be specified")
    @JsonProperty(required = true)
    @JsonPropertyDescription("The glob pattern to match files against")
    private String pattern;

    @JsonPropertyDescription("Whether the search should be case-sensitive. Optional, defaults to false")
    private Boolean caseSensitive;

    @JsonPropertyDescription("The ignore files which are used to contain exclude patterns."
            + " It's just like .gitignore file. Default is [\".gitignore\"]")
    private List<String> ignoreLikeFiles;

    @JsonCreator
    public GlobFileInput(@JsonProperty("pattern") String pattern) {
        this.pattern = pattern;
    }

    @Override
    public String display() {
        String dir = directory;
        if (StringUtils.isEmpty(dir)) {
            dir = Optional.ofNullable(System.getenv("CWD")).orElse(".");
        }
        return "Find files in directory '" + dir + "': " + pattern;
    }

}
