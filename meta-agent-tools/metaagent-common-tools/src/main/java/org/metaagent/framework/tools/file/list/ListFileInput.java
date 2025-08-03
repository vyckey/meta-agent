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

package org.metaagent.framework.tools.file.list;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import lombok.Getter;
import lombok.experimental.SuperBuilder;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.metaagent.framework.core.tool.schema.ToolDisplayable;

import java.util.List;

/**
 * List files input.
 *
 * @author vyckey
 */
@Getter
@SuperBuilder
public class ListFileInput implements ToolDisplayable {
    @JsonProperty(required = true)
    @JsonPropertyDescription("The target root directory.")
    private String directory;

    @JsonPropertyDescription("Whether to include the directories. Default is false")
    private boolean directoryIncluded;

    @JsonPropertyDescription("The max depth of directory. Default is null")
    private Integer maxDepth;

    @JsonPropertyDescription("The file patterns to include. e.g. *.java, *.py")
    private List<String> includeFilePatterns;

    @JsonPropertyDescription("The file patterns to exclude. e.g. *.log, build/, node_modules/")
    private List<String> excludeFilePatterns;

    @JsonPropertyDescription("The ignore files which are used to contain exclude patterns."
            + " It's just like .gitignore file. Default is [\".gitignore\"]")
    private List<String> ignoreLikeFiles;

    @JsonCreator
    public ListFileInput(@JsonProperty("directory") String directory) {
        this.directory = directory;
    }

    @Override
    public String display() {
        StringBuilder sb = new StringBuilder("List files in directory '")
                .append(directory).append("' with ");
        if (CollectionUtils.isNotEmpty(includeFilePatterns)) {
            sb.append("include patterns (").append(StringUtils.join(includeFilePatterns, ",")).append(")");
        }
        if (CollectionUtils.isNotEmpty(excludeFilePatterns)) {
            sb.append(" and exclude patterns (").append(StringUtils.join(excludeFilePatterns, ",")).append(")");
        }
        return sb.toString();
    }
}
