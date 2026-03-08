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

import org.apache.commons.lang3.StringUtils;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;

/**
 * ConfigPaths represents the paths for configuration files.
 *
 * @author vyckey
 */
public record ConfigPaths(
        String appName,
        Path globalConfigPath,
        Path userConfigPath
) {
    public ConfigPaths {
        Objects.requireNonNull(appName, "app name is required");
        Objects.requireNonNull(globalConfigPath, "global config path is required");
        Objects.requireNonNull(userConfigPath, "user config path is required");
        globalConfigPath = globalConfigPath.toAbsolutePath().normalize();
        userConfigPath = userConfigPath.toAbsolutePath().normalize();
    }

    public static ConfigPaths build(String app) {
        String os = System.getProperty("os.name").toLowerCase();
        if (os.contains("win")) {
            String global = StringUtils.isNotEmpty(System.getenv("PROGRAMDATA"))
                    ? System.getenv("PROGRAMDATA") : System.getProperty("user.home");
            String home = StringUtils.isNotEmpty(System.getenv("APPDATA"))
                    ? System.getenv("APPDATA") : System.getProperty("user.home");
            return new ConfigPaths(
                    app,
                    Paths.get(global, app),
                    Paths.get(home, app)
            );
        } else {
            String home = StringUtils.isNotEmpty(System.getenv("XDG_CONFIG_HOME"))
                    ? System.getenv("XDG_CONFIG_HOME") : System.getProperty("user.home");
            return new ConfigPaths(
                    app,
                    Paths.get("/etc", app),
                    Paths.get(home, ".config", app)
            );
        }
    }

    public static ConfigPaths get() {
        return build("meta-agent");
    }

    public Path projectConfigPath(WorkspaceConfig workspaceConfig) {
        Path projectDirectory = workspaceConfig.projectDirectory();
        return projectDirectory == null ? null : projectDirectory.resolve("." + appName);
    }
}
