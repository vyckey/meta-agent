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

package org.metaagent.framework.tools.time;

import org.apache.commons.lang3.StringUtils;
import org.metaagent.framework.core.tool.Tool;
import org.metaagent.framework.core.tool.ToolContext;
import org.metaagent.framework.core.tool.converter.ToolConverter;
import org.metaagent.framework.core.tool.converter.ToolConverters;
import org.metaagent.framework.core.tool.definition.ToolDefinition;
import org.metaagent.framework.core.tool.exception.ToolArgumentException;
import org.metaagent.framework.core.tool.exception.ToolExecutionException;

import java.time.DateTimeException;
import java.time.ZoneId;

/**
 * Gets the current time tool.
 *
 * @author vyckey
 */
public class GetCurrentTimeTool implements Tool<GetCurrentTimeInput, GetCurrentTimeOutput> {
    public static final String TOOL_NAME = "get_current_time";
    private static final ToolDefinition TOOL_DEFINITION = ToolDefinition.builder(TOOL_NAME)
            .description("Gets the current time in a specified timezone.")
            .inputSchema(GetCurrentTimeInput.class)
            .outputSchema(GetCurrentTimeOutput.class)
            .isConcurrencySafe(true)
            .isReadOnly(true)
            .build();
    private static final ToolConverter<GetCurrentTimeInput, GetCurrentTimeOutput> TOOL_CONVERTER =
            ToolConverters.jsonConverter(GetCurrentTimeInput.class);

    @Override
    public ToolDefinition getDefinition() {
        return TOOL_DEFINITION;
    }

    @Override
    public ToolConverter<GetCurrentTimeInput, GetCurrentTimeOutput> getConverter() {
        return TOOL_CONVERTER;
    }

    @Override
    public GetCurrentTimeOutput run(ToolContext toolContext, GetCurrentTimeInput input) throws ToolExecutionException {
        ZoneId zoneId;
        try {
            zoneId = StringUtils.isNotEmpty(input.timezone()) ? ZoneId.of(input.timezone()) : ZoneId.of("UTC");
        } catch (DateTimeException e) {
            throw new ToolArgumentException("Invalid timezone: " + input.timezone(), e);
        }
        return GetCurrentTimeOutput.fromNow(zoneId);
    }
}
