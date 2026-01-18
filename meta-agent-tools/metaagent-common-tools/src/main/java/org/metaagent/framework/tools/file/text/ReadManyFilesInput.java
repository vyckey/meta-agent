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

package org.metaagent.framework.tools.file.text;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import jakarta.validation.constraints.NotEmpty;
import lombok.Builder;
import lombok.Getter;
import lombok.experimental.SuperBuilder;
import org.apache.commons.collections.CollectionUtils;
import org.metaagent.framework.core.tool.schema.ToolDisplayable;

import java.util.List;

@Getter
@SuperBuilder
public class ReadManyFilesInput implements ToolDisplayable {
    @JsonPropertyDescription("The target root directory. Optional, default is current working directory.")
    private String directory;

    @NotEmpty(message = "Path patterns cannot be empty.")
    @JsonProperty(required = true)
    @JsonPropertyDescription("An array of glob patterns or paths relative to current working directory to read from. (e.g. [\"src/**/*.ts\"], [\"README.md\", \"docs/\"])")
    private List<String> pathPatterns;

    @JsonPropertyDescription("Glob patterns to exclude files from reading (e.g. [\"**/node_modules/**\", \"**/*.test.ts\"]). Optional.")
    private List<String> excludePatterns;

    @JsonPropertyDescription("Glob patterns to include files for reading. Optional.")
    private List<String> includePatterns;

    @Builder.Default
    @JsonPropertyDescription("Read files recursively from subdirectories. Optional, default is true.")
    private Boolean recursive = true;

    @JsonPropertyDescription("The ignore files which are used to contain exclude patterns."
            + " It's just like .gitignore file. Default is [\".gitignore\"]")
    private List<String> ignoreLikeFiles;

    public ReadManyFilesInput(String directory, List<String> pathPatterns) {
        this.directory = directory;
        this.pathPatterns = pathPatterns;
    }

    @Override
    public String display() {
        StringBuilder sb = new StringBuilder("Read many files in directory '")
                .append(directory).append("' with patterns (");
        sb.append(String.join(", ", pathPatterns)).append(")");
        if (CollectionUtils.isNotEmpty(excludePatterns)) {
            sb.append(" excluding (").append(String.join(", ", excludePatterns)).append(")");
        }
        if (CollectionUtils.isNotEmpty(includePatterns)) {
            sb.append(" including (").append(String.join(", ", includePatterns)).append(")");
        }
        return sb.toString();
    }
}
