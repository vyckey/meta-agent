package org.metaagent.framework.core.model.prompt.formatter;

import java.util.List;
import java.util.Map;

/**
 * String formatter
 *
 * @author vyckey
 */
public interface StringFormatter {

    String name();

    String format(String template, Object... args);

    String format(String template, Map<String, Object> args);

    List<String> extractVariables(String template);
}
