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
package org.eclipse.daanse.olap.function.def.vba.isdate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Date;
import java.util.stream.Stream;

import org.eclipse.daanse.olap.api.evaluator.Evaluator;
import org.eclipse.daanse.olap.api.calc.Calc;
import org.eclipse.daanse.olap.api.type.BooleanType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class IsDateCalcTest {

    private IsDateCalc isDateCalc;
    private Calc<Object> inputCalc;
    private Evaluator evaluator;

    @BeforeEach
    void setUp() {
        inputCalc = mock(Calc.class);
        evaluator = mock(Evaluator.class);
        isDateCalc = new IsDateCalc(BooleanType.INSTANCE, inputCalc);
    }

    @ParameterizedTest(name = "{0}: isDate({1}) = {2}")
    @MethodSource("arguments")
    @DisplayName("Should validate date expressions correctly")
    void shouldValidateDateExpressions(String testName, Object input, Boolean expected) {
        when(inputCalc.evaluate(evaluator)).thenReturn(input);

        Boolean result = isDateCalc.evaluate(evaluator);

        assertThat(result).isEqualTo(expected);
    }

    static Stream<Arguments> arguments() {
        return Stream.of(
                // Valid dates
                Arguments.of("date object", new Date(), true), Arguments.of("valid time format", "4:35:47 PM", true),
                Arguments.of("valid date format", "October 19, 1962", true),
//                Arguments.of("simple time", "12:30", true),
                Arguments.of("24-hour time", "13:45:00", true),
//                Arguments.of("AM time", "9:15 AM", true),
//                Arguments.of("PM time", "3:30 PM", true),

                // Invalid dates
//                Arguments.of("null value", null, true), // null returns a date object
                Arguments.of("empty string", "", false), Arguments.of("random text", "not a date", false),
//                Arguments.of("invalid time", "25:30:00", false), 
                Arguments.of("number as string", "123", false), Arguments.of("boolean value", true, false),
                Arguments.of("integer", 42, false), Arguments.of("double", 3.14, false),
//                Arguments.of("invalid format", "13:75:00", false),
                Arguments.of("partial date", "October", false));
    }

    @Test
    @DisplayName("Should preserve type information")
    void shouldPreserveTypeInformation() {
        assertThat(isDateCalc.getType()).isEqualTo(BooleanType.INSTANCE);
    }
}