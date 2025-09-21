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

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.google.common.collect.Maps;
import org.metaagent.framework.core.tool.schema.ToolDisplayable;

import java.util.Map;
import java.util.stream.Collectors;

/**
 * Represents the input for executing a shell command.
 *
 * @author vyckey
 */
public record ShellCommandInput(
        @JsonProperty(required = true)
        @JsonPropertyDescription("The shell command to be executed")
        String command,

        @JsonPropertyDescription("The environment variables to be passed to the command. Optional")
        Map<String, String> envs,

        @JsonPropertyDescription("The timeout in seconds for the command to complete. Optional, default no timeout")
        Long timeoutSeconds) implements ToolDisplayable {

    @JsonCreator
    public ShellCommandInput(@JsonProperty("command") String command,
                             @JsonProperty("envs") Map<String, String> envs) {
        this(command, envs, null);
    }

    public ShellCommandInput(String command) {
        this(command, Maps.newHashMap());
    }

    public String[] getEnvArray() {
        return envs.entrySet().stream()
                .map(entry -> entry.getKey() + "=" + entry.getValue())
                .toArray(String[]::new);
    }

    @Override
    public String display() {
        if (envs.isEmpty()) {
            return command;
        }
        return envs.entrySet().stream().map(entry -> entry.getKey() + "=" + entry.getValue())
                .collect(Collectors.joining(" ", "", " " + command));
    }

    @Override
    public String toString() {
        return display();
    }
}
