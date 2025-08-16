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

import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.metaagent.framework.core.tool.Tool;
import org.metaagent.framework.core.tool.ToolContext;
import org.metaagent.framework.core.tool.ToolExecutionException;
import org.metaagent.framework.core.tool.converter.ToolConverter;
import org.metaagent.framework.core.tool.converter.ToolConverters;
import org.metaagent.framework.core.tool.definition.ToolDefinition;
import org.metaagent.framework.core.util.abort.AbortException;
import org.metaagent.framework.tools.file.util.FilePathFilter;
import org.metaagent.framework.tools.file.util.FileUtils;
import org.metaagent.framework.tools.file.util.GitIgnoreLikeFileFilter;
import org.metaagent.framework.tools.file.util.GitUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

/**
 * Tool for finding files matching specific glob patterns.
 * This tool efficiently searches for files in a directory and its subdirectories,
 * returning absolute paths sorted by modification time (newest first).
 * It is particularly useful for quickly locating files based on their name or path structure,
 * especially in large codebases.
 *
 * @author vyckey
 */
@Slf4j
public class GlobFileTool implements Tool<GlobFileInput, GlobFileOutput> {
    private static final ToolDefinition TOOL_DEFINITION = ToolDefinition.builder("glob_files")
            .description("Efficiently finds files matching specific glob patterns (e.g., `src/**/*.ts`, `**/*.md`), " +
                    "returning absolute paths sorted by modification time (newest first). " +
                    "Ideal for quickly locating files based on their name or path structure, especially in large codebases.")
            .inputSchema(GlobFileInput.class)
            .outputSchema(GlobFileOutput.class)
            .isConcurrencySafe(true)
            .isReadOnly(true)
            .build();
    private static final ToolConverter<GlobFileInput, GlobFileOutput> TOOL_CONVERTER =
            ToolConverters.jsonConverter(GlobFileInput.class);

    @Override
    public ToolDefinition getDefinition() {
        return TOOL_DEFINITION;
    }

    @Override
    public ToolConverter<GlobFileInput, GlobFileOutput> getConverter() {
        return TOOL_CONVERTER;
    }

    protected void validateInput(GlobFileInput input) throws ToolExecutionException {
        if (input.getPattern() == null) {
            throw new ToolExecutionException("Glob pattern must be specified.");
        }
        if (StringUtils.isBlank(input.getDirectory()) && System.getenv("CWD") == null) {
            throw new ToolExecutionException("Current working directory is unknown. Please specify a path.");
        }
    }

    @Override
    public GlobFileOutput run(ToolContext toolContext, GlobFileInput input) throws ToolExecutionException {
        validateInput(input);

        String dir = StringUtils.isBlank(input.getDirectory()) ? System.getenv("CWD") : input.getDirectory();
        Path directory = Path.of(dir).toAbsolutePath();
        if (!directory.toFile().exists()) {
            throw new ToolExecutionException("Directory does not exist: " + directory);
        }

        if (toolContext.getAbortSignal().isAborted()) {
            throw new AbortException("Tool " + getName() + " is cancelled");
        }

        try {
            FilePathFilter filePathFilter = buildFilePathFilter(input, directory);
            List<File> files = Files.walk(directory)
                    .filter(Files::isRegularFile)
                    .filter(path -> filePathFilter.matchPath(directory, path) == FilePathFilter.MatchType.MATCHED)
                    .map(Path::toFile)
                    .sorted(new FileOrderComparator(TimeUnit.HOURS, 6))
                    .toList();
            String display = "Found " + files.size() + " file(s) matching pattern '" + input.getPattern() + "' in directory '" + directory + "'";
            return new GlobFileOutput(files, display);
        } catch (IOException e) {
            log.warn("Error while searching files in directory {}: {}", directory, e.getMessage());
            throw new ToolExecutionException("Error while searching files: " + e.getMessage(), e);
        }
    }

    protected FilePathFilter buildFilePathFilter(GlobFileInput input, Path directory) throws IOException {
        Pattern pattern = GitIgnoreLikeFileFilter.compileAsPattern(input.getPattern());
        if (BooleanUtils.isTrue(input.getCaseSensitive())) {
            pattern = Pattern.compile(pattern.toString(), Pattern.CASE_INSENSITIVE);
        }

        List<GitIgnoreLikeFileFilter> ignoreLikeFileFilters = Lists.newArrayList();
        Optional<Path> gitIgnorePath = GitUtils.findGitIgnorePath(directory);
        if (gitIgnorePath.isPresent()) {
            ignoreLikeFileFilters.add(new GitIgnoreLikeFileFilter(gitIgnorePath.get()));
        }
        for (Path path : FileUtils.resolvePaths(directory, input.getIgnoreLikeFiles(), true)) {
            ignoreLikeFileFilters.add(new GitIgnoreLikeFileFilter(path));
        }
        return FilePathFilter.builder().patterns(List.of(pattern))
                .ignoreLikeFileFilters(ignoreLikeFileFilters).build();
    }

    record FileOrderComparator(TimeUnit timeUnit, long timeThreshold) implements Comparator<File> {
        @Override
        public int compare(File f1, File f2) {
            long recencyThreshold = timeUnit.toMillis(timeThreshold);
            boolean isF1Recency = System.currentTimeMillis() - f1.lastModified() < recencyThreshold;
            boolean isF2Recency = System.currentTimeMillis() - f2.lastModified() < recencyThreshold;
            if (isF1Recency && isF2Recency) {
                return f2.lastModified() - f1.lastModified() > 0 ? -1 : 1;
            } else if (isF1Recency) {
                return -1;
            } else if (isF2Recency) {
                return 1;
            } else {
                return f1.getAbsolutePath().compareTo(f2.getAbsolutePath());
            }
        }
    }
}
