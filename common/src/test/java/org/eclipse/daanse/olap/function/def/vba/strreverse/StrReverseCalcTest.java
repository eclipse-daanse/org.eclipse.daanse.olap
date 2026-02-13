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
package org.eclipse.daanse.olap.function.def.vba.strreverse;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.stream.Stream;

import org.eclipse.daanse.olap.api.evaluator.Evaluator;
import org.eclipse.daanse.olap.api.calc.StringCalc;
import org.eclipse.daanse.olap.api.type.StringType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class StrReverseCalcTest {

    private StrReverseCalc strReverseCalc;
    private StringCalc stringCalc;
    private Evaluator evaluator;

    @BeforeEach
    void setUp() {
        stringCalc = mock(StringCalc.class);
        evaluator = mock(Evaluator.class);
        strReverseCalc = new StrReverseCalc(StringType.INSTANCE, stringCalc);
    }

    @ParameterizedTest(name = "{0}: StrReverse(\"{1}\") = \"{2}\"")
    @MethodSource("strReverseArguments")
    @DisplayName("Should reverse strings correctly")
    void shouldReverseStringsCorrectly(String testName, String input, String expectedResult) {
        when(stringCalc.evaluate(evaluator)).thenReturn(input);

        String result = strReverseCalc.evaluate(evaluator);

        assertThat(result).isEqualTo(expectedResult);
    }

    static Stream<Arguments> strReverseArguments() {
        return Stream.of(Arguments.of("simple word", "hello", "olleh"), Arguments.of("single character", "A", "A"),
                Arguments.of("empty string", "", ""), Arguments.of("palindrome", "racecar", "racecar"),
                Arguments.of("with spaces", "hello world", "dlrow olleh"),
                Arguments.of("mixed case", "Hello World", "dlroW olleH"),
                Arguments.of("with numbers", "abc123", "321cba"), Arguments.of("special characters", "!@#$%", "%$#@!"),
                Arguments.of("all spaces", "   ", "   "), Arguments.of("leading spaces", "  hello", "olleh  "),
                Arguments.of("trailing spaces", "hello  ", "  olleh"));
    }

    @Test
    @DisplayName("Should handle Unicode characters correctly")
    void shouldHandleUnicodeCharactersCorrectly() {
        when(stringCalc.evaluate(evaluator)).thenReturn("café");

        String result = strReverseCalc.evaluate(evaluator);

        assertThat(result).isEqualTo("éfac");
    }

    @Test
    @DisplayName("Should handle long strings correctly")
    void shouldHandleLongStringsCorrectly() {
        String longString = "a".repeat(1000) + "b".repeat(1000);
        String expectedReverse = "b".repeat(1000) + "a".repeat(1000);

        when(stringCalc.evaluate(evaluator)).thenReturn(longString);

        String result = strReverseCalc.evaluate(evaluator);

        assertThat(result).isEqualTo(expectedReverse);
        assertThat(result).hasSize(2000);
    }

    @Test
    @DisplayName("Should handle strings with newlines correctly")
    void shouldHandleStringsWithNewlinesCorrectly() {
        when(stringCalc.evaluate(evaluator)).thenReturn("hello\nworld");

        String result = strReverseCalc.evaluate(evaluator);

        assertThat(result).isEqualTo("dlrow\nolleh");
    }

    @Test
    @DisplayName("Should handle strings with tabs correctly")
    void shouldHandleStringsWithTabsCorrectly() {
        when(stringCalc.evaluate(evaluator)).thenReturn("hello\tworld");

        String result = strReverseCalc.evaluate(evaluator);

        assertThat(result).isEqualTo("dlrow\tolleh");
    }

    @Test
    @DisplayName("Should handle control characters correctly")
    void shouldHandleControlCharactersCorrectly() {
        String input = "a\b\f\r\n\t";
        String expected = "\t\n\r\f\ba";

        when(stringCalc.evaluate(evaluator)).thenReturn(input);

        String result = strReverseCalc.evaluate(evaluator);

        assertThat(result).isEqualTo(expected);
    }

    @Test
    @DisplayName("Should use static strReverse method correctly")
    void shouldUseStaticStrReverseMethodCorrectly() {
        // Test the static method directly
        String result1 = StrReverseCalc.strReverse("hello");
        String result2 = StrReverseCalc.strReverse("");
        String result3 = StrReverseCalc.strReverse("a");
        String result4 = StrReverseCalc.strReverse("12345");

        assertThat(result1).isEqualTo("olleh");
        assertThat(result2).isEqualTo("");
        assertThat(result3).isEqualTo("a");
        assertThat(result4).isEqualTo("54321");
    }

    @Test
    @DisplayName("Should handle even length strings correctly")
    void shouldHandleEvenLengthStringsCorrectly() {
        when(stringCalc.evaluate(evaluator)).thenReturn("abcd");

        String result = strReverseCalc.evaluate(evaluator);

        assertThat(result).isEqualTo("dcba");
    }

    @Test
    @DisplayName("Should handle odd length strings correctly")
    void shouldHandleOddLengthStringsCorrectly() {
        when(stringCalc.evaluate(evaluator)).thenReturn("abcde");

        String result = strReverseCalc.evaluate(evaluator);

        assertThat(result).isEqualTo("edcba");
    }

    @Test
    @DisplayName("Should handle repeated characters correctly")
    void shouldHandleRepeatedCharactersCorrectly() {
        when(stringCalc.evaluate(evaluator)).thenReturn("aaabbbccc");

        String result = strReverseCalc.evaluate(evaluator);

        assertThat(result).isEqualTo("cccbbbaaa");
    }

    @Test
    @DisplayName("Should handle numeric strings correctly")
    void shouldHandleNumericStringsCorrectly() {
        when(stringCalc.evaluate(evaluator)).thenReturn("123456789");

        String result = strReverseCalc.evaluate(evaluator);

        assertThat(result).isEqualTo("987654321");
    }

    @Test
    @DisplayName("Should handle mixed alphanumeric strings correctly")
    void shouldHandleMixedAlphanumericStringsCorrectly() {
        when(stringCalc.evaluate(evaluator)).thenReturn("a1b2c3");

        String result = strReverseCalc.evaluate(evaluator);

        assertThat(result).isEqualTo("3c2b1a");
    }

    @Test
    @DisplayName("Should handle strings with punctuation correctly")
    void shouldHandleStringsWithPunctuationCorrectly() {
        when(stringCalc.evaluate(evaluator)).thenReturn("Hello, World!");

        String result = strReverseCalc.evaluate(evaluator);

        assertThat(result).isEqualTo("!dlroW ,olleH");
    }

    @Test
    @DisplayName("Should preserve type information")
    void shouldPreserveTypeInformation() {
        assertThat(strReverseCalc.getType()).isEqualTo(StringType.INSTANCE);
    }

    @Test
    @DisplayName("Should be consistent across multiple calls")
    void shouldBeConsistentAcrossMultipleCalls() {
        when(stringCalc.evaluate(evaluator)).thenReturn("hello");

        String first = strReverseCalc.evaluate(evaluator);
        String second = strReverseCalc.evaluate(evaluator);

        assertThat(first).isEqualTo(second);
        assertThat(first).isEqualTo("olleh");
    }

    @Test
    @DisplayName("Should be reversible operation")
    void shouldBeReversibleOperation() {
        String original = "Hello World!";
        when(stringCalc.evaluate(evaluator)).thenReturn(original);

        String reversed = strReverseCalc.evaluate(evaluator);
        String doubleReversed = StrReverseCalc.strReverse(reversed);

        assertThat(doubleReversed).isEqualTo(original);
    }

    @Test
    @DisplayName("Should handle high Unicode characters correctly")
    void shouldHandleHighUnicodeCharactersCorrectly() {
        when(stringCalc.evaluate(evaluator)).thenReturn("αβγδε");

        String result = strReverseCalc.evaluate(evaluator);

        assertThat(result).isEqualTo("εδγβα");
    }

    @Test
    @DisplayName("Should handle emoji characters correctly")

    @Disabled
    void shouldHandleEmojiCharactersCorrectly() {
        when(stringCalc.evaluate(evaluator)).thenReturn("🙂🙃😊");

        String result = strReverseCalc.evaluate(evaluator);

        assertThat(result).isEqualTo("😊🙃🙂");
    }

    @Test
    @DisplayName("Should handle strings with quotes correctly")
    void shouldHandleStringsWithQuotesCorrectly() {
        when(stringCalc.evaluate(evaluator)).thenReturn("\"Hello 'World'\"");

        String result = strReverseCalc.evaluate(evaluator);

        assertThat(result).isEqualTo("\"'dlroW' olleH\"");
    }

    @Test
    @DisplayName("Should handle very large strings efficiently")
    void shouldHandleVeryLargeStringsEfficiently() {
        String largeString = "x".repeat(10000);
        when(stringCalc.evaluate(evaluator)).thenReturn(largeString);

        String result = strReverseCalc.evaluate(evaluator);

        assertThat(result).hasSize(10000);
        assertThat(result).isEqualTo(largeString); // All same character, so reverse equals original
    }
}