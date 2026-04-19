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

package org.metaagent.framework.core.agents.llm.processor;

import org.metaagent.framework.core.agents.llm.compaction.CompactionResult;
import org.metaagent.framework.core.agents.llm.compaction.PruningResult;
import org.metaagent.framework.core.agents.llm.context.LlmAgentStepContext;
import org.metaagent.framework.core.agents.llm.input.LlmAgentInput;

/**
 * Context compaction post processor interface.
 *
 * @author vyckey
 */
public interface ContextCompactionPostProcessor {
    ContextCompactionPostProcessor NOOP = (agentInput, stepContext,
                                           pruningResult, compactionResult) -> {
    };

    /**
     * Post process the context compaction result.
     *
     * @param agentInput       the agent input
     * @param stepContext      the step context
     * @param pruningResult    the pruning result
     * @param compactionResult the compaction result
     */
    void postProcess(LlmAgentInput agentInput, LlmAgentStepContext stepContext,
                     PruningResult pruningResult, CompactionResult compactionResult);
}
