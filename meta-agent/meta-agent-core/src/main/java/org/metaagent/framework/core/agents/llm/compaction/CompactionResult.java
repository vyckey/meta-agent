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

import org.metaagent.framework.core.agent.chat.message.Message;
import org.metaagent.framework.core.agent.chat.message.part.MessagePart;
import org.metaagent.framework.core.model.ModelId;
import org.metaagent.framework.core.model.chat.metadata.TokenUsage;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Result of a compaction operation.
 * <p>
 * This record encapsulates the outcome of context compaction, including whether
 * compaction occurred, the model used, messages that were kept, generated summary
 * messages, and token usage statistics.
 * <p>
 * When {@code compacted} is {@code true}, the {@code summaryMessages} contain
 * the generated summary that replaces pruned conversation history. The
 * {@code reservedMessages} are those kept intact (typically recent messages).
 *
 * @param compacted        {@code true} if compaction was performed, {@code false} otherwise
 * @param compactModelId   the model ID used for generating the compaction summary (may be null)
 * @param reservedMessages messages that were kept without modification
 * @param summaryMessages  generated summary message parts replacing compacted content
 * @param compactionUsage  token usage statistics for the compaction operation
 * @author vyckey
 * @see ContextCompactionService#compact(CompactionInput)
 */
public record CompactionResult(
        boolean compacted,
        ModelId compactModelId,
        List<Message> reservedMessages,
        List<MessagePart> summaryMessages,
        TokenUsage compactionUsage
) {
    public CompactionResult {
        if (compacted) {
            Objects.requireNonNull(compactModelId, "compactModelId cannot be null");
            Objects.requireNonNull(reservedMessages, "reservedMessages cannot be null");
            Objects.requireNonNull(summaryMessages, "summaryMessages cannot be null");
            Objects.requireNonNull(compactionUsage, "compactionUsage cannot be null");
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private boolean compacted;
        private ModelId compactModelId;
        private List<Message> reservedMessages = Collections.emptyList();
        private List<MessagePart> summaryMessages = Collections.emptyList();
        private TokenUsage compactionUsage;

        public Builder() {
        }

        public Builder compacted(boolean compacted) {
            this.compacted = compacted;
            return this;
        }

        public Builder compactModelId(ModelId compactModelId) {
            this.compactModelId = compactModelId;
            return this;
        }

        public Builder reservedMessages(List<Message> reservedMessages) {
            this.reservedMessages = reservedMessages == null ? Collections.emptyList() : new ArrayList<>(reservedMessages);
            return this;
        }

        public Builder summaryMessages(List<MessagePart> summaryMessages) {
            this.summaryMessages = summaryMessages == null ? Collections.emptyList() : new ArrayList<>(summaryMessages);
            return this;
        }

        public Builder compactionUsage(TokenUsage compactionUsage) {
            this.compactionUsage = compactionUsage;
            return this;
        }

        public CompactionResult build() {
            return new CompactionResult(
                    compacted,
                    compactModelId,
                    Collections.unmodifiableList(reservedMessages),
                    Collections.unmodifiableList(summaryMessages),
                    compactionUsage
            );
        }
    }
}
