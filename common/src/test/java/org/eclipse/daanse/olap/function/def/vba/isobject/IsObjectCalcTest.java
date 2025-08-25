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
package org.eclipse.daanse.olap.function.def.vba.isobject;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.stream.Stream;

import org.eclipse.daanse.olap.api.Evaluator;
import org.eclipse.daanse.olap.api.calc.Calc;
import org.eclipse.daanse.olap.api.type.BooleanType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class IsObjectCalcTest {

    private IsObjectCalc isObjectCalc;
    private Calc<Object> varNameCalc;
    private Evaluator evaluator;

    @BeforeEach
    void setUp() {
        varNameCalc = mock(Calc.class);
        evaluator = mock(Evaluator.class);
        isObjectCalc = new IsObjectCalc(BooleanType.INSTANCE, varNameCalc);
    }

    @ParameterizedTest(name = "{0}: isObject({1}) = {2}")
    @MethodSource("isObjectArguments")
    @DisplayName("Should always return false as VB objects are not supported")
    void shouldAlwaysReturnFalse(String testName, Object input, Boolean expected) {
        when(varNameCalc.evaluate(evaluator)).thenReturn(input);

        Boolean result = isObjectCalc.evaluate(evaluator);

        assertThat(result).isEqualTo(expected);
    }

    static Stream<Arguments> isObjectArguments() {
        return Stream.of(Arguments.of("string value", "test", false), Arguments.of("integer value", 123, false),
                Arguments.of("double value", 123.45, false), Arguments.of("boolean value", true, false),
                Arguments.of("null value", null, false), Arguments.of("empty string", "", false),
                Arguments.of("java object", new Object(), false),
                Arguments.of("java list", java.util.Arrays.asList(1, 2, 3), false),
                Arguments.of("java map", new java.util.HashMap<String, Object>(), false));
    }

    @Test
    @DisplayName("Should preserve type information")
    void shouldPreserveTypeInformation() {
        assertThat(isObjectCalc.getType()).isEqualTo(BooleanType.INSTANCE);
    }

    @Test
    @DisplayName("Should handle any input and return false")
    void shouldHandleAnyInput() {
        when(varNameCalc.evaluate(evaluator)).thenReturn("any value");

        Boolean result = isObjectCalc.evaluate(evaluator);

        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("Should return false even for complex Java objects")
    void shouldReturnFalseForJavaObjects() {
        // Even complex Java objects should return false as VB objects are not supported
        when(varNameCalc.evaluate(evaluator)).thenReturn(new StringBuilder("test"));
        assertThat(isObjectCalc.evaluate(evaluator)).isFalse();

        when(varNameCalc.evaluate(evaluator)).thenReturn(java.util.Arrays.asList(1, 2, 3));
        assertThat(isObjectCalc.evaluate(evaluator)).isFalse();

        when(varNameCalc.evaluate(evaluator)).thenReturn(new java.util.Date());
        assertThat(isObjectCalc.evaluate(evaluator)).isFalse();
    }
}