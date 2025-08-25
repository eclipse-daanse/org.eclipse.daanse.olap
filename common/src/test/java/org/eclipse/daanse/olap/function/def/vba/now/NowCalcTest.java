/*
 * Copyright (c) 2025 Contributors to the Eclipse Foundation.
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
package org.eclipse.daanse.olap.function.def.vba.now;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

import org.assertj.core.api.Assertions;
import org.eclipse.daanse.olap.api.Evaluator;
import org.eclipse.daanse.olap.api.type.DateTimeType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class NowCalcTest {

    private NowCalc nowCalc;
    private Evaluator evaluator;

    @BeforeEach
    void setUp() {
        evaluator = mock(Evaluator.class);
        nowCalc = new NowCalc(DateTimeType.INSTANCE);
    }

    @Test
    @DisplayName("Should return current date and time")
    void shouldReturnCurrentDateTime() throws InterruptedException {
        Instant beforeCall = Instant.now();
        Thread.sleep(100);
        Date result = nowCalc.evaluate(evaluator);
        Thread.sleep(100);
        Instant afterCall = Instant.now();

        assertThat(result).isNotNull();
        assertThat(result.toInstant()).isBetween(beforeCall, afterCall);
    }

    @Test
    @DisplayName("Should return different timestamps on subsequent calls")
    void shouldReturnDifferentTimestampsOnSubsequentCalls() throws InterruptedException {
        Date result1 = nowCalc.evaluate(evaluator);

        // Sleep for a small amount to ensure time difference
        Thread.sleep(10);

        Date result2 = nowCalc.evaluate(evaluator);

        assertThat(result1).isNotNull();
        assertThat(result2).isNotNull();
        assertThat(result2).isAfter(result1);
    }

    @Test
    @DisplayName("Should preserve type information")
    void shouldPreserveTypeInformation() {
        assertThat(nowCalc.getType()).isEqualTo(DateTimeType.INSTANCE);
    }

    @Test
    @DisplayName("Should return current time within reasonable bounds")
    void shouldReturnCurrentTimeWithinReasonableBounds() {
        Instant expectedTime = Instant.now();
        Date result = nowCalc.evaluate(evaluator);

        // Should be within 1 second of current time
        assertThat(result.toInstant()).isCloseTo(expectedTime, Assertions.within(1, ChronoUnit.SECONDS));
    }

    @Test
    @DisplayName("Should handle multiple evaluations consistently")
    void shouldHandleMultipleEvaluationsConsistently() {
        for (int i = 0; i < 10; i++) {
            Date result = nowCalc.evaluate(evaluator);
            assertThat(result).isNotNull();
            assertThat(result.toInstant()).isCloseTo(Instant.now(), Assertions.within(1, ChronoUnit.SECONDS));
        }
    }

    @Test
    @DisplayName("Should return dates that advance in time")
    void shouldReturnDatesThatAdvanceInTime() throws InterruptedException {
        Date[] results = new Date[5];

        for (int i = 0; i < 5; i++) {
            results[i] = nowCalc.evaluate(evaluator);
            if (i < 4) {
                Thread.sleep(5); // Small delay between calls
            }
        }

        // Verify that each timestamp is after or equal to the previous one
        for (int i = 1; i < 5; i++) {
            assertThat(results[i]).isAfterOrEqualTo(results[i - 1]);
        }
    }
}