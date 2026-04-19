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

import org.metaagent.framework.core.agent.chat.message.part.MessagePart;
import org.metaagent.framework.core.agent.chat.message.part.MessagePartId;

import java.util.List;

/**
 * Result of a pruning operation.
 * <p>
 * This record encapsulates the outcome of message pruning, including whether
 * pruning occurred, which message parts were deleted, and which parts were
 * modified (e.g., truncated) during the pruning process.
 * <p>
 * When {@code pruned} is {@code true}, at least one of the lists will contain
 * entries indicating what was changed. If both lists are empty, pruning was
 * attempted but no changes were necessary.
 *
 * @param pruned                {@code true} if pruning was performed, {@code false} otherwise
 * @param deletedMessagePartIds IDs of message parts that were completely removed
 * @param modifiedMessageParts  message parts that were modified (e.g., truncated)
 * @author vyckey
 * @see ContextCompactionService#prune(PruningInput)
 */
public record PruningResult(
        boolean pruned,
        List<MessagePartId> deletedMessagePartIds,
        List<MessagePart> modifiedMessageParts
) {
    public PruningResult {
        deletedMessagePartIds = deletedMessagePartIds != null ? deletedMessagePartIds : List.of();
        modifiedMessageParts = modifiedMessageParts != null ? modifiedMessageParts : List.of();
    }

    public static Builder builder() {
        return new Builder();
    }


    public static class Builder {
        private boolean pruned;
        private List<MessagePartId> deletedMessagePartIds;
        private List<MessagePart> modifiedMessageParts;

        public Builder pruned(boolean pruned) {
            this.pruned = pruned;
            return this;
        }

        public Builder deletedMessagePartIds(List<MessagePartId> deletedMessagePartIds) {
            this.deletedMessagePartIds = deletedMessagePartIds;
            return this;
        }

        public Builder modifiedMessageParts(List<MessagePart> modifiedMessageParts) {
            this.modifiedMessageParts = modifiedMessageParts;
            return this;
        }

        public PruningResult build() {
            return new PruningResult(pruned, deletedMessagePartIds, modifiedMessageParts);
        }
    }
}
