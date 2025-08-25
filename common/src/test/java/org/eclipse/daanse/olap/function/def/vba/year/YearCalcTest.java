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
package org.eclipse.daanse.olap.function.def.vba.year;

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

class YearCalcTest {

    private YearCalc yearCalc;
    private DateTimeCalc dateTimeCalc;
    private Evaluator evaluator;

    @BeforeEach
    void setUp() {
        dateTimeCalc = mock(DateTimeCalc.class);
        evaluator = mock(Evaluator.class);
        yearCalc = new YearCalc(NumericType.INSTANCE, dateTimeCalc);
    }

    @ParameterizedTest(name = "{0}: year({1}) = {2}")
    @MethodSource("arguments")
    @DisplayName("Should extract year from date correctly")
    void shouldExtractYear(String testName, Date inputDate, Integer expected) {
        when(dateTimeCalc.evaluate(evaluator)).thenReturn(inputDate);

        Integer result = yearCalc.evaluate(evaluator);

        assertThat(result).isEqualTo(expected);
    }

    static Stream<Arguments> arguments() {
        Calendar cal = Calendar.getInstance();

        // Year 2000 (leap year, Y2K)
        cal.set(2000, Calendar.JANUARY, 1, 0, 0, 0);
        Date year2000 = cal.getTime();

        // Year 2023 (recent year)
        cal.set(2023, Calendar.JULY, 15, 12, 30, 45);
        Date year2023 = cal.getTime();

        // Year 2024 (leap year)
        cal.set(2024, Calendar.FEBRUARY, 29, 6, 15, 30);
        Date year2024 = cal.getTime();

        // Year 1999 (pre-Y2K)
        cal.set(1999, Calendar.DECEMBER, 31, 23, 59, 59);
        Date year1999 = cal.getTime();

        // Year 1970 (Unix epoch)
        cal.set(1970, Calendar.JANUARY, 1, 0, 0, 0);
        Date year1970 = cal.getTime();

        // Year 2025 (future)
        cal.set(2025, Calendar.JUNE, 10, 18, 0, 0);
        Date year2025 = cal.getTime();

        return Stream.of(Arguments.of("Y2K year", year2000, 2000), Arguments.of("recent year", year2023, 2023),
                Arguments.of("leap year 2024", year2024, 2024), Arguments.of("pre-Y2K year", year1999, 1999),
                Arguments.of("Unix epoch year", year1970, 1970), Arguments.of("future year", year2025, 2025));
    }

    @Test
    @DisplayName("Should preserve type information")
    void shouldPreserveTypeInformation() {
        assertThat(yearCalc.getType()).isEqualTo(NumericType.INSTANCE);
    }
}