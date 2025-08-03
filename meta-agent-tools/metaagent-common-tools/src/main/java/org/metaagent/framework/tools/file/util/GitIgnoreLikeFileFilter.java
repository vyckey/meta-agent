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
    private final List<IgnorePattern> ignorePatterns = new ArrayList<>();

    public GitIgnoreLikeFileFilter(Path ignoreFilePath) throws IOException {
        Files.readAllLines(ignoreFilePath).stream()
                .filter(line -> !line.startsWith("#"))
                .map(String::trim)
                .filter(line -> !line.isEmpty())
                .forEach(this::parseRule);
        this.parseRule(ignoreFilePath.getFileName().toString());
        if (GitUtils.isGitIgnoreFile(ignoreFilePath)) {
            addGitIgnoreRules();
        }
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

    protected void addGitIgnoreRules() {
        this.parseRule("/.git/");
        this.parseRule(".gitignore");
    }

    public boolean ignoreFile(String filePath) {
        boolean ignored = false;
        for (IgnorePattern ignorePattern : ignorePatterns) {
            boolean matches = ignorePattern.pattern.matcher(filePath).matches();
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
}
