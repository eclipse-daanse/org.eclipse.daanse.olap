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
package org.eclipse.daanse.olap.function.def.vba.isarray;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

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

class IsArrayCalcTest {

    private IsArrayCalc isArrayCalc;
    private Calc<Object> varNameCalc;
    private Evaluator evaluator;

    @BeforeEach
    void setUp() {
        varNameCalc = mock(Calc.class);
        evaluator = mock(Evaluator.class);
        isArrayCalc = new IsArrayCalc(BooleanType.INSTANCE, varNameCalc);
    }

    @ParameterizedTest(name = "{0}: isArray({1}) = {2}")
    @MethodSource("isArrayArguments")
    @DisplayName("Should always return false as arrays are not supported")
    void shouldAlwaysReturnFalse(String testName, Object input, Boolean expected) {
        when(varNameCalc.evaluate(evaluator)).thenReturn(input);

        Boolean result = isArrayCalc.evaluate(evaluator);

        assertThat(result).isEqualTo(expected);
    }

    static Stream<Arguments> isArrayArguments() {
        return Stream.of(Arguments.of("string value", "test", false), Arguments.of("integer value", 123, false),
                Arguments.of("double value", 123.45, false), Arguments.of("boolean value", true, false),
                Arguments.of("null value", null, false), Arguments.of("empty string", "", false),
                Arguments.of("object value", new Object(), false));
    }

    @Test
    @DisplayName("Should preserve type information")
    void shouldPreserveTypeInformation() {
        assertThat(isArrayCalc.getType()).isEqualTo(BooleanType.INSTANCE);
    }

    @Test
    @DisplayName("Should handle any input and return false")
    void shouldHandleAnyInput() {
        when(varNameCalc.evaluate(evaluator)).thenReturn("any value");

        Boolean result = isArrayCalc.evaluate(evaluator);

        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("Should return false for java arrays as they are not VB arrays")
    void shouldReturnFalseForJavaArrays() {
        // Even Java arrays should return false as VB arrays are not supported
        when(varNameCalc.evaluate(evaluator)).thenReturn(new int[] { 1, 2, 3 });

        Boolean result = isArrayCalc.evaluate(evaluator);

        assertThat(result).isFalse();
    }
}