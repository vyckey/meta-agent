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

package org.metaagent.framework.tools.file.list;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.metaagent.framework.core.tool.schema.ToolDisplayable;
import org.metaagent.framework.tools.file.util.FileUtils;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * List files output.
 *
 * @author vyckey
 */
public class ListFileOutput implements ToolDisplayable {
    private final List<File> files;
    private final String display;

    public ListFileOutput(List<File> files, String display) {
        this.files = Objects.requireNonNull(files, "files cannot be null");
        this.display = display;
    }

    public ListFileOutput(List<File> files) {
        this(files, "Found " + files.size() + " files");
    }

    @JsonIgnore
    public List<File> getFiles() {
        return files;
    }

    public String getFileList() {
        return files.stream().map(ListFileOutput::formatFile).collect(Collectors.joining("\n"));
    }

    public int getFileCount() {
        return files.size();
    }

    public static String formatFile(File file) {
        String filePath = file.getPath().startsWith("./") ? file.getPath().substring(2) : file.getPath();
        return new StringBuilder()
                .append(file.isDirectory() ? 'd' : '-')
                .append(file.canRead() ? 'r' : '-')
                .append(file.canWrite() ? 'w' : '-')
                .append(file.canExecute() ? 'x' : '-')
                .append('\t')
                .append(FileUtils.formatFileSize(file.length()))
                .append('\t')
                .append(filePath)
                .append('\t')
                .append(LocalDateTime.now().format(DateTimeFormatter.ofPattern("d MMM yyyy HH:mm", Locale.ENGLISH)))
                .toString();
    }

    @Override
    public String display() {
        return display;
    }
}
