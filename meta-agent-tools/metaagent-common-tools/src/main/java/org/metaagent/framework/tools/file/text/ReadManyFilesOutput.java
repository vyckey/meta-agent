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

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import org.metaagent.framework.core.tool.schema.ToolDisplayable;

import java.nio.file.Path;
import java.util.List;

@Builder
public record ReadManyFilesOutput(
        @JsonIgnore
        Path directory,

        @JsonIgnore
        List<ReadFileContent> fileContents,

        @JsonIgnore
        List<SkipFileReason> skippedFiles,

        @JsonIgnore
        String display) implements ToolDisplayable {
    public static final String FILE_CONTENTS_FORMAT = "--- %s ---\n\n%s\n\n";

    public record ReadFileContent(Path filePath, String content) {
    }

    public record SkipFileReason(Path filePath, String reason) {
    }

    @JsonProperty("directory")
    public String getDirectoryPath() {
        return directory.toAbsolutePath().toString();
    }

    @JsonProperty("fileContents")
    public String getFileContentsText() {
        StringBuilder sb = new StringBuilder();
        for (ReadFileContent fileContent : fileContents) {
            if (!sb.isEmpty()) {
                sb.append("\n");
            }
            sb.append(FILE_CONTENTS_FORMAT.formatted(fileContent.filePath(), fileContent.content()));
        }
        return sb.toString();
    }

    @Override
    public String display() {
        return display;
    }

    @Override
    public String toString() {
        return display;
    }
}
