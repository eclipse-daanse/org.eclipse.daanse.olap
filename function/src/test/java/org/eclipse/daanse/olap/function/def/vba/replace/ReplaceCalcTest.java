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
package org.eclipse.daanse.olap.function.def.vba.replace;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.stream.Stream;

import org.eclipse.daanse.olap.api.evaluator.Evaluator;
import org.eclipse.daanse.olap.api.calc.IntegerCalc;
import org.eclipse.daanse.olap.api.calc.StringCalc;
import org.eclipse.daanse.olap.api.type.StringType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class ReplaceCalcTest {

    private ReplaceCalc replaceCalc;
    private StringCalc expressionCalc;
    private StringCalc findCalc;
    private StringCalc replaceStringCalc;
    private IntegerCalc startCalc;
    private IntegerCalc countCalc;
    private IntegerCalc compareCalc;
    private Evaluator evaluator;

    @BeforeEach
    void setUp() {
        expressionCalc = mock(StringCalc.class);
        findCalc = mock(StringCalc.class);
        replaceStringCalc = mock(StringCalc.class);
        startCalc = mock(IntegerCalc.class);
        countCalc = mock(IntegerCalc.class);
        compareCalc = mock(IntegerCalc.class);
        evaluator = mock(Evaluator.class);
        replaceCalc = new ReplaceCalc(StringType.INSTANCE, expressionCalc, findCalc, replaceStringCalc, startCalc,
                countCalc, compareCalc);
    }

    @ParameterizedTest(name = "{0}: replace('{1}', '{2}', '{3}', {4}, {5}) = '{6}'")
    @MethodSource("replaceArguments")
    @DisplayName("Should replace substrings correctly")
    void shouldReplaceSubstrings(String testName, String expression, String find, String replace, Integer start,
            Integer count, String expected) {
        when(expressionCalc.evaluate(evaluator)).thenReturn(expression);
        when(findCalc.evaluate(evaluator)).thenReturn(find);
        when(replaceStringCalc.evaluate(evaluator)).thenReturn(replace);
        when(startCalc.evaluate(evaluator)).thenReturn(start);
        when(countCalc.evaluate(evaluator)).thenReturn(count);
        when(compareCalc.evaluate(evaluator)).thenReturn(0);

        String result = replaceCalc.evaluate(evaluator);

        assertThat(result).isEqualTo(expected);
    }

    static Stream<Arguments> replaceArguments() {
        return Stream.of(Arguments.of("basic replacement", "hello world", "world", "universe", 1, -1, "hello universe"),
                Arguments.of("multiple occurrences", "hello hello hello", "hello", "hi", 1, -1, "hi hi hi"),
                Arguments.of("single occurrence only", "hello hello hello", "hello", "hi", 1, 1, "hi hello hello"),
                Arguments.of("start from middle", "abcabcabc", "abc", "xyz", 4, -1, "abcxyzxyz"),
                Arguments.of("no matches", "hello world", "xyz", "abc", 1, -1, "hello world"),
                Arguments.of("empty replacement", "hello world", "world", "", 1, -1, "hello "),
                Arguments.of("replace with longer string", "a", "a", "abc", 1, -1, "abc"),
                Arguments.of("replace at beginning", "hello world", "hello", "hi", 1, -1, "hi world"),
                Arguments.of("case sensitive", "Hello World", "hello", "hi", 1, -1, "Hello World"));
    }

    @Test
    @DisplayName("Should test static replace method directly")
    void shouldTestStaticMethod() {
        String result = ReplaceCalc.replace("hello world", "world", "universe", 1, -1);
        assertThat(result).isEqualTo("hello universe");
    }

    @Test
    @DisplayName("Should handle start position correctly")
    void shouldHandleStartPositionCorrectly() {
        // Start from position 1 (1-based indexing)
        String result1 = ReplaceCalc.replace("abcabcabc", "abc", "xyz", 1, -1);
        assertThat(result1).isEqualTo("xyzxyzxyz");

        // Start from position 4 (skip first "abc")
        String result2 = ReplaceCalc.replace("abcabcabc", "abc", "xyz", 4, -1);
        assertThat(result2).isEqualTo("abcxyzxyz");

        // Start from position 7 (skip first two "abc")
        String result3 = ReplaceCalc.replace("abcabcabc", "abc", "xyz", 7, -1);
        assertThat(result3).isEqualTo("abcabcxyz");
    }

    @Test
    @DisplayName("Should handle count parameter correctly")
    void shouldHandleCountParameterCorrectly() {
        String text = "hello hello hello hello";

        // Replace all occurrences
        String result1 = ReplaceCalc.replace(text, "hello", "hi", 1, -1);
        assertThat(result1).isEqualTo("hi hi hi hi");

        // Replace only first occurrence
        String result2 = ReplaceCalc.replace(text, "hello", "hi", 1, 1);
        assertThat(result2).isEqualTo("hi hello hello hello");

        // Replace first two occurrences
        String result3 = ReplaceCalc.replace(text, "hello", "hi", 1, 2);
        assertThat(result3).isEqualTo("hi hi hello hello");
    }

    @Test
    @DisplayName("Should preserve type information")
    void shouldPreserveTypeInformation() {
        assertThat(replaceCalc.getType()).isEqualTo(StringType.INSTANCE);
    }

    @Test
    @DisplayName("Should handle edge cases")
    void shouldHandleEdgeCases() {
        // Empty expression
        String result1 = ReplaceCalc.replace("", "abc", "xyz", 1, -1);
        assertThat(result1).isEqualTo("");

        // Empty find string
        String result2 = ReplaceCalc.replace("hello", "", "xyz", 1, -1);
        assertThat(result2).isEqualTo("hello"); // No replacement should occur

        // Start position beyond string length
        String result3 = ReplaceCalc.replace("hello", "hello", "hi", 10, -1);
        assertThat(result3).isEqualTo("hello");

        // Count of 0
        String result4 = ReplaceCalc.replace("hello world", "world", "universe", 1, 0);
        assertThat(result4).isEqualTo("hello world");
    }

    @Test
    @DisplayName("Should handle overlapping patterns")
    void shouldHandleOverlappingPatterns() {
        // Replace "aa" with "b" in "aaa" - should result in "ba" not "bb"
        String result = ReplaceCalc.replace("aaa", "aa", "b", 1, -1);
        assertThat(result).isEqualTo("ba");
    }
}