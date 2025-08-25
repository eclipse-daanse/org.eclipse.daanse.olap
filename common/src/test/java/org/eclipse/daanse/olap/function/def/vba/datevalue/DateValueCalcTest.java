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
package org.eclipse.daanse.olap.function.def.vba.datevalue;

import static org.assertj.core.api.Assertions.assertThat;
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

class DateValueCalcTest {

    private DateValueCalc dateValueCalc;
    private DateTimeCalc dateCalc;
    private Evaluator evaluator;

    @BeforeEach
    void setUp() {
        dateCalc = mock(DateTimeCalc.class);
        evaluator = mock(Evaluator.class);
        dateValueCalc = new DateValueCalc(DateTimeType.INSTANCE, dateCalc);
    }

    @ParameterizedTest(name = "{0}: DateValue strips time from {1}")
    @MethodSource("dateValueArguments")
    @DisplayName("Should strip time components and return date only")
    void shouldStripTimeComponentsAndReturnDateOnly(String testName, Date inputDate, Date expectedDate) {
        when(dateCalc.evaluate(evaluator)).thenReturn(inputDate);

        Date result = dateValueCalc.evaluate(evaluator);

        assertThat(result).isNotNull();

        Calendar resultCalendar = Calendar.getInstance();
        resultCalendar.setTime(result);

        Calendar expectedCalendar = Calendar.getInstance();
        expectedCalendar.setTime(expectedDate);

        // Check that date components match
        assertThat(resultCalendar.get(Calendar.YEAR)).isEqualTo(expectedCalendar.get(Calendar.YEAR));
        assertThat(resultCalendar.get(Calendar.MONTH)).isEqualTo(expectedCalendar.get(Calendar.MONTH));
        assertThat(resultCalendar.get(Calendar.DAY_OF_MONTH)).isEqualTo(expectedCalendar.get(Calendar.DAY_OF_MONTH));

        // Check that time components are set to midnight
        assertThat(resultCalendar.get(Calendar.HOUR_OF_DAY)).isEqualTo(0);
        assertThat(resultCalendar.get(Calendar.MINUTE)).isEqualTo(0);
        assertThat(resultCalendar.get(Calendar.SECOND)).isEqualTo(0);
        assertThat(resultCalendar.get(Calendar.MILLISECOND)).isEqualTo(0);
    }

    static Stream<Arguments> dateValueArguments() {
        Calendar cal1 = Calendar.getInstance();
        cal1.clear();
        cal1.set(2024, Calendar.MARCH, 15, 14, 30, 45);
        cal1.set(Calendar.MILLISECOND, 500);
        Date inputWithTime = cal1.getTime();

        Calendar cal2 = Calendar.getInstance();
        cal2.clear();
        cal2.set(2024, Calendar.MARCH, 15, 0, 0, 0);
        Date expectedDateOnly = cal2.getTime();

        Calendar cal3 = Calendar.getInstance();
        cal3.clear();
        cal3.set(2024, Calendar.DECEMBER, 31, 23, 59, 59);
        cal3.set(Calendar.MILLISECOND, 999);
        Date inputEndOfDay = cal3.getTime();

        Calendar cal4 = Calendar.getInstance();
        cal4.clear();
        cal4.set(2024, Calendar.DECEMBER, 31, 0, 0, 0);
        Date expectedEndOfDay = cal4.getTime();

        Calendar cal5 = Calendar.getInstance();
        cal5.clear();
        cal5.set(2024, Calendar.JANUARY, 1, 0, 0, 0);
        Date inputAlreadyMidnight = cal5.getTime();
        Date expectedAlreadyMidnight = cal5.getTime();

        return Stream.of(Arguments.of("datetime with time", inputWithTime, expectedDateOnly),
                Arguments.of("end of day", inputEndOfDay, expectedEndOfDay),
                Arguments.of("already midnight", inputAlreadyMidnight, expectedAlreadyMidnight));
    }

    @Test
    @DisplayName("Should handle date already at midnight")
    void shouldHandleDateAlreadyAtMidnight() {
        Calendar cal = Calendar.getInstance();
        cal.clear();
        cal.set(2024, Calendar.MARCH, 15, 0, 0, 0);
        Date midnightDate = cal.getTime();

        when(dateCalc.evaluate(evaluator)).thenReturn(midnightDate);

        Date result = dateValueCalc.evaluate(evaluator);

        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(midnightDate);

        Calendar resultCalendar = Calendar.getInstance();
        resultCalendar.setTime(result);

        assertThat(resultCalendar.get(Calendar.HOUR_OF_DAY)).isEqualTo(0);
        assertThat(resultCalendar.get(Calendar.MINUTE)).isEqualTo(0);
        assertThat(resultCalendar.get(Calendar.SECOND)).isEqualTo(0);
        assertThat(resultCalendar.get(Calendar.MILLISECOND)).isEqualTo(0);
    }

    @Test
    @DisplayName("Should handle leap year dates correctly")
    void shouldHandleLeapYearDatesCorrectly() {
        Calendar cal = Calendar.getInstance();
        cal.clear();
        cal.set(2024, Calendar.FEBRUARY, 29, 12, 30, 45);
        Date leapYearDate = cal.getTime();

        when(dateCalc.evaluate(evaluator)).thenReturn(leapYearDate);

        Date result = dateValueCalc.evaluate(evaluator);

        Calendar resultCalendar = Calendar.getInstance();
        resultCalendar.setTime(result);

        assertThat(resultCalendar.get(Calendar.YEAR)).isEqualTo(2024);
        assertThat(resultCalendar.get(Calendar.MONTH)).isEqualTo(Calendar.FEBRUARY);
        assertThat(resultCalendar.get(Calendar.DAY_OF_MONTH)).isEqualTo(29);
        assertThat(resultCalendar.get(Calendar.HOUR_OF_DAY)).isEqualTo(0);
        assertThat(resultCalendar.get(Calendar.MINUTE)).isEqualTo(0);
        assertThat(resultCalendar.get(Calendar.SECOND)).isEqualTo(0);
        assertThat(resultCalendar.get(Calendar.MILLISECOND)).isEqualTo(0);
    }

    @Test
    @DisplayName("Should handle different time zones consistently")
    void shouldHandleDifferentTimeZonesConsistently() {
        // Create a date with specific time
        Calendar cal = Calendar.getInstance();
        cal.clear();
        cal.set(2024, Calendar.JUNE, 15, 18, 45, 30);
        Date inputDate = cal.getTime();

        when(dateCalc.evaluate(evaluator)).thenReturn(inputDate);

        Date result = dateValueCalc.evaluate(evaluator);

        Calendar resultCalendar = Calendar.getInstance();
        resultCalendar.setTime(result);

        // Date portion should be preserved regardless of timezone
        assertThat(resultCalendar.get(Calendar.YEAR)).isEqualTo(2024);
        assertThat(resultCalendar.get(Calendar.MONTH)).isEqualTo(Calendar.JUNE);
        assertThat(resultCalendar.get(Calendar.DAY_OF_MONTH)).isEqualTo(15);

        // Time should be set to midnight
        assertThat(resultCalendar.get(Calendar.HOUR_OF_DAY)).isEqualTo(0);
        assertThat(resultCalendar.get(Calendar.MINUTE)).isEqualTo(0);
        assertThat(resultCalendar.get(Calendar.SECOND)).isEqualTo(0);
        assertThat(resultCalendar.get(Calendar.MILLISECOND)).isEqualTo(0);
    }

    @Test
    @DisplayName("Should handle historical dates correctly")
    void shouldHandleHistoricalDatesCorrectly() {
        Calendar cal = Calendar.getInstance();
        cal.clear();
        cal.set(1900, Calendar.JANUARY, 1, 15, 30, 0);
        Date historicalDate = cal.getTime();

        when(dateCalc.evaluate(evaluator)).thenReturn(historicalDate);

        Date result = dateValueCalc.evaluate(evaluator);

        Calendar resultCalendar = Calendar.getInstance();
        resultCalendar.setTime(result);

        assertThat(resultCalendar.get(Calendar.YEAR)).isEqualTo(1900);
        assertThat(resultCalendar.get(Calendar.MONTH)).isEqualTo(Calendar.JANUARY);
        assertThat(resultCalendar.get(Calendar.DAY_OF_MONTH)).isEqualTo(1);
        assertThat(resultCalendar.get(Calendar.HOUR_OF_DAY)).isEqualTo(0);
    }

    @Test
    @DisplayName("Should handle future dates correctly")
    void shouldHandleFutureDatesCorrectly() {
        Calendar cal = Calendar.getInstance();
        cal.clear();
        cal.set(2099, Calendar.DECEMBER, 31, 23, 59, 59);
        Date futureDate = cal.getTime();

        when(dateCalc.evaluate(evaluator)).thenReturn(futureDate);

        Date result = dateValueCalc.evaluate(evaluator);

        Calendar resultCalendar = Calendar.getInstance();
        resultCalendar.setTime(result);

        assertThat(resultCalendar.get(Calendar.YEAR)).isEqualTo(2099);
        assertThat(resultCalendar.get(Calendar.MONTH)).isEqualTo(Calendar.DECEMBER);
        assertThat(resultCalendar.get(Calendar.DAY_OF_MONTH)).isEqualTo(31);
        assertThat(resultCalendar.get(Calendar.HOUR_OF_DAY)).isEqualTo(0);
    }

    @Test
    @DisplayName("Should preserve type information")
    void shouldPreserveTypeInformation() {
        assertThat(dateValueCalc.getType()).isEqualTo(DateTimeType.INSTANCE);
    }

    @Test
    @DisplayName("Should be consistent across multiple calls")
    void shouldBeConsistentAcrossMultipleCalls() {
        Calendar cal = Calendar.getInstance();
        cal.clear();
        cal.set(2024, Calendar.MARCH, 15, 14, 30, 45);
        Date inputDate = cal.getTime();

        when(dateCalc.evaluate(evaluator)).thenReturn(inputDate);

        Date first = dateValueCalc.evaluate(evaluator);
        Date second = dateValueCalc.evaluate(evaluator);

        assertThat(first).isEqualTo(second);
    }

    @Test
    @DisplayName("Should handle millisecond precision stripping")
    void shouldHandleMillisecondPrecisionStripping() {
        Calendar cal = Calendar.getInstance();
        cal.clear();
        cal.set(2024, Calendar.MARCH, 15, 14, 30, 45);
        cal.set(Calendar.MILLISECOND, 999);
        Date inputDate = cal.getTime();

        when(dateCalc.evaluate(evaluator)).thenReturn(inputDate);

        Date result = dateValueCalc.evaluate(evaluator);

        Calendar resultCalendar = Calendar.getInstance();
        resultCalendar.setTime(result);

        assertThat(resultCalendar.get(Calendar.MILLISECOND)).isEqualTo(0);
    }
}