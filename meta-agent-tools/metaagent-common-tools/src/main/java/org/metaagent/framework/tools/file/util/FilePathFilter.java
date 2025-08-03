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

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

public record FilePathFilter(List<Pattern> includePatterns, List<Pattern> excludePatterns,
                             List<GitIgnoreLikeFileFilter> ignoreLikeFileFilters) {

    String relativizePath(Path directory, Path filePath) {
        directory = directory.toAbsolutePath();
        filePath = filePath.normalize().toAbsolutePath();
        if (GitUtils.isGitIgnoreFile(filePath)) {
            Optional<Path> gitRootPath = GitUtils.findGitRootPath(filePath.getParent());
            if (gitRootPath.isPresent()) {
                directory = gitRootPath.get();
            }
        }
        try {
            return directory.relativize(filePath).toString();
        } catch (Exception e) {
            throw e;
        }
    }

    public boolean accept(Path directory, Path filePath) {
        String path = relativizePath(directory, filePath);
        for (Pattern pattern : includePatterns) {
            if (!pattern.matcher(path).matches()) {
                return false;
            }
        }
        for (Pattern pattern : excludePatterns) {
            if (pattern.matcher(path).matches()) {
                return false;
            }
        }
        for (GitIgnoreLikeFileFilter ignoreLikeFileFilter : ignoreLikeFileFilters) {
            if (ignoreLikeFileFilter.ignoreFile(path)) {
                return false;
            }
        }
        return true;
    }
}
