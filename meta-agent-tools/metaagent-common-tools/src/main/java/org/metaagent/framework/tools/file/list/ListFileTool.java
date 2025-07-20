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

import lombok.extern.slf4j.Slf4j;
import org.metaagent.framework.core.tool.Tool;
import org.metaagent.framework.core.tool.ToolContext;
import org.metaagent.framework.core.tool.ToolExecutionException;
import org.metaagent.framework.core.tool.converter.JsonToolConverter;
import org.metaagent.framework.core.tool.converter.ToolConverter;
import org.metaagent.framework.core.tool.definition.ToolDefinition;
import org.metaagent.framework.tools.file.util.FilePathFilter;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * List files tool
 *
 * @author vyckey
 */
@Slf4j
public class ListFileTool implements Tool<ListFileInput, ListFileOutput> {
    private static final ToolDefinition TOOL_DEFINITION = ToolDefinition.builder("list_files")
            .description("List files under specialized directory")
            .inputSchema(ListFileInput.class)
            .outputSchema(ListFileOutput.class)
            .build();
    private static final ToolConverter<ListFileInput, ListFileOutput> TOOL_CONVERTER =
            JsonToolConverter.create(ListFileInput.class);

    @Override
    public ToolDefinition getDefinition() {
        return TOOL_DEFINITION;
    }

    @Override
    public ToolConverter<ListFileInput, ListFileOutput> getConverter() {
        return TOOL_CONVERTER;
    }

    @Override
    public ListFileOutput run(ToolContext toolContext, ListFileInput input) throws ToolExecutionException {
        Path directory = Path.of(input.getDirectory());
        try {
            FilePathFilter filePathFilter = FilePathFilter.create(input, directory);
            int maxDepth = input.getMaxDepth() == null || input.getMaxDepth() < 0 ? Integer.MAX_VALUE : input.getMaxDepth();
            List<File> files = Files.walk(directory, maxDepth)
                    .filter(path -> {
                        if (input.isDirectoryIncluded()) {
                            return true;
                        }
                        return Files.isRegularFile(path);
                    })
                    .filter(path -> filePathFilter.accept(directory, path))
                    .map(Path::toFile)
                    .toList();
            return new ListFileOutput(files);
        } catch (IOException e) {
            log.warn("Error when listing files for dir {}", directory, e);
            throw new ToolExecutionException(e);
        }
    }

    public static void main(String[] args) {
        ListFileInput input = ListFileInput.builder()
                .directory(".")
                .directoryIncluded(true)
                .build();
        ListFileTool tool = new ListFileTool();
        ListFileOutput output = tool.run(ToolContext.create(), input);
        System.out.println(output.getFileList());
    }
}
