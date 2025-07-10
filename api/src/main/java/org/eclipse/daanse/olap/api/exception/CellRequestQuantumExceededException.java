/*
 * This software is subject to the terms of the Eclipse Public License v1.0
 * Agreement, available at the following URL:
 * http://www.eclipse.org/legal/epl-v10.html.
 * You must accept the terms of that agreement to use this software.
 *
 * Copyright (c) 2002-2017 Hitachi Vantara..  All rights reserved.
 *
 * ---- All changes after Fork in 2023 ------------------------
 *
 * Project: Eclipse daanse
 *
 * Copyright (c) 2023 Contributors to the Eclipse Foundation.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors after Fork in 2023:
 *   SmartCity Jena - initial
 */
package org.eclipse.daanse.olap.api.exception;

/**
 * Signals that there are enough outstanding cell requests that it is
 * worth terminating this phase of execution and asking the segment cache
 * for all of the cells that have been asked for.
 *
 * Not really an exception, just a way of aborting a process so that we can
 * do some work and restart the process. Any code that handles this exception
 * is typically in a loop that calls  mondrian.rolap.RolapResult#phase.
 *
 *
 * There are several advantages to this:
 *
 *     If the query has been run before, the cells will be in the
 *     cache already, and this is an opportunity to copy them into the
 *     local cache.
 *     If cell requests are for the same or similar cells, it gives
 *     opportunity to fetch these cells. Then the requests can be answered
 *     from local cache, and we don't need to bother the cache manager with
 *     similar requests.
 *     Prevents memory from filling up with cell requests.
 *
 */
public final class CellRequestQuantumExceededException
    extends RuntimeException
{
    public static final CellRequestQuantumExceededException INSTANCE =
        new CellRequestQuantumExceededException();

    private CellRequestQuantumExceededException() {
    }
}
