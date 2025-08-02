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

package org.metaagent.framework.tools.file.find;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.io.File;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * GlobFileOutput represents the output of a file search operation using glob patterns.
 * It contains a list of files that match the specified glob patterns.
 *
 * @author vyckey
 */
public class GlobFileOutput {
    private final List<File> files;

    public GlobFileOutput(List<File> files) {
        this.files = Objects.requireNonNull(files, "Files list cannot be null");
    }

    @JsonIgnore
    public List<File> getFiles() {
        return files;
    }

    public int getFileCount() {
        return files.size();
    }

    public String getFileList() {
        return files.stream().map(GlobFileOutput::formatFile).collect(Collectors.joining("\n"));
    }

    public static String formatFile(File file) {
        return file.getAbsolutePath();
    }
}
