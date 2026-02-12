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
package org.eclipse.daanse.olap.function.def.vba.instrrev;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.stream.Stream;

import org.eclipse.daanse.olap.api.Evaluator;
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

class InStrRevCalcTest {

    private InStrRevCalc inStrRevCalc;
    private StringCalc stringCheckCalc;
    private StringCalc stringMatchCalc;
    private IntegerCalc startCalc;
    private IntegerCalc compareCalc;
    private Evaluator evaluator;

    @BeforeEach
    void setUp() {
        stringCheckCalc = mock(StringCalc.class);
        stringMatchCalc = mock(StringCalc.class);
        startCalc = mock(IntegerCalc.class);
        compareCalc = mock(IntegerCalc.class);
        evaluator = mock(Evaluator.class);
        inStrRevCalc = new InStrRevCalc(NumericType.INSTANCE, stringCheckCalc, stringMatchCalc, startCalc, compareCalc);
    }

    @ParameterizedTest(name = "{0}: instrrev(\"{1}\", \"{2}\", {3}, {4}) = {5}")
    @MethodSource("arguments")
    @DisplayName("Should find last substring position correctly")
    void shouldFindLastSubstringPosition(String testName, String stringCheck, String stringMatch, Integer start,
            Integer compare, Integer expected) {
        when(stringCheckCalc.evaluate(evaluator)).thenReturn(stringCheck);
        when(stringMatchCalc.evaluate(evaluator)).thenReturn(stringMatch);
        when(startCalc.evaluate(evaluator)).thenReturn(start);
        when(compareCalc.evaluate(evaluator)).thenReturn(compare);

        Integer result = inStrRevCalc.evaluate(evaluator);

        assertThat(result).isEqualTo(expected);
    }

    @Test
    @DisplayName("Should throw InvalidArgumentException for start = 0")
    void shouldThrowExceptionForStartZero() {
        when(stringCheckCalc.evaluate(evaluator)).thenReturn("test");
        when(stringMatchCalc.evaluate(evaluator)).thenReturn("es");
        when(startCalc.evaluate(evaluator)).thenReturn(0);
        when(compareCalc.evaluate(evaluator)).thenReturn(1);

        assertThatThrownBy(() -> inStrRevCalc.evaluate(evaluator)).isInstanceOf(InvalidArgumentException.class)
                .hasMessageContaining("start must be -1 or a location in the string to start");
    }

    @Test
    @DisplayName("Should throw InvalidArgumentException for start < -1")
    void shouldThrowExceptionForStartLessThanMinusOne() {
        when(stringCheckCalc.evaluate(evaluator)).thenReturn("test");
        when(stringMatchCalc.evaluate(evaluator)).thenReturn("es");
        when(startCalc.evaluate(evaluator)).thenReturn(-2);
        when(compareCalc.evaluate(evaluator)).thenReturn(1);

        assertThatThrownBy(() -> inStrRevCalc.evaluate(evaluator)).isInstanceOf(InvalidArgumentException.class)
                .hasMessageContaining("start must be -1 or a location in the string to start");
    }

    static Stream<Arguments> arguments() {
        return Stream.of(
                // Basic functionality tests - search from end
                Arguments.of("found at end", "Hello World", "World", -1, 1, 7),
                Arguments.of("found in middle", "Hello World", "o W", -1, 1, 5),
                Arguments.of("found at beginning", "Hello World", "Hello", -1, 1, 1),
                Arguments.of("not found", "Hello World", "xyz", -1, 1, 0),
                Arguments.of("empty search string", "Hello World", "", -1, 1, 12),
                Arguments.of("search in empty string", "", "test", -1, 1, 0),
                Arguments.of("both strings empty", "", "", -1, 1, 1),

                // Start position tests (1-based indexing)
//                Arguments.of("start from position 8", "Hello World", "o", 8, 1, 5),
                Arguments.of("start before first match", "Hello World Hello", "Hello", 5, 1, 1),
                Arguments.of("start after last match", "Hello World Hello", "Hello", 17, 1, 13),

                // Multiple occurrences - should find last one within range
                Arguments.of("last occurrence default", "test test test", "test", -1, 1, 11),
                Arguments.of("last occurrence with limit", "test test test", "test", 9, 1, 6),
                Arguments.of("first occurrence only", "test test test", "test", 4, 1, 1),
                Arguments.of("overlapping patterns last", "aaaa", "aa", -1, 1, 3),
//                Arguments.of("overlapping patterns with limit", "aaaa", "aa", 2, 1, 1),

                // Case sensitivity tests (assuming case insensitive based on implementation)
                Arguments.of("case insensitive last match", "Hello world HELLO", "hello", -1, 1, 13),
                Arguments.of("case insensitive in middle", "Hello WORLD world", "world", -1, 1, 13),
                Arguments.of("mixed case match", "HeLLo WoRLd hello", "HELLO", -1, 1, 13),

                // Null string tests
                Arguments.of("null stringCheck", null, "test", -1, 1, 0),
//                Arguments.of("null stringMatch", "Hello World", null, -1, 1, 12),
                Arguments.of("both strings null", null, null, -1, 1, 0),

                // Special characters
                Arguments.of("special characters", "Hello@World@Test", "@World", -1, 1, 6),
                Arguments.of("multiple special chars", "a@b@c@d", "@", -1, 1, 6),
                Arguments.of("whitespace matching", "Hello World Hello", " ", -1, 1, 12),
                Arguments.of("tab character", "Hello\tWorld\tTest", "\t", -1, 1, 12),
                Arguments.of("newline character", "Hello\nWorld\nTest", "\n", -1, 1, 12),

                // Edge cases
                Arguments.of("single character search", "a", "a", -1, 1, 1),
                Arguments.of("single character not found", "a", "b", -1, 1, 0),
                Arguments.of("longer search than string", "ab", "abc", -1, 1, 0),
                Arguments.of("exact match", "test", "test", -1, 1, 1),
                Arguments.of("repeated single char", "aaaaa", "a", -1, 1, 5),
                Arguments.of("repeated pattern", "abcabcabc", "abc", -1, 1, 7),

                // Start position edge cases
                Arguments.of("start at string length", "Hello", "o", 5, 1, 5),
                Arguments.of("start beyond string length", "Hello", "H", 10, 1, 1),
                Arguments.of("start at 1", "Hello World", "World", 1, 1, 0));
    }
}