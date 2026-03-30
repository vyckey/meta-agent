package org.metaagent.framework.core.model.prompt;

import java.io.Serializable;

/**
 * Prompt value interface.
 *
 * @author vyckey
 */
public non-sealed interface PromptValue extends Prompt, Serializable {
    /**
     * Get prompt value as text.
     *
     * @return prompt value as text
     */
    String text();

    /**
     * Create prompt value from string.
     *
     * @param string string
     * @return prompt value
     */
    static PromptValue from(String string) {
        return new StringPromptValue(string);
    }
}
