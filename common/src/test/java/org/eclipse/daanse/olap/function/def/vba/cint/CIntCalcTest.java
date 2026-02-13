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
package org.eclipse.daanse.olap.function.def.vba.cint;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.stream.Stream;

import org.eclipse.daanse.olap.api.evaluator.Evaluator;
import org.eclipse.daanse.olap.api.calc.Calc;
import org.eclipse.daanse.olap.api.type.NumericType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class CIntCalcTest {

    private CIntCalc cIntCalc;
    private Calc<Object> calc;
    private Evaluator evaluator;

    @BeforeEach
    void setUp() {
        calc = mock(Calc.class);
        evaluator = mock(Evaluator.class);
        cIntCalc = new CIntCalc(NumericType.INSTANCE, calc);
    }

    @ParameterizedTest(name = "{0}: CInt({1}) = {2}")
    @MethodSource("arguments")
    @DisplayName("Should convert values to integer correctly")
    void shouldConvertToInteger(String testName, Object input, Integer expected) {
        when(calc.evaluate(evaluator)).thenReturn(input);

        Integer result = cIntCalc.evaluate(evaluator);

        assertThat(result).isEqualTo(expected);
    }

    static Stream<Arguments> arguments() {
        return Stream.of(Arguments.of("integer input", 42, 42), Arguments.of("zero", 0, 0),
                Arguments.of("negative integer", -42, -42), Arguments.of("double round up", 42.7, 43),
                Arguments.of("double round down", 42.3, 42), Arguments.of("double zero", 0.0, 0),
                Arguments.of("negative double", -42.7, -43), Arguments.of("half to even 2.5", 2.5, 2),
                Arguments.of("half to even 3.5", 3.5, 4), Arguments.of("string integer", "42", 42),
                Arguments.of("string double", "42.7", 42), Arguments.of("string negative", "-42", -42),
                Arguments.of("float type", 42.7f, 43), Arguments.of("long type", 42L, 42));
    }

    @Test
    @DisplayName("Should throw exception for invalid string")
    void shouldThrowExceptionForInvalidString() {
        when(calc.evaluate(evaluator)).thenReturn("not a number");

        assertThatThrownBy(() -> cIntCalc.evaluate(evaluator)).isInstanceOf(NumberFormatException.class);
    }

    @Test
    @DisplayName("Should preserve type information")
    void shouldPreserveTypeInformation() {
        assertThat(cIntCalc.getType()).isEqualTo(NumericType.INSTANCE);
    }
}