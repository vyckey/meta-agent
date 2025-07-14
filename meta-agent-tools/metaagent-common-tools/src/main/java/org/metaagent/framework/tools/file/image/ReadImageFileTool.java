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
import java.util.Base64;

/**
 * Read image file content tool
 *
 * @author vyckey
 */
@Slf4j
public class ReadImageFileTool implements Tool<ReadImageFileInput, ReadImageFileOutput> {
    private final ToolDefinition toolDefinition = ToolDefinition.builder("read_image_file")
            .description("Read image file content tool")
            .inputSchema(ReadImageFileInput.class)
            .outputSchema(ReadImageFileOutput.class)
            .build();
    private final ToolConverter<ReadImageFileInput, ReadImageFileOutput> toolConverter =
            JsonToolConverter.create(ReadImageFileInput.class);

    @Override
    public ToolDefinition getDefinition() {
        return toolDefinition;
    }

    @Override
    public ToolConverter<ReadImageFileInput, ReadImageFileOutput> getConverter() {
        return toolConverter;
    }

    @Override
    public ReadImageFileOutput run(ToolContext toolContext, ReadImageFileInput input) throws ToolExecutionException {
        try {
            return readFile(input);
        } catch (IOException e) {
            log.warn("Error reading image file {}. err: {}", input.getFilePath(), e.getMessage());
            return ReadImageFileOutput.builder().exception(e).build();
        }
    }

    private ReadImageFileOutput readFile(ReadImageFileInput input) throws IOException {
        File file = new File(input.getFilePath());
        if (!file.exists()) {
            throw new FileNotFoundException("File not found: " + input.getFilePath());
        }
        if (!file.isFile()) {
            throw new IOException("File is a directory: " + input.getFilePath());
        }

        String base64 = Base64.getEncoder().encodeToString(Files.readAllBytes(file.toPath()));
        return ReadImageFileOutput.builder().fileSize(file.length()).base64(base64).build();
    }

}