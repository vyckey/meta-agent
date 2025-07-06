/*
 * MIT License
 *
 * Copyright (c) 2025 MetaAgent
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package org.metaagent.framework.core.model.prompt.registry;

import org.junit.jupiter.api.Test;
import org.metaagent.framework.core.model.prompt.PromptTemplate;
import org.metaagent.framework.core.model.prompt.PromptValue;
import org.metaagent.framework.core.model.prompt.StringPromptTemplate;
import org.metaagent.framework.core.model.prompt.StringPromptValue;

import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PromptRegistryTest {
    @Test
    void testGetPrompt() {
        PromptRegistry registry = PromptRegistry.global();
        assertFalse(registry.hasPromptTemplate("hello_prompt"));
        registry.registerPrompt("hello_prompt", StringPromptValue.from("How are you?"));
        assertTrue(registry.hasPrompt("hello_prompt"));
        assertNotNull(registry.getPrompt("hello_prompt"));
        assertNotNull(registry.getPrompt("hello_prompt", StringPromptValue.class));

        registry.unregisterPrompt("hello_prompt");
    }

    @Test
    void testRegisterPrompt() {
        PromptRegistry registry = PromptRegistry.global();
        registry.registerPrompt("river_prompt", StringPromptValue.from("How long is the river?"));
        assertThrows(IllegalStateException.class, () ->
                registry.registerPrompt("river_prompt", StringPromptValue.from("How long is the river?"))
        );
        assertNotNull(registry.unregisterPrompt("river_prompt"));
        assertThrows(IllegalStateException.class, () -> registry.getPrompt("river_prompt"));
    }

    @Test
    void testReplacePrompt() {
        PromptRegistry registry = PromptRegistry.global();
        StringPromptValue prompt = StringPromptValue.from("What is your age?");
        registry.registerPrompt("age_prompt", prompt);
        assertNotNull(registry.getPrompt("age_prompt"));
        PromptValue replaced = registry.replacePrompt("age_prompt", StringPromptValue.from("How old are you?"));
        assertNotNull(replaced);
        assertEquals(prompt, replaced);

        assertNotNull(registry.getPrompt("age_prompt"));
    }

    @Test
    void testGetPromptTemplate() {
        PromptRegistry registry = PromptRegistry.global();
        assertFalse(registry.hasPromptTemplate("greet_template"));
        PromptTemplate template = StringPromptTemplate.from("${name}, how are you?");
        registry.registerPromptTemplate("greet_template", template);
        assertTrue(registry.hasPromptTemplate("greet_template"));
        assertNotNull(registry.getPromptTemplate("greet_template"));
        assertNotNull(registry.getPromptTemplate("greet_template", StringPromptTemplate.class));

        registry.unregisterPromptTemplate("greet_template");
    }

    @Test
    void testRegisterPromptTemplate() {
        PromptRegistry registry = PromptRegistry.global();
        PromptTemplate template = StringPromptTemplate.from("${name}, welcome to the system!");
        registry.registerPromptTemplate("welcome_template", template);
        assertThrows(IllegalStateException.class, () ->
                registry.registerPromptTemplate("welcome_template", template)
        );
        assertNotNull(registry.unregisterPromptTemplate("welcome_template"));
        assertThrows(IllegalStateException.class, () -> registry.getPromptTemplate("welcome_template"));
    }

    @Test
    void testReplacePromptTemplate() {
        PromptRegistry registry = PromptRegistry.global();
        PromptTemplate oldTemplate = StringPromptTemplate.from("Which city do you live in?");
        registry.registerPromptTemplate("city_template", oldTemplate);
        assertNotNull(registry.getPromptTemplate("city_template"));
        PromptTemplate replaced = registry.replacePromptTemplate("city_template", StringPromptTemplate.from("What is your city?"));
        assertNotNull(replaced);

        registry.unregisterPromptTemplate("city_template");
    }

}