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
package org.eclipse.daanse.olap.function.def.vba.lcase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.stream.Stream;

import org.eclipse.daanse.olap.api.evaluator.Evaluator;
import org.eclipse.daanse.olap.api.calc.StringCalc;
import org.eclipse.daanse.olap.api.type.StringType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class LCaseCalcTest {

    private LCaseCalc lCaseCalc;
    private StringCalc stringCalc;
    private Evaluator evaluator;

    @BeforeEach
    void setUp() {
        stringCalc = mock(StringCalc.class);
        evaluator = mock(Evaluator.class);
        lCaseCalc = new LCaseCalc(StringType.INSTANCE, stringCalc);
    }

    @ParameterizedTest(name = "{0}: lcase(\"{1}\") = \"{2}\"")
    @MethodSource("arguments")
    @DisplayName("Should convert strings to lowercase correctly")
    void shouldConvertStringToLowercase(String testName, String input, String expected) {
        when(stringCalc.evaluate(evaluator)).thenReturn(input);

        String result = lCaseCalc.evaluate(evaluator);

        assertThat(result).isEqualTo(expected);
    }

    static Stream<Arguments> arguments() {
        return Stream.of(
                // Basic cases
                Arguments.of("all uppercase", "HELLO WORLD", "hello world"),
                Arguments.of("all lowercase", "hello world", "hello world"),
                Arguments.of("mixed case", "Hello World", "hello world"),
                Arguments.of("single uppercase letter", "A", "a"), Arguments.of("single lowercase letter", "a", "a"),
                Arguments.of("empty string", "", ""),

                // Special characters and numbers
                Arguments.of("with numbers", "Hello123World", "hello123world"),
                Arguments.of("with special chars", "Hello@World#", "hello@world#"),
                Arguments.of("only numbers", "12345", "12345"),
                Arguments.of("only special chars", "!@#$%^&*()", "!@#$%^&*()"),

                // Whitespace
                Arguments.of("with spaces", "HELLO WORLD", "hello world"),
                Arguments.of("with tabs", "HELLO\tWORLD", "hello\tworld"),
                Arguments.of("with newlines", "HELLO\nWORLD", "hello\nworld"),
                Arguments.of("leading/trailing spaces", " HELLO WORLD ", " hello world "),

                // International characters
                Arguments.of("accented uppercase", "ÀÁÂÃÄÅ", "àáâãäå"),
                Arguments.of("accented mixed case", "ÀáÂãÄå", "àáâãäå"), Arguments.of("german umlauts", "ÄÖÜß", "äöüß"),
                Arguments.of("french accents", "ÉÈÊË", "éèêë"),

                // Long strings
                Arguments.of("long mixed case", "THE QUICK BROWN FOX JUMPS OVER THE LAZY DOG",
                        "the quick brown fox jumps over the lazy dog"),
                Arguments.of("alternating case", "aBcDeFgHiJkLmNoPqRsTuVwXyZ", "abcdefghijklmnopqrstuvwxyz"),

                // Edge cases
                Arguments.of("only spaces", "   ", "   "), Arguments.of("single character uppercase", "Z", "z"),
                Arguments.of("punctuation and letters", "Hello, World!", "hello, world!"),
                Arguments.of("mixed with underscores", "Hello_WORLD_Test", "hello_world_test"),
                Arguments.of("camelCase", "camelCaseString", "camelcasestring"),
                Arguments.of("SCREAMING_SNAKE_CASE", "SCREAMING_SNAKE_CASE", "screaming_snake_case"),

                // Unicode and special cases
                Arguments.of("cyrillic uppercase", "ПРИВЕТ", "привет"),
                Arguments.of("greek uppercase", "ΑΒΓΔΕ", "αβγδε"),
                Arguments.of("mixed latin and cyrillic", "HelloПРИВЕТ", "helloпривет"));
    }
}