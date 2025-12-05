/*
 * This software is subject to the terms of the Eclipse Public License v1.0
 * Agreement, available at the following URL:
 * http://www.eclipse.org/legal/epl-v10.html.
 * You must accept the terms of that agreement to use this software.
 *
 * Copyright (c) 2002-2020 Hitachi Vantara..  All rights reserved.
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
package org.eclipse.daanse.olap.util;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.eclipse.daanse.olap.api.ConfigConstants;
import org.eclipse.daanse.olap.api.Context;
import org.eclipse.daanse.olap.api.Statement;
import org.eclipse.daanse.olap.api.connection.Connection;
import org.eclipse.daanse.olap.common.SystemWideProperties;
import org.eclipse.daanse.olap.execution.ExecutionImpl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

class CancellationCheckerTest {

    @AfterEach
    void afterEach() {
        SystemWideProperties.instance().populateInitial();
    }

    private ExecutionImpl excMock = mock(ExecutionImpl.class);

    @Test
    void checkCancelOrTimeoutWithIntExecution() {
        int currentIteration = 10;
        prepareCheckCancelOrTimeoutInterval(1);
        CancellationChecker.checkCancelOrTimeout(currentIteration, excMock);
        verify(excMock).checkCancelOrTimeout();
    }

    @Test
    void checkCancelOrTimeoutWithLongExecution() {
        long currentIteration = 10L;
        prepareCheckCancelOrTimeoutInterval(1);
        CancellationChecker.checkCancelOrTimeout(currentIteration, excMock);
        verify(excMock).checkCancelOrTimeout();
    }

    @Test
    void checkCancelOrTimeoutLongMoreThanIntExecution() {
        long currentIteration = 2147483648L;
        prepareCheckCancelOrTimeoutInterval(1);
        CancellationChecker.checkCancelOrTimeout(currentIteration, excMock);
        verify(excMock).checkCancelOrTimeout();
    }

    @Test
    void checkCancelOrTimeoutMaxLongExecution() {
        long currentIteration = 9223372036854775807L;
        prepareCheckCancelOrTimeoutInterval(1);
        CancellationChecker.checkCancelOrTimeout(currentIteration, excMock);
        verify(excMock).checkCancelOrTimeout();
    }

    @Test
    void checkCancelOrTimeoutNoExecutionIntervalZero() {
        int currentIteration = 10;
        prepareCheckCancelOrTimeoutInterval(0);
        CancellationChecker.checkCancelOrTimeout(currentIteration, excMock);
        verify(excMock, never()).checkCancelOrTimeout();
    }

    @Test
    void checkCancelOrTimeoutNoExecutionEvenIntervalOddIteration() {
        int currentIteration = 3;
        prepareCheckCancelOrTimeoutInterval(10);
        CancellationChecker.checkCancelOrTimeout(currentIteration, excMock);
        verify(excMock, never()).checkCancelOrTimeout();
    }

    private void prepareCheckCancelOrTimeoutInterval(int i) {
        Statement statement = mock(Statement.class);
        Connection rolapConnection = mock(Connection.class);
        Context context = mock(Context.class);
        when(context.getConfigValue(ConfigConstants.CHECK_CANCEL_OR_TIMEOUT_INTERVAL,
                ConfigConstants.CHECK_CANCEL_OR_TIMEOUT_INTERVAL_DEFAULT_VALUE, Integer.class)).thenReturn(i);
        when(rolapConnection.getContext()).thenReturn(context);
        when(statement.getDaanseConnection()).thenReturn(rolapConnection);
        when(excMock.getDaanseStatement()).thenReturn(statement);
    }

}
