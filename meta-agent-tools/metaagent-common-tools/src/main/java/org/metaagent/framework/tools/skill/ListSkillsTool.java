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

import lombok.extern.slf4j.Slf4j;
import org.metaagent.framework.core.skill.manager.SkillManager;
import org.metaagent.framework.core.skill.metadata.SkillMetadata;
import org.metaagent.framework.core.tool.Tool;
import org.metaagent.framework.core.tool.ToolContext;
import org.metaagent.framework.core.tool.converter.ToolConverter;
import org.metaagent.framework.core.tool.converter.ToolConverters;
import org.metaagent.framework.core.tool.definition.ToolDefinition;
import org.metaagent.framework.core.tool.exception.ToolExecutionException;

import java.util.List;

/**
 * Tool to list all available skills.
 *
 * @author vyckey
 */
@Slf4j
public class ListSkillsTool implements Tool<ListSkillsInput, ListSkillOutput> {
    public static final String TOOL_NAME = "list_skills";
    private static final ToolDefinition TOOL_DEFINITION = ToolDefinition.builder(TOOL_NAME)
            .description("Lists all available skills for the agent.")
            .inputSchema(ListSkillsInput.class)
            .outputSchema(ListSkillOutput.class)
            .isConcurrencySafe(true)
            .isReadOnly(true)
            .build();
    private static final ToolConverter<ListSkillsInput, ListSkillOutput> TOOL_CONVERTER =
            ToolConverters.jsonConverter(ListSkillsInput.class);

    @Override
    public ToolDefinition getDefinition() {
        return TOOL_DEFINITION;
    }

    @Override
    public ToolConverter<ListSkillsInput, ListSkillOutput> getConverter() {
        return TOOL_CONVERTER;
    }

    @Override
    public ListSkillOutput run(ToolContext toolContext, ListSkillsInput input) throws ToolExecutionException {
        SkillManager skillManager = toolContext.getAgent().getSkillManager();
        List<SkillMetadata> skillMetadata = skillManager.listSkills();

        List<ListSkillOutput.SkillMetadata> skillList = skillMetadata.stream()
                .map(metadata -> new ListSkillOutput.SkillMetadata(
                        metadata.name(), metadata.version(), metadata.description(), metadata.location())
                )
                .toList();
        return new ListSkillOutput(skillList);
    }

}