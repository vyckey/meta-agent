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

package org.metaagent.framework.tools.file.util;

import org.apache.commons.collections.CollectionUtils;

import java.io.FileNotFoundException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public abstract class FileUtils {
    public static boolean hasCommand(String... commands) {
        try {
            Process process = new ProcessBuilder(commands)
                    .redirectErrorStream(true)
                    .start();
            return process.waitFor() == 0;
        } catch (Exception e) {
            // If the command is not found or any other error occurs, return false
            return false;
        }
    }

    public static Path toNormalizedAbsolutePath(Path path) {
        if (path.isAbsolute()) {
            return path.normalize();
        }
        return path.toAbsolutePath().normalize();
    }

    public static boolean inFileDirectory(Path filePath, Path directory) {
        Path normalizedDirectory = toNormalizedAbsolutePath(directory);
        if (!normalizedDirectory.toFile().isDirectory()) {
            throw new IllegalArgumentException("parameter `directory` must be a directory");
        }
        return toNormalizedAbsolutePath(filePath).startsWith(normalizedDirectory);
    }

    public static Path resolvePath(Path workingDirectory, Path filePath) {
        if (filePath.isAbsolute()) {
            return filePath.normalize();
        }
        return toNormalizedAbsolutePath(workingDirectory.resolve(filePath));
    }

    public static List<Path> resolvePaths(Path directory, List<String> paths, boolean allowNotFound)
            throws FileNotFoundException {
        if (CollectionUtils.isEmpty(paths)) {
            return List.of();
        }
        List<Path> filePaths = new ArrayList<>(paths.size());
        for (String path : paths) {
            Path filePath = resolvePath(directory, Path.of(path));
            if (filePath.toFile().exists()) {
                filePaths.add(filePath);
            } else if (allowNotFound) {
                throw new FileNotFoundException(path);
            }
        }
        return filePaths;
    }

    public static boolean matchPath(Path path, String pattern) {
        String sepPath = path.toString().replace('\\', '/');

        String regex = pattern
                .replace('\\', '/')
                .replace(".", "\\.")
                .replace("**", "{DOUBLE_STAR}")
                .replace("*", "[^/]*")
                .replace("{DOUBLE_STAR}/", "(?:.*/)?")
                .replace("{DOUBLE_STAR}", ".*");
        regex = "^" + regex + "$";

        return sepPath.matches(regex);
    }

    public static String formatFileSize(long byteSize) {
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
