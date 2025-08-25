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
package org.eclipse.daanse.olap.function.def.vba.minute;

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

class MinuteCalcTest {

    private MinuteCalc minuteCalc;
    private DateTimeCalc dateTimeCalc;
    private Evaluator evaluator;

    @BeforeEach
    void setUp() {
        dateTimeCalc = mock(DateTimeCalc.class);
        evaluator = mock(Evaluator.class);
        minuteCalc = new MinuteCalc(NumericType.INSTANCE, dateTimeCalc);
    }

    @ParameterizedTest(name = "{0}: minute({1}) = {2}")
    @MethodSource("arguments")
    @DisplayName("Should extract minute from date correctly")
    void shouldExtractMinuteFromDate(String testName, Date input, Integer expected) {
        when(dateTimeCalc.evaluate(evaluator)).thenReturn(input);

        Integer result = minuteCalc.evaluate(evaluator);

        assertThat(result).isEqualTo(expected);
    }

    static Stream<Arguments> arguments() {
        return Stream.of(
                // Basic minute extraction
                Arguments.of("midnight", createDate(2024, Calendar.JANUARY, 1, 0, 0, 0), 0),
                Arguments.of("start of hour", createDate(2024, Calendar.JANUARY, 1, 12, 0, 0), 0),
                Arguments.of("fifteen minutes", createDate(2024, Calendar.JANUARY, 1, 12, 15, 0), 15),
                Arguments.of("thirty minutes", createDate(2024, Calendar.JANUARY, 1, 12, 30, 0), 30),
                Arguments.of("forty-five minutes", createDate(2024, Calendar.JANUARY, 1, 12, 45, 0), 45),
                Arguments.of("end of hour", createDate(2024, Calendar.JANUARY, 1, 12, 59, 0), 59),

                // Different hours
                Arguments.of("early morning", createDate(2024, Calendar.JANUARY, 1, 1, 23, 0), 23),
                Arguments.of("late morning", createDate(2024, Calendar.JANUARY, 1, 11, 37, 0), 37),
                Arguments.of("afternoon", createDate(2024, Calendar.JANUARY, 1, 15, 42, 0), 42),
                Arguments.of("evening", createDate(2024, Calendar.JANUARY, 1, 19, 18, 0), 18),
                Arguments.of("late night", createDate(2024, Calendar.JANUARY, 1, 23, 55, 0), 55),

                // Different dates - minute should be independent of date
                Arguments.of("different year", createDate(2023, Calendar.DECEMBER, 31, 14, 25, 0), 25),
                Arguments.of("different month", createDate(2024, Calendar.JUNE, 15, 9, 33, 0), 33),
                Arguments.of("leap year", createDate(2024, Calendar.FEBRUARY, 29, 16, 47, 0), 47),

                // Edge cases
                Arguments.of("new year midnight", createDate(2024, Calendar.JANUARY, 1, 0, 1, 0), 1),
                Arguments.of("new year almost midnight", createDate(2024, Calendar.DECEMBER, 31, 23, 58, 0), 58),

                // Different seconds - should not affect minute
                Arguments.of("with seconds", createDate(2024, Calendar.JANUARY, 1, 12, 25, 30), 25),
                Arguments.of("with milliseconds", createDate(2024, Calendar.JANUARY, 1, 12, 25, 30, 500), 25),

                // All possible minute values
                Arguments.of("minute 0", createDate(2024, Calendar.JANUARY, 1, 12, 0, 0), 0),
                Arguments.of("minute 1", createDate(2024, Calendar.JANUARY, 1, 12, 1, 0), 1),
                Arguments.of("minute 10", createDate(2024, Calendar.JANUARY, 1, 12, 10, 0), 10),
                Arguments.of("minute 20", createDate(2024, Calendar.JANUARY, 1, 12, 20, 0), 20),
                Arguments.of("minute 35", createDate(2024, Calendar.JANUARY, 1, 12, 35, 0), 35),
                Arguments.of("minute 50", createDate(2024, Calendar.JANUARY, 1, 12, 50, 0), 50),
                Arguments.of("minute 59", createDate(2024, Calendar.JANUARY, 1, 12, 59, 0), 59),

                // Historical dates
                Arguments.of("historical date", createDate(1999, Calendar.DECEMBER, 31, 23, 59, 0), 59),
                Arguments.of("very old date", createDate(1900, Calendar.JANUARY, 1, 0, 5, 0), 5),

                // Future dates
                Arguments.of("future date", createDate(2030, Calendar.JULY, 4, 16, 30, 0), 30),

                // DST transition dates (if applicable)
                Arguments.of("spring forward", createDate(2024, Calendar.MARCH, 10, 2, 15, 0), 15),
                Arguments.of("fall back", createDate(2024, Calendar.NOVEMBER, 3, 1, 45, 0), 45));
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