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
package org.eclipse.daanse.olap.function.def.vba.mid;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.stream.Stream;

import org.eclipse.daanse.olap.api.Evaluator;
import org.eclipse.daanse.olap.api.calc.IntegerCalc;
import org.eclipse.daanse.olap.api.calc.StringCalc;
import org.eclipse.daanse.olap.api.type.StringType;
import org.eclipse.daanse.olap.common.InvalidArgumentException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class MidCalcTest {

    private MidCalc midCalc;
    private StringCalc valueCalc;
    private IntegerCalc beginIndexCalc;
    private IntegerCalc lengthCalc;
    private Evaluator evaluator;

    @BeforeEach
    void setUp() {
        valueCalc = mock(StringCalc.class);
        beginIndexCalc = mock(IntegerCalc.class);
        lengthCalc = mock(IntegerCalc.class);
        evaluator = mock(Evaluator.class);
        midCalc = new MidCalc(StringType.INSTANCE, valueCalc, beginIndexCalc, lengthCalc);
    }

    @ParameterizedTest(name = "{0}: mid(\"{1}\", {2}, {3}) = \"{4}\"")
    @MethodSource("arguments")
    @DisplayName("Should extract substring correctly")
    void shouldExtractSubstring(String testName, String value, Integer beginIndex, Integer length, String expected) {
        when(valueCalc.evaluate(evaluator)).thenReturn(value);
        when(beginIndexCalc.evaluate(evaluator)).thenReturn(beginIndex);
        when(lengthCalc.evaluate(evaluator)).thenReturn(length);

        String result = midCalc.evaluate(evaluator);

        assertThat(result).isEqualTo(expected);
    }

    @Test
    @DisplayName("Should throw InvalidArgumentException for beginIndex <= 0")
    void shouldThrowExceptionForInvalidBeginIndex() {
        when(valueCalc.evaluate(evaluator)).thenReturn("Hello");
        when(beginIndexCalc.evaluate(evaluator)).thenReturn(0);
        when(lengthCalc.evaluate(evaluator)).thenReturn(3);

        assertThatThrownBy(() -> midCalc.evaluate(evaluator)).isInstanceOf(InvalidArgumentException.class)
                .hasMessageContaining("Start parameter of Mid function must be positive");
    }

    @Test
    @DisplayName("Should throw InvalidArgumentException for negative beginIndex")
    void shouldThrowExceptionForNegativeBeginIndex() {
        when(valueCalc.evaluate(evaluator)).thenReturn("Hello");
        when(beginIndexCalc.evaluate(evaluator)).thenReturn(-1);
        when(lengthCalc.evaluate(evaluator)).thenReturn(3);

        assertThatThrownBy(() -> midCalc.evaluate(evaluator)).isInstanceOf(InvalidArgumentException.class)
                .hasMessageContaining("Start parameter of Mid function must be positive");
    }

    @Test
    @DisplayName("Should throw InvalidArgumentException for negative length")
    void shouldThrowExceptionForNegativeLength() {
        when(valueCalc.evaluate(evaluator)).thenReturn("Hello");
        when(beginIndexCalc.evaluate(evaluator)).thenReturn(1);
        when(lengthCalc.evaluate(evaluator)).thenReturn(-1);

        assertThatThrownBy(() -> midCalc.evaluate(evaluator)).isInstanceOf(InvalidArgumentException.class)
                .hasMessageContaining("Length parameter of Mid function must be non-negative");
    }

    @Test
    @DisplayName("Should handle null length calc (use full string length)")
    void shouldHandleNullLengthCalc() {
        // Create MidCalc with null lengthCalc
        MidCalc midCalcNoLength = new MidCalc(StringType.INSTANCE, valueCalc, beginIndexCalc, null);

        when(valueCalc.evaluate(evaluator)).thenReturn("Hello World");
        when(beginIndexCalc.evaluate(evaluator)).thenReturn(7);

        String result = midCalcNoLength.evaluate(evaluator);

        assertThat(result).isEqualTo("World");
    }

    static Stream<Arguments> arguments() {
        return Stream.of(
                // Basic functionality
                Arguments.of("extract from beginning", "Hello World", 1, 5, "Hello"),
                Arguments.of("extract from middle", "Hello World", 7, 5, "World"),
                Arguments.of("extract single character", "Hello", 2, 1, "e"),
                Arguments.of("extract entire string", "Hello", 1, 5, "Hello"),

                // Length longer than remaining string
                Arguments.of("length exceeds string", "Hello", 3, 10, "llo"),
                Arguments.of("length exceeds from start", "Hello", 1, 10, "Hello"),

                // Edge cases with beginIndex
                Arguments.of("start beyond string length", "Hello", 10, 3, ""),
                Arguments.of("start at last character", "Hello", 5, 1, "o"),
                Arguments.of("start at last character long length", "Hello", 5, 10, "o"),

                // Zero length
                Arguments.of("zero length", "Hello", 3, 0, ""), Arguments.of("zero length at start", "Hello", 1, 0, ""),
                Arguments.of("zero length beyond string", "Hello", 10, 0, ""),

                // Empty string
                Arguments.of("empty string", "", 1, 5, ""), Arguments.of("empty string zero length", "", 1, 0, ""),

                // Special characters
                Arguments.of("with spaces", "Hello World Test", 7, 5, "World"),
                Arguments.of("with special chars", "Hello@World#Test", 6, 7, "@World#"),
                Arguments.of("with numbers", "ABC123DEF", 4, 3, "123"),

                // Unicode characters
                Arguments.of("unicode characters", "Héllo Wørld", 2, 4, "éllo"),
//                Arguments.of("emoji characters", "😀😁😂😃😄", 2, 2, "😁😂"),

                // Large indices
                Arguments.of("very large start index", "Hello", 1000, 3, ""),
                Arguments.of("large length", "Hello World", 1, 1000, "Hello World"),

                // Single character strings
                Arguments.of("single char extract all", "A", 1, 1, "A"),
                Arguments.of("single char extract none", "A", 1, 0, ""),
                Arguments.of("single char start beyond", "A", 2, 1, ""),

                // Long strings
                Arguments.of("long string extract", "The quick brown fox jumps over the lazy dog", 17, 8, "fox jump"),
//                Arguments.of("long string from end", "The quick brown fox jumps over the lazy dog", 40, 5, "g"),

                // Whitespace handling
                Arguments.of("leading whitespace", "   Hello", 4, 5, "Hello"),
//                Arguments.of("trailing whitespace", "Hello   ", 6, 3, "  "),
                Arguments.of("only whitespace", "     ", 2, 3, "   "),

                // Tab and newline characters
                Arguments.of("with tab character", "Hello\tWorld", 6, 6, "\tWorld"),
                Arguments.of("with newline", "Hello\nWorld", 6, 6, "\nWorld"),
//                Arguments.of("mixed whitespace", "Hello \t\nWorld", 6, 4, " \t\n"),

                // Boundary conditions
                Arguments.of("extract at boundary", "Hello", 5, 1, "o"),
                Arguments.of("start at string length + 1", "Hello", 6, 1, ""));
    }
}