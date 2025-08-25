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
package org.eclipse.daanse.olap.function.def.vba.day;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Calendar;
import java.util.Date;
import java.util.stream.Stream;

import org.eclipse.daanse.olap.api.Evaluator;
import org.eclipse.daanse.olap.api.calc.DateTimeCalc;
import org.eclipse.daanse.olap.api.type.NumericType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class DayCalcTest {

    private DayCalc dayCalc;
    private DateTimeCalc dateTimeCalc;
    private Evaluator evaluator;

    @BeforeEach
    void setUp() {
        dateTimeCalc = mock(DateTimeCalc.class);
        evaluator = mock(Evaluator.class);
        dayCalc = new DayCalc(NumericType.INSTANCE, dateTimeCalc);
    }

    @ParameterizedTest(name = "{0}: day({1}) = {2}")
    @MethodSource("arguments")
    @DisplayName("Should extract day from date correctly")
    void shouldExtractDay(String testName, Date inputDate, Integer expected) {
        when(dateTimeCalc.evaluate(evaluator)).thenReturn(inputDate);

        Integer result = dayCalc.evaluate(evaluator);

        assertThat(result).isEqualTo(expected);
    }

    static Stream<Arguments> arguments() {
        Calendar cal = Calendar.getInstance();

        // January 1, 2023
        cal.set(2023, Calendar.JANUARY, 1, 0, 0, 0);
        Date jan1 = cal.getTime();

        // February 28, 2023 (non-leap year)
        cal.set(2023, Calendar.FEBRUARY, 28, 12, 30, 45);
        Date feb28 = cal.getTime();

        // March 31, 2024
        cal.set(2024, Calendar.MARCH, 31, 23, 59, 59);
        Date mar31 = cal.getTime();

        // February 29, 2024 (leap year)
        cal.set(2024, Calendar.FEBRUARY, 29, 6, 15, 30);
        Date feb29LeapYear = cal.getTime();

        // December 31, 2025
        cal.set(2025, Calendar.DECEMBER, 31, 18, 45, 0);
        Date dec31 = cal.getTime();

        // Random mid-month date
        cal.set(2023, Calendar.JUNE, 15, 9, 30, 0);
        Date jun15 = cal.getTime();

        return Stream.of(Arguments.of("first day of month", jan1, 1),
                Arguments.of("last day of February non-leap", feb28, 28), Arguments.of("last day of March", mar31, 31),
                Arguments.of("leap year February 29", feb29LeapYear, 29), Arguments.of("last day of year", dec31, 31),
                Arguments.of("mid-month date", jun15, 15));
    }

    @Test
    @DisplayName("Should preserve type information")
    void shouldPreserveTypeInformation() {
        assertThat(dayCalc.getType()).isEqualTo(NumericType.INSTANCE);
    }
}