package org.metaagent.framework.core.model.prompt.formatter;

import org.metaagent.framework.core.model.prompt.PromptFormatException;

import java.util.List;
import java.util.Map;

/**
 * JavaStringFormatter is a StringFormatter implementation that uses Java's built-in String formatting.
 *
 * @author vyckey
 */
public final class JavaStringFormatter implements StringFormatter {
    @Override
    public String name() {
        return "java";
    }

    @Override
    public String format(String template, Object... args) {
        return template.formatted(args);
    }

    @Override
    public String format(String template, Map<String, Object> args) {
        throw new PromptFormatException("Not supported Key-Value formatting in JavaStringFormatter");
    }

    @Override
    public List<String> extractVariables(String template) {
        throw new UnsupportedOperationException("Variable extraction is not supported in JavaStringFormatter");
    }
}
