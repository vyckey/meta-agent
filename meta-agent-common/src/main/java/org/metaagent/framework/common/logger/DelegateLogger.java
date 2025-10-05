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

package org.metaagent.framework.common.logger;

import org.slf4j.Logger;
import org.slf4j.Marker;

import java.util.Objects;

/**
 * Delegate Logger
 *
 * @author vyckey
 */
public class DelegateLogger implements Logger {
    protected final Logger delegate;

    public DelegateLogger(Logger delegate) {
        this.delegate = Objects.requireNonNull(delegate, "Delegate logger is required");
    }

    @Override
    public String getName() {
        return delegate.getName();
    }

    protected String rebuildFormat(String format) {
        return format;
    }

    @Override
    public boolean isTraceEnabled() {
        return delegate.isTraceEnabled();
    }

    @Override
    public void trace(String format) {
        delegate.trace(rebuildFormat(format));
    }

    @Override
    public void trace(String format, Object arg1) {
        delegate.trace(rebuildFormat(format), arg1);
    }

    @Override
    public void trace(String format, Object arg1, Object arg2) {
        delegate.trace(rebuildFormat(format), arg1, arg2);
    }

    @Override
    public void trace(String format, Object... args) {
        delegate.trace(rebuildFormat(format), args);
    }

    @Override
    public void trace(String format, Throwable throwable) {
        delegate.trace(rebuildFormat(format), throwable);
    }

    @Override
    public boolean isTraceEnabled(Marker marker) {
        return delegate.isTraceEnabled(marker);
    }

    @Override
    public void trace(Marker marker, String format) {
        delegate.trace(marker, rebuildFormat(format));
    }

    @Override
    public void trace(Marker marker, String format, Object arg1) {
        delegate.trace(marker, rebuildFormat(format), arg1);
    }

    @Override
    public void trace(Marker marker, String format, Object arg1, Object arg2) {
        delegate.trace(marker, rebuildFormat(format), arg1, arg2);
    }

    @Override
    public void trace(Marker marker, String format, Object... args) {
        delegate.trace(marker, rebuildFormat(format), args);
    }

    @Override
    public void trace(Marker marker, String format, Throwable throwable) {
        delegate.trace(marker, rebuildFormat(format), throwable);
    }

    @Override
    public boolean isDebugEnabled() {
        return delegate.isDebugEnabled();
    }

    @Override
    public void debug(String format) {
        delegate.debug(rebuildFormat(format));
    }

    @Override
    public void debug(String format, Object arg1) {
        delegate.debug(rebuildFormat(format), arg1);
    }

    @Override
    public void debug(String format, Object arg1, Object arg2) {
        delegate.debug(rebuildFormat(format), arg1, arg2);
    }

    @Override
    public void debug(String format, Object... args) {
        delegate.debug(rebuildFormat(format), args);
    }

    @Override
    public void debug(String format, Throwable throwable) {
        delegate.debug(rebuildFormat(format), throwable);
    }

    @Override
    public boolean isDebugEnabled(Marker marker) {
        return delegate.isDebugEnabled(marker);
    }

    @Override
    public void debug(Marker marker, String format) {
        delegate.debug(marker, rebuildFormat(format));
    }

    @Override
    public void debug(Marker marker, String format, Object arg1) {
        delegate.debug(marker, rebuildFormat(format), arg1);
    }

    @Override
    public void debug(Marker marker, String format, Object arg1, Object arg2) {
        delegate.debug(marker, rebuildFormat(format), arg1, arg2);
    }

    @Override
    public void debug(Marker marker, String format, Object... args) {
        delegate.debug(marker, rebuildFormat(format), args);
    }

    @Override
    public void debug(Marker marker, String format, Throwable throwable) {
        delegate.debug(marker, rebuildFormat(format), throwable);
    }

    @Override
    public boolean isInfoEnabled() {
        return delegate.isInfoEnabled();
    }

    @Override
    public void info(String format) {
        delegate.info(rebuildFormat(format));
    }

    @Override
    public void info(String format, Object arg1) {
        delegate.info(rebuildFormat(format), arg1);
    }

    @Override
    public void info(String format, Object arg1, Object arg2) {
        delegate.info(rebuildFormat(format), arg1, arg2);
    }

    @Override
    public void info(String format, Object... args) {
        delegate.info(rebuildFormat(format), args);
    }

    @Override
    public void info(String format, Throwable throwable) {
        delegate.info(rebuildFormat(format), throwable);
    }

    @Override
    public boolean isInfoEnabled(Marker marker) {
        return delegate.isInfoEnabled(marker);
    }

    @Override
    public void info(Marker marker, String format) {
        delegate.info(marker, rebuildFormat(format));
    }

    @Override
    public void info(Marker marker, String format, Object arg1) {
        delegate.info(marker, rebuildFormat(format), arg1);
    }

    @Override
    public void info(Marker marker, String format, Object arg1, Object arg2) {
        delegate.info(marker, rebuildFormat(format), arg1, arg2);
    }

    @Override
    public void info(Marker marker, String format, Object... args) {
        delegate.info(marker, rebuildFormat(format), args);
    }

    @Override
    public void info(Marker marker, String format, Throwable throwable) {
        delegate.info(marker, rebuildFormat(format), throwable);
    }

    @Override
    public boolean isWarnEnabled() {
        return delegate.isWarnEnabled();
    }

    @Override
    public void warn(String format) {
        delegate.warn(rebuildFormat(format));
    }

    @Override
    public void warn(String format, Object arg1) {
        delegate.warn(rebuildFormat(format), arg1);
    }

    @Override
    public void warn(String format, Object arg1, Object arg2) {
        delegate.warn(rebuildFormat(format), arg1, arg2);
    }

    @Override
    public void warn(String format, Object... args) {
        delegate.warn(rebuildFormat(format), args);
    }

    @Override
    public void warn(String format, Throwable throwable) {
        delegate.warn(rebuildFormat(format), throwable);
    }

    @Override
    public boolean isWarnEnabled(Marker marker) {
        return delegate.isWarnEnabled(marker);
    }

    @Override
    public void warn(Marker marker, String format) {
        delegate.warn(marker, rebuildFormat(format));
    }

    @Override
    public void warn(Marker marker, String format, Object arg1) {
        delegate.warn(marker, rebuildFormat(format), arg1);
    }

    @Override
    public void warn(Marker marker, String format, Object arg1, Object arg2) {
        delegate.warn(marker, rebuildFormat(format), arg1, arg2);
    }

    @Override
    public void warn(Marker marker, String format, Object... args) {
        delegate.warn(marker, rebuildFormat(format), args);
    }

    @Override
    public void warn(Marker marker, String format, Throwable throwable) {
        delegate.warn(marker, rebuildFormat(format), throwable);
    }

    @Override
    public boolean isErrorEnabled() {
        return delegate.isErrorEnabled();
    }

    @Override
    public void error(String format) {
        delegate.error(rebuildFormat(format));
    }

    @Override
    public void error(String format, Object arg1) {
        delegate.error(rebuildFormat(format), arg1);
    }

    @Override
    public void error(String format, Object arg1, Object arg2) {
        delegate.error(rebuildFormat(format), arg1, arg2);
    }

    @Override
    public void error(String format, Object... args) {
        delegate.error(rebuildFormat(format), args);
    }

    @Override
    public void error(String format, Throwable throwable) {
        delegate.error(rebuildFormat(format), throwable);
    }

    @Override
    public boolean isErrorEnabled(Marker marker) {
        return delegate.isErrorEnabled(marker);
    }

    @Override
    public void error(Marker marker, String format) {
        delegate.error(marker, rebuildFormat(format));
    }

    @Override
    public void error(Marker marker, String format, Object arg1) {
        delegate.error(marker, rebuildFormat(format), arg1);
    }

    @Override
    public void error(Marker marker, String format, Object arg1, Object arg2) {
        delegate.error(marker, rebuildFormat(format), arg1, arg2);
    }

    @Override
    public void error(Marker marker, String format, Object... args) {
        delegate.error(marker, rebuildFormat(format), args);
    }

    @Override
    public void error(Marker marker, String format, Throwable throwable) {
        delegate.error(marker, rebuildFormat(format), throwable);
    }

    @Override
    public String toString() {
        return delegate.toString();
    }
}
