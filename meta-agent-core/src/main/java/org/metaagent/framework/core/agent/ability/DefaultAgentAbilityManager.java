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

package org.metaagent.framework.core.agent.ability;

import com.google.common.collect.Maps;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * description is here
 *
 * @author vyckey
 */
public class DefaultAgentAbilityManager implements AgentAbilityManager {
    protected final Map<String, AgentAbility> abilities;

    public DefaultAgentAbilityManager(Map<String, AgentAbility> abilities) {
        this.abilities = Objects.requireNonNull(abilities, "abilities is required");
    }

    public DefaultAgentAbilityManager() {
        this(Maps.newHashMap());
    }

    @Override
    public Set<String> getAbilityNames() {
        return Collections.unmodifiableSet(abilities.keySet());
    }

    @Override
    public boolean hasAbility(String abilityName) {
        return abilities.containsKey(abilityName);
    }

    @Override
    public boolean hasAbility(Class<? extends AgentAbility> abilityType) {
        return abilities.values().stream().anyMatch(abilityType::isInstance);
    }

    @Override
    public <T extends AgentAbility> T getAbility(Class<T> abilityType) {
        return abilities.values().stream()
                .filter(abilityType::isInstance)
                .map(abilityType::cast)
                .findFirst()
                .orElse(null);
    }

    @Override
    public AgentAbility getAbility(String abilityName) {
        return abilities.get(abilityName);
    }

    @Override
    public void addAbility(AgentAbility ability) {
        this.abilities.put(ability.getName(), ability);
    }

    @Override
    public void removeAbility(AgentAbility ability) {
        this.abilities.remove(ability.getName());
    }

    @Override
    public void removeAbility(String abilityName) {
        this.abilities.remove(abilityName);
    }
}
