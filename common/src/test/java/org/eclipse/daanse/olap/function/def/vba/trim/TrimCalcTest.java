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
package org.eclipse.daanse.olap.function.def.vba.trim;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.stream.Stream;

import org.eclipse.daanse.olap.api.Evaluator;
import org.eclipse.daanse.olap.api.calc.StringCalc;
import org.eclipse.daanse.olap.api.type.StringType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class TrimCalcTest {

    private TrimCalc trimCalc;
    private StringCalc stringCalc;
    private Evaluator evaluator;

    @BeforeEach
    void setUp() {
        stringCalc = mock(StringCalc.class);
        evaluator = mock(Evaluator.class);
        trimCalc = new TrimCalc(StringType.INSTANCE, stringCalc);
    }

    @ParameterizedTest(name = "{0}: trim('{1}') = '{2}'")
    @MethodSource("trimArguments")
    @DisplayName("Should trim whitespace from strings correctly")
    void shouldTrimWhitespace(String testName, String input, String expected) {
        when(stringCalc.evaluate(evaluator)).thenReturn(input);

        String result = trimCalc.evaluate(evaluator);

        assertThat(result).isEqualTo(expected);
    }

    static Stream<Arguments> trimArguments() {
        return Stream.of(Arguments.of("leading spaces", "   hello", "hello"),
                Arguments.of("trailing spaces", "hello   ", "hello"),
                Arguments.of("both sides spaces", "   hello   ", "hello"), Arguments.of("no spaces", "hello", "hello"),
                Arguments.of("empty string", "", ""), Arguments.of("only spaces", "   ", ""),
                Arguments.of("single space", " ", ""),
                Arguments.of("middle spaces preserved", "  hello world  ", "hello world"),
                Arguments.of("tabs and spaces", "\t  hello  \t", "hello"),
                Arguments.of("newlines", "\n  hello  \n", "hello"),
                Arguments.of("mixed whitespace", " \t\n hello \t\n ", "hello"),
                Arguments.of("multiple words", "  first second  ", "first second"), Arguments.of("null", null, null),
                Arguments.of("single character", " a ", "a"));
    }

    @Test
    @DisplayName("Should preserve type information")
    void shouldPreserveTypeInformation() {
        assertThat(trimCalc.getType()).isEqualTo(StringType.INSTANCE);
    }
}