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

import org.apache.commons.collections.CollectionUtils;
import org.metaagent.framework.core.tool.schema.ToolDisplayable;
import org.metaagent.framework.core.tool.schema.ToolPromptable;

import java.net.URL;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Output of tool {@link ListSkillsTool}.
 *
 * @author vyckey
 */
public record ListSkillOutput(List<SkillMetadata> skills) implements ToolPromptable, ToolDisplayable {
    @Override
    public String promptText() {
        if (CollectionUtils.isEmpty(skills)) {
            return "<skills>No available skills</skills>";
        } else {
            return "<skills>\n" + skills.stream().map(SkillMetadata::promptText).collect(Collectors.joining("\n")) + "\n</skills>";
        }
    }

    @Override
    public String display() {
        if (CollectionUtils.isEmpty(skills)) {
            return "No skills found.";
        }
        return "Found " + skills.size() + " skills:\n- "
                + skills.stream().map(SkillMetadata::display).collect(Collectors.joining("\n- "));
    }

    record SkillMetadata(String name,
                         String version,
                         String description,
                         URL location) implements ToolPromptable, ToolDisplayable {
        @Override
        public String promptText() {
            return "<skill name=\"" + name + "\" version=\"" + version + "\" location=\"" + location + "\">\n"
                    + description + "\n</skill>";
        }

        @Override
        public String display() {
            return name + " (v" + version + "): " + description + " [Location: " + location + "]";
        }
    }
}
