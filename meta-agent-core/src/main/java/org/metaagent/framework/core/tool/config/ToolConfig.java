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

package org.metaagent.framework.core.tool.config;

import java.nio.file.Path;
import java.util.Set;

/**
 * Configuration interface for tools.
 *
 * @author vyckey
 */
public interface ToolConfig {
    /**
     * Gets the working directory for executing tools.
     *
     * @return the working directory
     */
    default Path workingDirectory() {
        if (System.getProperty("CWD") != null) {
            return Path.of(System.getProperty("CWD")).toAbsolutePath();
        } else {
            return Path.of(".").toAbsolutePath().normalize();
        }
    }

    /**
     * Get the set of allowed commands prefixes, for example, {"ls", "cat", "echo", "git status"}.
     *
     * @return a set of allowed command strings
     */
    Set<String> allowedCommands();

    /**
     * Get the set of disallowed commands, for example, {"rm", "shutdown", "reboot"}.
     *
     * @return a set of disallowed command strings
     */
    Set<String> disallowedCommands();

    /**
     * Get the set of allowed commands in a session, for example, {"cd", "export"}.
     *
     * @return a set of allowed command strings
     */
    Set<String> sessionAllowedCommands();

    /**
     * Get the set of disallowed commands in a session, for example, {"rm", "shutdown", "reboot"}.
     *
     * @return a set of disallowed command strings
     */
    Set<String> sessionDisallowedCommands();
}
