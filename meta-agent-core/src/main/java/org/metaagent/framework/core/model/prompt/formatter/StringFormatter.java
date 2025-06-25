package org.metaagent.framework.core.model.prompt.formatter;

import java.util.List;

/**
 * String formatter
 *
 * @author vyckey
 */
public interface StringFormatter {

    String name();

    String format(String template, Object... args);

    List<String> extractVariables(String template);
}
