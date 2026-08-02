/*
 * Copyright (c) 2026 Contributors to the Eclipse Foundation.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   SmartCity Jena - initial
 *   Stefan Bischof (bipolis.org) - initial
 */
package org.eclipse.daanse.olap.execution;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;

import java.time.Duration;
import java.util.Optional;

import org.eclipse.daanse.olap.api.Context;
import org.eclipse.daanse.olap.api.connection.Connection;
import org.eclipse.daanse.olap.api.monitor.EventBus;
import org.eclipse.daanse.olap.exceptions.QueryTimeoutException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * The query timeout, at the one place that decides it:
 * {@link ExecutionImpl#checkCancelOrTimeout()} for the evaluator and
 * {@link ExecutionImpl#isCancelOrTimeout()} for the shepherd threads.
 *
 * <p>
 * These cases drive {@code ExecutionImpl} directly, without a database, a
 * catalog or MDX. A timeout test that needs a slow query is a test that can only
 * fail slowly and flakily.
 * </p>
 */
class ExecutionImplTimeoutTest {

    /**
     * A statement whose connection and context answer just enough for
     * {@link ExecutionImpl#start()} to fire its monitor event.
     */
    private static AbstractStatement statement() {
        AbstractStatement statement = mock(AbstractStatement.class);
        Connection connection = mock(Connection.class);
        Context<?> context = mock(Context.class);
        EventBus monitor = mock(EventBus.class);
        lenient().when(statement.getDaanseConnection()).thenReturn(connection);
        lenient().doReturn(context).when(connection).getContext();
        lenient().when(context.getName()).thenReturn("test");
        lenient().when(context.getMonitor()).thenReturn(monitor);
        return statement;
    }

    private static ExecutionImpl started(Optional<Duration> budget) {
        ExecutionImpl execution = new ExecutionImpl(statement(), budget);
        execution.start();
        return execution;
    }

    /**
     * Sleeps past a deadline. Uses a budget of a few milliseconds and waits
     * noticeably longer, so the case does not depend on clock resolution.
     */
    private static void waitPastDeadline() throws InterruptedException {
        Thread.sleep(50);
    }

    @Test
    @DisplayName("An execution past its budget throws QueryTimeoutException")
    void timeoutFires() throws InterruptedException {
        ExecutionImpl execution = started(Optional.of(Duration.ofMillis(5)));

        waitPastDeadline();

        assertThatThrownBy(execution::checkCancelOrTimeout)
                .isInstanceOf(QueryTimeoutException.class)
                .hasMessageContaining("seconds");
        // The execution is in state TIMEOUT, which is observable only through this.
        assertThat(execution.isCancelOrTimeout()).isTrue();
    }

    @Test
    @DisplayName("After the timeout the shepherd's isCancelOrTimeout() agrees")
    void shepherdSeesTheTimeout() throws InterruptedException {
        ExecutionImpl execution = started(Optional.of(Duration.ofMillis(5)));

        waitPastDeadline();

        // This is what RolapResultShepherd polls: it has to see the expiry without
        // checkCancelOrTimeout() having run first.
        assertThat(execution.isCancelOrTimeout()).as("expired").isTrue();
    }

    @Test
    @DisplayName("No budget means no timeout")
    void noBudgetNeverTimesOut() throws InterruptedException {
        ExecutionImpl execution = started(Optional.empty());

        waitPastDeadline();

        assertThatCode(execution::checkCancelOrTimeout).doesNotThrowAnyException();
        assertThat(execution.isCancelOrTimeout()).isFalse();
    }

    /**
     * {@code queryTimeout = 0} means "no limit". It must not reach this class as a
     * present {@code Duration.ZERO}, which would expire every query on its first
     * cell.
     */
    @Test
    @DisplayName("A zero budget means no limit, not an expired one")
    void zeroBudgetMeansNoLimit() throws InterruptedException {
        ExecutionImpl execution = started(Optional.of(Duration.ZERO));

        waitPastDeadline();

        assertThatCode(execution::checkCancelOrTimeout).doesNotThrowAnyException();
        assertThat(execution.isCancelOrTimeout()).isFalse();
    }

    @Test
    @DisplayName("A negative budget is treated like none")
    void negativeBudgetMeansNoLimit() throws InterruptedException {
        ExecutionImpl execution = started(Optional.of(Duration.ofMillis(-1)));

        waitPastDeadline();

        assertThatCode(execution::checkCancelOrTimeout).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Within its budget an execution is untouched")
    void withinBudgetDoesNotTimeOut() {
        ExecutionImpl execution = started(Optional.of(Duration.ofMinutes(5)));

        assertThatCode(execution::checkCancelOrTimeout).doesNotThrowAnyException();
        assertThat(execution.isCancelOrTimeout()).isFalse();
    }
}
