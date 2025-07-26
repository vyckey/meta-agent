package org.metaagent.framework.core.model.prompt;

import lombok.Getter;
import org.apache.commons.collections.MapUtils;
import org.metaagent.framework.core.model.prompt.formatter.StringFormatter;
import org.metaagent.framework.core.model.prompt.formatter.StringFormatterManager;
import org.metaagent.framework.core.util.IOUtils;
import org.metaagent.framework.core.util.MarkdownUtils;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * description is here
 *
 * @author vyckey
 */
@Getter
public class StringPromptTemplate implements PromptTemplate {
    private final StringFormatter stringFormatter;
    private final String template;
    private final Optional<List<String>> variables;

    public StringPromptTemplate(StringFormatter stringFormatter, String template, List<String> variables) {
        this.stringFormatter = Objects.requireNonNull(stringFormatter, "formatter must not be null");
        this.template = Objects.requireNonNull(template, "template must not be null");
        this.variables = Optional.ofNullable(variables);
    }

    public static StringPromptTemplate from(StringFormatter stringFormatter, String template) {
        List<String> variables;
        try {
            variables = stringFormatter.extractVariables(template);
        } catch (UnsupportedOperationException e) {
            variables = null;
        }
        return new StringPromptTemplate(stringFormatter, template, variables);
    }

    public static StringPromptTemplate from(String formatterName, String template) {
        StringFormatter formatter = StringFormatterManager.getInstance().getFormatter(formatterName);
        Objects.requireNonNull(formatter, "not found formatter " + formatterName);
        return from(formatter, template);
    }

    public static StringPromptTemplate from(String template) {
        return from(StringFormatterManager.getDefaultFormatter(), template);
    }

    public static StringPromptTemplate fromFile(String formatterName, String fileName) {
        String template;
        try {
            template = IOUtils.readToString(fileName);
        } catch (IOException e) {
            throw new IllegalArgumentException("Failed to read prompt template from file: " + fileName, e);
        }
        return from(formatterName, template);
    }

    /**
     * Creates a prompt template from a file.
     * If the file is a markdown file, it will try to extract the formatter from the front matter.
     * If no formatter is specified, it will use the default formatter.
     * This allows for more flexible prompt templates that can specify their own formatter.
     * If the file is not a markdown file, it will use the default formatter.
     *
     * @param fileName the prompt template file name, can be a classpath resource or a file path.
     * @return a StringPromptTemplate instance
     * @throws IllegalArgumentException if the file cannot be read or the formatter is not found
     */
    public static StringPromptTemplate fromFile(String fileName) {
        String formatterName = StringFormatterManager.getDefaultFormatter().name();
        if (!fileName.endsWith(".md") && !fileName.endsWith(".mdx")) {
            return fromFile(formatterName, fileName);
        }

        String template;
        try {
            template = IOUtils.readToString(fileName);
        } catch (IOException e) {
            throw new IllegalArgumentException("Failed to read prompt template from file: " + fileName, e);
        }
        String formatter = MapUtils.getString(MarkdownUtils.parseMetadata(template), "formatter");
        template = MarkdownUtils.removeFrontMatter(template);
        return from(Optional.ofNullable(formatter).orElse(formatterName), template);
    }

    @Override
    public PromptValue format(Object... args) {
        try {
            String value = stringFormatter.format(template, args);
            return PromptValue.from(value);
        } catch (Exception e) {
            throw new PromptFormatException("Failed to format prompt", e);
        }
    }

    @Override
    public PromptValue format(Map<String, Object> args) {
        try {
            String value = stringFormatter.format(template, args);
            return PromptValue.from(value);
        } catch (Exception e) {
            throw new PromptFormatException("Failed to format prompt", e);
        }
    }

    @Override
    public String toString() {
        String variableNames = variables.map(vars -> String.join(", ", vars)).orElse("<unknown>");
        return "Formatter: " + stringFormatter.name() + "\nVariables: " + variableNames + "\nTemplate: " + template;
    }
}
