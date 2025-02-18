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

package org.metaagent.framework.core.agent.action.executor;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.metaagent.framework.core.agent.action.Action;
import org.metaagent.framework.core.agent.action.ActionExecuteContext;
import org.metaagent.framework.core.agent.action.ActionExecutionException;
import org.metaagent.framework.core.agent.action.result.ActionResult;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * description is here
 *
 * @author vyckey
 */
public class ThreadPoolActionExecutor extends AbstractActionExecutor {
    protected ExecutorService executorService;

    public ThreadPoolActionExecutor(ExecutorService executorService) {
        this.executorService = executorService;
    }

    public ThreadPoolActionExecutor create(int threadCount, int queueSize) {
        ThreadPoolExecutor executor = new ThreadPoolExecutor(threadCount, threadCount,
                0L, TimeUnit.SECONDS,
                new ArrayBlockingQueue<>(queueSize),
                new ThreadFactoryBuilder().setNameFormat("ActionExecutor-T%d").build()
        );
        return new ThreadPoolActionExecutor(executor);
    }

    public ExecutorService getExecutorService() {
        return executorService;
    }

    @Override
    public ActionResult doExecute(ActionExecuteContext context, Action action) throws ActionExecutionException {
        Future<ActionResult> future = executorService.submit(() -> action.execute(context));
        try {
            return future.get();
        } catch (InterruptedException e) {
            Thread.interrupted();
            throw new ActionExecutionException("Action execution interrupted", e);
        } catch (ExecutionException e) {
            if (e.getCause() instanceof ActionExecutionException) {
                throw (ActionExecutionException) e.getCause();
            } else if (e.getCause() != null) {
                throw new ActionExecutionException(e.getCause());
            } else {
                throw new ActionExecutionException(e);
            }
        }
    }
}
