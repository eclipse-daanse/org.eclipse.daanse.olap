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
package org.eclipse.daanse.olap.function.def.vba.cdbl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.stream.Stream;

import org.eclipse.daanse.olap.api.Evaluator;
import org.eclipse.daanse.olap.api.calc.Calc;
import org.eclipse.daanse.olap.api.type.NumericType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class CDblCalcTest {

    private CDblCalc cDblCalc;
    private Calc<Object> calc;
    private Evaluator evaluator;

    @BeforeEach
    void setUp() {
        calc = mock(Calc.class);
        evaluator = mock(Evaluator.class);
        cDblCalc = new CDblCalc(NumericType.INSTANCE, calc);
    }

    @ParameterizedTest(name = "{0}: CDbl({1}) = {2}")
    @MethodSource("validArguments")
    @DisplayName("Should convert values to double correctly")
    void shouldConvertToDouble(String testName, Object input, Double expected) {
        when(calc.evaluate(evaluator)).thenReturn(input);

        Double result = cDblCalc.evaluate(evaluator);

        assertThat(result).isEqualTo(expected);
    }

    @ParameterizedTest(name = "{0}: CDbl(\"{1}\")")
    @MethodSource("invalidArguments")
    @DisplayName("Should throw NumberFormatException for invalid strings")
    void shouldThrowNumberFormatExceptionForInvalidStrings(String testName, String invalidString) {
        when(calc.evaluate(evaluator)).thenReturn(invalidString);

        assertThatThrownBy(() -> cDblCalc.evaluate(evaluator)).isInstanceOf(NumberFormatException.class);
    }

    static Stream<Arguments> validArguments() {
        return Stream.of(Arguments.of("integer", 42, 42.0), Arguments.of("long", 42L, 42.0),
                Arguments.of("float", 3.14f, 3.140000104904175), Arguments.of("double", 3.14159, 3.14159),
                Arguments.of("byte", (byte) 10, 10.0), Arguments.of("short", (short) 100, 100.0),
                Arguments.of("string integer", "42", 42.0), Arguments.of("string double", "3.14159", 3.14159),
                Arguments.of("string negative", "-42.5", -42.5), Arguments.of("string zero", "0", 0.0),
                Arguments.of("string scientific", "1.23E+5", 123000.0),
                Arguments.of("string infinity", "Infinity", Double.POSITIVE_INFINITY),
                Arguments.of("string negative infinity", "-Infinity", Double.NEGATIVE_INFINITY),
                Arguments.of("string NaN", "NaN", Double.NaN)
//                ,
//                Arguments.of("null", null, Double.NaN)
        );
    }

    static Stream<Arguments> invalidArguments() {
        return Stream.of(Arguments.of("empty string", ""), Arguments.of("non-numeric string", "hello"),
                Arguments.of("mixed alphanumeric", "123abc"), Arguments.of("special characters", "!@#$%"));
    }
}