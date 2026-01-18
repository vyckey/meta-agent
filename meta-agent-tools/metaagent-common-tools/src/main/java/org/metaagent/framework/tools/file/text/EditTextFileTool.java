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

/**
 * Tool for editing text files.
 * This tool allows you to replace content in a text file or create a new file with specified content.
 *
 * @author vyckey
 */
public class EditTextFileTool extends AbstractFileTool<EditTextFileInput, EditTextFileOutput>
        implements Tool<EditTextFileInput, EditTextFileOutput> {
    public static final String TOOL_NAME = "edit_text_file";
    private static final ToolDefinition TOOL_DEFINITION = ToolDefinition.builder(TOOL_NAME)
            .description("Replaces text within a file. By default, replaces a single occurrence, " +
                    "but can replace multiple occurrences when `expectedReplacements` is specified. " +
                    "This tool requires providing significant context around the change to ensure precise targeting. " +
                    "Always use the read_text_file tool to examine the file's current content before attempting a text replacement.")
            .inputSchema(EditTextFileInput.class)
            .outputSchema(EditTextFileOutput.class)
            .isConcurrencySafe(false)
            .isReadOnly(false)
            .build();
    private static final ToolConverter<EditTextFileInput, EditTextFileOutput> TOOL_CONVERTER =
            ToolConverters.jsonConverter(EditTextFileInput.class);

    @Override
    public ToolDefinition getDefinition() {
        return TOOL_DEFINITION;
    }

    @Override
    public ToolConverter<EditTextFileInput, EditTextFileOutput> getConverter() {
        return TOOL_CONVERTER;
    }

    private Path validateInput(ToolContext toolContext, EditTextFileInput input) {
        ToolArgsValidator.validate(input);
        Path filePath = FileUtils.resolvePath(toolContext.getWorkingDirectory(), Path.of(input.filePath()));
        if (filePath.toFile().isDirectory()) {
            throw new ToolArgumentException("filePath cannot be a directory");
        }
        return filePath;
    }

    @Override
    public EditTextFileOutput run(ToolContext toolContext, EditTextFileInput input) throws ToolExecutionException {
        Path filePath = validateInput(toolContext, input);
        File file = filePath.toFile();
        if (toolContext.getAbortSignal().isAborted()) {
            throw new AbortException("Tool " + getName() + " is cancelled");
        }

        FileContentReplacement contentReplacement = buildReplacement(file, input);
        if (!checkFileAccessible(toolContext, filePath)) {
            ToolApprovalRequest approvalRequest = ToolApprovalRequest.builder()
                    .id(toolContext.getExecutionId())
                    .toolName(getName())
                    .approvalContent("Request edit file: " + filePath)
                    .input(input)
                    .metadata("isNewFile", contentReplacement.isNewFile())
                    .metadata("fileContent", contentReplacement.fileContent())
                    .metadata("oldString", contentReplacement.oldString())
                    .metadata("newString", contentReplacement.newString())
                    .build();
            PermissionApproval approvalResult = toolContext.requestApproval(approvalRequest);
            if (approvalResult.getApprovalStatus() == ApprovalStatus.REJECTED) {
                throw new ToolRejectException(getName(), "user rejected to edit file '" + filePath + "'");
            }
        }

        if (toolContext.getAbortSignal().isAborted()) {
            throw new AbortException("Tool " + getName() + " is cancelled");
        }
        return applyReplacement(contentReplacement, true);
    }

    protected FileContentReplacement buildReplacement(File file, EditTextFileInput input) {
        int expectedReplacements = input.expectedReplacements() != null ? input.expectedReplacements() : 1;
        if (!file.exists()) {
            if (StringUtils.isNotEmpty(input.oldString())) {
                throw new ToolExecutionException("File does not exist, the `oldString` parameter must be empty");
            }
            return new FileContentReplacement(file.toPath(), null, input.oldString(), input.newString(), expectedReplacements, true);
        }
        String currentContent;
        try {
            currentContent = Files.readString(file.toPath());
            currentContent = currentContent.replaceAll("\r\n", "\n");
        } catch (IOException e) {
            throw new ToolExecutionException("Error reading file '" + file.getAbsolutePath() + "' caused by " + e, e);
        }
        return new FileContentReplacement(file.toPath(), currentContent, input.newString(), input.oldString(), expectedReplacements, false);
    }

    protected EditTextFileOutput applyReplacement(FileContentReplacement replacement, boolean editEnabled) throws ToolExecutionException {
        if (replacement.oldString().equals(replacement.newString())) {
            throw new ToolExecutionException("No change to apply because old string is same as the new string");
        }

        Path filePath = replacement.filePath.toAbsolutePath();
        TextFileEditDiff editDiff;
        String description;
        if (replacement.isNewFile()) {
            if (replacement.expectedReplacements > 1) {
                throw new ToolExecutionException("Expected replacements must be 1 for a new file");
            }

            if (editEnabled) {
                try {
                    Files.writeString(replacement.filePath, replacement.newString());
                } catch (IOException e) {
                    throw new ToolExecutionException("Error writing to new file '" + filePath + "' caused by " + e, e);
                }
            }
            editDiff = TextFileEditDiff.from(replacement.filePath, "", replacement.newString());
            description = "Created new file '" + filePath + "' with given content. " + editDiff;
        } else {
            int occurrences = replacement.fileContent().split(replacement.oldString(), -1).length - 1;
            if (occurrences != replacement.expectedReplacements) {
                throw new ToolExecutionException("Expected " + replacement.expectedReplacements +
                        " replacements, but found " + occurrences + " in file " + filePath);
            }

            String updatedContent = replacement.fileContent().replace(replacement.oldString(), replacement.newString());
            if (editEnabled) {
                try {
                    Files.writeString(replacement.filePath, updatedContent);
                } catch (IOException e) {
                    throw new ToolExecutionException("Error writing to existing file '" + filePath + "' caused by " + e, e);
                }
            }
            editDiff = TextFileEditDiff.from(replacement.filePath, replacement.fileContent, updatedContent);
            description = "Updated file '" + filePath + "' with given content (" +
                    replacement.expectedReplacements + " replacements). " + editDiff;
        }
        return new EditTextFileOutput(filePath, editDiff, description);
    }

    protected record FileContentReplacement(
            Path filePath,
            String fileContent,
            String oldString,
            String newString,
            int expectedReplacements,
            boolean isNewFile) {
    }
}
