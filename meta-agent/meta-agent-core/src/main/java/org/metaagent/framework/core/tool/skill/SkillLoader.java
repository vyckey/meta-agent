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

package org.metaagent.framework.core.tool.skill;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import org.apache.commons.lang3.StringUtils;
import org.metaagent.framework.common.io.MarkdownUtils;

import javax.validation.constraints.NotNull;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.metaagent.framework.core.tool.skill.SkillMetadata.SKILLS_FILENAME;

/**
 * Skill loader
 *
 * @author vyckey
 */
public class SkillLoader {
    private static final SkillLoader instance = new SkillLoader();

    public static SkillLoader getInstance() {
        return instance;
    }

    public record SkillLoadError(
            Path filePath,
            IOException exception) {
    }

    public List<SkillMetadata> loadSkillMetadata(List<Path> filePaths, List<SkillLoadError> errors) {
        List<SkillMetadata> skillMetadataList = new ArrayList<>();
        for (Path filePath : filePaths) {
            skillMetadataList.addAll(loadSkillMetadata(filePath, errors));
        }
        return skillMetadataList;
    }

    public List<SkillMetadata> loadSkillMetadata(Path filePath, List<SkillLoadError> errors) {
        Path rootPath = filePath.toAbsolutePath().normalize();
        List<File> skillFiles;
        try {
            skillFiles = Files.walk(rootPath)
                    .map(Path::toFile)
                    .filter(File::isFile)
                    .filter(file -> file.getName().equalsIgnoreCase(SKILLS_FILENAME))
                    .toList();
        } catch (IOException e) {
            errors.add(new SkillLoadError(rootPath, e));
            return Collections.emptyList();
        }

        List<SkillMetadata> skillMetadataList = new ArrayList<>();
        for (File skillFile : skillFiles) {
            try {
                SkillMetadata skillMetadata = readSkillMetadata(skillFile.toPath());
                skillMetadataList.add(skillMetadata);
            } catch (IOException e) {
                errors.add(new SkillLoadError(rootPath, e));
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
                .name(skillMetadataDO.name())
                .description(skillMetadataDO.description())
                .version(skillMetadataDO.version())
                .filePath(skillFilePath.toAbsolutePath().normalize())
                .setExtendProperties(skillMetadataDO.extendProperties())
                .build();
    }

    record SkillMetadataDO(
            @NotNull(message = "name is required")
            String name,

            @NotNull(message = "description is required")
            String description,

            String version,

            Map<String, Object> extendProperties) {


        SkillMetadataDO(String name, String description, String version, Map<String, Object> extendProperties) {
            this.name = name;
            this.description = description;
            this.version = version;
            this.extendProperties = extendProperties != null ? extendProperties : new HashMap<>();
        }

        @JsonAnySetter
        void setExtendProperty(String key, Object value) {
            this.extendProperties.put(key, value);
        }

    }
}
