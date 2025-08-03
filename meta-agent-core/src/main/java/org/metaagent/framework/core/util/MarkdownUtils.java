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

package org.metaagent.framework.core.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import org.apache.commons.lang3.StringUtils;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MarkdownUtils {
    private static final YAMLMapper YAML_MAPPER = new YAMLMapper();
    private static final Pattern FRONT_MATTER_PATTERN = Pattern.compile("^---\\n(.*?)\\n^---\\n", Pattern.MULTILINE);

    private MarkdownUtils() {
    }

    public static String parseFrontMatter(String markdownText) {
        Matcher matcher = FRONT_MATTER_PATTERN.matcher(markdownText);
        if (matcher.find()) {
            String frontMatter = matcher.group(1);
            return frontMatter.trim();
        }
        return "";
    }

    public static Map<String, Object> parseMetadata(String markdownText) {
        Map<String, Object> metadata = parseMetadata(markdownText, new TypeReference<>() {
        });
        return metadata != null ? metadata : Map.of();
    }

    public static <T> T parseMetadata(String markdownText, Class<T> metadataClass) {
        String frontMatter = parseFrontMatter(markdownText);
        if (StringUtils.isEmpty(frontMatter)) {
            return null;
        }
        try {
            return YAML_MAPPER.readValue(markdownText, metadataClass);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Failed to parse markdown metadata", e);
        }
    }

    public static <T> T parseMetadata(String markdownText, TypeReference<T> typeReference) {
        String frontMatter = parseFrontMatter(markdownText);
        if (StringUtils.isEmpty(frontMatter)) {
            return null;
        }
        try {
            return YAML_MAPPER.readValue(markdownText, typeReference);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Failed to parse markdown metadata", e);
        }
    }

    public static String removeFrontMatter(String markdownText) {
        return FRONT_MATTER_PATTERN.matcher(markdownText).replaceAll("");
    }
}
