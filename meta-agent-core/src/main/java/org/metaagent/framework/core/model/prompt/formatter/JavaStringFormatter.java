package org.metaagent.framework.core.model.prompt.formatter;

import java.util.List;

/**
 * description is here
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
    public List<String> extractVariables(String template) {
        throw new UnsupportedOperationException();
    }
}
