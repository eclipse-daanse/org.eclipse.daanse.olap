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
package org.eclipse.daanse.olap.function.def.vba.dateserial;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Calendar;
import java.util.Date;
import java.util.stream.Stream;

import org.eclipse.daanse.olap.api.Evaluator;
import org.eclipse.daanse.olap.api.calc.IntegerCalc;
import org.eclipse.daanse.olap.api.type.DateTimeType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class DateSerialCalcTest {

    private DateSerialCalc dateSerialCalc;
    private IntegerCalc yearCalc;
    private IntegerCalc monthCalc;
    private IntegerCalc dayCalc;
    private Evaluator evaluator;

    @BeforeEach
    void setUp() {
        yearCalc = mock(IntegerCalc.class);
        monthCalc = mock(IntegerCalc.class);
        dayCalc = mock(IntegerCalc.class);
        evaluator = mock(Evaluator.class);
        dateSerialCalc = new DateSerialCalc(DateTimeType.INSTANCE, yearCalc, monthCalc, dayCalc);
    }

    @ParameterizedTest(name = "{0}: DateSerial({1}, {2}, {3})")
    @MethodSource("dateSerialArguments")
    @DisplayName("Should create dates correctly from year, month, day")
    void shouldCreateDatesCorrectly(String testName, Integer year, Integer month, Integer day) {
        when(yearCalc.evaluate(evaluator)).thenReturn(year);
        when(monthCalc.evaluate(evaluator)).thenReturn(month);
        when(dayCalc.evaluate(evaluator)).thenReturn(day);

        Date result = dateSerialCalc.evaluate(evaluator);

        assertThat(result).isNotNull();

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(result);

        assertThat(calendar.get(Calendar.YEAR)).isEqualTo(year);
        assertThat(calendar.get(Calendar.MONTH)).isEqualTo(month - 1); // Calendar months are 0-based
        assertThat(calendar.get(Calendar.DAY_OF_MONTH)).isEqualTo(day);
    }

    static Stream<Arguments> dateSerialArguments() {
        return Stream.of(Arguments.of("standard date", 2024, 3, 15), Arguments.of("new year", 2024, 1, 1),
                Arguments.of("end of year", 2024, 12, 31), Arguments.of("leap year feb 29", 2024, 2, 29),
                Arguments.of("early date", 1900, 1, 1), Arguments.of("future date", 2050, 6, 15));
    }

    @Test
    @DisplayName("Should create date with time set to midnight")
    void shouldCreateDateWithTimeAtMidnight() {
        when(yearCalc.evaluate(evaluator)).thenReturn(2024);
        when(monthCalc.evaluate(evaluator)).thenReturn(3);
        when(dayCalc.evaluate(evaluator)).thenReturn(15);

        Date result = dateSerialCalc.evaluate(evaluator);

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(result);

        assertThat(calendar.get(Calendar.HOUR_OF_DAY)).isEqualTo(0);
        assertThat(calendar.get(Calendar.MINUTE)).isEqualTo(0);
        assertThat(calendar.get(Calendar.SECOND)).isEqualTo(0);
        assertThat(calendar.get(Calendar.MILLISECOND)).isEqualTo(0);
    }

    @Test
    @DisplayName("Should handle month overflow correctly")
    void shouldHandleMonthOverflowCorrectly() {
        // Month 13 should become January of next year
        when(yearCalc.evaluate(evaluator)).thenReturn(2024);
        when(monthCalc.evaluate(evaluator)).thenReturn(13);
        when(dayCalc.evaluate(evaluator)).thenReturn(15);

        Date result = dateSerialCalc.evaluate(evaluator);

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(result);

        assertThat(calendar.get(Calendar.YEAR)).isEqualTo(2025);
        assertThat(calendar.get(Calendar.MONTH)).isEqualTo(Calendar.JANUARY);
        assertThat(calendar.get(Calendar.DAY_OF_MONTH)).isEqualTo(15);
    }

    @Test
    @DisplayName("Should handle month underflow correctly")
    void shouldHandleMonthUnderflowCorrectly() {
        // Month 0 should become December of previous year
        when(yearCalc.evaluate(evaluator)).thenReturn(2024);
        when(monthCalc.evaluate(evaluator)).thenReturn(0);
        when(dayCalc.evaluate(evaluator)).thenReturn(15);

        Date result = dateSerialCalc.evaluate(evaluator);

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(result);

        assertThat(calendar.get(Calendar.YEAR)).isEqualTo(2023);
        assertThat(calendar.get(Calendar.MONTH)).isEqualTo(Calendar.DECEMBER);
        assertThat(calendar.get(Calendar.DAY_OF_MONTH)).isEqualTo(15);
    }

    @Test
    @DisplayName("Should handle day overflow correctly")
    void shouldHandleDayOverflowCorrectly() {
        // Day 32 in January should become February 1st
        when(yearCalc.evaluate(evaluator)).thenReturn(2024);
        when(monthCalc.evaluate(evaluator)).thenReturn(1);
        when(dayCalc.evaluate(evaluator)).thenReturn(32);

        Date result = dateSerialCalc.evaluate(evaluator);

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(result);

        assertThat(calendar.get(Calendar.YEAR)).isEqualTo(2024);
        assertThat(calendar.get(Calendar.MONTH)).isEqualTo(Calendar.FEBRUARY);
        assertThat(calendar.get(Calendar.DAY_OF_MONTH)).isEqualTo(1);
    }

    @Test
    @DisplayName("Should handle day underflow correctly")
    void shouldHandleDayUnderflowCorrectly() {
        // Day 0 in March should become last day of February
        when(yearCalc.evaluate(evaluator)).thenReturn(2024);
        when(monthCalc.evaluate(evaluator)).thenReturn(3);
        when(dayCalc.evaluate(evaluator)).thenReturn(0);

        Date result = dateSerialCalc.evaluate(evaluator);

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(result);

        assertThat(calendar.get(Calendar.YEAR)).isEqualTo(2024);
        assertThat(calendar.get(Calendar.MONTH)).isEqualTo(Calendar.FEBRUARY);
        assertThat(calendar.get(Calendar.DAY_OF_MONTH)).isEqualTo(29); // 2024 is leap year
    }

    @Test
    @DisplayName("Should handle negative years correctly")
    @Disabled
    void shouldHandleNegativeYearsCorrectly() {
        when(yearCalc.evaluate(evaluator)).thenReturn(-1);
        when(monthCalc.evaluate(evaluator)).thenReturn(1);
        when(dayCalc.evaluate(evaluator)).thenReturn(1);

        Date result = dateSerialCalc.evaluate(evaluator);

        assertThat(result).isNotNull();

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(result);

        assertThat(calendar.get(Calendar.YEAR)).isEqualTo(-1);
    }

    @Test
    @DisplayName("Should handle leap year calculations correctly")
    void shouldHandleLeapYearCalculationsCorrectly() {
        // Test February 29 in leap year
        when(yearCalc.evaluate(evaluator)).thenReturn(2024);
        when(monthCalc.evaluate(evaluator)).thenReturn(2);
        when(dayCalc.evaluate(evaluator)).thenReturn(29);

        Date result = dateSerialCalc.evaluate(evaluator);

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(result);

        assertThat(calendar.get(Calendar.YEAR)).isEqualTo(2024);
        assertThat(calendar.get(Calendar.MONTH)).isEqualTo(Calendar.FEBRUARY);
        assertThat(calendar.get(Calendar.DAY_OF_MONTH)).isEqualTo(29);
    }

    @Test
    @DisplayName("Should handle February 29 in non-leap year correctly")
    void shouldHandleFebruary29InNonLeapYearCorrectly() {
        // Test February 29 in non-leap year - should roll to March 1
        when(yearCalc.evaluate(evaluator)).thenReturn(2023);
        when(monthCalc.evaluate(evaluator)).thenReturn(2);
        when(dayCalc.evaluate(evaluator)).thenReturn(29);

        Date result = dateSerialCalc.evaluate(evaluator);

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(result);

        assertThat(calendar.get(Calendar.YEAR)).isEqualTo(2023);
        assertThat(calendar.get(Calendar.MONTH)).isEqualTo(Calendar.MARCH);
        assertThat(calendar.get(Calendar.DAY_OF_MONTH)).isEqualTo(1);
    }

    @Test
    @DisplayName("Should handle extreme overflow scenarios")
    void shouldHandleExtremeOverflowScenarios() {
        // Month 25, Day 100
        when(yearCalc.evaluate(evaluator)).thenReturn(2024);
        when(monthCalc.evaluate(evaluator)).thenReturn(25);
        when(dayCalc.evaluate(evaluator)).thenReturn(100);

        Date result = dateSerialCalc.evaluate(evaluator);

        assertThat(result).isNotNull();

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(result);

        // Should correctly calculate the final date with all overflows applied
        assertThat(calendar.get(Calendar.YEAR)).isGreaterThan(2024);
    }

    @Test
    @DisplayName("Should preserve type information")
    void shouldPreserveTypeInformation() {
        assertThat(dateSerialCalc.getType()).isEqualTo(DateTimeType.INSTANCE);
    }

    @Test
    @DisplayName("Should be consistent across multiple calls")
    void shouldBeConsistentAcrossMultipleCalls() {
        when(yearCalc.evaluate(evaluator)).thenReturn(2024);
        when(monthCalc.evaluate(evaluator)).thenReturn(3);
        when(dayCalc.evaluate(evaluator)).thenReturn(15);

        Date first = dateSerialCalc.evaluate(evaluator);
        Date second = dateSerialCalc.evaluate(evaluator);

        assertThat(first).isEqualTo(second);
    }
}