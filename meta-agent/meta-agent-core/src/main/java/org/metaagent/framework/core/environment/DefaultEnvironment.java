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

package org.metaagent.framework.core.environment;

import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.configuration2.BaseConfiguration;
import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.ImmutableConfiguration;
import org.metaagent.framework.core.agent.group.AgentGroup;

import java.util.List;
import java.util.Objects;

/**
 * description is here
 *
 * @author vyckey
 */
@Slf4j
public class DefaultEnvironment implements Environment {
    protected final Configuration configuration;
    protected final AgentGroup agentGroup;
    protected List<EnvironmentObserver> observers = Lists.newArrayList();
    protected List<EnvironmentActor> actors = Lists.newArrayList();

    public DefaultEnvironment(Configuration configuration, AgentGroup agentGroup) {
        this.configuration = Objects.requireNonNull(configuration, "configuration is required");
        this.agentGroup = Objects.requireNonNull(agentGroup, "agentGroup is required");
    }

    public DefaultEnvironment(AgentGroup agentGroup) {
        this(new BaseConfiguration(), agentGroup);
    }

    @Override
    public ImmutableConfiguration getConfiguration() {
        return configuration;
    }

    @Override
    public AgentGroup getAgentGroup() {
        return agentGroup;
    }

    @Override
    public void addObserver(EnvironmentObserver observer) {
        this.observers.add(observer);
    }

    @Override
    public void removeObserver(EnvironmentObserver observer) {
        this.observers.remove(observer);
    }

    @Override
    public void notifyObservers(EnvironmentEvent event) {
        for (EnvironmentObserver observer : observers) {
            try {
                observer.observe(event);
            } catch (Exception e) {
                log.error("Failed to observe event {}", event, e);
            }
        }
    }

    @Override
    public void addActor(EnvironmentActor actor) {
        actors.add(actor);
    }

    @Override
    public void removeActor(EnvironmentActor actor) {
        actors.remove(actor);
    }

    @Override
    public void performAction(EnvironmentAction action) {
        for (EnvironmentActor actor : actors) {
            try {
                actor.perform(action);
            } catch (Exception e) {
                log.error("{} failed to perform environment action {}", actor, action, e);
            }
        }
    }
}
