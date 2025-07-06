package org.metaagent.framework.core.model.prompt.formatter;

import org.slf4j.helpers.MessageFormatter;

import java.util.List;
import java.util.Map;

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
    public String format(String template, Map<String, Object> args) {
        throw new UnsupportedOperationException("Not supported Key-Value formatting in Slf4jStringFormatter");
    }

    @Override
    public List<String> extractVariables(String template) {
        throw new UnsupportedOperationException("Variable extraction is not supported in Slf4jStringFormatter");
    }
}
