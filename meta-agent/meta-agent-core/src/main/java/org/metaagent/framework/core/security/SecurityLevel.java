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

package org.metaagent.framework.core.security;

/**
 * Security level interface defining different levels of security restrictions.
 *
 * @author vyckey
 */
@FunctionalInterface
public interface SecurityLevel extends Comparable<SecurityLevel> {
    /**
     * Use with caution, this level allows execution of potentially dangerous commands. Recommended only for trusted sandbox environments.
     */
    SecurityLevel UNRESTRICTED_DANGEROUSLY = () -> 100;
    /**
     * Default level, allows execution of a broader set of commands while still maintaining safety.
     */
    SecurityLevel RESTRICTED_DEFAULT_SALE = () -> 10_000;
    /**
     * Most restrictive level, only allows execution of a minimal set of safe commands.
     */
    SecurityLevel RESTRICTED_HIGHLY_SAFE = () -> 100_000;

    /**
     * Get the integer value of the security level. The higher the value, the more restricted the level.
     *
     * @return The integer value of the security level.
     */
    int value();

    @Override
    default int compareTo(SecurityLevel another) {
        return Integer.compare(value(), another.value());
    }
}
