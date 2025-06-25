package org.metaagent.framework.core.model.prompt;

import lombok.Getter;
import org.metaagent.framework.core.model.prompt.formatter.StringFormatter;
import org.metaagent.framework.core.model.prompt.formatter.StringFormatterManager;

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

    public StringPromptTemplate fromFile(String formatterName, String fileName) {
        String content = StringPromptValue.readFileAsString(fileName);
        return from(formatterName, content);
    }

    public StringPromptTemplate fromFile(String fileName) {
        String content = StringPromptValue.readFileAsString(fileName);
        String formatterName = StringFormatterManager.getDefaultFormatter().name();
        return from(formatterName, content);
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
        if (variables.isPresent()) {
            Object[] argsArr = variables.get().stream().map(args::get).toArray();
            return format(argsArr);
        }
        throw new UnsupportedOperationException("Variables extraction is not supported by the formatter");
    }

    @Override
    public String toString() {
        String variableNames = variables.map(vars -> String.join(", ", vars)).orElse("<unknown>");
        return "Formatter: " + stringFormatter.name() + "\nVariables: " + variableNames + "\nTemplate: " + template;
    }
}
