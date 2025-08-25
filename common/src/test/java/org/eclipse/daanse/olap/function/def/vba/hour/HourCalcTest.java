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
package org.eclipse.daanse.olap.function.def.vba.hour;

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

class HourCalcTest {

    private HourCalc hourCalc;
    private DateTimeCalc dateTimeCalc;
    private Evaluator evaluator;

    @BeforeEach
    void setUp() {
        dateTimeCalc = mock(DateTimeCalc.class);
        evaluator = mock(Evaluator.class);
        hourCalc = new HourCalc(NumericType.INSTANCE, dateTimeCalc);
    }

    @ParameterizedTest(name = "{0}: hour({1}) = {2}")
    @MethodSource("arguments")
    @DisplayName("Should extract hour from datetime correctly")
    void shouldExtractHour(String testName, Date inputDate, Integer expected) {
        when(dateTimeCalc.evaluate(evaluator)).thenReturn(inputDate);

        Integer result = hourCalc.evaluate(evaluator);

        assertThat(result).isEqualTo(expected);
    }

    static Stream<Arguments> arguments() {
        Calendar cal = Calendar.getInstance();

        // Midnight (00:00)
        cal.set(2023, Calendar.JANUARY, 1, 0, 0, 0);
        Date midnight = cal.getTime();

        // Early morning (06:30)
        cal.set(2023, Calendar.JUNE, 15, 6, 30, 45);
        Date earlyMorning = cal.getTime();

        // Noon (12:00)
        cal.set(2023, Calendar.MARCH, 20, 12, 0, 0);
        Date noon = cal.getTime();

        // Afternoon (15:45)
        cal.set(2023, Calendar.SEPTEMBER, 10, 15, 45, 30);
        Date afternoon = cal.getTime();

        // Evening (18:20)
        cal.set(2023, Calendar.DECEMBER, 25, 18, 20, 10);
        Date evening = cal.getTime();

        // Late night (23:59)
        cal.set(2023, Calendar.JULY, 4, 23, 59, 59);
        Date lateNight = cal.getTime();

        return Stream.of(Arguments.of("midnight", midnight, 0), Arguments.of("early morning", earlyMorning, 6),
                Arguments.of("noon", noon, 12), Arguments.of("afternoon", afternoon, 15),
                Arguments.of("evening", evening, 18), Arguments.of("late night", lateNight, 23));
    }

    @Test
    @DisplayName("Should preserve type information")
    void shouldPreserveTypeInformation() {
        assertThat(hourCalc.getType()).isEqualTo(NumericType.INSTANCE);
    }
}