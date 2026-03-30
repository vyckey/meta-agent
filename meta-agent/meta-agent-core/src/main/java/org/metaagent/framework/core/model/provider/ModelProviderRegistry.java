/*
 * MIT License
 *
 * Copyright (c) 2026 MetaAgent
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

package org.metaagent.framework.core.model.provider;

import java.util.Set;

/**
 * Registry contract for managing {@link ModelProvider} instances.
 *
 * <p>Implementations are responsible for registering and unregistering
 * provider instances as well as providing lookup operations by provider id.
 * The registry itself is {@link AutoCloseable} so implementations can release
 * resources when the application shuts down.</p>
 *
 * @author vyckey
 */
public interface ModelProviderRegistry extends AutoCloseable {

    /**
     * Create a global thread-safe {@link ModelProviderRegistry} instance.
     *
     * <p>This factory method returns a new {@link DefaultModelProviderRegistry}
     * which is a simple ConcurrentHashMap-backed implementation. Callers may
     * use this when no custom registry implementation is required.</p>
     *
     * @return a new {@link ModelProviderRegistry}
     */
    static ModelProviderRegistry global() {
        return DefaultModelProviderRegistry.INSTANCE;
    }

    /**
     * Register a provider with the registry. Implementations should tolerate
     * duplicate registrations (either ignore or replace) depending on their
     * semantics.
     *
     * @param provider the provider to register; must not be null
     */
    void registerProvider(ModelProvider provider);

    /**
     * Unregister a provider from the registry. Removing a provider should make
     * it unavailable for subsequent lookups.
     *
     * @param provider the provider to unregister; must not be null
     */
    void unregisterProvider(ModelProvider provider);

    /**
     * Return the set of currently registered provider ids.
     *
     * @return a set of provider id strings; never null
     */
    Set<ModelProviderId> getProviderIds();

    /**
     * Check whether a provider with the given id is registered.
     *
     * @param providerId the provider id to check
     * @return true if the provider is registered, false otherwise
     */
    boolean hasProvider(ModelProviderId providerId);

    /**
     * Look up a {@link ModelProvider} by its id.
     *
     * @param providerId the provider id to look up
     * @return the {@link ModelProvider} instance or null if not found
     */
    ModelProvider getProvider(ModelProviderId providerId);

    /**
     * Close the registry and release any resources held by it. Implementations
     * should ensure subsequent calls to registry methods behave sensibly after
     * close (for example, return empty sets or false for existence checks).
     */
    @Override
    void close();

}
