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
import org.metaagent.framework.core.common.metadata.MetadataProvider;
import org.metaagent.framework.core.common.security.SecurityLevel;
import org.metaagent.framework.core.common.security.approver.HumanApprovalInput;
import org.metaagent.framework.core.common.security.approver.HumanApprover;
import org.metaagent.framework.core.common.security.approver.TerminalHumanApprover;
import org.metaagent.framework.core.tool.Tool;
import org.metaagent.framework.core.tool.ToolContext;
import org.metaagent.framework.core.tool.converter.ToolConverter;
import org.metaagent.framework.core.tool.converter.ToolConverters;
import org.metaagent.framework.core.tool.definition.ToolDefinition;
import org.metaagent.framework.core.tool.exception.ToolExecutionException;
import org.metaagent.framework.core.tool.exception.ToolParameterException;
import org.metaagent.framework.core.util.abort.AbortException;
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
public class EditTextFileTool implements Tool<EditTextFileInput, EditTextFileOutput> {
    private static final ToolDefinition TOOL_DEFINITION = ToolDefinition.builder("edit_text_file")
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
    protected HumanApprover humanApprover = TerminalHumanApprover.INSTANCE;

    @Override
    public ToolDefinition getDefinition() {
        return TOOL_DEFINITION;
    }

    @Override
    public ToolConverter<EditTextFileInput, EditTextFileOutput> getConverter() {
        return TOOL_CONVERTER;
    }

    public void setHumanApprover(HumanApprover humanApprover) {
        this.humanApprover = humanApprover;
    }

    private File validateInput(ToolContext toolContext, EditTextFileInput input) {
        if (StringUtils.isBlank(input.filePath())) {
            throw new ToolParameterException("filePath is required");
        }

        Path workingDirectory = toolContext.getToolConfig().workingDirectory();
        Path path = FileUtils.resolvePath(workingDirectory, Path.of(input.filePath()));
        if (toolContext.getSecurityLevel().compareTo(SecurityLevel.UNRESTRICTED_DANGEROUSLY) < 0
                && !path.startsWith(workingDirectory)) {
            throw new ToolParameterException("filePath must be within the working directory: " + workingDirectory);
        }
        if (!path.isAbsolute()) {
            throw new ToolParameterException("filePath must be absolute");
        }
        File file = path.toFile();
        if (file.isDirectory()) {
            throw new ToolParameterException("filePath cannot be a directory");
        }
        return file;
    }

    @Override
    public EditTextFileOutput run(ToolContext toolContext, EditTextFileInput input) throws ToolExecutionException {
        File file = validateInput(toolContext, input);
        if (toolContext.getAbortSignal().isAborted()) {
            throw new AbortException("Tool " + getName() + " is cancelled");
        }

        FileContentReplacement contentReplacement = buildReplacement(file, input);
        if (toolContext.getSecurityLevel().compareTo(SecurityLevel.UNRESTRICTED_DANGEROUSLY) < 0) {
            EditTextFileOutput editOutput = applyReplacement(contentReplacement, false);
            confirmReplacement(contentReplacement, editOutput.editDiff());
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

    protected void confirmReplacement(FileContentReplacement replacement, TextFileEditDiff editDiff) throws ToolExecutionException {
        StringBuilder sb = new StringBuilder("Are you sure you want to replace below content for file '")
                .append(replacement.filePath).append("'?");
        sb.append("\nOld String: ").append(replacement.oldString());
        sb.append("\n\nNew String: ").append(replacement.newString());

        MetadataProvider metadata = MetadataProvider.create();
        metadata.setProperty("replacement", replacement);
        metadata.setProperty("editDiff", editDiff);
        HumanApprovalInput approvalInput = HumanApprovalInput.ofTool(getName(), sb.toString(), metadata);
        if (!humanApprover.request(approvalInput).isApproved()) {
            throw new ToolExecutionException("Replacement not approved by user");
        }
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
