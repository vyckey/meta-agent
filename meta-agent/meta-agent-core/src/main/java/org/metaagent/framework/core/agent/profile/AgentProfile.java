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

import org.metaagent.framework.common.metadata.MetadataProvider;

/**
 * Agent profile.
 *
 * @author vyckey
 */
public interface AgentProfile {
    /**
     * Creates a new agent profile.
     *
     * @param name        the agent name
     * @param description the agent description
     * @return the agent profile
     */
    static AgentProfile create(String name, String description) {
        return AgentProfile.builder().name(name).description(description).build();
    }

    /**
     * Creates a new builder for agent profile.
     *
     * @return the builder
     */
    static Builder builder() {
        return DefaultAgentProfile.builder();
    }

    /**
     * Gets agent name.
     *
     * @return the agent name.
     */
    String getName();

    /**
     * Gets agent description.
     *
     * @return the agent description.
     */
    String getDescription();

    /**
     * Gets agent metadata provider.
     *
     * @return the metadata provider.
     */
    MetadataProvider getMetadata();

    /**
     * Agent profile builder.
     */
    interface Builder {
        /**
         * Sets agent name.
         *
         * @param name the agent name
         * @return the builder
         */
        Builder name(String name);

        /**
         * Sets agent description.
         *
         * @param description the agent description
         * @return the builder
         */
        Builder description(String description);

        /**
         * Sets agent metadata provider.
         *
         * @param metadata the metadata provider
         * @return the builder
         */
        Builder metadata(MetadataProvider metadata);

        /**
         * Sets agent metadata.
         *
         * @param key   the metadata key
         * @param value the metadata value
         * @return the builder
         */
        Builder metadata(String key, Object value);

        /**
         * Builds the agent profile.
         *
         * @return the agent profile
         */
        AgentProfile build();
    }

}
