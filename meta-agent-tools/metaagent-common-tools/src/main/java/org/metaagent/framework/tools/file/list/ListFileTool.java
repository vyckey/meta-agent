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
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.metaagent.framework.common.abort.AbortException;
import org.metaagent.framework.common.ignorefile.GitIgnoreLikeFileFilter;
import org.metaagent.framework.core.security.approval.ApprovalStatus;
import org.metaagent.framework.core.security.approval.PermissionApproval;
import org.metaagent.framework.core.tool.Tool;
import org.metaagent.framework.core.tool.ToolContext;
import org.metaagent.framework.core.tool.approval.ToolApprovalRequest;
import org.metaagent.framework.core.tool.converter.ToolConverter;
import org.metaagent.framework.core.tool.converter.ToolConverters;
import org.metaagent.framework.core.tool.definition.ToolDefinition;
import org.metaagent.framework.core.tool.exception.ToolArgumentException;
import org.metaagent.framework.core.tool.exception.ToolExecutionException;
import org.metaagent.framework.core.tool.exception.ToolRejectException;
import org.metaagent.framework.core.tool.schema.ToolArgsValidator;
import org.metaagent.framework.tools.file.AbstractFileTool;
import org.metaagent.framework.tools.file.util.FilePathFilter;
import org.metaagent.framework.tools.file.util.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.regex.Pattern;

/**
 * List files tool
 *
 * @author vyckey
 */
@Slf4j
public class ListFileTool extends AbstractFileTool<ListFileInput, ListFileOutput>
        implements Tool<ListFileInput, ListFileOutput> {
    public static final String TOOL_NAME = "list_files";
    private static final ToolDefinition TOOL_DEFINITION = ToolDefinition.builder(TOOL_NAME)
            .description("List files which contains file name, size and permissions under specialized directory."
                    + "Can optionally include or ignore files by glob patterns.")
            .inputSchema(ListFileInput.class)
            .outputSchema(ListFileOutput.class)
            .isConcurrencySafe(true)
            .isReadOnly(true)
            .build();
    private static final ToolConverter<ListFileInput, ListFileOutput> TOOL_CONVERTER =
            ToolConverters.jsonConverter(ListFileInput.class);

    @Override
    public ToolDefinition getDefinition() {
        return TOOL_DEFINITION;
    }

    @Override
    public ToolConverter<ListFileInput, ListFileOutput> getConverter() {
        return TOOL_CONVERTER;
    }

    protected Path validateInput(ToolContext toolContext, ListFileInput input) throws ToolExecutionException {
        ToolArgsValidator.validate(input);

        Path directory = toolContext.getWorkingDirectory();
        if (StringUtils.isNotEmpty(input.getDirectory())) {
            Path filePath = Path.of(input.getDirectory());
            directory = FileUtils.resolvePath(toolContext.getWorkingDirectory(), filePath);

            if (!checkFileAccessible(toolContext, directory)) {
                ToolApprovalRequest approvalRequest = ToolApprovalRequest.builder()
                        .id(toolContext.getExecutionId())
                        .toolName(getName())
                        .approvalContent("Request list files in root directory: " + directory)
                        .input(input)
                        .build();
                PermissionApproval approvalResult = toolContext.requestApproval(approvalRequest);
                if (approvalResult.getApprovalStatus() == ApprovalStatus.REJECTED) {
                    throw new ToolRejectException(getName(), "user rejected to list files in root directory '" + directory + "'");
                }
            }
        }

        if (!directory.toFile().isDirectory()) {
            throw new ToolArgumentException("'" + directory + "' is not a valid directory");
        }
        if (!Files.exists(directory)) {
            throw new ToolArgumentException("Directory '" + directory + "' does not exist");
        }
        return directory;
    }

    @Override
    public ListFileOutput run(ToolContext toolContext, ListFileInput input) throws ToolExecutionException {
        Path directory = validateInput(toolContext, input);
        if (toolContext.getAbortSignal().isAborted()) {
            throw new AbortException("Tool " + getName() + " is cancelled");
        }

        List<File> files;
        try {
            FilePathFilter filePathFilter = buildFilePathFilter(input, directory);
            int maxDepth = input.getMaxDepth() == null || input.getMaxDepth() < 0 ? Integer.MAX_VALUE : input.getMaxDepth();
            Path finalDirectory = directory;
            files = Files.walk(directory, maxDepth)
                    .filter(path -> {
                        if (input.isDirectoryIncluded()) {
                            return true;
                        }
                        return Files.isRegularFile(path);
                    })
                    .filter(path -> filePathFilter.matchPath(finalDirectory, path) == FilePathFilter.MatchType.MATCHED)
                    .map(Path::toFile)
                    .toList();
        } catch (IOException e) {
            log.warn("Error when listing files for dir {}", directory, e);
            throw new ToolExecutionException(e);
        }

        StringBuilder displayBuilder = new StringBuilder("Found ")
                .append(files.size()).append(" file(s) in directory '").append(directory).append("'");
        if (CollectionUtils.isNotEmpty(input.getExcludePatterns())) {
            displayBuilder.append(" with exclude patterns (")
                    .append(StringUtils.join(input.getExcludePatterns(), ",")).append(")");
        }
        return new ListFileOutput(files, displayBuilder.toString());
    }

    protected FilePathFilter buildFilePathFilter(ListFileInput input, Path directory) throws IOException {
        List<Pattern> excludePatterns;
        if (CollectionUtils.isNotEmpty(input.getExcludePatterns())) {
            excludePatterns = input.getExcludePatterns().stream().map(GitIgnoreLikeFileFilter::compileAsPattern).toList();
        } else {
            excludePatterns = List.of();
        }

        return FilePathFilter.builder(directory)
                .excludePatterns(excludePatterns)
                .ignoreFileFilters(input.getIgnoreLikeFiles())
                .build();
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
