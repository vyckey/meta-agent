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

import com.google.common.collect.Lists;
import org.apache.commons.collections.CollectionUtils;
import org.metaagent.framework.common.ignorefile.GitUtils;
import org.metaagent.framework.common.ignorefile.IgnoreFileFilter;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

/**
 * FilePathFilter is a utility class that provides methods to filter file paths based on specified patterns.
 *
 * @param patterns          patterns to match file paths against.
 * @param excludePatterns   patterns to exclude from the file paths.
 * @param includePatterns   patterns to include in the file paths after exclusion.
 * @param ignoreFileFilters list of filters that behave like .gitignore files, allowing for more complex ignore rules.
 * @author vyckey
 */
public record FilePathFilter(List<Pattern> patterns,
                             List<Pattern> excludePatterns,
                             List<Pattern> includePatterns,
                             List<IgnoreFileFilter> ignoreFileFilters) {
    public FilePathFilter(List<Pattern> patterns,
                          List<Pattern> excludePatterns,
                          List<Pattern> includePatterns,
                          List<IgnoreFileFilter> ignoreFileFilters) {
        this.patterns = patterns != null ? patterns : List.of();
        this.excludePatterns = excludePatterns != null ? excludePatterns : List.of();
        this.includePatterns = includePatterns != null ? includePatterns : List.of();
        this.ignoreFileFilters = ignoreFileFilters != null ? ignoreFileFilters : List.of();
    }

    public static FilePathFilter.Builder builder(Path directory) throws IOException {
        return new Builder(directory);
    }

    public MatchType matchPath(Path directory, Path filePath) {
        directory = directory.toAbsolutePath().normalize();
        filePath = filePath.toAbsolutePath().normalize();
        if (!filePath.startsWith(directory)) {
            return MatchType.UNMATCHED;
        }

        String path = directory.relativize(filePath).toString();
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
        for (IgnoreFileFilter ignoreFileFilter : ignoreFileFilters) {
            if (ignoreFileFilter.ignoreFile(filePath)) {
                return MatchType.IGNORED;
            }
        }
        return MatchType.MATCHED;
    }

    public enum MatchType {
        MATCHED, UNMATCHED, EXCLUDED, IGNORED
    }

    public static class Builder {
        private final Path directory;
        private List<Pattern> patterns;
        private List<Pattern> excludePatterns;
        private List<Pattern> includePatterns;
        private final List<IgnoreFileFilter> ignoreFileFilters = Lists.newArrayList();

        private Builder(Path directory) throws IOException {
            this.directory = directory;
            Optional<Path> gitRootPath = GitUtils.findGitRootPath(directory);
            if (gitRootPath.isPresent()) {
                Path rootPath = gitRootPath.get();
                this.ignoreFileFilters.add(IgnoreFileFilter.gitignoreFilter(rootPath));
                this.ignoreFileFilters.add(IgnoreFileFilter.agentignoreFilter(rootPath));
            } else {
                this.ignoreFileFilters.add(IgnoreFileFilter.agentignoreFilter(directory));
            }
        }

        public Builder patterns(List<Pattern> patterns) {
            this.patterns = patterns;
            return this;
        }

        public Builder excludePatterns(List<Pattern> excludePatterns) {
            this.excludePatterns = excludePatterns;
            return this;
        }

        public Builder includePatterns(List<Pattern> includePatterns) {
            this.includePatterns = includePatterns;
            return this;
        }

        public Builder ignoreFileFilters(List<String> ignoreLikeFiles) throws IOException {
            if (CollectionUtils.isNotEmpty(ignoreLikeFiles)) {
                Path rootPath = directory;
                Optional<Path> gitRootPath = GitUtils.findGitRootPath(directory);
                if (gitRootPath.isPresent()) {
                    rootPath = gitRootPath.get();
                }
                for (String ignoreLikeFile : ignoreLikeFiles) {
                    this.ignoreFileFilters.add(new IgnoreFileFilter(rootPath, ignoreLikeFile));
                }
            }
            return this;
        }

        public FilePathFilter build() {
            return new FilePathFilter(patterns, excludePatterns, includePatterns, ignoreFileFilters);
        }
    }
}
