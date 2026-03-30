package org.metaagent.framework.core.model.prompt;

import java.util.Map;

/**
 * Prompt template interface.
 *
 * @author vyckey
 */
public non-sealed interface PromptTemplate extends Prompt {
    /**
     * Format the prompt template with the given arguments.
     *
     * @param args the arguments to format the prompt template with
     * @return the formatted prompt
     */
    PromptValue format(Object... args);

    /**
     * Format the prompt template with the given arguments.
     *
     * @param args the arguments to format the prompt template with
     * @return the formatted prompt
     */
    PromptValue format(Map<String, Object> args);
}
