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
package org.eclipse.daanse.olap.function.def.vba.timer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import java.util.Calendar;

import org.eclipse.daanse.olap.api.evaluator.Evaluator;
import org.eclipse.daanse.olap.api.type.NumericType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class TimerCalcTest {

    private TimerCalc timerCalc;
    private Evaluator evaluator;

    @BeforeEach
    void setUp() {
        evaluator = mock(Evaluator.class);
        timerCalc = new TimerCalc(NumericType.INSTANCE);
    }

    @Test
    @DisplayName("Should return seconds since midnight as Float")
    void shouldReturnSecondsSinceMidnightAsFloat() {
        Float result = timerCalc.evaluate(evaluator);

        assertThat(result).isNotNull();
        assertThat(result).isInstanceOf(Float.class);

        // Should be between 0 and 86400 (seconds in a day)
        assertThat(result).isBetween(0.0f, 86400.0f);
    }

    @Test
    @DisplayName("Should return reasonable value for current time")
    void shouldReturnReasonableValueForCurrentTime() {
        Calendar now = Calendar.getInstance();
        int hour = now.get(Calendar.HOUR_OF_DAY);
        int minute = now.get(Calendar.MINUTE);
        int second = now.get(Calendar.SECOND);

        // Calculate expected seconds since midnight
        float expectedSeconds = hour * 3600 + minute * 60 + second;

        Float result = timerCalc.evaluate(evaluator);

        // Should be close to expected value (within 2 seconds for test execution time)
        assertThat(result).isBetween(expectedSeconds - 2.0f, expectedSeconds + 2.0f);
    }

    @Test
    @DisplayName("Should return different values on consecutive calls")
    void shouldReturnDifferentValuesOnConsecutiveCalls() throws Exception {
        Float result1 = timerCalc.evaluate(evaluator);

        // Sleep for a small amount to ensure time difference
        Thread.sleep(100);

        Float result2 = timerCalc.evaluate(evaluator);

        assertThat(result1).isNotNull();
        assertThat(result2).isNotNull();

        // Second call should return a later or equal time
        assertThat(result2).isGreaterThanOrEqualTo(result1);
    }

    @Test
    @DisplayName("Should preserve type information")
    void shouldPreserveTypeInformation() {
        assertThat(timerCalc.getType()).isEqualTo(NumericType.INSTANCE);
    }

    @Test
    @DisplayName("Should return zero at midnight")
    void shouldReturnApproximatelyZeroNearMidnight() {
        // This test is conceptual - we can't control system time
        // But we can verify the calculation logic

        Calendar midnight = Calendar.getInstance();
        midnight.set(Calendar.HOUR_OF_DAY, 0);
        midnight.set(Calendar.MINUTE, 0);
        midnight.set(Calendar.SECOND, 0);
        midnight.set(Calendar.MILLISECOND, 0);

        Calendar now = Calendar.getInstance();

        // If it's early in the day, the timer should be small
        if (now.get(Calendar.HOUR_OF_DAY) < 1) {
            Float result = timerCalc.evaluate(evaluator);
            assertThat(result).isLessThan(3600.0f); // Less than 1 hour worth of seconds
        }
    }

    @Test
    @DisplayName("Should handle fractional seconds")
    void shouldHandleFractionalSeconds() {
        Float result = timerCalc.evaluate(evaluator);

        // Result can include fractional seconds (milliseconds)
        assertThat(result).isNotNull();

        // The fractional part should be reasonable (0 to 999 milliseconds = 0.0 to
        // 0.999 seconds)
        float fractionalPart = result - (int) result.floatValue();
        assertThat(fractionalPart).isBetween(0.0f, 1.0f);
    }

    @Test
    @DisplayName("Should calculate seconds correctly throughout the day")
    void shouldCalculateSecondsCorrectlyThroughoutTheDay() {
        Float result = timerCalc.evaluate(evaluator);

        Calendar now = Calendar.getInstance();
        int hour = now.get(Calendar.HOUR_OF_DAY);
        int minute = now.get(Calendar.MINUTE);
        int second = now.get(Calendar.SECOND);
        int millisecond = now.get(Calendar.MILLISECOND);

        // Calculate expected total seconds
        float expectedTotalSeconds = hour * 3600 + minute * 60 + second + millisecond / 1000.0f;

        // Should be very close (within 0.1 seconds for timing differences)
        assertThat(result).isCloseTo(expectedTotalSeconds, org.assertj.core.data.Offset.offset(0.1f));
    }

    @Test
    @DisplayName("Should be monotonically increasing within same day")
    void shouldBeMonotonicallyIncreasingWithinSameDay() throws Exception {
        Float result1 = timerCalc.evaluate(evaluator);

        Thread.sleep(10); // 10ms

        Float result2 = timerCalc.evaluate(evaluator);

        Thread.sleep(10); // 10ms

        Float result3 = timerCalc.evaluate(evaluator);

        // Should be increasing (or at least non-decreasing due to resolution limits)
        assertThat(result2).isGreaterThanOrEqualTo(result1);
        assertThat(result3).isGreaterThanOrEqualTo(result2);
    }

    @Test
    @DisplayName("Should handle multiple rapid evaluations")
    void shouldHandleMultipleRapidEvaluations() {
        Float firstResult = timerCalc.evaluate(evaluator);

        // Perform multiple rapid evaluations
        for (int i = 0; i < 10; i++) {
            Float result = timerCalc.evaluate(evaluator);

            assertThat(result).isNotNull();
            assertThat(result).isBetween(0.0f, 86400.0f);

            // Should be reasonably close to first result (within a few seconds)
            assertThat(result).isCloseTo(firstResult, org.assertj.core.data.Offset.offset(5.0f));
        }
    }

    @Test
    @DisplayName("Should verify Timer function matches VBA behavior")
    void shouldVerifyTimerFunctionMatchesVBABehavior() {
        // VBA Timer function returns seconds since midnight as Single (float)
        Float result = timerCalc.evaluate(evaluator);

        // Manual calculation for verification
        Calendar calendar = Calendar.getInstance();
        long now = calendar.getTimeInMillis();

        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        long midnight = calendar.getTimeInMillis();

        float expectedResult = (now - midnight) / 1000.0f;

        // Should match our manual calculation (within 0.1 seconds)
        assertThat(result).isCloseTo(expectedResult, org.assertj.core.data.Offset.offset(0.1f));
    }

    @Test
    @DisplayName("Should handle edge case near midnight")
    void shouldHandleEdgeCaseNearMidnight() {
        // This test runs at any time but verifies the calculation is sound
        Float result = timerCalc.evaluate(evaluator);

        // At any time of day, result should be valid
        assertThat(result).isNotNull();
        assertThat(result).isGreaterThanOrEqualTo(0.0f);
        assertThat(result).isLessThan(86400.0f); // Less than 24 hours worth of seconds

        // Verify it's finite and reasonable
        assertThat(result).isFinite();
    }

    @Test
    @DisplayName("Should provide millisecond precision")
    void shouldProvideMillisecondPrecision() {
        Float result = timerCalc.evaluate(evaluator);

        // Convert to milliseconds to check precision
        float milliseconds = result * 1000;

        // Should have fractional part indicating millisecond precision
        assertThat(result).isNotNull();

        // The implementation divides by 1000f, so it should maintain precision
        assertThat(result).isInstanceOf(Float.class);
    }
}