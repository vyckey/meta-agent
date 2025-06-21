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

package org.metaagent.framework.core.agent.profile;

import lombok.Getter;
import org.apache.commons.configuration2.BaseConfiguration;
import org.apache.commons.configuration2.Configuration;
import org.apache.commons.lang3.StringUtils;
import org.metaagent.framework.core.common.metadata.ConfigurationMetadataProvider;

import java.util.Objects;

/**
 * Default implementation of {@link AgentProfile}.
 *
 * @author vyckey
 */
@Getter
public class DefaultAgentProfile implements AgentProfile {
    protected final String name;
    protected String description;
    protected final Configuration configuration;
    protected final ConfigurationMetadataProvider metadata;

    public DefaultAgentProfile(String name, String description, Configuration configuration) {
        if (StringUtils.isBlank(name)) {
            throw new IllegalArgumentException("Agent name is blank");
        }
        this.name = name.trim();
        setDescription(description);
        this.configuration = Objects.requireNonNull(configuration, "configuration is required");
        this.metadata = new ConfigurationMetadataProvider(configuration);
    }

    public DefaultAgentProfile(String name, String description) {
        this(name, description, new BaseConfiguration());
    }

    public DefaultAgentProfile(String name) {
        this(name, null);
    }

    public void setDescription(String description) {
        this.description = description != null ? description.trim() : "";
    }

    @Override
    public String toString() {
        if (StringUtils.isEmpty(description)) {
            return name;
        }
        return name + ": " + description;
    }
}
