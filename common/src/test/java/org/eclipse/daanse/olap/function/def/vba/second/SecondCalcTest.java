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
package org.eclipse.daanse.olap.function.def.vba.second;

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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class SecondCalcTest {

    private SecondCalc secondCalc;
    private DateTimeCalc dateTimeCalc;
    private Evaluator evaluator;

    @BeforeEach
    void setUp() {
        dateTimeCalc = mock(DateTimeCalc.class);
        evaluator = mock(Evaluator.class);
        secondCalc = new SecondCalc(NumericType.INSTANCE, dateTimeCalc);
    }

    @ParameterizedTest(name = "{0}: second({1}) = {2}")
    @MethodSource("arguments")
    @DisplayName("Should extract second from date correctly")
    void shouldExtractSecondFromDate(String testName, Date input, Integer expected) {
        when(dateTimeCalc.evaluate(evaluator)).thenReturn(input);

        Integer result = secondCalc.evaluate(evaluator);

        assertThat(result).isEqualTo(expected);
    }

    static Stream<Arguments> arguments() {
        return Stream.of(
                // Basic second extraction
                Arguments.of("start of minute", createDate(2024, Calendar.JANUARY, 1, 12, 30, 0), 0),
                Arguments.of("fifteen seconds", createDate(2024, Calendar.JANUARY, 1, 12, 30, 15), 15),
                Arguments.of("thirty seconds", createDate(2024, Calendar.JANUARY, 1, 12, 30, 30), 30),
                Arguments.of("forty-five seconds", createDate(2024, Calendar.JANUARY, 1, 12, 30, 45), 45),
                Arguments.of("end of minute", createDate(2024, Calendar.JANUARY, 1, 12, 30, 59), 59),

                // Different times of day
                Arguments.of("midnight", createDate(2024, Calendar.JANUARY, 1, 0, 0, 5), 5),
                Arguments.of("early morning", createDate(2024, Calendar.JANUARY, 1, 1, 23, 42), 42),
                Arguments.of("late morning", createDate(2024, Calendar.JANUARY, 1, 11, 37, 18), 18),
                Arguments.of("afternoon", createDate(2024, Calendar.JANUARY, 1, 15, 42, 33), 33),
                Arguments.of("evening", createDate(2024, Calendar.JANUARY, 1, 19, 18, 27), 27),
                Arguments.of("late night", createDate(2024, Calendar.JANUARY, 1, 23, 55, 51), 51),

                // Different dates - second should be independent of date/time
                Arguments.of("different year", createDate(2023, Calendar.DECEMBER, 31, 14, 25, 37), 37),
                Arguments.of("different month", createDate(2024, Calendar.JUNE, 15, 9, 33, 22), 22),
                Arguments.of("leap year", createDate(2024, Calendar.FEBRUARY, 29, 16, 47, 8), 8),

                // Edge cases
                Arguments.of("new year midnight", createDate(2024, Calendar.JANUARY, 1, 0, 0, 1), 1),
                Arguments.of("new year almost midnight", createDate(2024, Calendar.DECEMBER, 31, 23, 59, 58), 58),

                // Milliseconds should not affect second extraction
                Arguments.of("with milliseconds", createDate(2024, Calendar.JANUARY, 1, 12, 25, 30, 500), 30),
                Arguments.of("near next second", createDate(2024, Calendar.JANUARY, 1, 12, 25, 30, 999), 30),
                Arguments.of("start of second", createDate(2024, Calendar.JANUARY, 1, 12, 25, 30, 0), 30),

                // All possible second values (sample range)
                Arguments.of("second 0", createDate(2024, Calendar.JANUARY, 1, 12, 0, 0), 0),
                Arguments.of("second 1", createDate(2024, Calendar.JANUARY, 1, 12, 0, 1), 1),
                Arguments.of("second 10", createDate(2024, Calendar.JANUARY, 1, 12, 0, 10), 10),
                Arguments.of("second 20", createDate(2024, Calendar.JANUARY, 1, 12, 0, 20), 20),
                Arguments.of("second 35", createDate(2024, Calendar.JANUARY, 1, 12, 0, 35), 35),
                Arguments.of("second 50", createDate(2024, Calendar.JANUARY, 1, 12, 0, 50), 50),
                Arguments.of("second 59", createDate(2024, Calendar.JANUARY, 1, 12, 0, 59), 59),

                // Historical dates
                Arguments.of("historical date", createDate(1999, Calendar.DECEMBER, 31, 23, 59, 45), 45),
                Arguments.of("very old date", createDate(1900, Calendar.JANUARY, 1, 0, 5, 12), 12),

                // Future dates
                Arguments.of("future date", createDate(2030, Calendar.JULY, 4, 16, 30, 25), 25),

                // Different minutes and hours - should not affect second
                Arguments.of("different minute", createDate(2024, Calendar.JANUARY, 1, 12, 45, 30), 30),
                Arguments.of("different hour", createDate(2024, Calendar.JANUARY, 1, 6, 30, 30), 30),

                // DST transition dates (if applicable)
                Arguments.of("spring forward", createDate(2024, Calendar.MARCH, 10, 2, 15, 40), 40),
                Arguments.of("fall back", createDate(2024, Calendar.NOVEMBER, 3, 1, 45, 17), 17),

                // Precise timing scenarios
                Arguments.of("exactly half minute", createDate(2024, Calendar.JANUARY, 1, 12, 30, 30, 0), 30),
                Arguments.of("leap second scenario", createDate(2024, Calendar.JUNE, 30, 23, 59, 59), 59),

                // Random sampling of second values
                Arguments.of("random second 7", createDate(2024, Calendar.MARCH, 15, 8, 22, 7), 7),
                Arguments.of("random second 23", createDate(2024, Calendar.APRIL, 20, 14, 18, 23), 23),
                Arguments.of("random second 41", createDate(2024, Calendar.MAY, 25, 20, 5, 41), 41),
                Arguments.of("random second 56", createDate(2024, Calendar.JUNE, 30, 3, 47, 56), 56));
    }

    private static Date createDate(int year, int month, int day, int hour, int minute, int second) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month, day, hour, minute, second);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTime();
    }

    private static Date createDate(int year, int month, int day, int hour, int minute, int second, int millisecond) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month, day, hour, minute, second);
        calendar.set(Calendar.MILLISECOND, millisecond);
        return calendar.getTime();
    }
}