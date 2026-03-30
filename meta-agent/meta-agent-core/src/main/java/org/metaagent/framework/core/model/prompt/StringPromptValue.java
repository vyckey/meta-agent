package org.metaagent.framework.core.model.prompt;

import org.apache.commons.lang3.StringUtils;
import org.metaagent.framework.common.io.IOUtils;

import java.io.IOException;
import java.util.Objects;

/**
 * String format prompt value.
 *
 * @author vyckey
 */
public record StringPromptValue(String value) implements PromptValue {

    public StringPromptValue {
        if (StringUtils.isEmpty(value)) {
            throw new PromptFormatException("prompt value is empty");
        }
    }

    public static StringPromptValue from(String value) {
        return new StringPromptValue(value);
    }

    public static StringPromptValue fromFile(String fileName) {
        String prompt;
        try {
            Objects.requireNonNull(fileName, "fileName is null");
            prompt = IOUtils.readToString(fileName);
        } catch (IOException e) {
            throw new IllegalArgumentException("Failed to read prompt from file: " + fileName, e);
        }
        return new StringPromptValue(prompt);
    }

    @Override
    public String text() {
        return value;
    }
}
