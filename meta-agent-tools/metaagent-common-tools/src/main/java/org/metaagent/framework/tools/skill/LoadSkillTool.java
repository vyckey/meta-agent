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
import org.metaagent.framework.core.skill.Skill;
import org.metaagent.framework.core.skill.exception.SkillLoadException;
import org.metaagent.framework.core.skill.loader.SkillLoaders;
import org.metaagent.framework.core.skill.manager.SkillManager;
import org.metaagent.framework.core.tool.Tool;
import org.metaagent.framework.core.tool.ToolContext;
import org.metaagent.framework.core.tool.converter.ToolConverter;
import org.metaagent.framework.core.tool.converter.ToolConverters;
import org.metaagent.framework.core.tool.definition.ToolDefinition;
import org.metaagent.framework.core.tool.exception.ToolExecutionException;
import org.metaagent.framework.core.tool.schema.ToolArgsValidator;

import java.net.URL;
import java.util.Objects;

import static org.metaagent.framework.core.skill.loader.SkillLoader.SKILL_FILENAME;

/**
 * Tool to list all available skills.
 *
 * @author vyckey
 */
@Slf4j
public class LoadSkillTool implements Tool<LoadSkillInput, LoadSkillOutput> {
    public static final String TOOL_NAME = "load_skill";
    private static final ToolDefinition TOOL_DEFINITION = ToolDefinition.builder(TOOL_NAME)
            .description("Lists all available skills for the agent.")
            .inputSchema(LoadSkillInput.class)
            .outputSchema(LoadSkillOutput.class)
            .isConcurrencySafe(true)
            .isReadOnly(true)
            .build();
    private static final ToolConverter<LoadSkillInput, LoadSkillOutput> TOOL_CONVERTER =
            ToolConverters.jsonConverter(LoadSkillInput.class);
    private final SkillManager skillManager;

    public LoadSkillTool(SkillManager skillManager) {
        this.skillManager = Objects.requireNonNull(skillManager, "skillManager cannot be null");
    }

    @Override
    public ToolDefinition getDefinition() {
        return TOOL_DEFINITION;
    }

    @Override
    public ToolConverter<LoadSkillInput, LoadSkillOutput> getConverter() {
        return TOOL_CONVERTER;
    }

    @Override
    public LoadSkillOutput run(ToolContext toolContext, LoadSkillInput input) throws ToolExecutionException {
        ToolArgsValidator.validate(input);

        Skill skill = skillManager.getSkill(input.skillName(), input.skillVersion());

        String relativePath = Objects.requireNonNullElse(input.relativePath(), SKILL_FILENAME);
        if (skill == null) {
            String error = "Skill not found: " + input.skillName()
                    + (input.skillVersion() != null ? " with version " + input.skillVersion() : "");
            return LoadSkillOutput.error(input.skillName(), relativePath, error);
        }
        URL resourceLocation = SkillLoaders.resolveLocation(skill.getMetadata().location(), relativePath);
        try {
            String content;
            if (SKILL_FILENAME.equals(relativePath)) {
                content = skill.loadInstruction();
            } else {
                content = skill.loadContent(relativePath);
            }
            return LoadSkillOutput.from(input.skillName(), resourceLocation.toString(), content);
        } catch (SkillLoadException e) {
            log.error("Error loading skill '{}' content from '{}': {}", input.skillName(), resourceLocation, e.getMessage(), e);
            return LoadSkillOutput.error(input.skillName(), resourceLocation.toString(), e.getMessage());
        }
    }
}