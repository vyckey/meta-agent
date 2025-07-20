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

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.metaagent.framework.core.tool.Tool;
import org.metaagent.framework.core.tool.ToolContext;
import org.metaagent.framework.core.tool.ToolExecutionException;
import org.metaagent.framework.core.tool.converter.JsonToolConverter;
import org.metaagent.framework.core.tool.converter.ToolConverter;
import org.metaagent.framework.core.tool.definition.ToolDefinition;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Read text file content tool.
 *
 * @author vyckey
 */
@Slf4j
public class ReadTextFileTool implements Tool<ReadTextFileInput, ReadTextFileOutput> {
    private static final ToolDefinition TOOL_DEFINITION = ToolDefinition.builder("read_text_file")
            .description("Read text file content")
            .inputSchema(ReadTextFileInput.class)
            .outputSchema(ReadTextFileOutput.class)
            .build();
    private static final ToolConverter<ReadTextFileInput, ReadTextFileOutput> TOOL_CONVERTER =
            JsonToolConverter.create(ReadTextFileInput.class);

    @Override
    public ToolDefinition getDefinition() {
        return TOOL_DEFINITION;
    }

    @Override
    public ToolConverter<ReadTextFileInput, ReadTextFileOutput> getConverter() {
        return TOOL_CONVERTER;
    }

    @Override
    public ReadTextFileOutput run(ToolContext toolContext, ReadTextFileInput input) throws ToolExecutionException {
        try {
            return readFile(input);
        } catch (IOException e) {
            log.warn("Error reading text file {}. err: {}", input.getFilePath(), e.getMessage());
            return ReadTextFileOutput.builder().exception(e).build();
        }
    }

    private ReadTextFileOutput readFile(ReadTextFileInput input) throws IOException {
        File file = new File(input.getFilePath());
        if (!file.exists()) {
            throw new FileNotFoundException("File not found: " + input.getFilePath());
        }
        if (!file.isFile()) {
            throw new IOException("File is a directory: " + input.getFilePath());
        }

        String content = readFileContent(file, input);
        return ReadTextFileOutput.builder().fileSize(file.length()).content(content).build();
    }

    static String readFileContent(File file, ReadTextFileInput input) throws IOException {
        String lineLength = System.getenv("MAX_TEXT_FILE_LINE_LENGTH");
        final int maxLineLength = StringUtils.isNotEmpty(lineLength) ? Integer.parseInt(lineLength) : 2000;
        List<String> lines = Files.readAllLines(file.toPath());
        Stream<String> lineStream = lines.stream().skip(input.getOffset());
        if (input.getLimit() >= 0) {
            lineStream = lineStream.limit(input.getLimit());
        }
        if (input.isTruncate()) {
            lineStream = lineStream.map(line ->
                    line.length() > maxLineLength ? line.substring(0, maxLineLength) + "... [truncated]" : line);
        }
        return lineStream.collect(Collectors.joining("\n"));
    }

}