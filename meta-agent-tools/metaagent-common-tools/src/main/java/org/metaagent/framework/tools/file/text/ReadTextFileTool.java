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
import org.metaagent.framework.common.abort.AbortException;
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
import org.metaagent.framework.tools.file.util.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Read text file content tool.
 *
 * @author vyckey
 */
@Slf4j
public class ReadTextFileTool extends AbstractFileTool<ReadTextFileInput, ReadTextFileOutput>
        implements Tool<ReadTextFileInput, ReadTextFileOutput> {
    public static final String TOOL_NAME = "read_text_file";
    private static final ToolDefinition TOOL_DEFINITION = ToolDefinition.builder(TOOL_NAME)
            .description("Read and return text file content. Can optionally specialize the start number and limit of lines to read.")
            .inputSchema(ReadTextFileInput.class)
            .outputSchema(ReadTextFileOutput.class)
            .isConcurrencySafe(true)
            .isReadOnly(true)
            .build();
    private static final ToolConverter<ReadTextFileInput, ReadTextFileOutput> TOOL_CONVERTER =
            ToolConverters.jsonConverter(ReadTextFileInput.class);

    @Override
    public ToolDefinition getDefinition() {
        return TOOL_DEFINITION;
    }

    @Override
    public ToolConverter<ReadTextFileInput, ReadTextFileOutput> getConverter() {
        return TOOL_CONVERTER;
    }

    private File validateInput(ToolContext toolContext, ReadTextFileInput input) {
        ToolArgsValidator.validate(input);

        Path filePath = FileUtils.resolvePath(toolContext.workingDirectory(), Path.of(input.getFilePath()));
        File file = filePath.toFile();
        if (file.isDirectory()) {
            throw new ToolArgumentException("filePath cannot be a directory");
        }
        if (!file.exists()) {
            throw new ToolArgumentException("File not found: " + input.getFilePath());
        }

        if (!checkFileAccessible(toolContext, filePath)) {
            ToolApprovalRequest approvalRequest = ToolApprovalRequest.builder()
                    .id(toolContext.getExecutionId())
                    .toolName(getName())
                    .approvalContent("Request read file: " + filePath)
                    .input(input)
                    .build();
            PermissionApproval approvalResult = toolContext.requestApproval(approvalRequest);
            if (approvalResult.getApprovalStatus() == ApprovalStatus.REJECTED) {
                throw new ToolRejectException(getName(), "user rejected to read file '" + filePath + "'");
            }
        }
        return file;
    }

    @Override
    public ReadTextFileOutput run(ToolContext toolContext, ReadTextFileInput input) throws ToolExecutionException {
        File file = validateInput(toolContext, input);
        if (toolContext.getAbortSignal().isAborted()) {
            throw new AbortException("Tool " + getName() + " is cancelled");
        }

        try {
            String content = readFileContent(file, input);
            return ReadTextFileOutput.builder().filePath(file.getCanonicalPath())
                    .fileSize(file.length()).content(content).build();
        } catch (IOException e) {
            log.warn("Error reading text file {}. err: {}", input.getFilePath(), e.getMessage());
            return ReadTextFileOutput.builder().exception(e).build();
        }
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