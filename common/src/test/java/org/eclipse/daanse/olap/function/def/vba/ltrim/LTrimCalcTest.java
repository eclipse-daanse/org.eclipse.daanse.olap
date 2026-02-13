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
package org.eclipse.daanse.olap.function.def.vba.ltrim;

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

class LTrimCalcTest {

    private LTrimCalc lTrimCalc;
    private StringCalc stringCalc;
    private Evaluator evaluator;

    @BeforeEach
    void setUp() {
        stringCalc = mock(StringCalc.class);
        evaluator = mock(Evaluator.class);
        lTrimCalc = new LTrimCalc(StringType.INSTANCE, stringCalc);
    }

    @ParameterizedTest(name = "{0}: ltrim(\"{1}\") = \"{2}\"")
    @MethodSource("arguments")
    @DisplayName("Should trim leading whitespace correctly")
    void shouldTrimLeadingWhitespace(String testName, String input, String expected) {
        when(stringCalc.evaluate(evaluator)).thenReturn(input);

        String result = lTrimCalc.evaluate(evaluator);

        assertThat(result).isEqualTo(expected);
    }

    static Stream<Arguments> arguments() {
        return Stream.of(
                // Basic leading space trimming
                Arguments.of("single leading space", " Hello", "Hello"),
                Arguments.of("multiple leading spaces", "   Hello", "Hello"),
                Arguments.of("no leading spaces", "Hello", "Hello"),
                Arguments.of("only trailing spaces", "Hello   ", "Hello   "),
                Arguments.of("leading and trailing spaces", "   Hello   ", "Hello   "),

                // Empty and whitespace-only strings
                Arguments.of("empty string", "", ""), Arguments.of("single space", " ", ""),
                Arguments.of("multiple spaces", "   ", ""), Arguments.of("only whitespace", "     ", ""),

                // Different whitespace characters (chars <= ' ' which is ASCII 32)
                Arguments.of("leading tab", "\tHello", "Hello"), Arguments.of("leading newline", "\nHello", "Hello"),
                Arguments.of("leading carriage return", "\rHello", "Hello"),
                Arguments.of("leading form feed", "\fHello", "Hello"),
                Arguments.of("mixed leading whitespace", " \t\n\rHello", "Hello"),

                // Control characters (ASCII < 32)
                Arguments.of("leading null char", "\u0000Hello", "Hello"),
                Arguments.of("leading backspace", "\u0008Hello", "Hello"),
                Arguments.of("leading vertical tab", "\u000BHello", "Hello"),
                Arguments.of("leading escape", "\u001BHello", "Hello"),
                Arguments.of("mixed control chars", "\u0000\u0008\u001BHello", "Hello"),

                // Preserving internal whitespace
                Arguments.of("spaces in middle preserved", "  Hello World  ", "Hello World  "),
                Arguments.of("tabs in middle preserved", "  Hello\tWorld  ", "Hello\tWorld  "),
                Arguments.of("newlines in middle preserved", "  Hello\nWorld  ", "Hello\nWorld  "),

                // Edge cases with special characters
                Arguments.of("starts with exclamation", " !Hello", "!Hello"),
                Arguments.of("starts with number", "  123", "123"),
                Arguments.of("starts with symbol", "   @Hello", "@Hello"),

                // Unicode and non-ASCII
                Arguments.of("unicode content", "  Héllo Wørld", "Héllo Wørld"),
                Arguments.of("emoji content", "  😀Hello", "😀Hello"),

                // Long strings
                Arguments.of("long string with many leading spaces",
                        "          This is a very long string with many leading spaces",
                        "This is a very long string with many leading spaces"),

                // All characters are whitespace
                Arguments.of("all spaces", "     ", ""), Arguments.of("all tabs", "\t\t\t", ""),
                Arguments.of("mixed all whitespace", " \t\n\r\f", ""),

                // Single characters
                Arguments.of("single char no leading", "A", "A"), Arguments.of("single char with leading", " A", "A"),
                Arguments.of("just space char", " ", ""),

                // Special boundary cases
                Arguments.of("space then space char (ASCII 32)", "  Hello", "Hello"),
                Arguments.of("char 33 (!) - should not trim", "!!Hello", "!!Hello"),
                Arguments.of("mixed low ASCII", "\u0001\u0020Hello", "Hello") // char 1 and space (32)
        );
    }
}