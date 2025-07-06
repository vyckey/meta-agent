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

import org.metaagent.framework.core.model.prompt.PromptTemplate;
import org.metaagent.framework.core.model.prompt.PromptValue;

import java.util.function.Supplier;

/**
 * PromptRegistry is an interface for managing prompts and prompt templates.
 * It provides methods to retrieve, register, and replace prompts and templates by their names.
 * This interface allows for flexible management of prompts and templates, enabling dynamic updates and retrievals.
 *
 * @author vyckey
 * @see GlobalPromptRegistry
 */
public interface PromptRegistry {
    /**
     * Returns the global instance of PromptRegistry.
     *
     * @return the global PromptRegistry instance
     */
    static PromptRegistry global() {
        return GlobalPromptRegistry.getInstance();
    }

    /**
     * Checks if a prompt with the specified name exists.
     *
     * @param name the name of the prompt
     * @return true if the prompt exists, false otherwise
     */
    boolean hasPrompt(String name);

    /**
     * Retrieves a prompt by its name.
     *
     * @param name the name of the prompt
     * @return the prompt value
     * @throws IllegalStateException if the prompt with the specified name does not exist
     */
    default PromptValue getPrompt(String name) {
        return getPrompt(name, PromptValue.class);
    }

    /**
     * Retrieves a prompt by its name and type.
     *
     * @param name       the name of the prompt
     * @param promptType the class type of the prompt
     * @param <P>        the type of the prompt
     * @return the prompt value of the specified type
     * @throws IllegalStateException if the prompt with the specified name does not exist or cannot be cast to the specified type
     */
    <P extends PromptValue> P getPrompt(String name, Class<P> promptType);

    /**
     * Retrieves a prompt by its name, or registers it using the provided supplier if it does not exist.
     *
     * @param name           the name of the prompt
     * @param promptProvider a provider that creates the prompt value if it does not exist
     * @return the prompt value, either retrieved or newly created
     */
    default PromptValue getPromptOrRegister(String name, Supplier<PromptValue> promptProvider) {
        if (!hasPrompt(name)) {
            registerPrompt(name, promptProvider.get());
        }
        return getPrompt(name);
    }

    /**
     * Retrieves a prompt by its name and type, or registers it using the provided supplier if it does not exist.
     *
     * @param name           the name of the prompt
     * @param promptType     the class type of the prompt
     * @param promptProvider a provider that creates the prompt value if it does not exist
     * @param <P>            the type of the prompt
     * @return the prompt value of the specified type, either retrieved or newly created
     * @throws IllegalStateException if the prompt with the specified name does not exist or cannot be cast to the specified type
     */
    default <P extends PromptValue> P getPromptOrRegister(String name, Class<P> promptType, Supplier<P> promptProvider) {
        if (!hasPrompt(name)) {
            registerPrompt(name, promptProvider.get());
        }
        return getPrompt(name, promptType);
    }

    /**
     * Registers a prompt with the specified name.
     *
     * @param name   the name of the prompt
     * @param prompt the prompt value to register
     * @throws IllegalStateException if a prompt with the same name already exists
     */
    void registerPrompt(String name, PromptValue prompt);

    /**
     * Replaces an existing prompt with a new one.
     *
     * @param name      the name of the prompt to replace
     * @param newPrompt the new prompt value to replace the existing one
     * @return the replaced prompt value
     * @throws IllegalStateException if the prompt with the specified name does not exist
     */
    PromptValue replacePrompt(String name, PromptValue newPrompt);

    /**
     * Unregisters a prompt by its name.
     *
     * @param name the name of the prompt to unregister
     * @return the unregistered prompt value
     * @throws IllegalStateException if the prompt with the specified name does not exist
     */
    PromptValue unregisterPrompt(String name);

    /**
     * Checks if a prompt template with the specified name exists.
     *
     * @param name the name of the prompt template
     * @return true if the prompt template exists, false otherwise
     */
    boolean hasPromptTemplate(String name);

    /**
     * Retrieves a prompt template by its name.
     *
     * @param name the name of the prompt template
     * @return the prompt template
     * @throws IllegalStateException if the prompt template with the specified name does not exist
     */
    default PromptTemplate getPromptTemplate(String name) {
        return getPromptTemplate(name, PromptTemplate.class);
    }

    /**
     * Retrieves a prompt template by its name and type.
     *
     * @param name               the name of the prompt template
     * @param promptTemplateType the class type of the prompt template
     * @param <P>                the type of the prompt template
     * @return the prompt template of the specified type
     * @throws IllegalStateException if the prompt template with the specified name does not exist or cannot be cast to the specified type
     */
    <P extends PromptTemplate> P getPromptTemplate(String name, Class<P> promptTemplateType);

    /**
     * Retrieves a prompt template by its name, or registers it using the provided supplier if it does not exist.
     *
     * @param name                   the name of the prompt template
     * @param promptTemplateProvider a provider that creates the prompt template if it does not exist
     * @return the prompt template, either retrieved or newly created
     */
    default PromptTemplate getPromptTemplateOrRegister(String name, Supplier<PromptTemplate> promptTemplateProvider) {
        if (!hasPromptTemplate(name)) {
            registerPromptTemplate(name, promptTemplateProvider.get());
        }
        return getPromptTemplate(name);
    }

    /**
     * Retrieves a prompt template by its name, or registers it using the provided supplier if it does not exist.
     *
     * @param name                   the name of the prompt template
     * @param promptTemplateProvider a provider that creates the prompt template if it does not exist
     * @param <P>                    the type of the prompt template
     * @return the prompt template, either retrieved or newly created
     * @throws IllegalStateException if the prompt template with the specified name does not exist or cannot be cast to the specified type
     */
    default <P extends PromptTemplate> P getPromptTemplateOrRegister(String name, Class<P> promptTemplateType,
                                                                     Supplier<P> promptTemplateProvider) {
        if (!hasPromptTemplate(name)) {
            registerPromptTemplate(name, promptTemplateProvider.get());
        }
        return getPromptTemplate(name, promptTemplateType);
    }

    /**
     * Registers a prompt template with the specified name.
     *
     * @param name           the name of the prompt template
     * @param promptTemplate the prompt template to register
     * @throws IllegalStateException if a prompt template with the same name already exists
     */
    void registerPromptTemplate(String name, PromptTemplate promptTemplate);

    /**
     * Replaces an existing prompt template with a new one.
     *
     * @param name              the name of the prompt template to replace
     * @param newPromptTemplate the new prompt template to replace the existing one
     * @return the replaced prompt template
     * @throws IllegalStateException if the prompt template with the specified name does not exist
     */
    PromptTemplate replacePromptTemplate(String name, PromptTemplate newPromptTemplate);

    /**
     * Unregisters a prompt template by its name.
     *
     * @param name the name of the prompt template to unregister
     * @return the unregistered prompt template
     * @throws IllegalStateException if the prompt template with the specified name does not exist
     */
    PromptTemplate unregisterPromptTemplate(String name);
}
