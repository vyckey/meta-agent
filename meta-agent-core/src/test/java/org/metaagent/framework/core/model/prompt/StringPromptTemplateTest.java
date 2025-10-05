package org.metaagent.framework.core.model.prompt;

import org.junit.jupiter.api.Test;
import org.metaagent.framework.common.template.impl.DefaultTemplateRenderer;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for {@link StringPromptTemplate}.
 *
 * @author vyckey
 */
class StringPromptTemplateTest {

    @Test
    void formatTest() {
        StringPromptTemplate template = StringPromptTemplate.from("java", "Hello, %s");
        assertEquals("Hello, world", template.format("world").toString());

        StringPromptTemplate template2 = StringPromptTemplate.from("slf4j", "{}, would you like go vacation with me on {}");
        LocalDate nextMonth = LocalDate.now().plusMonths(1);
        assertEquals("Bob, would you like go vacation with me on " + nextMonth,
                template2.format("Bob", nextMonth).toString());
    }

    @Test
    void formatWithVariableTest() {
        StringPromptTemplate promptTemplate = StringPromptTemplate
                .from("default", "${name}, would you like go vacation with me on ${date}?");
        assertEquals(List.of("name", "date"), promptTemplate.getVariables().orElse(null));
        LocalDate nextMonth = LocalDate.now().plusMonths(1);
        assertEquals("Bob, would you like go vacation with me on " + nextMonth + "?",
                promptTemplate.format("name", "Bob", "date", nextMonth).toString());
    }

    @Test
    void formatWithFileTest() {
        StringPromptTemplate promptTemplate = StringPromptTemplate.fromFile("classpath:prompts/default_prompt_template_test.md");
        assertEquals(List.of("search_results", "question"), promptTemplate.getVariables().orElse(null));
        PromptValue promptValue = promptTemplate.format("search_results", "1. Apple ...\n2. Banana ...", "question", "What is the best fruit?");
        assertTrue(DefaultTemplateRenderer.INSTANCE.extractVariables(promptValue.toString()).isEmpty());
    }
}