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

package org.metaagent.framework.tools.file.image;

import lombok.extern.slf4j.Slf4j;
import org.metaagent.framework.common.abort.AbortException;
import org.metaagent.framework.core.tool.Tool;
import org.metaagent.framework.core.tool.ToolContext;
import org.metaagent.framework.core.tool.converter.ToolConverter;
import org.metaagent.framework.core.tool.converter.ToolConverters;
import org.metaagent.framework.core.tool.definition.ToolDefinition;
import org.metaagent.framework.core.tool.exception.ToolExecutionException;
import org.metaagent.framework.core.tool.exception.ToolParameterException;
import org.metaagent.framework.tools.file.util.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;

/**
 * Read image file content tool
 *
 * @author vyckey
 */
@Slf4j
public class ReadImageFileTool implements Tool<ReadImageFileInput, ReadImageFileOutput> {
    public static final String TOOL_NAME = "read_image_file";
    private static final ToolDefinition TOOL_DEFINITION = ToolDefinition.builder(TOOL_NAME)
            .description("Read image file content and return it as a Base64 encoded string. "
                    + "The file must be an image file (e.g., PNG, JPEG). ")
            .inputSchema(ReadImageFileInput.class)
            .outputSchema(ReadImageFileOutput.class)
            .isConcurrencySafe(true)
            .isReadOnly(true)
            .build();
    private static final ToolConverter<ReadImageFileInput, ReadImageFileOutput> TOOL_CONVERTER =
            ToolConverters.jsonConverter(ReadImageFileInput.class);

    @Override
    public ToolDefinition getDefinition() {
        return TOOL_DEFINITION;
    }

    @Override
    public ToolConverter<ReadImageFileInput, ReadImageFileOutput> getConverter() {
        return TOOL_CONVERTER;
    }

    protected File resolveFile(Path workingDirectory, ReadImageFileInput input) {
        Path filePath = FileUtils.resolvePath(workingDirectory, Path.of(input.filePath()));
        File file = filePath.toFile();
        if (!file.exists()) {
            throw new ToolParameterException("File not found: " + filePath);
        }
        if (!file.isFile()) {
            throw new ToolParameterException("File is a directory: " + file);
        }
        return file;
    }

    @Override
    public ReadImageFileOutput run(ToolContext toolContext, ReadImageFileInput input) throws ToolExecutionException {
        if (toolContext.getAbortSignal().isAborted()) {
            throw new AbortException("Tool " + getName() + " is cancelled");
        }

        File file = resolveFile(toolContext.getToolConfig().workingDirectory(), input);
        try {
            return readFile(file);
        } catch (IOException e) {
            log.warn("Error reading image file {}. err: {}", input.filePath(), e.getMessage());
            return ReadImageFileOutput.builder().exception(e).build();
        }
    }

    private ReadImageFileOutput readFile(File file) throws IOException {
        String base64 = Base64.getEncoder().encodeToString(Files.readAllBytes(file.toPath()));
        return ReadImageFileOutput.builder().fileSize(file.length()).base64(base64).build();
    }

}