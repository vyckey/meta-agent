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

import lombok.Setter;
import org.metaagent.framework.core.common.metadata.MapMetadataProvider;
import org.metaagent.framework.core.tool.Tool;
import org.metaagent.framework.core.tool.ToolContext;
import org.metaagent.framework.core.tool.ToolExecutionException;
import org.metaagent.framework.core.tool.converter.ToolConverter;
import org.metaagent.framework.core.tool.converter.ToolConverters;
import org.metaagent.framework.core.tool.definition.ToolDefinition;
import org.metaagent.framework.core.tool.human.HumanApprover;
import org.metaagent.framework.core.tool.human.SystemAutoApprover;

import javax.script.Bindings;
import javax.script.Compilable;
import javax.script.CompiledScript;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

/**
 * Script engine tool
 *
 * @author vyckey
 */
@Setter
public class ScriptEngineTool implements Tool<ScriptInput, ScriptOutput> {
    private static final ToolDefinition TOOL_DEFINITION = ToolDefinition.builder("execute_script")
            .description("Execute script with specialized language")
            .inputSchema(ScriptInput.class)
            .outputSchema(ScriptOutput.class)
            .build();
    private static final ToolConverter<ScriptInput, ScriptOutput> TOOL_CONVERTER =
            ToolConverters.jsonConverter(ScriptInput.class);
    protected final ScriptEngineManager scriptEngineManager;
    protected HumanApprover humanApprover = SystemAutoApprover.INSTANCE;

    public ScriptEngineTool(ScriptEngineManager scriptEngineManager) {
        this.scriptEngineManager = scriptEngineManager;
    }

    @Override
    public ToolDefinition getDefinition() {
        return TOOL_DEFINITION;
    }

    @Override
    public ToolConverter<ScriptInput, ScriptOutput> getConverter() {
        return TOOL_CONVERTER;
    }

    @Override
    public ScriptOutput run(ToolContext toolContext, ScriptInput scriptInput) throws ToolExecutionException {
        ScriptEngine scriptEngine = this.scriptEngineManager.getEngineByName(scriptInput.getEngine());
        return executeScript(scriptEngine, scriptInput);
    }

    private String getShortScript(String script) {
        return script.length() > 100 ? script.substring(0, 100) + "..." : script;
    }

    protected ScriptOutput executeScript(ScriptEngine scriptEngine, ScriptInput scriptInput) {
        String script = scriptInput.getScript();
        requestApprovalBeforeExecute(scriptInput.getLanguage(), script);

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

    protected void requestApprovalBeforeExecute(String language, String script) {
        String approval = "Whether execute the " + language + " script:\n" + getShortScript(script);
        HumanApprover.ApprovalInput approvalInput = new HumanApprover.ApprovalInput(approval, new MapMetadataProvider());
        HumanApprover.ApprovalOutput approvalOutput = humanApprover.request(approvalInput);
        if (!approvalOutput.isApproved()) {
            throw new ToolExecutionException("User reject to execute script");
        }
    }
}
