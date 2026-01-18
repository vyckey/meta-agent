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

package org.metaagent.framework.tools.script.engine;

import com.google.common.collect.Maps;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.metaagent.framework.common.abort.AbortException;
import org.metaagent.framework.core.tool.Tool;
import org.metaagent.framework.core.tool.ToolContext;
import org.metaagent.framework.core.tool.converter.ToolConverter;
import org.metaagent.framework.core.tool.converter.ToolConverters;
import org.metaagent.framework.core.tool.definition.ToolDefinition;
import org.metaagent.framework.core.tool.exception.ToolArgumentException;
import org.metaagent.framework.core.tool.exception.ToolExecutionException;
import org.metaagent.framework.core.tool.schema.ToolArgsValidator;

import javax.script.Bindings;
import javax.script.Compilable;
import javax.script.CompiledScript;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.util.Map;

/**
 * Script engine tool
 *
 * @author vyckey
 */
@Setter
public class ScriptEngineTool implements Tool<ScriptInput, ScriptOutput>, AutoCloseable {
    public static final String TOOL_NAME = "execute_script";
    private static final ToolDefinition TOOL_DEFINITION = ToolDefinition.builder(TOOL_NAME)
            .description("Execute script with specialized language")
            .inputSchema(ScriptInput.class)
            .outputSchema(ScriptOutput.class)
            .isConcurrencySafe(false)
            .isReadOnly(false)
            .build();
    private static final ToolConverter<ScriptInput, ScriptOutput> TOOL_CONVERTER =
            ToolConverters.jsonConverter(ScriptInput.class);
    private static volatile ScriptEngineManager defaultScriptEngineManager;
    protected final ScriptEngineManager scriptEngineManager;
    protected final ThreadLocal<Map<String, ScriptEngine>> scriptEngineCache = ThreadLocal.withInitial(Maps::newConcurrentMap);

    public static ScriptEngineManager getDefaultScriptEngineManager() {
        if (defaultScriptEngineManager == null) {
            synchronized (ScriptEngineTool.class) {
                if (defaultScriptEngineManager == null) {
                    defaultScriptEngineManager = new ScriptEngineManager();
                }
            }
        }
        return defaultScriptEngineManager;
    }

    public ScriptEngineTool(ScriptEngineManager scriptEngineManager) {
        this.scriptEngineManager = scriptEngineManager;
    }

    public ScriptEngineTool() {
        this(getDefaultScriptEngineManager());
    }

    @Override
    public ToolDefinition getDefinition() {
        return TOOL_DEFINITION;
    }

    @Override
    public ToolConverter<ScriptInput, ScriptOutput> getConverter() {
        return TOOL_CONVERTER;
    }

    protected ScriptEngine getScriptEngine(String engineName, String language) {
        String cacheKey = engineName + ":" + language;
        Map<String, ScriptEngine> engineCache = scriptEngineCache.get();
        return engineCache.computeIfAbsent(cacheKey, key -> {
            for (ScriptEngineFactory engineFactory : scriptEngineManager.getEngineFactories()) {
                if (engineName != null && !engineName.equalsIgnoreCase(engineFactory.getEngineName())) {
                    continue;
                }
                if (language != null && !language.equalsIgnoreCase(engineFactory.getLanguageName())) {
                    continue;
                }
                return engineFactory.getScriptEngine();
            }
            StringBuilder error = new StringBuilder("No script engine found for ");
            if (engineName != null) {
                error.append("engineName=").append(engineName);
            }
            if (language != null) {
                error.append("language=").append(language);
            }
            throw new ToolArgumentException(error.toString());
        });
    }

    @Override
    public ScriptOutput run(ToolContext toolContext, ScriptInput scriptInput) throws ToolExecutionException {
        ToolArgsValidator.validate(scriptInput);
        if (StringUtils.isEmpty(scriptInput.getEngine()) && StringUtils.isEmpty(scriptInput.getLanguage())) {
            throw new ToolArgumentException("Either engine or language must be specified");
        }

        ScriptEngine scriptEngine = getScriptEngine(scriptInput.getEngine(), scriptInput.getLanguage());
        if (toolContext.getAbortSignal().isAborted()) {
            throw new AbortException("Tool " + getName() + " is cancelled");
        }
        return executeScript(scriptEngine, scriptInput);
    }

    private String getShortScript(String script) {
        return script.length() > 100 ? script.substring(0, 100) + "..." : script;
    }

    protected ScriptOutput executeScript(ScriptEngine scriptEngine, ScriptInput scriptInput) {
        String script = scriptInput.getScript();

        CompiledScript compiledScript = null;
        if (scriptEngine instanceof Compilable) {
            try {
                compiledScript = ((Compilable) scriptEngine).compile(script);
            } catch (ScriptException e) {
                String error = "Failed to compile script \"" + getShortScript(script) + "\"";
                return ScriptOutput.builder().error(error).build();
            }
        }

        Bindings bindings = scriptEngine.createBindings();
        bindings.putAll(scriptInput.getVariables());
        Object result;
        try {
            if (compiledScript == null) {
                result = scriptEngine.eval(script, bindings);
            } else {
                result = compiledScript.eval(bindings);
            }
        } catch (ScriptException e) {
            String error = "Failed to eval script caused by " + e.getMessage();
            return ScriptOutput.builder().error(error).build();
        }
        return ScriptOutput.builder().result(result != null ? result.toString() : null).build();
    }

    @Override
    public void close() {
        scriptEngineCache.remove();
    }
}
