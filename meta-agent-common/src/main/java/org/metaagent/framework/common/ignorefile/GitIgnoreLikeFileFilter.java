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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * GitIgnoreLikeFileFilter is a file filter that uses .gitignore like files to filter files.
 *
 * @author vyckey
 */
public class GitIgnoreLikeFileFilter {
    // exclude characters *,?,[,],!,/,**
    private static final String[] ESCAPE_CHARS = {"\\", "^", "$", "{", "}", "(", ")", ".", "+", "|", "<", ">"};
    private final Path ignoreFilePath;
    private final Path ignoreFileDirectory;
    private final List<IgnorePattern> ignorePatterns = new ArrayList<>();

    public GitIgnoreLikeFileFilter(Path ignoreFilePath, boolean ignoreFileIncluded) throws IOException {
        this.ignoreFilePath = ignoreFilePath.toAbsolutePath().normalize();
        this.ignoreFileDirectory = this.ignoreFilePath.getParent();
        if (Files.exists(ignoreFilePath)) {
            Files.readAllLines(ignoreFilePath).stream()
                    .filter(line -> !line.startsWith("#"))
                    .map(String::trim)
                    .filter(line -> !line.isEmpty())
                    .forEach(this::parseRule);
            if (ignoreFileIncluded) {
                this.parseRule(this.ignoreFilePath.getFileName().toString());
            }
        }
    }

    public Path getIgnoreFilePath() {
        return ignoreFilePath;
    }

    public Path getIgnoreFileDirectory() {
        return ignoreFileDirectory;
    }

    public String getIgnoreFileName() {
        return ignoreFilePath.getFileName().toString();
    }

    private void parseRule(String rule) {
        boolean whitelist = rule.startsWith("!");
        Pattern pattern;
        if (whitelist) {
            pattern = compileAsPattern(rule.substring(1));
        } else {
            pattern = compileAsPattern(rule);
        }
        ignorePatterns.add(new IgnorePattern(pattern, whitelist));
    }

    public boolean ignoreFile(String filePath) {
        return ignoreFile(getIgnoreFileDirectory().resolve(filePath));
    }

    public boolean ignoreFile(Path filePath) {
        String filePathStr;
        if (filePath.isAbsolute()) {
            filePathStr = ignoreFileDirectory.relativize(filePath).normalize().toString();
        } else {
            filePathStr = filePath.normalize().toString();
        }
        // Do not ignore files outside the ignore file directory
        if (filePathStr.startsWith("..")) {
            return false;
        }

        boolean ignored = false;
        for (IgnorePattern ignorePattern : ignorePatterns) {
            boolean matches = ignorePattern.pattern.matcher(filePathStr).matches();
            if (matches) {
                ignored = !ignorePattern.whitelist;
            }
        }
        return ignored;
    }

    public static Pattern compileAsPattern(String rule) {
        boolean fromRoot = rule.startsWith("/");
        if (fromRoot) {
            rule = rule.substring(1);
        }
        boolean isDirectory = rule.endsWith("/");
        if (isDirectory) {
            rule = rule.substring(0, rule.length() - 1);
        }

        for (String escapeChar : ESCAPE_CHARS) {
            if (rule.contains(escapeChar)) {
                rule = rule.replace(escapeChar, "\\" + escapeChar);
            }
        }
        rule = rule.replace("**", "*").replace("*", ".*").replace("?", ".");

        StringBuilder regex = new StringBuilder();
        if (fromRoot) {
            regex.append("^");
        } else {
            regex.append("(^|.*/)");
        }
        regex.append(rule);
        if (isDirectory) {
            regex.append("(/.*|$)");
        } else {
            regex.append("$");
        }
        return Pattern.compile(regex.toString());
    }

    record IgnorePattern(Pattern pattern, boolean whitelist) {
    }

    @Override
    public String toString() {
        return "GitIgnoreLikeFileFilter{" + ignoreFilePath + "}";
    }

    public static void main(String[] args) throws IOException {
        GitIgnoreLikeFileFilter filter = new GitIgnoreLikeFileFilter(Path.of(".gitignore"), false);
        List<String> filePaths = List.of(
                "app.log"
        );
        for (String filePath : filePaths) {
            System.out.println(filePath + " -> ignored:" + filter.ignoreFile(Path.of(filePath)));
        }
    }
}
