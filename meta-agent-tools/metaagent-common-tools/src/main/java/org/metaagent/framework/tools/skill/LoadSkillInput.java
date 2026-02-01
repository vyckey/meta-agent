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
import org.metaagent.framework.core.skill.loader.SkillLoader;
import org.metaagent.framework.core.tool.schema.ToolDisplayable;

import java.util.Objects;

/**
 * Input of tool {@link LoadSkillTool}.
 *
 * @author vyckey
 */
public record LoadSkillInput(
        @JsonProperty(value = "skillName", required = true)
        @JsonPropertyDescription("The name of the skill to load")
        String skillName,

        @JsonProperty(value = "skillVersion")
        @JsonPropertyDescription("The version of the skill to load, if not specified, the latest version will be loaded")
        String skillVersion,

        @JsonProperty(value = "relativePath")
        @JsonPropertyDescription("The relative path to the skill directory, "
                + "e.g. SKILL.md, references/guide.md, scripts/write_pdf.py. "
                + "If not specified, the default SKILL.md will be loaded")
        String relativePath
) implements ToolDisplayable {
    @Override
    public String display() {
        StringBuilder sb = new StringBuilder("Load resource '");
        sb.append(Objects.requireNonNullElse(relativePath, SkillLoader.SKILL_FILENAME));
        sb.append("' of skill '").append(skillName).append("'");
        if (skillVersion != null) {
            sb.append(" with version ").append(skillVersion);
        }
        sb.append(".");
        return sb.toString();
    }
}
