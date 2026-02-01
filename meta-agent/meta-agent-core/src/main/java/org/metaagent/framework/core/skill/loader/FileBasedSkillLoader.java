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
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.validator.messageinterpolation.ParameterMessageInterpolator;
import org.metaagent.framework.common.io.MarkdownUtils;
import org.metaagent.framework.core.skill.DefaultSkill;
import org.metaagent.framework.core.skill.Skill;
import org.metaagent.framework.core.skill.exception.SkillLoadException;
import org.metaagent.framework.core.skill.metadata.DefaultSkillMetadata;
import org.metaagent.framework.core.skill.metadata.SkillMetadata;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

/**
 * FileBasedSkillLoader is a SkillLoader that loads skills from files.
 *
 * @author vyckey
 */
@Slf4j
public class FileBasedSkillLoader implements SkillLoader {
    static final Validator VALIDATOR;

    static {
        try (ValidatorFactory factory = Validation.byDefaultProvider()
                .configure()
                .messageInterpolator(new ParameterMessageInterpolator())
                .buildValidatorFactory()) {
            VALIDATOR = factory.getValidator();
        }
    }

    @Override
    public boolean supports(URL location) {
        return location.getProtocol().equalsIgnoreCase("file");
    }

    @Override
    public List<Skill> load(URL location, List<SkillLoadError> errors) {
        Path filePath;
        try {
            filePath = Paths.get(location.toURI()).toAbsolutePath().normalize();
        } catch (URISyntaxException e) {
            errors.add(new SkillLoadError(location, new SkillLoadException("Invalid file URL: " + location, e)));
            return Collections.emptyList();
        }

        List<SkillMetadata> skillMetadataList = loadSkillMetadata(filePath, errors);
        return skillMetadataList.stream()
                .map(DefaultSkill::new)
                .map(skill -> (Skill) skill)
                .toList();
    }

    public List<SkillMetadata> loadSkillMetadata(Path filePath, List<SkillLoadError> errors) {
        if (Files.isSymbolicLink(filePath)) {
            try {
                filePath = filePath.toRealPath();
            } catch (IOException e) {
                errors.add(SkillLoadError.from(filePath, new SkillLoadException("Cannot convert symbolic link path: " + filePath, e)));
                return Collections.emptyList();
            }
        }
        Path rootPath = filePath.toAbsolutePath().normalize();
        if (!Files.exists(rootPath)) {
            return Collections.emptyList();
        }

        List<File> skillFiles;
        try (Stream<Path> pathStream = Files.walk(rootPath)) {
            skillFiles = pathStream
                    .filter(Files::isRegularFile)
                    .filter(path -> SKILL_FILENAME.equals(path.getFileName().toString()))
                    .map(Path::toFile)
                    .toList();
        } catch (IOException e) {
            SkillLoadException pe = new SkillLoadException("Failed to walk directory tree: " + filePath + ", reason: " + e, e);
            errors.add(SkillLoadError.from(filePath, pe));
            return Collections.emptyList();
        }

        List<SkillMetadata> skillMetadataList = new ArrayList<>();
        for (File skillFile : skillFiles) {
            try {
                SkillMetadata skillMetadata = readSkillMetadata(skillFile.toPath());
                skillMetadataList.add(skillMetadata);
            } catch (IOException e) {
                SkillLoadException pe = new SkillLoadException("Failed to read the skill metadata from file: " + filePath);
                errors.add(SkillLoadError.from(filePath, pe));
            } catch (SkillLoadException e) {
                errors.add(SkillLoadError.from(filePath, e));
            }
        }
        return skillMetadataList;
    }

    public SkillMetadata readSkillMetadata(Path skillFilePath) throws IOException {
        if (!Files.isReadable(skillFilePath)) {
            throw new SkillLoadException("The file " + skillFilePath + " is not readable");
        }
        String frontMatter = MarkdownUtils.readFrontMatter(skillFilePath);
        if (StringUtils.isEmpty(frontMatter)) {
            throw new SkillLoadException("The front-matter content isn't exists in file " + skillFilePath);
        }
        SkillMetadataDO skillMetadataDO = MarkdownUtils.parseFrontMatter(frontMatter, SkillMetadataDO.class);
        Set<ConstraintViolation<SkillMetadataDO>> violations = VALIDATOR.validate(skillMetadataDO);
        if (!violations.isEmpty()) {
            StringBuilder errorMessage = new StringBuilder();
            for (ConstraintViolation<SkillMetadataDO> violation : violations) {
                errorMessage.append(violation.getMessage()).append("\n");
            }
            throw new SkillLoadException("Skill metadata validation failed for file " + skillFilePath + ": " + errorMessage.toString().trim());
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
            @NotBlank(message = "name is required")
            @JsonProperty(required = true)
            String name,

            @NotEmpty(message = "description is required")
            @JsonProperty(required = true)
            String description,

            @Pattern(regexp = "^\\d+\\.\\d+\\.\\d+$", message = "version must follow semantic versioning (x.y.z)")
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
