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

package org.metaagent.framework.core.agents.llm.compaction;

/**
 * Service interface for context compaction operations.
 * <p>
 * Context compaction is the process of managing conversation history size to fit
 * within model token limits. This service provides two main operations:
 * <ul>
 *   <li><b>Pruning:</b> Removes less relevant message parts to reduce token count</li>
 *   <li><b>Compaction:</b> Summarizes conversation history into a condensed form</li>
 * </ul>
 * <p>
 *
 * @author vyckey
 * @see CompactionOptions#isThresholdReached(int, int)
 */
public interface ContextCompactionService {
    /**
     * Prunes less relevant message parts to reduce token count.
     * <p>
     * This method analyzes message importance and removes or truncates
     * less critical message parts (e.g., old tool outputs, redundant content).
     *
     * @param pruningInput the pruning input containing messages and options
     * @return the pruning result indicating what was removed or modified
     */
    PruningResult prune(PruningInput pruningInput);

    /**
     * Compacts conversation history by generating a summary.
     * <p>
     * This method summarizes older messages to reduce token count while
     * preserving important conversation context. Recent messages are typically
     * reserved and not included in the summary.
     *
     * @param input the compaction input containing messages, options, and prompts
     * @return the compaction result with summary messages and metadata
     */
    CompactionResult compact(CompactionInput input);
}
