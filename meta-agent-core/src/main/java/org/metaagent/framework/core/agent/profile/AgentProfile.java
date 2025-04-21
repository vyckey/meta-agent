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

import org.metaagent.framework.core.common.MetadataProvider;

/**
 * Agent profile.
 *
 * @author vyckey
 */
public interface AgentProfile extends MetadataProvider {
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
     * Sets agent description.
     *
     * @param description the agent description.
     */
    void setDescription(String description);

    /**
     * Gets profile property.
     *
     * @param key the property key.
     * @return the property value.
     */
    Object getProperty(String key);

    /**
     * Gets profile property.
     *
     * @param key the property key.
     * @param <T> the property type.
     * @return the property value.
     */
    <T> T getProperty(String key, Class<T> type);

    /**
     * Sets the property value.
     *
     * @param key   the property key.
     * @param value the property value.
     * @return the self profile.
     */
    AgentProfile setProperty(String key, Object value);
}
