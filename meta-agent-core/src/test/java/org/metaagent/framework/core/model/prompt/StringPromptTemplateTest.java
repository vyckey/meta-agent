package org.metaagent.framework.core.model.prompt;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * description is here
 *
 * @author vyckey
 */
class StringPromptTemplateTest {

    @org.junit.jupiter.api.Test
    void formatTest() {
        StringPromptTemplate template = StringPromptTemplate.from("java", "Hello, %s");
        assertEquals("Hello, world", template.format("world").toString());

        StringPromptTemplate template2 = StringPromptTemplate.from("slf4j", "{}, would you like go vacation with me on {}");
        LocalDate nextMonth = LocalDate.now().plusMonths(1);
        assertEquals("Bob, would you like go vacation with me on " + nextMonth,
                template2.format("Bob", nextMonth).toString());
    }
}