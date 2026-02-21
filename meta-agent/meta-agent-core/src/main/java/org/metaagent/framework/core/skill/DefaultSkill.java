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

package org.metaagent.framework.core.skill;

import org.apache.commons.lang3.StringUtils;
import org.metaagent.framework.common.io.IOUtils;
import org.metaagent.framework.common.io.MarkdownUtils;
import org.metaagent.framework.core.skill.exception.SkillLoadException;
import org.metaagent.framework.core.skill.loader.SkillLoaders;
import org.metaagent.framework.core.skill.metadata.SkillMetadata;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;

import static org.metaagent.framework.core.skill.loader.SkillLoader.SKILL_FILENAME;

/**
 * Default skill implementation.
 *
 * @author vyckey
 */
public class DefaultSkill implements Skill {
    private final SkillMetadata metadata;
    private String instruction;

    public DefaultSkill(SkillMetadata metadata, String instruction) {
        this.metadata = Objects.requireNonNull(metadata, "metadata cannot be null");
        this.instruction = instruction;
    }

    public DefaultSkill(SkillMetadata metadata) {
        this(metadata, null);
    }

    @Override
    public SkillMetadata getMetadata() {
        return metadata;
    }

    @Override
    public String loadInstruction() throws SkillLoadException {
        if (StringUtils.isNotEmpty(instruction)) {
            return instruction;
        }
        String content = loadContent(SKILL_FILENAME);
        this.instruction = MarkdownUtils.parseFrontMatterAndMainText(content).getRight();
        return this.instruction;
    }

    @Override
    public String loadContent(String relativePath) throws SkillLoadException {
        URL location = SkillLoaders.resolveLocation(metadata.location(), relativePath);
        try {
            if ("file".equalsIgnoreCase(location.getProtocol())) {
                Path filePath = Paths.get(location.toURI()).toAbsolutePath().normalize();
                if (!Files.exists(filePath)) {
                    throw new SkillLoadException("File " + filePath + " not exists");
                }
                if (!Files.isReadable(filePath)) {
                    throw new SkillLoadException("File " + filePath + " is not readable");
                }
            }
            return IOUtils.toString(location, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new SkillLoadException("Failed to read content from URL: " + location, e);
        } catch (URISyntaxException e) {
            throw new SkillLoadException("Invalid file URL: " + location, e);
        }
    }

    @Override
    public String toString() {
        return "SKILL - " + getName() + " (" + getMetadata().version() + ")\n" + getMetadata().description();
    }
}
