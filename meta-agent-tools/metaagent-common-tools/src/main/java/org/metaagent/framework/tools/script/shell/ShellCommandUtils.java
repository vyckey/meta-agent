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

package org.metaagent.framework.tools.script.shell;

import com.google.common.collect.Lists;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ShellCommandUtils {
    private static final Pattern COMMAND_ROOT_PATTERN = Pattern.compile("^\"([^\"]+)\"|^'([^']+)'|^(\\S+)");

    /**
     * Splits a shell command into a list of individual commands, respecting quotes.
     * For example, the command:
     * <pre>echo "Hello World" && ls -la; mkdir 'New Folder'</pre>
     * would be split into:
     * <pre>["echo \"Hello World\"", "ls -la", "mkdir 'New Folder'"]</pre>
     *
     * @param command the shell command string
     * @return a list of individual commands
     */
    public static List<String> splitCommands(String command) {
        List<String> commands = Lists.newArrayList();
        if (StringUtils.isEmpty(command)) {
            return commands;
        }

        StringBuilder current = new StringBuilder();
        boolean inSingleQuotes = false;
        boolean inDoubleQuotes = false;
        int len = command.length();

        for (int i = 0; i < len; i++) {
            char ch = command.charAt(i);

            // Escape character
            if (ch == '\\' && i + 1 < len) {
                current.append(ch).append(command.charAt(i + 1));
                i++; // Skip the next character
                continue;
            }

            // Toggle quote states
            if (ch == '\'' && !inDoubleQuotes) {
                inSingleQuotes = !inSingleQuotes;
            } else if (ch == '"' && !inSingleQuotes) {
                inDoubleQuotes = !inDoubleQuotes;
            }

            // Split on operators if not within quotes
            if (!inSingleQuotes && !inDoubleQuotes) {
                char next = i + 1 < len ? command.charAt(i + 1) : '\0';

                if ((ch == '&' && next == '&') || (ch == '|' && next == '|')) {
                    addIfNotBlank(commands, current);
                    i++; // Skip the next character
                } else if (ch == ';' || ch == '&' || ch == '|') {
                    addIfNotBlank(commands, current);
                } else {
                    current.append(ch);
                }
            } else {
                current.append(ch);
            }
        }

        addIfNotBlank(commands, current);
        return commands;
    }

    private static void addIfNotBlank(List<String> list, StringBuilder sb) {
        String s = sb.toString().trim();
        if (!s.isEmpty()) {
            list.add(s);
        }
        sb.setLength(0);
    }

    /**
     * Extracts the root command from a given shell command string.
     * This is used to identify the base command for permission checks.
     * <p>
     * For example:
     * <pre>
     * ls - la / tmp -> ls
     * git commit -m "Initial commit" -> git
     * /usr/bin/python script.py -> python
     * "C:\Users\My User\python.exe" script.py -> C:\Users\My User\python.exe
     * </pre>
     *
     * @param command The shell command string to parse
     * @return The root command name, or null if it cannot be determined
     */
    public static String getCommandRoot(String command) {
        if (command == null) {
            return null;
        }

        String trimmedCommand = command.trim();
        if (trimmedCommand.isEmpty()) {
            return null;
        }

        // The regex is designed to find the first token in the command, while respecting quotes.
        Matcher m = COMMAND_ROOT_PATTERN.matcher(trimmedCommand);

        if (m.find()) {
            // group 1：double quotes match；group 2：single quotes match；group 3：non-quoted match
            String commandRoot = m.group(1) != null ? m.group(1)
                    : m.group(2) != null ? m.group(2)
                    : m.group(3);
            if (commandRoot != null && !commandRoot.isEmpty()) {
                // Returns the last part of the command in case it's a path, e.g. /usr/bin/python -> python
                return commandRoot.replaceAll("[\\\\/]", "/")
                        .replaceAll(".*/", "");
            }
        }
        return null;
    }

    /**
     * Detects if exists command-substitution patterns in a shell command according to Bash quoting rules:
     * <ul>
     *   <li>Single quotes ('): everything is literal, no substitution possible.</li>
     *   <li>Double quotes ("): command substitution with $() and back-ticks works unless escaped with \.</li>
     *   <li>No quotes: command substitution with $(), &lt;(), and back-ticks works.</li>
     * </ul>
     *
     * @param command the shell command string to inspect
     * @return {@code true} when the supplied command string contains at least one command-substitution construct
     * that would be executed by Bash., {@code false} otherwise
     */
    public static boolean hasCommandSubstitution(String command) {
        if (command == null || command.isEmpty()) {
            return false;
        }

        boolean inSingleQuotes = false;
        boolean inDoubleQuotes = false;
        boolean inBackticks = false;

        int len = command.length();
        for (int i = 0; i < len; i++) {
            char ch = command.charAt(i);
            char next = (i + 1 < len) ? command.charAt(i + 1) : '\0';

            // Escaping: effective everywhere except inside single quotes
            if (ch == '\\' && !inSingleQuotes) {
                i++; // skip the escaped character
                continue;
            }

            // Update quote / back-tick states
            if (ch == '\'' && !inDoubleQuotes && !inBackticks) {
                inSingleQuotes = !inSingleQuotes;
            } else if (ch == '"' && !inSingleQuotes && !inBackticks) {
                inDoubleQuotes = !inDoubleQuotes;
            } else if (ch == '`' && !inSingleQuotes) {
                // back-ticks are recognised outside single quotes (even inside doubles)
                inBackticks = !inBackticks;
            }

            // Detect substitution constructs that would be executed
            if (!inSingleQuotes) {
                // $(...) command substitution: active in double quotes and unquoted
                if (ch == '$' && next == '(') {
                    return true;
                }
                // <(...) process substitution: active only when completely unquoted
                if (ch == '<' && next == '(' && !inDoubleQuotes && !inBackticks) {
                    return true;
                }
                // opening ` of back-tick substitution
                if (ch == '`' && !inBackticks) {
                    return true;
                }
            }
        }
        return false;
    }
}
