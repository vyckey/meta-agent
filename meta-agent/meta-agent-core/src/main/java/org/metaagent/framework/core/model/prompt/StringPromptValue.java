package org.metaagent.framework.core.model.prompt;

import lombok.EqualsAndHashCode;
import org.apache.commons.lang3.StringUtils;
import org.metaagent.framework.common.io.IOUtils;

import java.io.IOException;

/**
 * String format prompt value.
 *
 * @author vyckey
 */
@EqualsAndHashCode
public final class StringPromptValue implements PromptValue {
    private final String value;

    StringPromptValue(String value) {
        if (StringUtils.isEmpty(value)) {
            throw new PromptFormatException("prompt value is empty");
        }
        this.value = value;
    }

    public static StringPromptValue from(String value) {
        return new StringPromptValue(value);
    }

    public static StringPromptValue fromFile(String fileName) {
        String prompt;
        try {
            prompt = IOUtils.readToString(fileName);
        } catch (IOException e) {
            throw new IllegalArgumentException("Failed to read prompt from file: " + fileName, e);
        }
        return new StringPromptValue(prompt);
    }

    @Override
    public String toString() {
        return value;
    }
}
