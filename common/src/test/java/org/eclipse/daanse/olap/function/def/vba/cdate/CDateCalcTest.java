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
package org.eclipse.daanse.olap.function.def.vba.cdate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.stream.Stream;

import org.eclipse.daanse.olap.api.Evaluator;
import org.eclipse.daanse.olap.api.calc.Calc;
import org.eclipse.daanse.olap.api.type.DateTimeType;
import org.eclipse.daanse.olap.common.InvalidArgumentException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class CDateCalcTest {

    private CDateCalc cDateCalc;
    private Calc<Object> calc;
    private Evaluator evaluator;

    @BeforeEach
    void setUp() {
        calc = mock(Calc.class);
        evaluator = mock(Evaluator.class);
        cDateCalc = new CDateCalc(DateTimeType.INSTANCE, calc);
    }

    @Test
    @DisplayName("Should return null for null input")
    void shouldReturnNullForNullInput() {
        when(calc.evaluate(evaluator)).thenReturn(null);

        Date result = cDateCalc.evaluate(evaluator);

        assertThat(result).isNull();
    }

    @Test
    @DisplayName("Should return same Date object for Date input")
    void shouldReturnSameDateForDateInput() throws Exception {
        Date testDate = new SimpleDateFormat("yyyy-MM-dd").parse("2023-05-15");
        when(calc.evaluate(evaluator)).thenReturn(testDate);

        Date result = cDateCalc.evaluate(evaluator);

        assertThat(result).isSameAs(testDate);
    }

    @ParameterizedTest(name = "{0}: CDate(\"{1}\")")
    @MethodSource("validDateArguments")
    @DisplayName("Should parse valid date strings")
    void shouldParseValidDateStrings(String testName, String dateString) throws Exception {
        when(calc.evaluate(evaluator)).thenReturn(dateString);

        Date result = cDateCalc.evaluate(evaluator);

        assertThat(result).isNotNull();
    }

    @ParameterizedTest(name = "{0}: CDate(\"{1}\")")
    @MethodSource("validTimeArguments")
    @DisplayName("Should parse valid time strings")
    void shouldParseValidTimeStrings(String testName, String timeString) throws Exception {
        when(calc.evaluate(evaluator)).thenReturn(timeString);

        Date result = cDateCalc.evaluate(evaluator);

        assertThat(result).isNotNull();
    }

    @ParameterizedTest(name = "{0}: CDate(\"{1}\")")
    @MethodSource("invalidDateArguments")
    @DisplayName("Should throw InvalidArgumentException for invalid date strings")
    void shouldThrowInvalidArgumentExceptionForInvalidDateStrings(String testName, String invalidDateString) {
        when(calc.evaluate(evaluator)).thenReturn(invalidDateString);

        assertThatThrownBy(() -> cDateCalc.evaluate(evaluator)).isInstanceOf(InvalidArgumentException.class)
                .hasMessageContaining("Invalid parameter")
                .hasMessageContaining("expression parameter of CDate function must be formatted correctly");
    }

    static Stream<Arguments> validDateArguments() {
        return Stream.of(Arguments.of("full date", "May 15, 2023")
//                ,
//                Arguments.of("short date", "5/15/23"),
//                Arguments.of("iso date", "2023-05-15")
        );
    }

    static Stream<Arguments> validTimeArguments() {
        return Stream.of(Arguments.of("24-hour time", "14:30:45"), Arguments.of("12-hour time with AM", "2:30:45 PM"),
                Arguments.of("simple time", "10:15:30"));
    }

    static Stream<Arguments> invalidDateArguments() {
        return Stream.of(Arguments.of("invalid format", "not-a-date"), Arguments.of("empty string", ""),
                Arguments.of("random text", "hello world")
//                ,
//                Arguments.of("invalid date", "February 30, 2023"),
//                Arguments.of("malformed", "25:99:99")
        );
    }
}