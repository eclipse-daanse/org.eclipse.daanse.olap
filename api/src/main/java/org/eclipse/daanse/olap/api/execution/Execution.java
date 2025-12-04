/*
 * This software is subject to the terms of the Eclipse Public License v1.0
 * Agreement, available at the following URL:
 * http://www.eclipse.org/legal/epl-v10.html.
 * You must accept the terms of that agreement to use this software.
 *
 * Copyright (C) 2001-2005 Julian Hyde
 * Copyright (C) 2005-2017 Hitachi Vantara and others
 * All Rights Reserved.
 *
 * For more information please visit the Project: Hitachi Vantara - Mondrian
 *
 * ---- All changes after Fork in 2023 ------------------------
 *
 * Project: Eclipse daanse
 *
 * Copyright (c) 2024 Contributors to the Eclipse Foundation.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors after Fork in 2023:
 *   SmartCity Jena - initial
 *   Stefan Bischof (bipolis.org) - initial
 */
package org.eclipse.daanse.olap.api.execution;

import java.time.Duration;

import org.eclipse.daanse.olap.api.QueryTiming;
import org.eclipse.daanse.olap.api.Statement;

public interface Execution {

    void cancelSqlStatements();

    Statement getDaanseStatement();

    void setOutOfMemory(String string);

    long getId();

    void checkCancelOrTimeout();

    boolean isCancelOrTimeout();

    Duration getElapsedMillis();

    void tracePhase(int hitCount, int missCount, int pendingCount);

    void setCellCacheHitCount(int hitCount);

    void setCellCacheMissCount(int missCount);

    void setCellCachePendingCount(int pendingCount);

    void setExpCacheCounts(int expResultCacheHitCount, int expResultCacheMissCount);

    void end();

    void start();

    void cancel();

    QueryTiming getQueryTiming();

    int getExpCacheHitCount();

    int getExpCacheMissCount();

    /**
     * Converts this Execution to an ExecutionContext for use with ScopedValue-based
     * context propagation. This method provides a bridge between the legacy
     * Execution interface and the new ExecutionContext approach.
     *
     * @return the ExecutionContext representation of this execution
     */
    ExecutionContext asContext();

    /**
     * Enumeration of the states of an Execution instance.
     */
    public enum State {
        /**
         * Identifies the state in which an execution is before it has started resolving
         * the query. This doesn't mean that there are no current SQL statements already
         * beeing executed.
         */
        FRESH, RUNNING, ERROR, CANCELED, TIMEOUT, DONE,
    }
}
