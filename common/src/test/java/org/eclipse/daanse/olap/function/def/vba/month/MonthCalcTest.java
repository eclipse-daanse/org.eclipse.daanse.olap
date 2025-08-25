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
package org.eclipse.daanse.olap.function.def.vba.month;

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

class MonthCalcTest {

    private MonthCalc monthCalc;
    private DateTimeCalc dateTimeCalc;
    private Evaluator evaluator;

    @BeforeEach
    void setUp() {
        dateTimeCalc = mock(DateTimeCalc.class);
        evaluator = mock(Evaluator.class);
        monthCalc = new MonthCalc(NumericType.INSTANCE, dateTimeCalc);
    }

    @ParameterizedTest(name = "{0}: month({1}) = {2}")
    @MethodSource("arguments")
    @DisplayName("Should extract month from date correctly")
    void shouldExtractMonth(String testName, Date inputDate, Integer expected) {
        when(dateTimeCalc.evaluate(evaluator)).thenReturn(inputDate);

        Integer result = monthCalc.evaluate(evaluator);

        assertThat(result).isEqualTo(expected);
    }

    static Stream<Arguments> arguments() {
        Calendar cal = Calendar.getInstance();

        // January 15, 2023
        cal.set(2023, Calendar.JANUARY, 15, 10, 30, 0);
        Date january = cal.getTime();

        // February 28, 2023
        cal.set(2023, Calendar.FEBRUARY, 28, 0, 0, 0);
        Date february = cal.getTime();

        // March 1, 2024
        cal.set(2024, Calendar.MARCH, 1, 12, 0, 0);
        Date march = cal.getTime();

        // June 15, 2023
        cal.set(2023, Calendar.JUNE, 15, 18, 45, 30);
        Date june = cal.getTime();

        // September 30, 2023
        cal.set(2023, Calendar.SEPTEMBER, 30, 23, 59, 59);
        Date september = cal.getTime();

        // December 31, 2025
        cal.set(2025, Calendar.DECEMBER, 31, 6, 0, 0);
        Date december = cal.getTime();

        return Stream.of(Arguments.of("January", january, 1), Arguments.of("February", february, 2),
                Arguments.of("March", march, 3), Arguments.of("June", june, 6), Arguments.of("September", september, 9),
                Arguments.of("December", december, 12));
    }

    @Test
    @DisplayName("Should preserve type information")
    void shouldPreserveTypeInformation() {
        assertThat(monthCalc.getType()).isEqualTo(NumericType.INSTANCE);
    }
}