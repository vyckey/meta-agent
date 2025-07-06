package org.metaagent.framework.core.model.prompt;

/**
 * Prompt format exception.
 *
 * @author vyckey
 */
public class PromptFormatException extends RuntimeException {
    public PromptFormatException(String message) {
        super(message);
    }

    public PromptFormatException(String message, Throwable cause) {
        super(message, cause);
    }
}