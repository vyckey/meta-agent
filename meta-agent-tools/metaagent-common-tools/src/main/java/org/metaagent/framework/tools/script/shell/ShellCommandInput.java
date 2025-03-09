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
import com.google.common.collect.Maps;
import lombok.Getter;

import java.util.Map;
import java.util.stream.Collectors;

/**
 * description is here
 *
 * @author vyckey
 */
@Getter
public class ShellCommandInput {
    @JsonProperty(required = true)
    private String command;

    private final Map<String, String> envs;

    public ShellCommandInput(String command, Map<String, String> envs) {
        this.command = command;
        this.envs = envs;
    }

    @JsonCreator
    public ShellCommandInput(@JsonProperty("command") String command) {
        this(command, Maps.newHashMap());
    }

    public String[] getEnvArray() {
        return envs.entrySet().stream()
                .map(entry -> entry.getKey() + "=" + entry.getValue())
                .toArray(String[]::new);
    }

    public ShellCommandInput addEnv(String key, String value) {
        envs.put(key, value);
        return this;
    }

    public void clearEnvs() {
        envs.clear();
    }

    @Override
    public String toString() {
        if (envs.isEmpty()) {
            return command;
        }
        return envs.entrySet().stream().map(entry -> entry.getKey() + "=" + entry.getValue())
                .collect(Collectors.joining(" ", "", " " + command));
    }
}
