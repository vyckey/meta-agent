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

package org.metaagent.framework.core.config;

import org.metaagent.framework.common.ignorefile.GitUtils;

import java.nio.file.Path;
import java.util.Objects;
import java.util.Optional;

/**
 * ConfigPaths represents the paths for configuration files.
 *
 * @author vyckey
 */
public record ConfigPaths(
        Path globalConfigPath,
        Path userConfigPath,
        Path projectConfigPath,
        Path currentWorkingDirectory
) {
    public ConfigPaths {
        Objects.requireNonNull(globalConfigPath, "global config path is required");
        Objects.requireNonNull(userConfigPath, "user config path is required");
        Objects.requireNonNull(currentWorkingDirectory, "current working directory is required");
        globalConfigPath = globalConfigPath.toAbsolutePath().normalize();
        userConfigPath = userConfigPath.toAbsolutePath().normalize();
        currentWorkingDirectory = currentWorkingDirectory.toAbsolutePath().normalize();

        if (projectConfigPath != null) {
            projectConfigPath = projectConfigPath.toAbsolutePath().normalize();
        } else {
            Optional<Path> rootPath = GitUtils.findGitRootPath(currentWorkingDirectory);
            if (rootPath.isPresent()) {
                projectConfigPath = rootPath.get();
            }
        }
    }

    public static Path defaultWorkingDirectory() {
        if (System.getProperty("CWD") != null) {
            return Path.of(System.getProperty("CWD")).toAbsolutePath();
        } else {
            return Path.of(".").toAbsolutePath().normalize();
        }
    }

    public static ConfigPaths from(String appName, Path projectPath, Path currentWorkingDirectory) {
        return new ConfigPaths(
                Path.of("/etc/" + appName + "/"),
                Path.of("~/." + appName + "/"),
                projectPath,
                currentWorkingDirectory
        );
    }

    public static ConfigPaths newDefault() {
        return from("meta-agent", null, defaultWorkingDirectory());
    }
}
