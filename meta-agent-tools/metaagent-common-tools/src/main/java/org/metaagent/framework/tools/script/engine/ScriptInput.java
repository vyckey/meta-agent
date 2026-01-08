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

package org.metaagent.framework.tools.script.engine;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.metaagent.framework.core.tool.schema.ToolDisplayable;

import java.util.Map;
import java.util.Optional;

/**
 * Represents the input for executing a script using a specific engine.
 *
 * @author vyckey
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScriptInput implements ToolDisplayable {
    @JsonProperty(required = true)
    @JsonPropertyDescription("The engine to be used to execute the script")
    private String engine;

    @JsonPropertyDescription("The language of the script (e.g., JavaScript, Python)")
    private String language;

    @NotNull(message = "Script cannot be null")
    @JsonProperty(required = true)
    @JsonPropertyDescription("The script to be executed")
    private String script;

    @JsonPropertyDescription("The variables to be passed to the script")
    private Map<String, Object> variables;

    @Override
    public String display() {
        String lang = Optional.ofNullable(language).orElse(engine);
        String scriptSnippet = script.length() > 50 ? script.substring(0, 50) + "..." : script;
        return "Execute " + lang + " script: " + scriptSnippet;
    }
}
