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
package org.eclipse.daanse.olap.function.def.vba.instr;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.stream.Stream;

import org.eclipse.daanse.olap.api.evaluator.Evaluator;
import org.eclipse.daanse.olap.api.calc.IntegerCalc;
import org.eclipse.daanse.olap.api.calc.StringCalc;
import org.eclipse.daanse.olap.api.type.NumericType;
import org.eclipse.daanse.olap.exceptions.InvalidArgumentException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class InStrCalcTest {

    private InStrCalc inStrCalc;
    private IntegerCalc startCalc;
    private StringCalc stringCheckCalc;
    private StringCalc stringMatchCalc;
    private IntegerCalc compareCalc;
    private Evaluator evaluator;

    @BeforeEach
    void setUp() {
        startCalc = mock(IntegerCalc.class);
        stringCheckCalc = mock(StringCalc.class);
        stringMatchCalc = mock(StringCalc.class);
        compareCalc = mock(IntegerCalc.class);
        evaluator = mock(Evaluator.class);
        inStrCalc = new InStrCalc(NumericType.INSTANCE, startCalc, stringCheckCalc, stringMatchCalc, compareCalc);
    }

    @ParameterizedTest(name = "{0}: instr({1}, \"{2}\", \"{3}\", {4}) = {5}")
    @MethodSource("arguments")
    @DisplayName("Should find substring position correctly")
    void shouldFindSubstringPosition(String testName, Integer start, String stringCheck, String stringMatch,
            Integer compare, Integer expected) {
        when(startCalc.evaluate(evaluator)).thenReturn(start);
        when(stringCheckCalc.evaluate(evaluator)).thenReturn(stringCheck);
        when(stringMatchCalc.evaluate(evaluator)).thenReturn(stringMatch);
        when(compareCalc.evaluate(evaluator)).thenReturn(compare);

        Integer result = inStrCalc.evaluate(evaluator);

        assertThat(result).isEqualTo(expected);
    }

    @Test
    @DisplayName("Should throw InvalidArgumentException for start = 0")
    void shouldThrowExceptionForStartZero() {
        when(startCalc.evaluate(evaluator)).thenReturn(0);
        when(stringCheckCalc.evaluate(evaluator)).thenReturn("test");
        when(stringMatchCalc.evaluate(evaluator)).thenReturn("es");
        when(compareCalc.evaluate(evaluator)).thenReturn(1);

        assertThatThrownBy(() -> inStrCalc.evaluate(evaluator)).isInstanceOf(InvalidArgumentException.class)
                .hasMessageContaining("start must be -1 or a location in the string to start");
    }

    @Test
    @DisplayName("Should throw InvalidArgumentException for start < -1")
    void shouldThrowExceptionForStartLessThanMinusOne() {
        when(startCalc.evaluate(evaluator)).thenReturn(-2);
        when(stringCheckCalc.evaluate(evaluator)).thenReturn("test");
        when(stringMatchCalc.evaluate(evaluator)).thenReturn("es");
        when(compareCalc.evaluate(evaluator)).thenReturn(1);

        assertThatThrownBy(() -> inStrCalc.evaluate(evaluator)).isInstanceOf(InvalidArgumentException.class)
                .hasMessageContaining("start must be -1 or a location in the string to start");
    }

    static Stream<Arguments> arguments() {
        return Stream.of(
                // Basic functionality tests
                Arguments.of("found at beginning", 1, "Hello World", "Hello", 1, 1),
                Arguments.of("found in middle", 1, "Hello World", "o W", 1, 5),
                Arguments.of("found at end", 1, "Hello World", "World", 1, 7),
                Arguments.of("not found", 1, "Hello World", "xyz", 1, 0),
                Arguments.of("empty search string", 1, "Hello World", "", 1, 1),
                Arguments.of("search in empty string", 1, "", "test", 1, 0),
                Arguments.of("both strings empty", 1, "", "", 1, 1),

                // Start position tests
                Arguments.of("start from position 3", 3, "Hello World", "llo", 1, 3),
                Arguments.of("start from position beyond match", 6, "Hello World", "Hello", 1, 0),
                Arguments.of("start -1 (beginning)", -1, "Hello World", "Hello", 1, 1),
                Arguments.of("start -1 found in middle", -1, "Hello World", "World", 1, 7),

                // Case sensitivity tests (assuming case insensitive based on implementation)
                Arguments.of("case insensitive match", 1, "Hello World", "hello", 1, 1),
                Arguments.of("case insensitive in middle", 1, "Hello World", "WORLD", 1, 7),
                Arguments.of("mixed case match", 1, "HeLLo WoRLd", "llo wo", 1, 3),

                // Null string tests
                Arguments.of("null stringCheck", 1, null, "test", 1, 0),
//                Arguments.of("null stringMatch", 1, "Hello World", null, 1, 1),
                Arguments.of("both strings null", 1, null, null, 1, 0),

                // Multiple occurrences
                Arguments.of("first occurrence", 1, "test test test", "test", 1, 1),
                Arguments.of("second occurrence with start", 6, "test test test", "test", 1, 6),
                Arguments.of("overlapping patterns", 1, "aaaa", "aa", 1, 1),

                // Special characters
                Arguments.of("special characters", 1, "Hello@World#Test", "@World", 1, 6),
                Arguments.of("whitespace matching", 1, "Hello World", " ", 1, 6),
                Arguments.of("tab character", 1, "Hello\tWorld", "\t", 1, 6),
                Arguments.of("newline character", 1, "Hello\nWorld", "\n", 1, 6),

                // Edge cases
                Arguments.of("single character search", 1, "a", "a", 1, 1),
                Arguments.of("single character not found", 1, "a", "b", 1, 0),
                Arguments.of("longer search than string", 1, "ab", "abc", 1, 0),
                Arguments.of("exact match", 1, "test", "test", 1, 1));
    }
}