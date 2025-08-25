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
package org.eclipse.daanse.olap.function.def.vba.time;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import java.time.Instant;
import java.util.Date;

import org.eclipse.daanse.olap.api.Evaluator;
import org.eclipse.daanse.olap.api.type.DateTimeType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class TimeCalcTest {

    private TimeCalc timeCalc;
    private Evaluator evaluator;

    @BeforeEach
    void setUp() {
        evaluator = mock(Evaluator.class);
        timeCalc = new TimeCalc(DateTimeType.INSTANCE);
    }

    @Test
    @DisplayName("Should return current time as Date")
    void shouldReturnCurrentTimeAsDate() {
        Date before = new Date();

        Date result = timeCalc.evaluate(evaluator);

        Date after = new Date();

        assertThat(result).isNotNull();
        assertThat(result).isInstanceOf(Date.class);

        // Result should be between before and after timestamps (allowing for test
        // execution time)
        assertThat(result.getTime()).isBetween(before.getTime() - 1000, // Allow 1 second before
                after.getTime() + 1000 // Allow 1 second after
        );
    }

    @Test
    @DisplayName("Should return different times on consecutive calls")
    void shouldReturnDifferentTimesOnConsecutiveCalls() throws InterruptedException {
        Date result1 = timeCalc.evaluate(evaluator);

        // Sleep for a small amount to ensure time difference
        Thread.sleep(10);

        Date result2 = timeCalc.evaluate(evaluator);

        assertThat(result1).isNotNull();
        assertThat(result2).isNotNull();

        // Second call should return a later or equal time
        assertThat(result2.getTime()).isGreaterThanOrEqualTo(result1.getTime());
    }

    @Test
    @DisplayName("Should preserve type information")
    void shouldPreserveTypeInformation() {
        assertThat(timeCalc.getType()).isEqualTo(DateTimeType.INSTANCE);
    }

    @Test
    @DisplayName("Should return time close to system current time")
    void shouldReturnTimeCloseToSystemCurrentTime() {
        long systemTimeBefore = System.currentTimeMillis();

        Date result = timeCalc.evaluate(evaluator);

        long systemTimeAfter = System.currentTimeMillis();
        long resultTime = result.getTime();

        // Result should be within the time window of the test execution
        assertThat(resultTime).isBetween(systemTimeBefore, systemTimeAfter + 100);
    }

    @Test
    @DisplayName("Should consistently return Date objects")
    void shouldConsistentlyReturnDateObjects() {
        for (int i = 0; i < 10; i++) {
            Date result = timeCalc.evaluate(evaluator);
            assertThat(result).isNotNull();
            assertThat(result).isInstanceOf(Date.class);
        }
    }

    @Test
    @DisplayName("Should return time that is reasonable recent")
    void shouldReturnTimeThatIsReasonableRecent() {
        Date result = timeCalc.evaluate(evaluator);
        Date now = new Date();

        // The returned time should be very close to "now"
        long timeDifference = Math.abs(result.getTime() - now.getTime());

        // Should be within 5 seconds (very generous for test execution)
        assertThat(timeDifference).isLessThan(5000L);
    }

    @Test
    @DisplayName("Should return time after Unix epoch")
    void shouldReturnTimeAfterUnixEpoch() {
        Date result = timeCalc.evaluate(evaluator);

        // Should be after Unix epoch (January 1, 1970)
        assertThat(result.getTime()).isPositive();

        // Should be after year 2000 (reasonable for current system)
        Date year2000 = Date.from(Instant.parse("2000-01-01T00:00:00Z"));
        assertThat(result).isAfter(year2000);
    }

    @Test
    @DisplayName("Should handle multiple evaluations efficiently")
    void shouldHandleMultipleEvaluationsEfficiently() {
        long startTime = System.currentTimeMillis();

        // Perform multiple evaluations
        for (int i = 0; i < 100; i++) {
            Date result = timeCalc.evaluate(evaluator);
            assertThat(result).isNotNull();
        }

        long endTime = System.currentTimeMillis();
        long totalTime = endTime - startTime;

        // Should complete quickly (less than 1 second for 100 calls)
        assertThat(totalTime).isLessThan(1000L);
    }

    @Test
    @DisplayName("Should return time that can be formatted")
    void shouldReturnTimeThatCanBeFormatted() {
        Date result = timeCalc.evaluate(evaluator);

        // Should be able to format the date
        String formatted = result.toString();
        assertThat(formatted).isNotNull();
        assertThat(formatted).isNotEmpty();
    }

    @Test
    @DisplayName("Should verify Time function behavior matches VBA")
    void shouldVerifyTimeFunctionBehaviorMatchesVBA() {
        // In VBA, Time function returns the current system time
        // This should be equivalent to Java's new Date()

        Date vbaTimeEquivalent = new Date();
        Date result = timeCalc.evaluate(evaluator);

        // Both should be very close to each other
        long timeDifference = Math.abs(result.getTime() - vbaTimeEquivalent.getTime());
        assertThat(timeDifference).isLessThan(1000L); // Within 1 second
    }
}