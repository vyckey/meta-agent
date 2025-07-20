package org.metaagent.framework.core.model.prompt.script;

import lombok.Getter;
import org.metaagent.framework.core.model.prompt.PromptFormatException;
import org.metaagent.framework.core.model.prompt.PromptTemplate;
import org.metaagent.framework.core.model.prompt.PromptValue;

import javax.script.Bindings;
import javax.script.Compilable;
import javax.script.CompiledScript;
import javax.script.ScriptEngine;
import javax.script.ScriptException;
import java.util.Map;
import java.util.Objects;

/**
 * Script prompt template.
 *
 * @author vyckey
 */
public class ScriptPromptTemplate implements PromptTemplate {
    @Getter
    private final String language;
    @Getter
    private final String script;
    @Getter
    private final ScriptEngine scriptEngine;
    private volatile CompiledScript compiledScript;

    public ScriptPromptTemplate(ScriptEngine scriptEngine, String language, String script) {
        this.language = Objects.requireNonNull(language, "language must not be null");
        this.script = script;
        this.scriptEngine = Objects.requireNonNull(scriptEngine, "scriptEngine must not be null");
    }

    private String getShortScript() {
        return script.length() > 100 ? script.substring(0, 100) + "..." : script;
    }

    private void compileScriptIfPossible() {
        if (compiledScript == null && scriptEngine instanceof Compilable) {
            try {
                compiledScript = ((Compilable) scriptEngine).compile(script);
            } catch (ScriptException e) {
                throw new PromptFormatException("Failed to compile script \"" + getShortScript() + "\"", e);
            }
        }
    }

    protected String executeScript(ScriptEngine scriptEngine, Map<String, Object> args) {
        compileScriptIfPossible();

        Bindings bindings = scriptEngine.createBindings();
        bindings.putAll(args);
        Object result;
        try {
            if (compiledScript == null) {
                result = scriptEngine.eval(script, bindings);
            } else {
                result = compiledScript.eval(bindings);
            }
        } catch (ScriptException e) {
            throw new PromptFormatException("Failed to eval script \"" + getShortScript() + "\"", e);
        }
        if (result instanceof CharSequence) {
            return result.toString();
        }
        throw new PromptFormatException("Script \"" + getShortScript() + "\" returned unexpected type: "
                + result.getClass().getName());
    }

    @Override
    public PromptValue format(Object... args) {
        throw new UnsupportedOperationException();
    }

    @Override
    public PromptValue format(Map<String, Object> args) {
        String output = executeScript(scriptEngine, args);
        return PromptValue.from(output);
    }

    @Override
    public String toString() {
        return "```" + language + "\n" + script + "\n```";
    }
}
