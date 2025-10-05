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

package org.metaagent.framework.common.ignorefile;

import com.google.common.collect.Maps;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * IgnoreFileFilter is a file filter that uses .gitignore like files to filter files.
 *
 * @author vyckey
 */
public class IgnoreFileFilter {
    public static final String AGENT_IGNORE_FILE_NAME = ".agentignore";
    public static final String GIT_IGNORE_FILE_NAME = GitUtils.GIT_IGNORE_FILE_NAME;
    private static final Map<String, IgnoreFileFilter> FILTER_CACHE = Maps.newConcurrentMap();

    private final String ignoreFileName;
    private final Path rootPath;
    private final List<GitIgnoreLikeFileFilter> fileFilters;
    private final List<Pattern> extraIgnorePatterns = new ArrayList<>();

    public IgnoreFileFilter(Path rootPath, String ignoreFileName) throws IOException {
        this.ignoreFileName = ignoreFileName.trim();
        this.rootPath = Objects.requireNonNull(rootPath, "rootPath is required").normalize().toAbsolutePath();
        this.fileFilters = buildFileFilters(rootPath, ignoreFileName);
        if (ignoreFileName.equalsIgnoreCase(AGENT_IGNORE_FILE_NAME)) {
            this.extraIgnorePatterns.add(GitIgnoreLikeFileFilter.compileAsPattern(".git/"));
        }
    }

    public static IgnoreFileFilter gitignoreFilter(Path currentPath) throws IOException {
        Path rootPath = GitUtils.findGitRootPath(currentPath)
                .orElseThrow(() -> new IOException("Current path is not in a git repository: " + currentPath));
        String cacheKey = cacheKey(rootPath, GIT_IGNORE_FILE_NAME);
        IgnoreFileFilter ignoreFileFilter = FILTER_CACHE.get(cacheKey);
        if (ignoreFileFilter == null) {
            ignoreFileFilter = new IgnoreFileFilter(rootPath, GIT_IGNORE_FILE_NAME);
            FILTER_CACHE.put(cacheKey, ignoreFileFilter);
        }
        return ignoreFileFilter;
    }

    public static IgnoreFileFilter agentignoreFilter(Path rootPath) throws IOException {
        String cacheKey = cacheKey(rootPath, AGENT_IGNORE_FILE_NAME);
        IgnoreFileFilter ignoreFileFilter = FILTER_CACHE.get(cacheKey);
        if (ignoreFileFilter == null) {
            ignoreFileFilter = new IgnoreFileFilter(rootPath, AGENT_IGNORE_FILE_NAME);
            FILTER_CACHE.put(cacheKey, ignoreFileFilter);
        }
        return ignoreFileFilter;
    }

    private static String cacheKey(Path rootPath, String ignoreFileName) {
        return rootPath.toAbsolutePath().normalize() + "|" + ignoreFileName.trim().toLowerCase();
    }

    protected List<GitIgnoreLikeFileFilter> buildFileFilters(Path rootPath, String ignoreFileName) throws IOException {
        List<GitIgnoreLikeFileFilter> fileFilters = new ArrayList<>();
        List<Path> ignoreFilePaths = Files.walk(rootPath)
                .filter(Files::isRegularFile)
                .filter(path -> path.getFileName().toString().equalsIgnoreCase(this.ignoreFileName))
                .sorted()
                .toList();
        for (Path ignoreFilePath : ignoreFilePaths) {
            fileFilters.add(new GitIgnoreLikeFileFilter(ignoreFilePath, false));
        }
        return fileFilters;
    }

    public boolean ignoreFile(Path filePath) {
        filePath = filePath.normalize();
        // If the file is outside the root path, do not ignore it
        if (filePath.isAbsolute() && !filePath.startsWith(rootPath)) {
            return false;
        }

        // Convert to absolute path
        if (filePath.isAbsolute()) {
            filePath = filePath.toAbsolutePath();
        } else {
            filePath = rootPath.resolve(filePath).toAbsolutePath();
        }

        for (Pattern ignorePattern : extraIgnorePatterns) {
            if (ignorePattern.matcher(rootPath.relativize(filePath).toString()).matches()) {
                return true;
            }
        }

        boolean ignored = false;
        for (GitIgnoreLikeFileFilter fileFilter : fileFilters) {
            if (!filePath.startsWith(fileFilter.getIgnoreFileDirectory())) {
                continue;
            }
            ignored = fileFilter.ignoreFile(filePath);
        }
        return ignored;
    }
}
