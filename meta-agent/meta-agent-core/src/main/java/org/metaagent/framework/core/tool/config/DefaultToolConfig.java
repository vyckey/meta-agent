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

import com.google.common.collect.Sets;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;

public class DefaultToolConfig implements ToolConfig {
    protected Path workingDirectory;
    protected Set<String> allowedCommands = Sets.newHashSet();
    protected Set<String> disallowedCommands = Sets.newHashSet();
    protected Set<String> sessionAllowedCommands = Sets.newHashSet();
    protected Set<String> sessionDisallowedCommands = Sets.newHashSet();

    @Override
    public Path workingDirectory() {
        if (workingDirectory != null) {
            return workingDirectory;
        }
        if (System.getProperty("CWD") != null) {
            return Paths.get(System.getProperty("CWD")).toAbsolutePath().normalize();
        } else {
            return Paths.get(".").toAbsolutePath().normalize();
        }
    }

    @Override
    public Set<String> allowedCommands() {
        return allowedCommands;
    }

    @Override
    public Set<String> disallowedCommands() {
        return disallowedCommands;
    }

    @Override
    public Set<String> sessionAllowedCommands() {
        return sessionAllowedCommands;
    }

    @Override
    public Set<String> sessionDisallowedCommands() {
        return sessionDisallowedCommands;
    }
}
