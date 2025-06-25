package org.metaagent.framework.core.model.prompt.formatter;

import org.slf4j.helpers.MessageFormatter;

import java.util.List;

/**
 * description is here
 *
 * @author vyckey
 */
public class Slf4jStringFormatter implements StringFormatter {
    @Override
    public String name() {
        return "slf4j";
    }

    @Override
    public String format(String template, Object... args) {
        return MessageFormatter.arrayFormat(template, args).getMessage();
    }

    @Override
    public List<String> extractVariables(String template) {
        throw new UnsupportedOperationException();
    }
}
