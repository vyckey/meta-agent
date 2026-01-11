/*
 * MIT License
 *
 * Copyright (c) 2026 MetaAgent
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

package org.metaagent.framework.core.tool.config;

import org.apache.commons.lang3.StringUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * ToolPattern represents a tool name with an optional pattern.
 * It can be used to match a tool name against a pattern.
 *
 * <p>
 * Examples of valid tool patterns:
 * <ul>
 *     <li>http_request</li>
 *     <li>execute_shell_command(ls)</li>
 *     <li>execute_shell_command(git add *)</li>
 *     <li>edit_text_file(docs/**\/*.md)</li>
 * </ul>
 *
 * @author vyckey
 */
public record ToolPattern(String toolName, String pattern) {
    private static final Pattern PATTERN = Pattern.compile("^(\\w?[\\w-_]*)\\((.*)\\)$");

    public ToolPattern {
        if (StringUtils.isEmpty(toolName)) {
            throw new IllegalArgumentException("toolName is required.");
        }
    }

    public ToolPattern(String toolName) {
        this(toolName, null);
    }

    public static ToolPattern parse(String string) {
        Matcher matcher = PATTERN.matcher(string);
        if (!matcher.matches()) {
            throw new IllegalArgumentException("Invalid tool pattern: " + string);
        }
        if (matcher.groupCount() == 2) {
            return new ToolPattern(matcher.group(1), matcher.group(2));
        }
        return new ToolPattern(matcher.group(1));
    }

    @Override
    public String toString() {
        if (pattern == null) {
            return toolName;
        }
        return toolName + "(" + pattern + ")";
    }

}
