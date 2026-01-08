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

import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.metaagent.framework.common.abort.AbortException;
import org.metaagent.framework.common.ignorefile.GitIgnoreLikeFileFilter;
import org.metaagent.framework.core.tool.Tool;
import org.metaagent.framework.core.tool.ToolContext;
import org.metaagent.framework.core.tool.converter.ToolConverter;
import org.metaagent.framework.core.tool.converter.ToolConverters;
import org.metaagent.framework.core.tool.definition.ToolDefinition;
import org.metaagent.framework.core.tool.exception.ToolArgumentException;
import org.metaagent.framework.core.tool.exception.ToolExecutionException;
import org.metaagent.framework.core.tool.schema.ToolArgsValidator;
import org.metaagent.framework.tools.file.util.FilePathFilter;
import org.metaagent.framework.tools.file.util.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.regex.Pattern;

@Slf4j
public class ReadManyFilesTool implements Tool<ReadManyFilesInput, ReadManyFilesOutput> {
    public static final String TOOL_NAME = "read_many_files";
    private static final ToolDefinition TOOL_DEFINITION = ToolDefinition.builder(TOOL_NAME)
            .description("Reads content from multiple files specified by paths or glob patterns within a specialized directory." +
                    " For text files, it concatenates their content into a single string. It is primarily designed for text-based files.")
            .inputSchema(ReadManyFilesInput.class)
            .outputSchema(ReadManyFilesOutput.class)
            .isConcurrencySafe(true)
            .isReadOnly(true)
            .build();
    private static final ToolConverter<ReadManyFilesInput, ReadManyFilesOutput> TOOL_CONVERTER =
            ToolConverters.jsonConverter(ReadManyFilesInput.class);
    private static final int MAX_DISPLAY_FILES = 10;
    private static final int MAX_DISPLAY_SKIP_FILES = 5;

    @Override
    public ToolDefinition getDefinition() {
        return TOOL_DEFINITION;
    }

    @Override
    public ToolConverter<ReadManyFilesInput, ReadManyFilesOutput> getConverter() {
        return TOOL_CONVERTER;
    }

    private Path validateDirectory(Path workingDirectory, String dir) {
        Path directory = workingDirectory;
        if (StringUtils.isNotEmpty(dir)) {
            directory = FileUtils.resolvePath(workingDirectory, Path.of(dir));
        }

        if (!Files.exists(directory)) {
            throw new ToolArgumentException("Directory " + directory + " does not exist");
        }
        return directory;
    }

    @Override
    public ReadManyFilesOutput run(ToolContext toolContext, ReadManyFilesInput input) throws ToolExecutionException {
        ToolArgsValidator.validate(input);
        Path directory = validateDirectory(toolContext.workingDirectory(), input.getDirectory());
        FilteredFiles filteredFiles = filterFiles(input, directory);

        if (toolContext.getAbortSignal().isAborted()) {
            throw new AbortException("Tool " + getName() + " is cancelled");
        }
        return readFiles(directory, filteredFiles, input);
    }

    protected FilteredFiles filterFiles(ReadManyFilesInput input, Path directory) {
        try {
            FilePathFilter filePathFilter = buildFilePathFilter(input, directory);
            int maxDepth = BooleanUtils.isFalse(input.getRecursive()) ? 1 : Integer.MAX_VALUE;
            List<File> ignoredFiles = Lists.newArrayList();
            List<File> foundFiles = Files.walk(directory, maxDepth)
                    .filter(Files::isRegularFile)
                    .map(Path::toFile)
                    .filter(file -> {
                        FilePathFilter.MatchType matchType = filePathFilter.matchPath(directory, file.toPath());
                        if (matchType == FilePathFilter.MatchType.MATCHED) {
                            return true;
                        } else if (matchType == FilePathFilter.MatchType.IGNORED) {
                            System.out.println("Ignored file: " + file);
                            ignoredFiles.add(file);
                        }
                        return false;
                    })
                    .toList();
            return new FilteredFiles(foundFiles, ignoredFiles);
        } catch (IOException e) {
            log.warn("Error when listing files for dir {}", directory, e);
            throw new ToolExecutionException("Failed to find files caused by " + e, e);
        }
    }

    protected FilePathFilter buildFilePathFilter(ReadManyFilesInput input, Path directory) throws IOException {
        List<Pattern> patterns = input.getPathPatterns().stream().map(GitIgnoreLikeFileFilter::compileAsPattern).toList();

        List<Pattern> excludePatterns = List.of();
        if (CollectionUtils.isNotEmpty(input.getExcludePatterns())) {
            excludePatterns = input.getExcludePatterns().stream().map(GitIgnoreLikeFileFilter::compileAsPattern).toList();
        }

        return FilePathFilter.builder(directory)
                .patterns(patterns)
                .excludePatterns(excludePatterns)
                .ignoreFileFilters(input.getIgnoreLikeFiles())
                .build();
    }

    public record FilteredFiles(List<File> foundFiles, List<File> ignoredFiles) {
    }

    protected ReadManyFilesOutput readFiles(Path directory, FilteredFiles filteredFiles, ReadManyFilesInput input) {
        List<ReadManyFilesOutput.ReadFileContent> fileContents = Lists.newArrayList();
        List<ReadManyFilesOutput.SkipFileReason> skipFileReasons = Lists.newArrayList();
        for (File file : filteredFiles.foundFiles) {
            try {
                String content = readFileContent(file);
                fileContents.add(new ReadManyFilesOutput.ReadFileContent(file.toPath(), content));
            } catch (IOException e) {
                log.warn("Failed to read file {}: {}", file.getAbsolutePath(), e.getMessage());
                skipFileReasons.add(new ReadManyFilesOutput.SkipFileReason(file.toPath(), e.getMessage()));
            }
        }

        StringBuilder displayBuilder = new StringBuilder("Read result in directory '" + directory + "':\n");
        if (fileContents.isEmpty()) {
            displayBuilder.append("No files were read based on the provided filters.\n");
        } else if (fileContents.size() <= MAX_DISPLAY_FILES) {
            displayBuilder.append("Proceed files:\n");
            for (ReadManyFilesOutput.ReadFileContent fileContent : fileContents) {
                Path filePath = directory.relativize(fileContent.filePath());
                displayBuilder.append("- ").append(filePath).append("\n");
            }
        } else {
            displayBuilder.append("Proceed files (first ").append(MAX_DISPLAY_FILES).append(" shown):\n");
            for (ReadManyFilesOutput.ReadFileContent fileContent : fileContents.subList(0, MAX_DISPLAY_FILES)) {
                Path filePath = directory.relativize(fileContent.filePath());
                displayBuilder.append("- ").append(filePath).append("\n");
            }
            displayBuilder.append("- ... and ").append(fileContents.size() - MAX_DISPLAY_FILES).append(" more.\n");
        }

        int ignoredFileCount = filteredFiles.ignoredFiles.size();
        if (skipFileReasons.size() + ignoredFileCount > 0) {
            displayBuilder.append("\nSkipped ").append(ignoredFileCount).append(" ignored file(s)");
            if (skipFileReasons.isEmpty()) {
                displayBuilder.append("\n");
            } else {
                displayBuilder.append(" and ").append(skipFileReasons.size()).append(" file(s) with errors:\n");
            }

            List<ReadManyFilesOutput.SkipFileReason> shownSkipFileReasons = skipFileReasons;
            if (skipFileReasons.size() > MAX_DISPLAY_SKIP_FILES) {
                shownSkipFileReasons = skipFileReasons.subList(0, MAX_DISPLAY_SKIP_FILES);
            }
            for (ReadManyFilesOutput.SkipFileReason skipReason : shownSkipFileReasons) {
                Path filePath = directory.relativize(skipReason.filePath());
                displayBuilder.append("- ").append(filePath).append(" (").append(skipReason.reason()).append(")\n");
            }
            if (skipFileReasons.size() > MAX_DISPLAY_SKIP_FILES) {
                displayBuilder.append("- ... and ").append(skipFileReasons.size() - MAX_DISPLAY_SKIP_FILES)
                        .append(" more skipped files.\n");
            }
        }

        return ReadManyFilesOutput.builder()
                .directory(directory)
                .fileContents(fileContents)
                .skippedFiles(skipFileReasons)
                .display(displayBuilder.toString())
                .build();
    }

    protected String readFileContent(File file) throws IOException {
        if (!file.canRead()) {
            throw new IOException("File is not readable");
        }
        try {
            return Files.readString(file.toPath());
        } catch (IOException e) {
            throw new IOException("Read file error: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        ReadManyFilesInput input = ReadManyFilesInput.builder()
                .directory(".")
                .pathPatterns(List.of("**/resources/"))
                .recursive(true)
                .build();

        ReadManyFilesTool tool = new ReadManyFilesTool();
        ReadManyFilesOutput output = tool.run(ToolContext.create(), input);
        System.out.println(output.display());
    }
}
