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

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.metaagent.framework.core.tool.Tool;
import org.metaagent.framework.core.tool.ToolContext;
import org.metaagent.framework.core.tool.ToolExecutionException;
import org.metaagent.framework.core.tool.ToolParameterException;
import org.metaagent.framework.core.tool.converter.ToolConverter;
import org.metaagent.framework.core.tool.converter.ToolConverters;
import org.metaagent.framework.core.tool.definition.ToolDefinition;
import org.metaagent.framework.core.tool.human.HumanApprover;
import org.metaagent.framework.core.tool.human.SystemAutoApprover;
import org.metaagent.framework.core.util.abort.AbortException;
import org.metaagent.framework.tools.file.util.FileUtils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;

/**
 * Write text file content tool.
 *
 * @author vyckey
 */
@Slf4j
@Setter
public class WriteTextFileTool implements Tool<WriteTextFileInput, WriteTextFileOutput> {
    private static final ToolDefinition TOOL_DEFINITION = ToolDefinition
            .builder("write_text_file")
            .description("Write text content to a specified file")
            .inputSchema(WriteTextFileInput.class)
            .outputSchema(WriteTextFileOutput.class)
            .isConcurrencySafe(false)
            .isReadOnly(false)
            .build();
    private static final ToolConverter<WriteTextFileInput, WriteTextFileOutput> TOOL_CONVERTER =
            ToolConverters.jsonConverter(WriteTextFileInput.class);
    private HumanApprover humanApprover = SystemAutoApprover.INSTANCE;

    @Override
    public ToolDefinition getDefinition() {
        return TOOL_DEFINITION;
    }

    @Override
    public ToolConverter<WriteTextFileInput, WriteTextFileOutput> getConverter() {
        return TOOL_CONVERTER;
    }

    @Override
    public WriteTextFileOutput run(ToolContext toolContext, WriteTextFileInput input) throws ToolExecutionException {
        if (toolContext.getAbortSignal().isAborted()) {
            throw new AbortException("Tool " + getName() + " is cancelled");
        }

        final String content = input.getContent() == null ? "" : input.getContent();
        Path filePath = FileUtils.resolvePath(toolContext.getWorkingDirectory(), Path.of(input.getFilePath()));

        requestApprovalBeforeWriteFile(filePath, content);
        if (toolContext.getAbortSignal().isAborted()) {
            throw new AbortException("Tool " + getName() + " is cancelled");
        }

        try {
            if (!filePath.isAbsolute()) {
                throw new IOException("File path is not absolute: " + input.getFilePath());
            }

            File file = createFileIfNotExists(filePath);
            try (FileWriter writer = new FileWriter(file, input.isAppend())) {
                writer.write(content);
            }
            return WriteTextFileOutput.builder().filePath(filePath.toString()).success(true).build();
        } catch (IOException e) {
            log.warn("Error writing text file {}. err: {}", input.getFilePath(), e.getMessage());
            return WriteTextFileOutput.builder().filePath(filePath.toString()).success(false).exception(e).build();
        }
    }

    private void requestApprovalBeforeWriteFile(Path filePath, String content) {
        String truncatedContent = content.length() > 100 ? content.substring(0, 100) + "..." : content;
        String approval = "Request to write file " + filePath + " with content:\n" + truncatedContent;
        HumanApprover.ApprovalInput approvalInput = new HumanApprover.ApprovalInput(approval, null);
        HumanApprover.ApprovalOutput approvalOutput = humanApprover.request(approvalInput);
        if (!approvalOutput.isApproved()) {
            throw new ToolExecutionException("User reject to write file: " + filePath);
        }
    }

    @NotNull
    private static File createFileIfNotExists(Path filePath) throws IOException {
        File file = new File(filePath.toString());
        if (file.exists()) {
            if (!file.isFile()) {
                throw new ToolParameterException("File is a directory");
            }
        } else {
            if (!file.getParentFile().mkdirs()) {
                throw new IOException("Failed to create directory: " + file.getParentFile().getAbsolutePath());
            }
            if (!file.createNewFile()) {
                throw new IOException("Failed to create file: " + filePath);
            }
        }
        return file;
    }

}
