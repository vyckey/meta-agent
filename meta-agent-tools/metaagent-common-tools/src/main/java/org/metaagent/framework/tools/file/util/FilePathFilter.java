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

import lombok.Builder;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

/**
 * FilePathFilter is a utility class that provides methods to filter file paths based on specified patterns.
 *
 * @param patterns              patterns to match file paths against.
 * @param excludePatterns       patterns to exclude from the file paths.
 * @param includePatterns       patterns to include in the file paths after exclusion.
 * @param ignoreLikeFileFilters list of filters that behave like .gitignore files, allowing for more complex ignore rules.
 * @author vyckey
 */
public record FilePathFilter(List<Pattern> patterns,
                             List<Pattern> excludePatterns,
                             List<Pattern> includePatterns,
                             List<GitIgnoreLikeFileFilter> ignoreLikeFileFilters) {
    @Builder
    public FilePathFilter(List<Pattern> patterns,
                          List<Pattern> excludePatterns,
                          List<Pattern> includePatterns,
                          List<GitIgnoreLikeFileFilter> ignoreLikeFileFilters) {
        this.patterns = patterns != null ? patterns : List.of();
        this.excludePatterns = excludePatterns != null ? excludePatterns : List.of();
        this.includePatterns = includePatterns != null ? includePatterns : List.of();
        this.ignoreLikeFileFilters = ignoreLikeFileFilters != null ? ignoreLikeFileFilters : List.of();
    }

    String relativizePath(Path directory, Path filePath) {
        directory = directory.toAbsolutePath();
        filePath = filePath.normalize().toAbsolutePath();
        if (GitUtils.isGitIgnoreFile(filePath)) {
            Optional<Path> gitRootPath = GitUtils.findGitRootPath(filePath.getParent());
            if (gitRootPath.isPresent()) {
                directory = gitRootPath.get();
            }
        }
        return directory.relativize(filePath).toString();
    }

    public MatchType matchPath(Path directory, Path filePath) {
        String path = relativizePath(directory, filePath);
        boolean matched = patterns.isEmpty();
        for (Pattern pattern : patterns) {
            if (pattern.matcher(path).matches()) {
                matched = true;
                break;
            }
        }
        if (!matched) {
            return MatchType.UNMATCHED;
        }

        for (Pattern excludePattern : excludePatterns) {
            if (!excludePattern.matcher(path).matches()) {
                continue;
            }
            boolean excluded = true;
            for (Pattern includePattern : includePatterns) {
                if (includePattern.matcher(path).matches()) {
                    excluded = false;
                }
            }
            if (excluded) {
                return MatchType.EXCLUDED;
            }
        }
        for (GitIgnoreLikeFileFilter ignoreLikeFileFilter : ignoreLikeFileFilters) {
            if (ignoreLikeFileFilter.ignoreFile(path)) {
                return MatchType.IGNORED;
            }
        }
        return MatchType.MATCHED;
    }

    public enum MatchType {
        MATCHED, UNMATCHED, EXCLUDED, IGNORED
    }
}
