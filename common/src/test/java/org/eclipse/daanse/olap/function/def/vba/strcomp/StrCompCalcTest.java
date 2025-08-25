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
package org.eclipse.daanse.olap.function.def.vba.strcomp;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.stream.Stream;

import org.eclipse.daanse.olap.api.Evaluator;
import org.eclipse.daanse.olap.api.calc.IntegerCalc;
import org.eclipse.daanse.olap.api.calc.StringCalc;
import org.eclipse.daanse.olap.api.type.NumericType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class StrCompCalcTest {

    private StrCompCalc strCompCalc;
    private StringCalc string1Calc;
    private StringCalc string2Calc;
    private IntegerCalc compareCalc;
    private Evaluator evaluator;

    @BeforeEach
    void setUp() {
        string1Calc = mock(StringCalc.class);
        string2Calc = mock(StringCalc.class);
        compareCalc = mock(IntegerCalc.class);
        evaluator = mock(Evaluator.class);
        strCompCalc = new StrCompCalc(NumericType.INSTANCE, string1Calc, string2Calc, compareCalc);
    }

    @ParameterizedTest(name = "{0}: StrComp(\"{1}\", \"{2}\", {3}) = {4}")
    @MethodSource("strCompArguments")
    @DisplayName("Should compare strings correctly")
    void shouldCompareStringsCorrectly(String testName, String string1, String string2, Integer compare,
            Integer expectedResult) {
        when(string1Calc.evaluate(evaluator)).thenReturn(string1);
        when(string2Calc.evaluate(evaluator)).thenReturn(string2);
        when(compareCalc.evaluate(evaluator)).thenReturn(compare);

        Integer result = strCompCalc.evaluate(evaluator);

        // String.compareTo returns:
        // - negative if string1 < string2
        // - 0 if string1 equals string2
        // - positive if string1 > string2
        if (expectedResult < 0) {
            assertThat(result).isNegative();
        } else if (expectedResult > 0) {
            assertThat(result).isPositive();
        } else {
            assertThat(result).isZero();
        }
    }

    static Stream<Arguments> strCompArguments() {
        return Stream.of(Arguments.of("equal strings", "hello", "hello", 0, 0),
                Arguments.of("first less than second", "apple", "banana", 0, -1),
                Arguments.of("first greater than second", "banana", "apple", 0, 1),
                Arguments.of("empty strings", "", "", 0, 0), Arguments.of("empty vs non-empty", "", "hello", 0, -1),
                Arguments.of("non-empty vs empty", "hello", "", 0, 1),
                Arguments.of("case sensitive comparison", "Hello", "hello", 0, -1),
                Arguments.of("different lengths same prefix", "test", "testing", 0, -1),
                Arguments.of("same prefix different length", "testing", "test", 0, 1),
                Arguments.of("numeric strings", "123", "124", 0, -1),
                Arguments.of("special characters", "!@#", "$%^", 0, -1));
    }

    @Test
    @DisplayName("Should handle identical strings")
    void shouldHandleIdenticalStrings() {
        String testString = "identical";
        when(string1Calc.evaluate(evaluator)).thenReturn(testString);
        when(string2Calc.evaluate(evaluator)).thenReturn(testString);
        when(compareCalc.evaluate(evaluator)).thenReturn(0);

        Integer result = strCompCalc.evaluate(evaluator);

        assertThat(result).isZero();
    }

    @Test
    @DisplayName("Should handle unicode strings correctly")
    void shouldHandleUnicodeStringsCorrectly() {
        when(string1Calc.evaluate(evaluator)).thenReturn("café");
        when(string2Calc.evaluate(evaluator)).thenReturn("cafe");
        when(compareCalc.evaluate(evaluator)).thenReturn(0);

        Integer result = strCompCalc.evaluate(evaluator);

        assertThat(result).isNotZero(); // Unicode é != e
    }

    @Test
    @DisplayName("Should handle whitespace differences")
    void shouldHandleWhitespaceDifferences() {
        when(string1Calc.evaluate(evaluator)).thenReturn("hello world");
        when(string2Calc.evaluate(evaluator)).thenReturn("hello  world");
        when(compareCalc.evaluate(evaluator)).thenReturn(0);

        Integer result = strCompCalc.evaluate(evaluator);

        assertThat(result).isNotZero(); // Different whitespace
    }

    @Test
    @DisplayName("Should handle leading and trailing whitespace")
    void shouldHandleLeadingAndTrailingWhitespace() {
        when(string1Calc.evaluate(evaluator)).thenReturn(" hello ");
        when(string2Calc.evaluate(evaluator)).thenReturn("hello");
        when(compareCalc.evaluate(evaluator)).thenReturn(0);

        Integer result = strCompCalc.evaluate(evaluator);

        assertThat(result).isNotZero(); // Leading space makes it different
    }

    @Test
    @DisplayName("Should handle very long strings")
    void shouldHandleVeryLongStrings() {
        String long1 = "a".repeat(1000);
        String long2 = "a".repeat(999) + "b";

        when(string1Calc.evaluate(evaluator)).thenReturn(long1);
        when(string2Calc.evaluate(evaluator)).thenReturn(long2);
        when(compareCalc.evaluate(evaluator)).thenReturn(0);

        Integer result = strCompCalc.evaluate(evaluator);

        assertThat(result).isNegative(); // 'a' < 'b' at the end
    }

    @Test
    @DisplayName("Should handle compare parameter variations")
    void shouldHandleCompareParameterVariations() {
        // According to the code comment, compare parameter is currently ignored
        when(string1Calc.evaluate(evaluator)).thenReturn("Hello");
        when(string2Calc.evaluate(evaluator)).thenReturn("hello");

        // Test with different compare values (should have same result since ignored)
        when(compareCalc.evaluate(evaluator)).thenReturn(0);
        Integer result1 = strCompCalc.evaluate(evaluator);

        when(compareCalc.evaluate(evaluator)).thenReturn(1);
        Integer result2 = strCompCalc.evaluate(evaluator);

        assertThat(result1).isEqualTo(result2); // Compare parameter is ignored
        assertThat(result1).isNegative(); // 'H' < 'h' in ASCII
    }

    @Test
    @DisplayName("Should use static strComp method correctly")
    void shouldUseStaticStrCompMethodCorrectly() {
        // Test the static method directly
        int result1 = StrCompCalc.strComp("apple", "banana", 0);
        int result2 = StrCompCalc.strComp("banana", "apple", 0);
        int result3 = StrCompCalc.strComp("test", "test", 0);

        assertThat(result1).isNegative();
        assertThat(result2).isPositive();
        assertThat(result3).isZero();
    }

    @Test
    @DisplayName("Should preserve type information")
    void shouldPreserveTypeInformation() {
        assertThat(strCompCalc.getType()).isEqualTo(NumericType.INSTANCE);
    }

    @Test
    @DisplayName("Should be consistent across multiple calls")
    void shouldBeConsistentAcrossMultipleCalls() {
        when(string1Calc.evaluate(evaluator)).thenReturn("test1");
        when(string2Calc.evaluate(evaluator)).thenReturn("test2");
        when(compareCalc.evaluate(evaluator)).thenReturn(0);

        Integer first = strCompCalc.evaluate(evaluator);
        Integer second = strCompCalc.evaluate(evaluator);

        assertThat(first).isEqualTo(second);
        assertThat(first).isNegative(); // "test1" < "test2"
    }

    @Test
    @DisplayName("Should handle numbers as strings correctly")
    void shouldHandleNumbersAsStringsCorrectly() {
        when(string1Calc.evaluate(evaluator)).thenReturn("10");
        when(string2Calc.evaluate(evaluator)).thenReturn("2");
        when(compareCalc.evaluate(evaluator)).thenReturn(0);

        Integer result = strCompCalc.evaluate(evaluator);

        // String comparison: "10" < "2" (lexicographic)
        assertThat(result).isNegative();
    }

    @Test
    @DisplayName("Should handle case sensitivity correctly")
    void shouldHandleCaseSensitivityCorrectly() {
        when(string1Calc.evaluate(evaluator)).thenReturn("ABC");
        when(string2Calc.evaluate(evaluator)).thenReturn("abc");
        when(compareCalc.evaluate(evaluator)).thenReturn(0);

        Integer result = strCompCalc.evaluate(evaluator);

        // Uppercase letters have smaller ASCII values than lowercase
        assertThat(result).isNegative(); // "ABC" < "abc"
    }

    @Test
    @DisplayName("Should handle mixed case strings correctly")
    void shouldHandleMixedCaseStringsCorrectly() {
        when(string1Calc.evaluate(evaluator)).thenReturn("Hello World");
        when(string2Calc.evaluate(evaluator)).thenReturn("hello world");
        when(compareCalc.evaluate(evaluator)).thenReturn(0);

        Integer result = strCompCalc.evaluate(evaluator);

        assertThat(result).isNegative(); // 'H' < 'h'
    }

    @Test
    @DisplayName("Should handle special character strings correctly")
    void shouldHandleSpecialCharacterStringsCorrectly() {
        when(string1Calc.evaluate(evaluator)).thenReturn("hello@world");
        when(string2Calc.evaluate(evaluator)).thenReturn("hello#world");
        when(compareCalc.evaluate(evaluator)).thenReturn(0);

        Integer result = strCompCalc.evaluate(evaluator);

        assertThat(result).isPositive(); // '@' > '#' in ASCII
    }
}