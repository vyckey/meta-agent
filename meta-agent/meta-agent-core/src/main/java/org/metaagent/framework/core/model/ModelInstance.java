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

package org.metaagent.framework.core.model;

import org.springframework.ai.model.Model;
import org.springframework.ai.model.ModelRequest;
import org.springframework.ai.model.ModelResponse;

/**
 * Represents a concrete, runtime-accessible model instance exposed by a provider.
 *
 * <p>This interface binds a model's request and response types via the generic
 * parameters {@code T} (request) and {@code R} (response). Implementations
 * provide two primary responsibilities:
 * <ul>
 *   <li>Expose descriptive metadata about the model via {@link #getInfo()}.</li>
 *   <li>Provide access to the runtime/execution handle (a {@link Model}) via {@link #getRuntime()}.</li>
 * </ul>
 *
 * <p>Guidance for implementers and callers:
 * <ul>
 *   <li><b>Nullability</b>: Prefer returning non-null values. If a value may be absent,
 *       document that explicitly or throw an appropriate exception (e.g. {@link IllegalStateException}).</li>
 *   <li><b>Thread-safety</b>: Document whether {@link #getRuntime()} returns a thread-safe handle that may be reused
 *       concurrently. If the runtime is not thread-safe, callers must synchronize as documented by the implementation.</li>
 *   <li><b>Lifecycle</b>: Document who owns the lifecycle of the returned runtime (who closes/releases resources) and whether
 *       the runtime remains valid after provider shutdown or {@code close()} semantics.</li>
 *   <li><b>Immutability</b>: Prefer returning immutable metadata from {@link #getInfo()} to avoid surprising callers.</li>
 * </ul>
 *
 * @param <T> the request type, must extend {@link ModelRequest}
 * @param <R> the response type, must extend {@link ModelResponse}
 * @see org.springframework.ai.model.Model
 * @see org.springframework.ai.model.ModelRequest
 * @see org.springframework.ai.model.ModelResponse
 */
public interface ModelInstance<T extends ModelRequest<?>, R extends ModelResponse<?>> {

    /**
     * Return metadata that describes this model instance.
     *
     * <p>The returned {@link ModelInfo} should describe identifying information
     * (id, provider id, name, family) and any model-specific capabilities or
     * configuration that callers might need before invoking the runtime.</p>
     *
     * <p>Implementations should prefer returning a cached, immutable {@link ModelInfo}
     * instance to avoid allocation and to make the metadata safe to share between threads.</p>
     *
     * @return a non-null {@link ModelInfo} describing this model instance; if metadata cannot be provided,
     *         implementations should document that behavior (or throw an exception) rather than return {@code null}.
     */
    ModelInfo getInfo();

    /**
     * Return the runtime handle used to execute requests against this model.
     *
     * <p>The returned {@link Model} instance is the object callers use to perform
     * inference or other model operations for the given request/response types.
     * Implementations must document the concurrency guarantees of the returned
     * runtime (for example whether it is safe for multiple threads to call into
     * the runtime concurrently).</p>
     *
     * <p>If the runtime is temporarily unavailable, cannot be created, or has
     * been shut down, implementations should throw a runtime exception (for
     * example {@link IllegalStateException}) rather than returning {@code null},
     * unless a {@code null} return value is explicitly documented.</p>
     *
     * @return a non-null {@link Model} instance parameterized with {@code T} and {@code R}
     * @throws IllegalStateException if the runtime is not available or cannot be returned
     */
    Model<T, R> getRuntime();
}
