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

package org.metaagent.framework.common.template.impl;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.metaagent.framework.common.template.TemplateRenderException;
import org.metaagent.framework.common.template.TemplateRenderer;

import javax.script.Bindings;
import javax.script.Compilable;
import javax.script.CompiledScript;
import javax.script.ScriptEngine;
import javax.script.ScriptException;
import java.time.Duration;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

/**
 * Script prompt template.
 *
 * @author vyckey
 */
public class ScriptTemplateRenderer implements TemplateRenderer {
    private final String language;
    private final ScriptEngine scriptEngine;
    private static final Cache<String, CompiledScript> COMPILED_SCRIPTS = CacheBuilder.newBuilder()
            .maximumSize(10000)
            .expireAfterAccess(Duration.ofHours(12))
            .build();

    public ScriptTemplateRenderer(String language, ScriptEngine scriptEngine) {
        this.language = Objects.requireNonNull(language, "language must not be null");
        this.scriptEngine = Objects.requireNonNull(scriptEngine, "scriptEngine must not be null");
    }

    private String getShortScript(String script) {
        return script.length() > 100 ? script.substring(0, 100) + "..." : script;
    }

    @Override
    public String name() {
        return "";
    }

    @Override
    public String render(String template, Map<String, Object> variables) {
        return executeScript(scriptEngine, template, variables);
    }

    @Override
    public String render(String template, Object... args) {
        throw new TemplateRenderException("Unsupported operation");
    }

    private CompiledScript compileScriptIfPossible(String script) {
        if (!(scriptEngine instanceof Compilable)) {
            return null;
        }
        try {
            return COMPILED_SCRIPTS.get(script, () -> {
                try {
                    return ((Compilable) scriptEngine).compile(script);
                } catch (ScriptException e) {
                    throw new TemplateRenderException("Failed to compile script \"" + getShortScript(script) + "\"", e);
                }
            });
        } catch (ExecutionException e) {
            if (e.getCause() instanceof TemplateRenderException) {
                throw (TemplateRenderException) e.getCause();
            }
            throw new TemplateRenderException("Failed to compile script \"" + getShortScript(script) + "\"", e);
        }
    }

    protected String executeScript(ScriptEngine scriptEngine, String script, Map<String, Object> variables) {
        CompiledScript compiledScript = compileScriptIfPossible(script);

        Bindings bindings = scriptEngine.createBindings();
        bindings.putAll(variables);
        Object result;
        try {
            if (compiledScript == null) {
                result = scriptEngine.eval(script, bindings);
            } else {
                result = compiledScript.eval(bindings);
            }
        } catch (ScriptException e) {
            throw new TemplateRenderException("Failed to eval script \"" + getShortScript(script) + "\"", e);
        }
        if (result instanceof CharSequence || result instanceof Number || result instanceof Boolean) {
            return result.toString();
        }
        String typeName = result == null ? "null" : result.getClass().getName();
        throw new TemplateRenderException("Script \"" + getShortScript(script) + "\" returned unexpected type: " + typeName);
    }

    @Override
    public String toString() {
        return "ScriptTemplateRenderer{language=\"" + language + "\"}";
    }
}
