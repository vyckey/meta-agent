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
public class ListFileOutput {
    private final List<File> files;

    public ListFileOutput(List<File> files) {
        this.files = Objects.requireNonNull(files, "files cannot be null");
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
                .append(formatFileSize(file.length()))
                .append('\t')
                .append(filePath)
                .append('\t')
                .append(LocalDateTime.now().format(DateTimeFormatter.ofPattern("d MMM yyyy HH:mm", Locale.ENGLISH)))
                .toString();
    }

    private static String formatFileSize(long byteSize) {
        if (byteSize < 1024) {
            return byteSize + "B";
        }
        double kbSize = byteSize / 1024.0;
        if (kbSize < 1024.0) {
            return String.format("%.2fKB", kbSize);
        }
        double mbSize = kbSize / 1024.0;
        if (mbSize < 1024.0) {
            return String.format("%.2fMB", mbSize);
        }
        return String.format("%.2fGB", mbSize / 1024.0);
    }
}
