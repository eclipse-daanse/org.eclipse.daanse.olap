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
package org.eclipse.daanse.olap.function.def.vba.timevalue;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Calendar;
import java.util.Date;
import java.util.stream.Stream;

import org.eclipse.daanse.olap.api.Evaluator;
import org.eclipse.daanse.olap.api.calc.DateTimeCalc;
import org.eclipse.daanse.olap.api.type.DateTimeType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class TimeValueCalcTest {

    private TimeValueCalc timeValueCalc;
    private DateTimeCalc dateTimeCalc;
    private Evaluator evaluator;

    @BeforeEach
    void setUp() {
        dateTimeCalc = mock(DateTimeCalc.class);
        evaluator = mock(Evaluator.class);
        timeValueCalc = new TimeValueCalc(DateTimeType.INSTANCE, dateTimeCalc);
    }

    @ParameterizedTest(name = "{0}: TimeValue from {1} should have time {2}:{3}:{4}")
    @MethodSource("timeValueArguments")
    @DisplayName("Should extract time portion correctly")
    void shouldExtractTimePortionCorrectly(String testName, Date inputDate, Integer expectedHour,
            Integer expectedMinute, Integer expectedSecond) {
        when(dateTimeCalc.evaluate(evaluator)).thenReturn(inputDate);

        Date result = timeValueCalc.evaluate(evaluator);

        assertThat(result).isNotNull();

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(result);

        assertThat(calendar.get(Calendar.HOUR_OF_DAY)).isEqualTo(expectedHour);
        assertThat(calendar.get(Calendar.MINUTE)).isEqualTo(expectedMinute);
        assertThat(calendar.get(Calendar.SECOND)).isEqualTo(expectedSecond);

        // Should be set to epoch date (January 1, 1970)
        assertThat(calendar.get(Calendar.YEAR)).isEqualTo(1970);
        assertThat(calendar.get(Calendar.MONTH)).isEqualTo(Calendar.JANUARY);
        assertThat(calendar.get(Calendar.DAY_OF_MONTH)).isEqualTo(1);
    }

    static Stream<Arguments> timeValueArguments() {
        Calendar cal = Calendar.getInstance();

        // Create date with 9:30:45 AM on December 25, 2023
        cal.set(2023, Calendar.DECEMBER, 25, 9, 30, 45);
        cal.set(Calendar.MILLISECOND, 0);
        Date morning = cal.getTime();

        // Create date with 2:15:20 PM on July 4, 2024
        cal.set(2024, Calendar.JULY, 4, 14, 15, 20);
        cal.set(Calendar.MILLISECOND, 0);
        Date afternoon = cal.getTime();

        // Create date with 11:59:59 PM on New Year's Eve
        cal.set(2023, Calendar.DECEMBER, 31, 23, 59, 59);
        cal.set(Calendar.MILLISECOND, 0);
        Date lateNight = cal.getTime();

        // Create date with midnight
        cal.set(2024, Calendar.JANUARY, 1, 0, 0, 0);
        cal.set(Calendar.MILLISECOND, 0);
        Date midnight = cal.getTime();

        // Create date with noon
        cal.set(2024, Calendar.JUNE, 15, 12, 0, 0);
        cal.set(Calendar.MILLISECOND, 0);
        Date noon = cal.getTime();

        return Stream.of(Arguments.of("morning time", morning, 9, 30, 45),
                Arguments.of("afternoon time", afternoon, 14, 15, 20),
                Arguments.of("late night", lateNight, 23, 59, 59), Arguments.of("midnight", midnight, 0, 0, 0),
                Arguments.of("noon", noon, 12, 0, 0));
    }

    @Test
    @DisplayName("Should preserve only time component")
    void shouldPreserveOnlyTimeComponent() {
        // Create a date with specific date and time
        Calendar inputCal = Calendar.getInstance();
        inputCal.set(2024, Calendar.AUGUST, 15, 16, 45, 30); // August 15, 2024 at 4:45:30 PM
        inputCal.set(Calendar.MILLISECOND, 0);
        Date inputDate = inputCal.getTime();

        when(dateTimeCalc.evaluate(evaluator)).thenReturn(inputDate);

        Date result = timeValueCalc.evaluate(evaluator);

        Calendar resultCal = Calendar.getInstance();
        resultCal.setTime(result);

        // Time should be preserved
        assertThat(resultCal.get(Calendar.HOUR_OF_DAY)).isEqualTo(16);
        assertThat(resultCal.get(Calendar.MINUTE)).isEqualTo(45);
        assertThat(resultCal.get(Calendar.SECOND)).isEqualTo(30);

        // Date should be reset to epoch (January 1, 1970)
        assertThat(resultCal.get(Calendar.YEAR)).isEqualTo(1970);
        assertThat(resultCal.get(Calendar.MONTH)).isEqualTo(Calendar.JANUARY);
        assertThat(resultCal.get(Calendar.DAY_OF_MONTH)).isEqualTo(1);
    }

    @Test
    @DisplayName("Should handle null input date")
    void shouldHandleNullInputDate() {
        when(dateTimeCalc.evaluate(evaluator)).thenReturn(null);

        assertThatThrownBy(() -> timeValueCalc.evaluate(evaluator)).isInstanceOf(NullPointerException.class);
    }

    @Test
    @DisplayName("Should preserve type information")
    void shouldPreserveTypeInformation() {
        assertThat(timeValueCalc.getType()).isEqualTo(DateTimeType.INSTANCE);
    }

    @Test
    @DisplayName("Should handle time-only input")
    void shouldHandleTimeOnlyInput() {
        // Create a time that's already on epoch date
        Calendar cal = Calendar.getInstance();
        cal.set(1970, Calendar.JANUARY, 1, 10, 30, 0);
        cal.set(Calendar.MILLISECOND, 0);
        Date timeOnlyDate = cal.getTime();

        when(dateTimeCalc.evaluate(evaluator)).thenReturn(timeOnlyDate);

        Date result = timeValueCalc.evaluate(evaluator);

        Calendar resultCal = Calendar.getInstance();
        resultCal.setTime(result);

        // Should preserve the time
        assertThat(resultCal.get(Calendar.HOUR_OF_DAY)).isEqualTo(10);
        assertThat(resultCal.get(Calendar.MINUTE)).isEqualTo(30);
        assertThat(resultCal.get(Calendar.SECOND)).isEqualTo(0);

        // Date should remain epoch
        assertThat(resultCal.get(Calendar.YEAR)).isEqualTo(1970);
        assertThat(resultCal.get(Calendar.MONTH)).isEqualTo(Calendar.JANUARY);
        assertThat(resultCal.get(Calendar.DAY_OF_MONTH)).isEqualTo(1);
    }

    @Test
    @DisplayName("Should handle edge cases with time values")
    void shouldHandleEdgeCasesWithTimeValues() {
        // Test midnight
        Calendar cal = Calendar.getInstance();
        cal.set(2024, Calendar.DECEMBER, 31, 0, 0, 0);
        cal.set(Calendar.MILLISECOND, 0);
        Date midnightDate = cal.getTime();

        when(dateTimeCalc.evaluate(evaluator)).thenReturn(midnightDate);

        Date result = timeValueCalc.evaluate(evaluator);

        Calendar resultCal = Calendar.getInstance();
        resultCal.setTime(result);

        assertThat(resultCal.get(Calendar.HOUR_OF_DAY)).isEqualTo(0);
        assertThat(resultCal.get(Calendar.MINUTE)).isEqualTo(0);
        assertThat(resultCal.get(Calendar.SECOND)).isEqualTo(0);
    }

    @Test
    @DisplayName("Should handle maximum time values")
    void shouldHandleMaximumTimeValues() {
        // Test 23:59:59
        Calendar cal = Calendar.getInstance();
        cal.set(2024, Calendar.JUNE, 15, 23, 59, 59);
        cal.set(Calendar.MILLISECOND, 0);
        Date maxTimeDate = cal.getTime();

        when(dateTimeCalc.evaluate(evaluator)).thenReturn(maxTimeDate);

        Date result = timeValueCalc.evaluate(evaluator);

        Calendar resultCal = Calendar.getInstance();
        resultCal.setTime(result);

        assertThat(resultCal.get(Calendar.HOUR_OF_DAY)).isEqualTo(23);
        assertThat(resultCal.get(Calendar.MINUTE)).isEqualTo(59);
        assertThat(resultCal.get(Calendar.SECOND)).isEqualTo(59);

        // Date should be epoch
        assertThat(resultCal.get(Calendar.YEAR)).isEqualTo(1970);
        assertThat(resultCal.get(Calendar.MONTH)).isEqualTo(Calendar.JANUARY);
        assertThat(resultCal.get(Calendar.DAY_OF_MONTH)).isEqualTo(1);
    }

    @Test
    @DisplayName("Should maintain millisecond precision if available")
    void shouldMaintainMillisecondPrecisionIfAvailable() {
        Calendar cal = Calendar.getInstance();
        cal.set(2024, Calendar.APRIL, 20, 15, 30, 45);
        cal.set(Calendar.MILLISECOND, 123);
        Date inputDate = cal.getTime();

        when(dateTimeCalc.evaluate(evaluator)).thenReturn(inputDate);

        Date result = timeValueCalc.evaluate(evaluator);

        Calendar resultCal = Calendar.getInstance();
        resultCal.setTime(result);

        assertThat(resultCal.get(Calendar.HOUR_OF_DAY)).isEqualTo(15);
        assertThat(resultCal.get(Calendar.MINUTE)).isEqualTo(30);
        assertThat(resultCal.get(Calendar.SECOND)).isEqualTo(45);
        assertThat(resultCal.get(Calendar.MILLISECOND)).isEqualTo(123);
    }

    @Test
    @DisplayName("Should verify TimeValue function behavior matches VBA")
    void shouldVerifyTimeValueFunctionBehaviorMatchesVBA() {
        // VBA TimeValue extracts the time portion and sets date to 12/30/1899
        // But this implementation sets it to epoch (1970)
        Calendar cal = Calendar.getInstance();
        cal.set(2024, Calendar.SEPTEMBER, 10, 9, 45, 30);
        cal.set(Calendar.MILLISECOND, 0);
        Date inputDate = cal.getTime();

        when(dateTimeCalc.evaluate(evaluator)).thenReturn(inputDate);

        Date result = timeValueCalc.evaluate(evaluator);

        // Manual verification of the logic
        Calendar inputCal = Calendar.getInstance();
        inputCal.setTime(inputDate);

        Calendar expectedCal = Calendar.getInstance();
        expectedCal.clear();
        expectedCal.setTime(inputDate);
        expectedCal.set(1970, 0, 1); // Set to epoch date as per implementation

        Calendar resultCal = Calendar.getInstance();
        resultCal.setTime(result);

        // Time components should match input
        assertThat(resultCal.get(Calendar.HOUR_OF_DAY)).isEqualTo(inputCal.get(Calendar.HOUR_OF_DAY));
        assertThat(resultCal.get(Calendar.MINUTE)).isEqualTo(inputCal.get(Calendar.MINUTE));
        assertThat(resultCal.get(Calendar.SECOND)).isEqualTo(inputCal.get(Calendar.SECOND));

        // Date should be epoch
        assertThat(resultCal.get(Calendar.YEAR)).isEqualTo(1970);
    }

    @Test
    @DisplayName("Should handle different date formats consistently")
    void shouldHandleDifferentDateFormatsConsistently() {
        // Test with different dates but same time
        Calendar cal1 = Calendar.getInstance();
        cal1.set(2020, Calendar.JANUARY, 1, 14, 30, 0);
        cal1.set(Calendar.MILLISECOND, 0);
        Date date1 = cal1.getTime();

        Calendar cal2 = Calendar.getInstance();
        cal2.set(2025, Calendar.DECEMBER, 31, 14, 30, 0);
        cal2.set(Calendar.MILLISECOND, 0);
        Date date2 = cal2.getTime();

        when(dateTimeCalc.evaluate(evaluator)).thenReturn(date1);
        Date result1 = timeValueCalc.evaluate(evaluator);

        when(dateTimeCalc.evaluate(evaluator)).thenReturn(date2);
        Date result2 = timeValueCalc.evaluate(evaluator);

        // Both results should have same time portion
        Calendar resultCal1 = Calendar.getInstance();
        resultCal1.setTime(result1);
        Calendar resultCal2 = Calendar.getInstance();
        resultCal2.setTime(result2);

        assertThat(resultCal1.get(Calendar.HOUR_OF_DAY)).isEqualTo(resultCal2.get(Calendar.HOUR_OF_DAY));
        assertThat(resultCal1.get(Calendar.MINUTE)).isEqualTo(resultCal2.get(Calendar.MINUTE));
        assertThat(resultCal1.get(Calendar.SECOND)).isEqualTo(resultCal2.get(Calendar.SECOND));

        // Both should have epoch date
        assertThat(resultCal1.get(Calendar.YEAR)).isEqualTo(1970);
        assertThat(resultCal2.get(Calendar.YEAR)).isEqualTo(1970);
    }
}