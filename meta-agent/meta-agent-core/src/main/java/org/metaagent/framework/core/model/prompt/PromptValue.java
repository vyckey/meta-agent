package org.metaagent.framework.core.model.prompt;

import java.io.Serializable;

/**
 * Prompt value.
 *
 * @author vyckey
 */
public interface PromptValue extends Serializable {
    @Override
    String toString();

    static PromptValue from(String string) {
        return new StringPromptValue(string);
    }
}
