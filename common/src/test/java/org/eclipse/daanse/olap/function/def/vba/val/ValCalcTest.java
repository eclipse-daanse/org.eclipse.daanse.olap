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
package org.eclipse.daanse.olap.function.def.vba.val;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.stream.Stream;

import org.eclipse.daanse.olap.api.Evaluator;
import org.eclipse.daanse.olap.api.calc.StringCalc;
import org.eclipse.daanse.olap.api.type.NumericType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class ValCalcTest {

    private ValCalc valCalc;
    private StringCalc stringCalc;
    private Evaluator evaluator;

    @BeforeEach
    void setUp() {
        stringCalc = mock(StringCalc.class);
        evaluator = mock(Evaluator.class);
        valCalc = new ValCalc(NumericType.INSTANCE, stringCalc);
    }

    @ParameterizedTest(name = "{0}: val('{1}') = {2}")
    @MethodSource("valArguments")
    @DisplayName("Should extract numeric values from strings correctly")
    void shouldExtractNumericValues(String testName, String input, Double expected) {
        when(stringCalc.evaluate(evaluator)).thenReturn(input);

        Double result = valCalc.evaluate(evaluator);

        if (Double.isNaN(expected)) {
            assertThat(result).isNaN();
        } else {
            assertThat(result).isCloseTo(expected, within(0.0001));
        }
    }

    static Stream<Arguments> valArguments() {
        return Stream.of(
                // Basic decimal numbers
                Arguments.of("simple integer", "123", 123.0), Arguments.of("simple decimal", "123.45", 123.45),
                Arguments.of("negative integer", "-123", -123.0), Arguments.of("negative decimal", "-123.45", -123.45),

                // Numbers with whitespace
                Arguments.of("leading spaces", "   123", 123.0), Arguments.of("trailing spaces", "123   ", 123.0),
                Arguments.of("tabs and spaces", " \t 123 \t ", 123.0),
                Arguments.of("internal whitespace", " 1 6 1 5 1 9 8", 1615198.0),

                // Numbers with text
                Arguments.of("number with text", "123abc", 123.0),
                Arguments.of("address example", " 1615 198th Street N.E.", 1615198.0),
                Arguments.of("price with currency", "99.99$", 99.99),

                // Hexadecimal numbers
                Arguments.of("hex prefix", "&H10", 16.0), Arguments.of("hex FFFF", "&HFFFF", 65535.0),
                Arguments.of("hex with spaces", " &H FF ", 255.0),
//                Arguments.of("hex lowercase", "&habcd", 43981.0),

                // Octal numbers
                Arguments.of("octal prefix", "&O10", 8.0), Arguments.of("octal 777", "&O777", 511.0),
                Arguments.of("octal with spaces", " &O 123 ", 83.0),

                // Edge cases
                Arguments.of("zero", "0", 0.0), Arguments.of("decimal only", ".5", 0.5),
                Arguments.of("negative decimal only", "-.75", -0.75),
                Arguments.of("starting with period", ".123", 0.123)
//                ,
//                Arguments.of("empty string", "", 0.0)
        );
    }

    @Test
    @DisplayName("Should preserve type information")
    void shouldPreserveTypeInformation() {
        assertThat(valCalc.getType()).isEqualTo(NumericType.INSTANCE);
    }

    @Test
    @DisplayName("Should handle hexadecimal conversion correctly")
    void shouldHandleHexadecimalConversion() {
        when(stringCalc.evaluate(evaluator)).thenReturn("&HFF");
        Double result = valCalc.evaluate(evaluator);
        assertThat(result).isEqualTo(255.0);

        when(stringCalc.evaluate(evaluator)).thenReturn("&H1A3F");
        result = valCalc.evaluate(evaluator);
        assertThat(result).isEqualTo(6719.0);
    }

    @Test
    @DisplayName("Should handle octal conversion correctly")
    void shouldHandleOctalConversion() {
        when(stringCalc.evaluate(evaluator)).thenReturn("&O17");
        Double result = valCalc.evaluate(evaluator);
        assertThat(result).isEqualTo(15.0);

        when(stringCalc.evaluate(evaluator)).thenReturn("&O777");
        result = valCalc.evaluate(evaluator);
        assertThat(result).isEqualTo(511.0);
    }

    @Test
    @DisplayName("Should stop at first non-numeric character")
    void shouldStopAtFirstNonNumericCharacter() {
        when(stringCalc.evaluate(evaluator)).thenReturn("123abc456");
        Double result = valCalc.evaluate(evaluator);
        assertThat(result).isEqualTo(123.0);

        when(stringCalc.evaluate(evaluator)).thenReturn("12.34xyz");
        result = valCalc.evaluate(evaluator);
        assertThat(result).isEqualTo(12.34);
    }

    @Test
    @DisplayName("Should remove all whitespace before processing")
    void shouldRemoveAllWhitespace() {
        when(stringCalc.evaluate(evaluator)).thenReturn("  1 2 3 . 4 5  ");
        Double result = valCalc.evaluate(evaluator);
        assertThat(result).isEqualTo(123.45);

        when(stringCalc.evaluate(evaluator)).thenReturn("\t1\n2\r3\t");
        result = valCalc.evaluate(evaluator);
        assertThat(result).isEqualTo(123.0);
    }

    @Test
    @DisplayName("Should handle invalid hex and octal gracefully")
    void shouldHandleInvalidHexAndOctalGracefully() {
        // Invalid hex characters should stop conversion
        when(stringCalc.evaluate(evaluator)).thenReturn("&H123G456");
        Double result = valCalc.evaluate(evaluator);
        assertThat(result).isEqualTo(291.0); // Only "123" part

        // Invalid octal characters should stop conversion
        when(stringCalc.evaluate(evaluator)).thenReturn("&O1238");
        result = valCalc.evaluate(evaluator);
        assertThat(result).isEqualTo(83.0); // Only "123" part
    }

    @Test
    @DisplayName("Should handle text-only input")
    @Disabled
    void shouldHandleTextOnlyInput() {
        when(stringCalc.evaluate(evaluator)).thenReturn("hello");
        Double result = valCalc.evaluate(evaluator);
        assertThat(result).isEqualTo(0.0); // No leading number

        when(stringCalc.evaluate(evaluator)).thenReturn("xyz123");
        result = valCalc.evaluate(evaluator);
        assertThat(result).isEqualTo(0.0); // No leading number
    }
}