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
package org.eclipse.daanse.olap.function.def.vba.str;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.stream.Stream;

import org.eclipse.daanse.olap.api.Evaluator;
import org.eclipse.daanse.olap.api.calc.Calc;
import org.eclipse.daanse.olap.api.type.StringType;
import org.eclipse.daanse.olap.common.InvalidArgumentException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class StrCalcTest {

    private StrCalc strCalc;
    private Calc<Object> calc;
    private Evaluator evaluator;

    @BeforeEach
    void setUp() {
        calc = mock(Calc.class);
        evaluator = mock(Evaluator.class);
        strCalc = new StrCalc(StringType.INSTANCE, calc);
    }

    @ParameterizedTest(name = "{0}: Str({1}) = \"{2}\"")
    @MethodSource("strConversionArguments")
    @DisplayName("Should convert numbers to strings with proper sign formatting")
    void shouldConvertNumbersToStringsWithProperSignFormatting(String testName, Number input, String expectedResult) {
        when(calc.evaluate(evaluator)).thenReturn(input);

        String result = strCalc.evaluate(evaluator);

        assertThat(result).isEqualTo(expectedResult);
    }

    static Stream<Arguments> strConversionArguments() {
        return Stream.of(
                // Positive numbers get leading space
                Arguments.of("positive integer", 123, " 123"), Arguments.of("positive double", 123.45, " 123.45"),
                Arguments.of("positive zero", 0, " 0"), Arguments.of("positive float", 3.14f, " 3.14"),
                Arguments.of("positive long", 1000000L, " 1000000"),

                // Negative numbers don't get leading space
                Arguments.of("negative integer", -123, "-123"), Arguments.of("negative double", -123.45, "-123.45"),
                Arguments.of("negative float", -3.14f, "-3.14"), Arguments.of("negative long", -1000000L, "-1000000"),

                // Edge cases
                Arguments.of("positive one", 1, " 1"), Arguments.of("negative one", -1, "-1"),
                Arguments.of("small positive decimal", 0.01, " 0.01"),
                Arguments.of("small negative decimal", -0.01, "-0.01"));
    }

    @ParameterizedTest(name = "{0}: Str({1}) handles different number types")
    @MethodSource("numberTypeArguments")
    @DisplayName("Should handle different numeric types correctly")
    void shouldHandleDifferentNumericTypesCorrectly(String testName, Number input, String expectedResult) {
        when(calc.evaluate(evaluator)).thenReturn(input);

        String result = strCalc.evaluate(evaluator);

        assertThat(result).isEqualTo(expectedResult);
    }

    static Stream<Arguments> numberTypeArguments() {
        return Stream.of(Arguments.of("Integer", Integer.valueOf(42), " 42"),
                Arguments.of("Long", Long.valueOf(42), " 42"), Arguments.of("Double", Double.valueOf(42.0), " 42.0"),
                Arguments.of("Float", Float.valueOf(42.0f), " 42.0"),
                Arguments.of("Short", Short.valueOf((short) 42), " 42"),
                Arguments.of("Byte", Byte.valueOf((byte) 42), " 42"));
    }

    @Test
    @DisplayName("Should handle positive zero correctly")
    void shouldHandlePositiveZeroCorrectly() {
        when(calc.evaluate(evaluator)).thenReturn(0.0);

        String result = strCalc.evaluate(evaluator);

        assertThat(result).isEqualTo(" 0.0");
    }

    @Test
    @DisplayName("Should handle negative zero correctly")
    void shouldHandleNegativeZeroCorrectly() {
        when(calc.evaluate(evaluator)).thenReturn(-0.0);

        String result = strCalc.evaluate(evaluator);

        // -0.0 is still >= 0 in double comparison
        assertThat(result).isEqualTo(" -0.0");
    }

    @Test
    @DisplayName("Should handle very large positive numbers correctly")
    void shouldHandleVeryLargePositiveNumbersCorrectly() {
        when(calc.evaluate(evaluator)).thenReturn(Double.MAX_VALUE);

        String result = strCalc.evaluate(evaluator);

        assertThat(result).startsWith(" ");
        assertThat(result).contains(String.valueOf(Double.MAX_VALUE));
    }

    @Test
    @DisplayName("Should handle very large negative numbers correctly")
    void shouldHandleVeryLargeNegativeNumbersCorrectly() {
        when(calc.evaluate(evaluator)).thenReturn(-Double.MAX_VALUE);

        String result = strCalc.evaluate(evaluator);

        assertThat(result).startsWith("-");
        assertThat(result).contains(String.valueOf(Double.MAX_VALUE));
    }

    @Test
    @DisplayName("Should handle infinity correctly")
    void shouldHandleInfinityCorrectly() {
        when(calc.evaluate(evaluator)).thenReturn(Double.POSITIVE_INFINITY);

        String result = strCalc.evaluate(evaluator);

        assertThat(result).isEqualTo(" Infinity");
    }

    @Test
    @DisplayName("Should handle negative infinity correctly")
    void shouldHandleNegativeInfinityCorrectly() {
        when(calc.evaluate(evaluator)).thenReturn(Double.NEGATIVE_INFINITY);

        String result = strCalc.evaluate(evaluator);

        assertThat(result).isEqualTo("-Infinity");
    }

    @Test
    @DisplayName("Should handle NaN correctly")
    void shouldHandleNaNCorrectly() {
        when(calc.evaluate(evaluator)).thenReturn(Double.NaN);

        String result = strCalc.evaluate(evaluator);

        // NaN is not >= 0, so no leading space
        assertThat(result).isEqualTo("NaN");
    }

    @Test
    @DisplayName("Should throw exception for non-numeric input")
    void shouldThrowExceptionForNonNumericInput() {
        when(calc.evaluate(evaluator)).thenReturn("not a number");

        assertThatThrownBy(() -> strCalc.evaluate(evaluator)).isInstanceOf(InvalidArgumentException.class)
                .hasMessageContaining("Invalid parameter").hasMessageContaining("number parameter not a number")
                .hasMessageContaining("Str function must be of type number");
    }

    @Test
    @DisplayName("Should throw exception for null input")
    void shouldThrowExceptionForNullInput() {
        when(calc.evaluate(evaluator)).thenReturn(null);

        assertThatThrownBy(() -> strCalc.evaluate(evaluator)).isInstanceOf(InvalidArgumentException.class)
                .hasMessageContaining("Invalid parameter").hasMessageContaining("number parameter null")
                .hasMessageContaining("Str function must be of type number");
    }

    @Test
    @DisplayName("Should throw exception for boolean input")
    void shouldThrowExceptionForBooleanInput() {
        when(calc.evaluate(evaluator)).thenReturn(true);

        assertThatThrownBy(() -> strCalc.evaluate(evaluator)).isInstanceOf(InvalidArgumentException.class)
                .hasMessageContaining("Invalid parameter").hasMessageContaining("Str function must be of type number");
    }

    @Test
    @DisplayName("Should throw exception for date input")
    void shouldThrowExceptionForDateInput() {
        when(calc.evaluate(evaluator)).thenReturn(new java.util.Date());

        assertThatThrownBy(() -> strCalc.evaluate(evaluator)).isInstanceOf(InvalidArgumentException.class)
                .hasMessageContaining("Invalid parameter").hasMessageContaining("Str function must be of type number");
    }

    @Test
    @DisplayName("Should preserve type information")
    void shouldPreserveTypeInformation() {
        assertThat(strCalc.getType()).isEqualTo(StringType.INSTANCE);
    }

    @Test
    @DisplayName("Should be consistent across multiple calls")
    void shouldBeConsistentAcrossMultipleCalls() {
        when(calc.evaluate(evaluator)).thenReturn(123.45);

        String first = strCalc.evaluate(evaluator);
        String second = strCalc.evaluate(evaluator);

        assertThat(first).isEqualTo(second);
        assertThat(first).isEqualTo(" 123.45");
    }

    @Test
    @DisplayName("Should handle scientific notation correctly")
    void shouldHandleScientificNotationCorrectly() {
        when(calc.evaluate(evaluator)).thenReturn(1.23e10);

        String result = strCalc.evaluate(evaluator);

        assertThat(result).startsWith(" ");
        assertThat(result).contains("1.23E10");
    }

    @Test
    @DisplayName("Should handle very small positive numbers correctly")
    void shouldHandleVerySmallPositiveNumbersCorrectly() {
        when(calc.evaluate(evaluator)).thenReturn(1e-10);

        String result = strCalc.evaluate(evaluator);

        assertThat(result).startsWith(" ");
        assertThat(result.trim()).isEqualTo("1.0E-10");
    }

    @Test
    @DisplayName("Should handle very small negative numbers correctly")
    void shouldHandleVerySmallNegativeNumbersCorrectly() {
        when(calc.evaluate(evaluator)).thenReturn(-1e-10);

        String result = strCalc.evaluate(evaluator);

        assertThat(result).startsWith("-");
        assertThat(result).contains("-1.0E-10");
    }

    @Test
    @DisplayName("Should demonstrate difference from Format function behavior")
    void shouldDemonstrateFormatFunctionBehaviorDifference() {
        // This test documents the VBA behavior mentioned in comments:
        // Str includes leading space for positive numbers, Format doesn't
        when(calc.evaluate(evaluator)).thenReturn(123);

        String result = strCalc.evaluate(evaluator);

        assertThat(result).startsWith(" ");
        assertThat(result).isEqualTo(" 123");
    }
}