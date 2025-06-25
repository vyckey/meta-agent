package org.metaagent.framework.core.model.prompt;

import com.google.common.io.Files;
import lombok.EqualsAndHashCode;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.ResourceUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

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

    public StringPromptValue from(String value) {
        return new StringPromptValue(value);
    }

    public StringPromptValue fromFile(String fileName) {
        return new StringPromptValue(readFileAsString(fileName));
    }

    static String readFileAsString(String fileName) {
        try {
            File file = ResourceUtils.getFile(fileName);
            return Files.asCharSource(file, StandardCharsets.UTF_8).read();
        } catch (IOException e) {
            throw new IllegalArgumentException("Failed to read prompt file: " + fileName, e);
        }
    }

    @Override
    public String toString() {
        return value;
    }
}
