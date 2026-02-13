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
package org.eclipse.daanse.olap.function.def.vba.rtrim;

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

class RTrimCalcTest {

    private RTrimCalc rTrimCalc;
    private StringCalc stringCalc;
    private Evaluator evaluator;

    @BeforeEach
    void setUp() {
        stringCalc = mock(StringCalc.class);
        evaluator = mock(Evaluator.class);
        rTrimCalc = new RTrimCalc(StringType.INSTANCE, stringCalc);
    }

    @ParameterizedTest(name = "{0}: rtrim(\"{1}\") = \"{2}\"")
    @MethodSource("arguments")
    @DisplayName("Should trim trailing whitespace correctly")
    void shouldTrimTrailingWhitespace(String testName, String input, String expected) {
        when(stringCalc.evaluate(evaluator)).thenReturn(input);

        String result = rTrimCalc.evaluate(evaluator);

        assertThat(result).isEqualTo(expected);
    }

    static Stream<Arguments> arguments() {
        return Stream.of(
                // Basic trailing space trimming
                Arguments.of("single trailing space", "Hello ", "Hello"),
                Arguments.of("multiple trailing spaces", "Hello   ", "Hello"),
                Arguments.of("no trailing spaces", "Hello", "Hello"),
                Arguments.of("only leading spaces", "   Hello", "   Hello"),
                Arguments.of("leading and trailing spaces", "   Hello   ", "   Hello"),

                // Empty and whitespace-only strings
                Arguments.of("empty string", "", ""), Arguments.of("single space", " ", ""),
                Arguments.of("multiple spaces", "   ", ""), Arguments.of("only whitespace", "     ", ""),

                // Different whitespace characters (chars <= ' ' which is ASCII 32)
                Arguments.of("trailing tab", "Hello\t", "Hello"), Arguments.of("trailing newline", "Hello\n", "Hello"),
                Arguments.of("trailing carriage return", "Hello\r", "Hello"),
                Arguments.of("trailing form feed", "Hello\f", "Hello"),
                Arguments.of("mixed trailing whitespace", "Hello \t\n\r", "Hello"),

                // Control characters (ASCII < 32)
                Arguments.of("trailing null char", "Hello\u0000", "Hello"),
                Arguments.of("trailing backspace", "Hello\u0008", "Hello"),
                Arguments.of("trailing vertical tab", "Hello\u000B", "Hello"),
                Arguments.of("trailing escape", "Hello\u001B", "Hello"),
                Arguments.of("mixed control chars", "Hello\u0000\u0008\u001B", "Hello"),

                // Preserving internal whitespace
                Arguments.of("spaces in middle preserved", "  Hello World  ", "  Hello World"),
                Arguments.of("tabs in middle preserved", "  Hello\tWorld  ", "  Hello\tWorld"),
                Arguments.of("newlines in middle preserved", "  Hello\nWorld  ", "  Hello\nWorld"),

                // Edge cases with special characters
                Arguments.of("ends with exclamation", "Hello! ", "Hello!"),
                Arguments.of("ends with number", "123  ", "123"),
                Arguments.of("ends with symbol", "Hello@   ", "Hello@"),

                // Unicode and non-ASCII
                Arguments.of("unicode content", "Héllo Wørld  ", "Héllo Wørld"),
                Arguments.of("emoji content", "😀Hello  ", "😀Hello"),

                // Long strings
                Arguments.of("long string with many trailing spaces",
                        "This is a very long string with many trailing spaces          ",
                        "This is a very long string with many trailing spaces"),

                // All characters are whitespace
                Arguments.of("all spaces", "     ", ""), Arguments.of("all tabs", "\t\t\t", ""),
                Arguments.of("mixed all whitespace", " \t\n\r\f", ""),

                // Single characters
                Arguments.of("single char no trailing", "A", "A"), Arguments.of("single char with trailing", "A ", "A"),
                Arguments.of("just space char", " ", ""),

                // Special boundary cases
                Arguments.of("space then space char (ASCII 32)", "Hello  ", "Hello"),
                Arguments.of("char 33 (!) - should not trim", "Hello!!", "Hello!!"),
                Arguments.of("mixed low ASCII", "Hello\u0020\u0001", "Hello"), // space (32) and char 1

                // Complex whitespace patterns
                Arguments.of("alternating spaces and text", " A B C ", " A B C"),
                Arguments.of("tabs between words", "Hello\t\tWorld\t", "Hello\t\tWorld"),
                Arguments.of("newlines between words", "Hello\n\nWorld\n", "Hello\n\nWorld"),

                // Edge case: string ending exactly at space boundary
                Arguments.of("ends with char 32", "Hello ", "Hello"),
                Arguments.of("ends with char 33", "Hello!", "Hello!"),
                Arguments.of("ends with multiple char 32", "Hello   ", "Hello"),

                // Mixed content with whitespace
                Arguments.of("numbers with trailing spaces", "12345   ", "12345"),
                Arguments.of("special chars with trailing spaces", "!@#$%   ", "!@#$%"),
                Arguments.of("mixed alphanumeric with trailing", "abc123XYZ   ", "abc123XYZ"));
    }
}