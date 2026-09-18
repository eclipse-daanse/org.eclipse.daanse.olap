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
package org.eclipse.daanse.olap.function.def.vba.hex;

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

class HexCalcTest {

    private HexCalc hexCalc;
    private Calc<Object> numberCalc;
    private Evaluator evaluator;

    @BeforeEach
    void setUp() {
        numberCalc = mock(Calc.class);
        evaluator = mock(Evaluator.class);
        hexCalc = new HexCalc(StringType.INSTANCE, numberCalc);
    }

    @ParameterizedTest(name = "{0}: hex({1}) = {2}")
    @MethodSource("arguments")
    @DisplayName("Should convert numbers to hexadecimal correctly")
    void shouldConvertNumberToHex(String testName, Number input, String expected) {
        when(numberCalc.evaluate(evaluator)).thenReturn(input);

        String result = hexCalc.evaluate(evaluator);

        assertThat(result).isEqualTo(expected);
    }

    @Test
    @DisplayName("Should throw InvalidArgumentException for non-number input")
    void shouldThrowExceptionForNonNumberInput() {
        when(numberCalc.evaluate(evaluator)).thenReturn("not a number");

        assertThatThrownBy(() -> hexCalc.evaluate(evaluator)).isInstanceOf(InvalidArgumentException.class)
                .hasMessageContaining("Invalid parameter")
                .hasMessageContaining("number parameter not a number of Hex function must be of type number");
    }

    @Test
    @DisplayName("Should throw InvalidArgumentException for null input")
    void shouldThrowExceptionForNullInput() {
        when(numberCalc.evaluate(evaluator)).thenReturn(null);

        assertThatThrownBy(() -> hexCalc.evaluate(evaluator)).isInstanceOf(InvalidArgumentException.class)
                .hasMessageContaining("Invalid parameter")
                .hasMessageContaining("number parameter null of Hex function must be of type number");
    }

    static Stream<Arguments> arguments() {
        return Stream.of(Arguments.of("zero", 0, "0"), Arguments.of("positive single digit", 5, "5"),
                Arguments.of("positive single digit max", 15, "F"), Arguments.of("positive two digits", 16, "10"),
                Arguments.of("positive large number", 255, "FF"),
                Arguments.of("positive very large number", 4095, "FFF"),
                Arguments.of("negative small number", -1, "FFFFFFFF"),
                Arguments.of("negative large number", -16, "FFFFFFF0"), Arguments.of("decimal rounds down", 10.9, "A"),
                Arguments.of("decimal rounds down negative", -10.9, "FFFFFFF6"),
                Arguments.of("byte value", (byte) 127, "7F"), Arguments.of("short value", (short) 32767, "7FFF"),
                Arguments.of("long value truncated", 2147483647L, "7FFFFFFF"),
                Arguments.of("double value", 100.0, "64"), Arguments.of("float value", 50.0f, "32"),
                Arguments.of("hex A equivalent", 10, "A"), Arguments.of("hex B equivalent", 11, "B"),
                Arguments.of("hex C equivalent", 12, "C"), Arguments.of("hex D equivalent", 13, "D"),
                Arguments.of("hex E equivalent", 14, "E"), Arguments.of("power of 16", 256, "100"),
                Arguments.of("large hex pattern", 3735928559L, "DEADBEEF"));
    }
}