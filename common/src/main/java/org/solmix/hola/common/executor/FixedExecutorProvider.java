/*
 * Copyright 2013 The Solmix Project
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.gnu.org/licenses/ 
 * or see the FSF site: http://www.fsf.org. 
 */
package org.solmix.hola.common.executor;

import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.solmix.commons.util.NamedThreadFactory;
import org.solmix.hola.common.HolaConstants;
import org.solmix.hola.common.config.RemoteInfo;
import org.solmix.runtime.Extension;


/**
 * 不可收缩的线程池
 * 
 * @author solmix.f@gmail.com
 * @version 0.0.1  2014年8月16日
 */
@Extension(name = "fixed")
public class FixedExecutorProvider implements ExecutorProvider
{

    
    @Override
    public Executor getExecutor(RemoteInfo info) {
        int maxSize = info.getThreads(Integer.MAX_VALUE);
        int queues = info.getQueues(HolaConstants.DEFAULT_QUEUES);
        final String name = info.getThreadName("PooledExecutor");
        return new ThreadPoolExecutor(maxSize, maxSize, 0,
            TimeUnit.MILLISECONDS,
            queues == 0 ? new SynchronousQueue<Runnable>()
                : (queues < 0 ? new LinkedBlockingQueue<Runnable>()
                    : new LinkedBlockingQueue<Runnable>(queues)),
            new NamedThreadFactory(name, true),
            new ThreadPoolExecutor.AbortPolicy() {

                @Override
                public void rejectedExecution(Runnable r, ThreadPoolExecutor e) {
                    String msg = String.format(
                        "Thread pool is EXHAUSTED!"
                            + " Thread Name: %s, Pool Size: %d (active: %d, core: %d, max: %d, largest: %d), "
                            + " Task: %d (completed: %d),"
                            + " Executor status:(isShutdown:%s, isTerminated:%s, isTerminating:%s)",
                        name, e.getPoolSize(), e.getActiveCount(),
                        e.getCorePoolSize(), e.getMaximumPoolSize(),
                        e.getLargestPoolSize(), e.getTaskCount(),
                        e.getCompletedTaskCount(), e.isShutdown(),
                        e.isTerminated(), e.isTerminating());
                    throw new RejectedExecutionException(msg);
                }
            });
    }

}
