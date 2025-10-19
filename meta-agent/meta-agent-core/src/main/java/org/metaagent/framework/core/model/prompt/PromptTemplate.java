package org.metaagent.framework.core.model.prompt;

import java.util.Map;

/**
 * Prompt template.
 *
 * @author vyckey
 */
public interface PromptTemplate {
    PromptValue format(Object... args);

    PromptValue format(Map<String, Object> args);
}
