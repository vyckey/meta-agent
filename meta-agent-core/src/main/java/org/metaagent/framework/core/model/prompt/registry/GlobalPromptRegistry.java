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

import org.apache.commons.lang3.StringUtils;
import org.metaagent.framework.core.model.prompt.PromptTemplate;
import org.metaagent.framework.core.model.prompt.PromptValue;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * GlobalPromptRegistry is a default implementation of PromptRegistry that does not store any prompts.
 * It serves as a placeholder for cases where no prompts are registered.
 *
 * @author vyckey
 */
public final class GlobalPromptRegistry implements PromptRegistry {
    private static final GlobalPromptRegistry INSTANCE = new GlobalPromptRegistry();
    private final Map<String, PromptValue> promptMap = new HashMap<>();
    private final Map<String, PromptTemplate> templateMap = new HashMap<>();

    private GlobalPromptRegistry() {
    }

    public static GlobalPromptRegistry getInstance() {
        return INSTANCE;
    }

    @Override
    public boolean hasPrompt(String name) {
        return promptMap.containsKey(name);
    }

    @Override
    public <P extends PromptValue> P getPrompt(String name, Class<P> promptType) {
        Objects.requireNonNull(name, "Prompt name cannot be null");
        PromptValue promptValue = promptMap.get(name);
        if (promptValue == null) {
            throw new IllegalStateException(String.format("Prompt '%s' does not exist", name));
        }
        if (promptType.isInstance(promptValue)) {
            return promptType.cast(promptValue);
        }
        throw new ClassCastException(String.format("Prompt '%s' is not of type %s", name, promptType.getName()));
    }

    @Override
    public void registerPrompt(String name, PromptValue prompt) {
        if (StringUtils.isBlank(name)) {
            throw new IllegalArgumentException("Prompt name cannot be null or empty");
        }
        Objects.requireNonNull(prompt, "Prompt cannot be null");
        if (promptMap.putIfAbsent(name, prompt) != null) {
            throw new IllegalStateException(String.format("Prompt '%s' is already registered, please use replace operation.", name));
        }
    }

    @Override
    public PromptValue replacePrompt(String name, PromptValue newPrompt) {
        Objects.requireNonNull(newPrompt, "Prompt cannot be null");
        PromptValue oldPrompt = promptMap.replace(name, newPrompt);
        if (oldPrompt == null) {
            throw new IllegalStateException(String.format("Prompt '%s' does not exist, please use register operation.", name));
        }
        return oldPrompt;
    }

    @Override
    public PromptValue unregisterPrompt(String name) {
        PromptValue removedPrompt = promptMap.remove(name);
        if (removedPrompt == null) {
            throw new IllegalStateException(String.format("Prompt '%s' does not exist", name));
        }
        return removedPrompt;
    }

    @Override
    public boolean hasPromptTemplate(String name) {
        return templateMap.containsKey(name);
    }

    @Override
    public <P extends PromptTemplate> P getPromptTemplate(String name, Class<P> promptTemplateType) {
        Objects.requireNonNull(name, "Prompt template name cannot be null");
        PromptTemplate promptTemplate = templateMap.get(name);
        if (promptTemplate == null) {
            throw new IllegalStateException(String.format("Prompt template '%s' does not exist", name));
        }
        if (promptTemplateType.isInstance(promptTemplate)) {
            return promptTemplateType.cast(promptTemplate);
        }
        throw new ClassCastException(String.format("Prompt template '%s' is not of type %s", name, promptTemplateType.getName()));
    }

    @Override
    public void registerPromptTemplate(String name, PromptTemplate promptTemplate) {
        if (StringUtils.isBlank(name)) {
            throw new IllegalArgumentException("Prompt template name cannot be null or empty");
        }
        Objects.requireNonNull(promptTemplate, "Prompt template cannot be null");
        if (templateMap.putIfAbsent(name, promptTemplate) != null) {
            throw new IllegalStateException(String.format("Prompt template '%s' is already registered, please use replace operation.", name));
        }
    }

    @Override
    public PromptTemplate replacePromptTemplate(String name, PromptTemplate newPromptTemplate) {
        Objects.requireNonNull(newPromptTemplate, "Prompt template cannot be null");
        PromptTemplate oldPromptTemplate = templateMap.replace(name, newPromptTemplate);
        if (oldPromptTemplate == null) {
            throw new IllegalStateException(String.format("Prompt template '%s' does not exist, please use register operation.", name));
        }
        return oldPromptTemplate;
    }

    @Override
    public PromptTemplate unregisterPromptTemplate(String name) {
        PromptTemplate removedPromptTemplate = templateMap.remove(name);
        if (removedPromptTemplate == null) {
            throw new IllegalStateException(String.format("Prompt template '%s' does not exist", name));
        }
        return removedPromptTemplate;
    }

}