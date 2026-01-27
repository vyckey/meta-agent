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

package org.metaagent.framework.core.skill.loader;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.metaagent.framework.common.io.MarkdownUtils;
import org.metaagent.framework.core.skill.DefaultSkill;
import org.metaagent.framework.core.skill.Skill;
import org.metaagent.framework.core.skill.exception.SkillParseException;
import org.metaagent.framework.core.skill.metadata.DefaultSkillMetadata;
import org.metaagent.framework.core.skill.metadata.SkillMetadata;

import javax.validation.constraints.NotNull;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

/**
 * FileBasedSkillLoader is a SkillLoader that loads skills from files.
 *
 * @author vyckey
 */
@Slf4j
public class FileBasedSkillLoader implements SkillLoader {

    @Override
    public boolean supports(URL location) {
        return location.getProtocol().equalsIgnoreCase("file");
    }

    @Override
    public List<Skill> load(URL location, List<SkillLoadError> errors) throws SkillParseException {
        Path filePath = Path.of(location.getPath());
        List<SkillMetadata> skillMetadataList = loadSkillMetadata(filePath, errors);
        return skillMetadataList.stream()
                .map(DefaultSkill::new)
                .map(skill -> (Skill) skill)
                .toList();
    }

    public List<SkillMetadata> loadSkillMetadata(Path filePath, List<SkillLoadError> errors) {
        Path rootPath = filePath.toAbsolutePath().normalize();
        List<File> skillFiles;
        try (Stream<Path> pathStream = Files.walk(rootPath)) {
            skillFiles = pathStream
                    .filter(Files::isRegularFile)
                    .filter(path -> SKILL_FILENAME.equalsIgnoreCase(path.getFileName().toString()))
                    .map(Path::toFile)
                    .toList();
        } catch (IOException e) {
            SkillParseException pe = new SkillParseException("Failed to read the skills file: " + filePath);
            errors.add(SkillLoadError.from(filePath, pe));
            return Collections.emptyList();
        }

        List<SkillMetadata> skillMetadataList = new ArrayList<>();
        for (File skillFile : skillFiles) {
            try {
                SkillMetadata skillMetadata = readSkillMetadata(skillFile.toPath());
                skillMetadataList.add(skillMetadata);
            } catch (IOException e) {
                SkillParseException pe = new SkillParseException("Failed to read the skill metadata from file: " + filePath);
                errors.add(SkillLoadError.from(filePath, pe));
            } catch (SkillParseException e) {
                errors.add(SkillLoadError.from(filePath, e));
            }
        }
        return skillMetadataList;
    }

    public SkillMetadata readSkillMetadata(Path skillFilePath) throws IOException {
        String frontMatter = MarkdownUtils.readFrontMatter(skillFilePath);
        if (StringUtils.isEmpty(frontMatter)) {
            throw new SkillParseException("The front-matter content isn't exists in file " + skillFilePath);
        }
        SkillMetadataDO skillMetadataDO = MarkdownUtils.parseFrontMatter(frontMatter, SkillMetadataDO.class);
        if (StringUtils.isEmpty(skillMetadataDO.name()) || StringUtils.isEmpty(skillMetadataDO.description())) {
            throw new SkillParseException("The name or description of skill is missing in file " + skillFilePath);
        }

        return DefaultSkillMetadata.builder()
                .name(skillMetadataDO.name().trim())
                .description(skillMetadataDO.description().trim())
                .version(StringUtils.isNotBlank(skillMetadataDO.version()) ? skillMetadataDO.version().trim() : "1.0.0")
                .allowedTools(skillMetadataDO.allowedTools())
                .filePath(skillFilePath.getParent().toAbsolutePath().normalize())
                .setExtendProperties(skillMetadataDO.extendProperties())
                .build();
    }

    record SkillMetadataDO(
            @NotNull(message = "name is required")
            String name,

            @NotNull(message = "description is required")
            String description,

            String version,

            @JsonProperty("allowedTools")
            List<String> allowedTools,

            Map<String, Object> extendProperties) {


        SkillMetadataDO(String name, String description, String version, List<String> allowedTools, Map<String, Object> extendProperties) {
            this.name = name;
            this.description = description;
            this.version = version;
            this.allowedTools = allowedTools;
            this.extendProperties = extendProperties != null ? extendProperties : new HashMap<>();
        }

        @JsonAnySetter
        void setExtendProperty(String key, Object value) {
            this.extendProperties.put(key, value);
        }

    }
}
