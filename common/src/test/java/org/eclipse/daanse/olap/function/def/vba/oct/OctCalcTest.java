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
package org.eclipse.daanse.olap.function.def.vba.oct;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.stream.Stream;

import org.eclipse.daanse.olap.api.evaluator.Evaluator;
import org.eclipse.daanse.olap.api.calc.Calc;
import org.eclipse.daanse.olap.api.type.StringType;
import org.eclipse.daanse.olap.exceptions.InvalidArgumentException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class OctCalcTest {

    private OctCalc octCalc;
    private Calc<Object> calc;
    private Evaluator evaluator;

    @BeforeEach
    void setUp() {
        calc = mock(Calc.class);
        evaluator = mock(Evaluator.class);
        octCalc = new OctCalc(StringType.INSTANCE, calc);
    }

    @ParameterizedTest(name = "{0}: Oct({1}) = {2}")
    @MethodSource("octConversionArguments")
    @DisplayName("Should convert numbers to octal strings correctly")
    void shouldConvertNumbersToOctalStringsCorrectly(String testName, Number input, String expectedOctal) {
        when(calc.evaluate(evaluator)).thenReturn(input);

        String result = octCalc.evaluate(evaluator);

        assertThat(result).isEqualTo(expectedOctal);
    }

    static Stream<Arguments> octConversionArguments() {
        return Stream.of(Arguments.of("zero", 0, "0"), Arguments.of("one", 1, "1"), Arguments.of("seven", 7, "7"),
                Arguments.of("eight", 8, "10"), Arguments.of("fifteen", 15, "17"), Arguments.of("sixteen", 16, "20"),
                Arguments.of("sixty-four", 64, "100"), Arguments.of("decimal 10", 10, "12"),
                Arguments.of("decimal 100", 100, "144"), Arguments.of("decimal 255", 255, "377"),
                Arguments.of("decimal 512", 512, "1000"), Arguments.of("large number", 1000, "1750"));
    }

    @ParameterizedTest(name = "{0}: Oct({1}) handles different number types")
    @MethodSource("numberTypeArguments")
    @DisplayName("Should handle different numeric types correctly")
    void shouldHandleDifferentNumericTypesCorrectly(String testName, Number input, String expectedOctal) {
        when(calc.evaluate(evaluator)).thenReturn(input);

        String result = octCalc.evaluate(evaluator);

        assertThat(result).isEqualTo(expectedOctal);
    }

    static Stream<Arguments> numberTypeArguments() {
        return Stream.of(Arguments.of("Integer", Integer.valueOf(8), "10"), Arguments.of("Long", Long.valueOf(8), "10"),
                Arguments.of("Double", Double.valueOf(8.0), "10"), Arguments.of("Float", Float.valueOf(8.0f), "10"),
                Arguments.of("Short", Short.valueOf((short) 8), "10"),
                Arguments.of("Byte", Byte.valueOf((byte) 8), "10"));
    }

    @Test
    @DisplayName("Should handle negative numbers correctly")
    void shouldHandleNegativeNumbersCorrectly() {
        when(calc.evaluate(evaluator)).thenReturn(-8);

        String result = octCalc.evaluate(evaluator);

        // Negative numbers are converted using intValue() which handles 2's complement
        assertThat(result).isNotEmpty();
        assertThat(Integer.toOctalString(-8)).isEqualTo(result);
    }

    @Test
    @DisplayName("Should handle zero correctly")
    void shouldHandleZeroCorrectly() {
        when(calc.evaluate(evaluator)).thenReturn(0);

        String result = octCalc.evaluate(evaluator);

        assertThat(result).isEqualTo("0");
    }

    @Test
    @DisplayName("Should handle fractional numbers by truncating")
    void shouldHandleFractionalNumbersByTruncating() {
        when(calc.evaluate(evaluator)).thenReturn(8.7);

        String result = octCalc.evaluate(evaluator);

        // intValue() truncates fractional part
        assertThat(result).isEqualTo("10"); // 8 in octal
    }

    @Test
    @DisplayName("Should handle large integers correctly")
    void shouldHandleLargeIntegersCorrectly() {
        when(calc.evaluate(evaluator)).thenReturn(Integer.MAX_VALUE);

        String result = octCalc.evaluate(evaluator);

        assertThat(result).isEqualTo(Integer.toOctalString(Integer.MAX_VALUE));
    }

    @Test
    @DisplayName("Should handle minimum integer value correctly")
    void shouldHandleMinimumIntegerValueCorrectly() {
        when(calc.evaluate(evaluator)).thenReturn(Integer.MIN_VALUE);

        String result = octCalc.evaluate(evaluator);

        assertThat(result).isEqualTo(Integer.toOctalString(Integer.MIN_VALUE));
    }

    @Test
    @DisplayName("Should throw exception for non-numeric input")
    void shouldThrowExceptionForNonNumericInput() {
        when(calc.evaluate(evaluator)).thenReturn("not a number");

        assertThatThrownBy(() -> octCalc.evaluate(evaluator)).isInstanceOf(InvalidArgumentException.class)
                .hasMessageContaining("Invalid parameter").hasMessageContaining("number parameter not a number")
                .hasMessageContaining("Oct function must be of type number");
    }

    @Test
    @DisplayName("Should throw exception for null input")
    void shouldThrowExceptionForNullInput() {
        when(calc.evaluate(evaluator)).thenReturn(null);

        assertThatThrownBy(() -> octCalc.evaluate(evaluator)).isInstanceOf(InvalidArgumentException.class)
                .hasMessageContaining("Invalid parameter").hasMessageContaining("number parameter null")
                .hasMessageContaining("Oct function must be of type number");
    }

    @Test
    @DisplayName("Should throw exception for boolean input")
    void shouldThrowExceptionForBooleanInput() {
        when(calc.evaluate(evaluator)).thenReturn(true);

        assertThatThrownBy(() -> octCalc.evaluate(evaluator)).isInstanceOf(InvalidArgumentException.class)
                .hasMessageContaining("Invalid parameter").hasMessageContaining("Oct function must be of type number");
    }

    @Test
    @DisplayName("Should throw exception for date input")
    void shouldThrowExceptionForDateInput() {
        when(calc.evaluate(evaluator)).thenReturn(new java.util.Date());

        assertThatThrownBy(() -> octCalc.evaluate(evaluator)).isInstanceOf(InvalidArgumentException.class)
                .hasMessageContaining("Invalid parameter").hasMessageContaining("Oct function must be of type number");
    }

    @Test
    @DisplayName("Should preserve type information")
    void shouldPreserveTypeInformation() {
        assertThat(octCalc.getType()).isEqualTo(StringType.INSTANCE);
    }

    @Test
    @DisplayName("Should be consistent across multiple calls")
    void shouldBeConsistentAcrossMultipleCalls() {
        when(calc.evaluate(evaluator)).thenReturn(64);

        String first = octCalc.evaluate(evaluator);
        String second = octCalc.evaluate(evaluator);

        assertThat(first).isEqualTo(second);
        assertThat(first).isEqualTo("100");
    }

    @Test
    @DisplayName("Should handle powers of 8 correctly")
    void shouldHandlePowersOfEightCorrectly() {
        when(calc.evaluate(evaluator)).thenReturn(64); // 8^2

        String result = octCalc.evaluate(evaluator);

        assertThat(result).isEqualTo("100");
    }

    @Test
    @DisplayName("Should handle edge case numbers correctly")
    void shouldHandleEdgeCaseNumbersCorrectly() {
        // Test 511 (largest 3-digit octal: 777)
        when(calc.evaluate(evaluator)).thenReturn(511);

        String result = octCalc.evaluate(evaluator);

        assertThat(result).isEqualTo("777");
    }
}