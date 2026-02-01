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

package org.metaagent.framework.tools.skill;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import org.metaagent.framework.core.tool.schema.ToolDisplayable;
import org.metaagent.framework.core.tool.schema.ToolErrorOutput;
import org.metaagent.framework.core.tool.schema.ToolPromptable;

/**
 * Output of tool {@link LoadSkillTool}.
 *
 * @author vyckey
 */
public record LoadSkillOutput(
        @JsonProperty(value = "skillName", required = true)
        String skillName,

        @JsonProperty(value = "resourceLocation", required = true)
        String resourceLocation,

        @JsonProperty(value = "content")
        @JsonPropertyDescription("The content of the loaded skill resource, null if error occurs")
        String content,

        @JsonProperty(value = "error")
        @JsonPropertyDescription("The error message, null if no error occurs")
        String error
) implements ToolPromptable, ToolDisplayable, ToolErrorOutput {

    public static LoadSkillOutput from(String skillName, String resourceLocation, String content) {
        return new LoadSkillOutput(skillName, resourceLocation, content, null);
    }

    public static LoadSkillOutput error(String skillName, String resourceLocation, String error) {
        return new LoadSkillOutput(skillName, resourceLocation, null, error);
    }

    @Override
    public String promptText() {
        if (error != null) {
            return "Failed to load skill '" + skillName + "' resource '" + resourceLocation + "': " + error;
        }
        return "Loaded skill '" + skillName + "' resource '" + resourceLocation + "':\n" + content;
    }

    @Override
    public String display() {
        if (error != null) {
            return "Failed to load skill '" + skillName + "' resource '" + resourceLocation + "': " + error;
        } else {
            final int maxDisplayLength = 100;
            return "Loaded skill '" + skillName + "' resource '" + resourceLocation + "':\n"
                    + (content.length() > maxDisplayLength ? content.substring(0, maxDisplayLength)
                    + "...(" + (content.length() - maxDisplayLength) + " chars hidden)" : content);
        }
    }

    @Override
    public String getError() {
        return error;
    }
}
