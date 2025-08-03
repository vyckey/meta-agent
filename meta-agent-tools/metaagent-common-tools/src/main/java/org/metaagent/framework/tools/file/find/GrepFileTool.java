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
import com.google.common.io.CharStreams;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.metaagent.framework.core.tool.Tool;
import org.metaagent.framework.core.tool.ToolContext;
import org.metaagent.framework.core.tool.ToolExecutionException;
import org.metaagent.framework.core.tool.converter.ToolConverter;
import org.metaagent.framework.core.tool.converter.ToolConverters;
import org.metaagent.framework.core.tool.definition.ToolDefinition;
import org.metaagent.framework.tools.file.util.FileUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.regex.Pattern;

/**
 * GrepFileTool is a tool for searching files in a directory based on a specified pattern.
 *
 * @author vyckey
 */
@Slf4j
public class GrepFileTool implements Tool<GrepFileInput, GrepFileOutput> {
    private static final ToolDefinition TOOL_DEFINITION = ToolDefinition.builder("grep_files")
            .description("Searches for a regular expression pattern within the content of files in a specified directory" +
                    " (default current working directory). Can filter files by a glob pattern. Returns the lines " +
                    "containing matches, along with their file paths and line numbers.")
            .inputSchema(GrepFileInput.class)
            .outputSchema(GrepFileOutput.class)
            .build();
    private static final ToolConverter<GrepFileInput, GrepFileOutput> TOOL_CONVERTER =
            ToolConverters.jsonConverter(GrepFileInput.class);

    @Override
    public ToolDefinition getDefinition() {
        return TOOL_DEFINITION;
    }

    @Override
    public ToolConverter<GrepFileInput, GrepFileOutput> getConverter() {
        return TOOL_CONVERTER;
    }

    protected void validateInput(GrepFileInput input) throws ToolExecutionException {
        if (input.getPattern() == null) {
            throw new ToolExecutionException("Grep pattern must be specified.");
        }
        if (StringUtils.isBlank(input.getDirectory()) && System.getenv("CWD") == null) {
            throw new ToolExecutionException("Current working directory is unknown. Please specify a path.");
        }
    }

    @Override
    public GrepFileOutput run(ToolContext toolContext, GrepFileInput input) throws ToolExecutionException {
        validateInput(input);

        String dir = StringUtils.isBlank(input.getDirectory()) ? System.getenv("CWD") : input.getDirectory();
        Path directory = Path.of(dir).toAbsolutePath();
        if (!directory.toFile().exists()) {
            throw new ToolExecutionException("Directory does not exist: " + directory);
        }

        List<GrepMatchLine> matchLines;
        try {
            if (FileUtils.hasCommand("git", "--version")) {
                matchLines = searchByGitGrep(directory, input.getPattern(), input.getInclude());
            } else if (FileUtils.hasCommand("grep", "-h")) {
                matchLines = searchByGrep(directory, input.getPattern(), input.getInclude());
            } else {
                matchLines = searchByRead(directory, input.getPattern(), input.getInclude());
            }
        } catch (Exception e) {
            log.warn("Failed to execute grep command", e);
            throw new ToolExecutionException(e.getMessage(), e);
        }
        return buildGrepFileOutput(input, matchLines);
    }

    protected List<GrepMatchLine> searchByGitGrep(Path directory, Pattern pattern, String include) throws IOException {
        List<String> command = Lists.newArrayList("git", "grep", "--untracked", "-n", "-E", "--ignore-case", pattern.toString());
        if (StringUtils.isNotBlank(include)) {
            command.add("--");
            command.add(include);
        }
        Process process = Runtime.getRuntime().exec(command.toArray(new String[0]), null, directory.toFile());
        String error = CharStreams.toString(new InputStreamReader(process.getErrorStream(), StandardCharsets.UTF_8));
        if (StringUtils.isNotEmpty(error)) {
            throw new ToolExecutionException(error);
        }

        String output = CharStreams.toString(new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8));
        return parseSearchOutput(directory, output);
    }

    protected List<GrepMatchLine> searchByGrep(Path directory, Pattern pattern, String include) throws IOException {
        final String[] ignoreDirectories = {
                ".git", "node_modules", "build", "dist"
        };
        List<String> command = Lists.newArrayList("grep", "-r", "-n", "-H", "-E", pattern.toString());
        for (String ignoreDirectory : ignoreDirectories) {
            command.add("--exclude-dir=" + ignoreDirectory);
        }
        if (StringUtils.isNotEmpty(include)) {
            command.add("--include=" + include);
        }
        command.add(pattern.toString());
        command.add(directory.toString());
        Process process = Runtime.getRuntime().exec(command.toArray(new String[0]), null, directory.toFile());
        String error = CharStreams.toString(new InputStreamReader(process.getErrorStream(), StandardCharsets.UTF_8));
        if (StringUtils.isNotEmpty(error)) {
            throw new ToolExecutionException(error);
        }

        String output = CharStreams.toString(new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8));
        return parseSearchOutput(directory, output);
    }

    protected List<GrepMatchLine> parseSearchOutput(Path directory, String output) {
        List<GrepMatchLine> matchLines = Lists.newArrayList();
        String[] lines = output.split("\n");
        for (String line : lines) {
            if (StringUtils.isBlank(line)) {
                continue;
            }
            String[] parts = line.split(":", 3);
            if (parts.length < 3) {
                continue;
            }
            Path filePath = directory.relativize(Path.of(parts[0].trim()));
            int lineNumber = Integer.parseInt(parts[1].trim());
            String content = parts[2];
            matchLines.add(new GrepMatchLine(filePath.toString(), lineNumber, content));
        }
        return matchLines;
    }

    protected List<GrepMatchLine> searchByRead(Path directory, Pattern pattern, String include) throws IOException {
        GlobFileInput globFileInput = GlobFileInput.builder()
                .directory(directory.toString())
                .pattern(StringUtils.isEmpty(include) ? "**/*" : include)
                .build();
        GlobFileOutput globFileOutput = new GlobFileTool().run(ToolContext.create(), globFileInput);

        List<GrepMatchLine> matchLines = Lists.newArrayList();
        for (File file : globFileOutput.getFiles()) {
            List<String> lines;
            try {
                lines = Files.readAllLines(file.toPath(), StandardCharsets.UTF_8);
            } catch (IOException e) {
                continue;
            }

            for (int i = 0; i < lines.size(); i++) {
                String line = lines.get(i);
                if (pattern.matcher(line).find()) {
                    String path = directory.relativize(file.toPath()).toString();
                    GrepMatchLine matchLine = new GrepMatchLine(path, i + 1, line);
                    matchLines.add(matchLine);
                }
            }
        }
        return matchLines;
    }

    protected GrepFileOutput buildGrepFileOutput(GrepFileInput input, List<GrepMatchLine> matchLines) {
        long matchFileCount = matchLines.stream().map(GrepMatchLine::filePath).distinct().count();
        StringBuilder displayBuilder = new StringBuilder("Found " + matchLines.size() + " matched line(s) in ")
                .append(matchFileCount).append(" file(s)");
        if (StringUtils.isNotBlank(input.getInclude())) {
            displayBuilder.append(" (").append(input.getInclude()).append(")");
        }
        displayBuilder.append(" with pattern '").append(input.getPattern().pattern()).append("'");
        matchLines = matchLines.stream().map(l ->
                new GrepMatchLine(l.filePath(), l.lineNumber(), l.lineContent().trim())
        ).toList();
        return new GrepFileOutput(matchLines, displayBuilder.toString());
    }
}
