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

package org.metaagent.framework.tools.file;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.metaagent.framework.core.tool.Tool;
import org.metaagent.framework.core.tool.ToolContext;
import org.metaagent.framework.core.tool.ToolExecutionException;
import org.metaagent.framework.core.tool.converter.JsonToolConverter;
import org.metaagent.framework.core.tool.converter.ToolConverter;
import org.metaagent.framework.core.tool.definition.ToolDefinition;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
public class ReadTextFileTool implements Tool<ReadTextFileInput, ReadTextFileOutput> {
    @Override
    public ToolDefinition getDefinition() {
        return ToolDefinition.builder("read_text_file")
                .description("Read text file content tool")
                .inputSchema(ReadTextFileInput.class)
                .outputSchema(ReadTextFileOutput.class)
                .build();
    }

    @Override
    public ToolConverter<ReadTextFileInput, ReadTextFileOutput> getConverter() {
        return JsonToolConverter.create(ReadTextFileInput.class);
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

        FileExtType fileExtType = resolveFileType(file);
        String content = switch (fileExtType) {
            case TEXT -> readTextFile(file, input);
            case IMAGE -> readImageFile(file);
            default -> throw new IOException("Cannot read binary file as text");
        };
        return ReadTextFileOutput.builder().fileSize(file.length()).content(content).build();
    }

    private boolean isPrintableTextFile(File file) {
        final int MAX_READ_BYTES = 1024;
        final float MIN_TEXT_RATIO = 0.3f;
        try (FileInputStream fis = new FileInputStream(file)) {
            byte[] buffer = new byte[Math.min(MAX_READ_BYTES, (int) file.length())];
            fis.read(buffer);

            // count printable characters
            int printableCharCount = 0;
            for (byte b : buffer) {
                if (b == 0) {
                    return false;
                }
                if (!Character.isISOControl(b) && Character.isDefined(b)) {
                    printableCharCount++;
                }
            }
            return printableCharCount * 1.0f / buffer.length >= MIN_TEXT_RATIO;
        } catch (IOException e) {
            return false;
        }
    }

    private FileExtType resolveFileType(File file) {
        FileExtType fileExtType = FileExtType.from(file.toPath());
        if (FileExtType.UNKNOWN == fileExtType) {
            return isPrintableTextFile(file) ? FileExtType.TEXT : FileExtType.BINARY;
        }
        return fileExtType;
    }

    static String readTextFile(File file, ReadTextFileInput input) throws IOException {
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

    static String readImageFile(File file) throws IOException {
        return Base64.getEncoder().encodeToString(Files.readAllBytes(file.toPath()));
    }
}

enum FileExtType {
    TEXT(".txt", ".md", ".html", ".htm", ".css", ".js", ".ts", ".tsx", ".css",
            ".java", ".gradle", ".py", ".rt", ".go", ".c", ".cpp", ".php", ".sh", ".bat", ".conf", ".ini",
            ".json", ".yml", ".yaml", ".xml", ".csv", ".log"),
    IMAGE(".jpg", ".jpeg", ".png", ".gif", ".bmp", ".ico", ".svg", ".webp", ".psd",
            ".ai", ".eps", ".cdr", ".sketch", ".fig", ".raw", ".tiff", ".tif"),
    BINARY(".zip", ".tar", ".gz", ".exe", ".dll", ".so", ".class", ".jar", ".war", ".7z",
            ".doc", ".docx", ".xls", ".xlsx", ".ppt", ".pptx", ".odt", ".ods", ".odp",
            ".bin", ".dat", ".obj", ".o", ".a", ".lib", ".wasm", ".pyc", ".pyo"),
    UNKNOWN,
    ;

    final String[] fileExtensions;
    static final Map<String, FileExtType> FILE_EXT_TYPE_MAP = new HashMap<>();

    static {
        for (FileExtType fileExtType : FileExtType.values()) {
            for (String fileExtension : fileExtType.fileExtensions) {
                FILE_EXT_TYPE_MAP.put(fileExtension, fileExtType);
            }
        }
    }

    FileExtType(String... fileExtensions) {
        this.fileExtensions = fileExtensions;
    }

    static FileExtType from(Path filePath) {
        int index = filePath.toString().lastIndexOf(".");
        String fileExt = index > 0 ? filePath.toString().substring(index) : "";
        return FILE_EXT_TYPE_MAP.getOrDefault(fileExt, FileExtType.UNKNOWN);
    }
}