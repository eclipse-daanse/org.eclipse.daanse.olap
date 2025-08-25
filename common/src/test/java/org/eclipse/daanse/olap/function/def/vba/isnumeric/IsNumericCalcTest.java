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
package org.eclipse.daanse.olap.function.def.vba.isnumeric;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
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

class IsNumericCalcTest {

    private IsNumericCalc isNumericCalc;
    private Calc<Object> varNameCalc;
    private Evaluator evaluator;

    @BeforeEach
    void setUp() {
        varNameCalc = mock(Calc.class);
        evaluator = mock(Evaluator.class);
        isNumericCalc = new IsNumericCalc(BooleanType.INSTANCE, varNameCalc);
    }

    @ParameterizedTest(name = "{0}: isNumeric({1}) = {2}")
    @MethodSource("isNumericArguments")
    @DisplayName("Should identify numeric objects correctly")
    void shouldIdentifyNumericObjects(String testName, Object input, Boolean expected) {
        when(varNameCalc.evaluate(evaluator)).thenReturn(input);

        Boolean result = isNumericCalc.evaluate(evaluator);

        assertThat(result).isEqualTo(expected);
    }

    static Stream<Arguments> isNumericArguments() {
        return Stream.of(Arguments.of("integer", 123, true), Arguments.of("double", 123.45, true),
                Arguments.of("float", 123.45f, true), Arguments.of("long", 123L, true),
                Arguments.of("byte", (byte) 42, true), Arguments.of("short", (short) 123, true),
                Arguments.of("big decimal", new BigDecimal("123.45"), true),
                Arguments.of("big integer", new BigInteger("123"), true),
                Arguments.of("atomic integer", new AtomicInteger(123), true),
                Arguments.of("atomic long", new AtomicLong(123L), true), Arguments.of("string value", "test", false),
                Arguments.of("numeric string", "123", false), // String representation of number is not Number
                Arguments.of("boolean value", true, false), Arguments.of("null value", null, false),
                Arguments.of("empty string", "", false), Arguments.of("object value", new Object(), false));
    }

    @Test
    @DisplayName("Should preserve type information")
    void shouldPreserveTypeInformation() {
        assertThat(isNumericCalc.getType()).isEqualTo(BooleanType.INSTANCE);
    }

    @Test
    @DisplayName("Should return true for all Number subtypes")
    void shouldReturnTrueForAllNumberSubtypes() {
        when(varNameCalc.evaluate(evaluator)).thenReturn(Integer.valueOf(42));
        assertThat(isNumericCalc.evaluate(evaluator)).isTrue();

        when(varNameCalc.evaluate(evaluator)).thenReturn(Double.valueOf(42.5));
        assertThat(isNumericCalc.evaluate(evaluator)).isTrue();

        when(varNameCalc.evaluate(evaluator)).thenReturn(Float.valueOf(42.5f));
        assertThat(isNumericCalc.evaluate(evaluator)).isTrue();

        when(varNameCalc.evaluate(evaluator)).thenReturn(Long.valueOf(42L));
        assertThat(isNumericCalc.evaluate(evaluator)).isTrue();
    }

    @Test
    @DisplayName("Should return false for string representations of numbers")
    void shouldReturnFalseForStringNumbers() {
        when(varNameCalc.evaluate(evaluator)).thenReturn("42");
        assertThat(isNumericCalc.evaluate(evaluator)).isFalse();

        when(varNameCalc.evaluate(evaluator)).thenReturn("3.14159");
        assertThat(isNumericCalc.evaluate(evaluator)).isFalse();

        when(varNameCalc.evaluate(evaluator)).thenReturn("-123");
        assertThat(isNumericCalc.evaluate(evaluator)).isFalse();
    }

    @Test
    @DisplayName("Should handle special number values")
    void shouldHandleSpecialNumberValues() {
        when(varNameCalc.evaluate(evaluator)).thenReturn(Double.NaN);
        assertThat(isNumericCalc.evaluate(evaluator)).isTrue();

        when(varNameCalc.evaluate(evaluator)).thenReturn(Double.POSITIVE_INFINITY);
        assertThat(isNumericCalc.evaluate(evaluator)).isTrue();

        when(varNameCalc.evaluate(evaluator)).thenReturn(Double.NEGATIVE_INFINITY);
        assertThat(isNumericCalc.evaluate(evaluator)).isTrue();
    }
}