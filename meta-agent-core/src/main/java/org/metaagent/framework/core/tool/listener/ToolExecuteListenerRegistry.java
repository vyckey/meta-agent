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

package org.metaagent.framework.core.tool.listener;

import java.util.List;

/**
 * Registry for managing ToolExecuteListener instances.
 *
 * @author vyckey
 */
public interface ToolExecuteListenerRegistry {
    /**
     * Default instance of ToolExecuteListenerRegistry.
     * This is a singleton instance that can be used throughout the application.
     */
    ToolExecuteListenerRegistry DEFAULT = DefaultToolExecuteListenerRegistry.getInstance();

    /**
     * Retrieves the list of registered ToolExecuteListener instances.
     *
     * @return List of ToolExecuteListener
     */
    List<ToolExecuteListener> getListeners();

    /**
     * Registers a ToolExecuteListener instance.
     *
     * @param listener the ToolExecuteListener to register
     */
    void registerListener(ToolExecuteListener listener);

    /**
     * Unregisters a ToolExecuteListener instance.
     *
     * @param listener the ToolExecuteListener to unregister
     */
    void unregisterListener(ToolExecuteListener listener);

    /**
     * Unregisters all ToolExecuteListener instances.
     */
    void removeAllListeners();
}
