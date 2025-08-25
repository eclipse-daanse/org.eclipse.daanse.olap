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
package org.eclipse.daanse.olap.function.def.vba.asc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.stream.Stream;

import org.eclipse.daanse.olap.api.Evaluator;
import org.eclipse.daanse.olap.api.calc.StringCalc;
import org.eclipse.daanse.olap.api.type.NumericType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class AscCalcTest {

    private AscCalc ascCalc;
    private StringCalc stringCalc;
    private Evaluator evaluator;

    @BeforeEach
    void setUp() {
        stringCalc = mock(StringCalc.class);
        evaluator = mock(Evaluator.class);
        ascCalc = new AscCalc(NumericType.INSTANCE, stringCalc);
    }

    @ParameterizedTest(name = "{0}: asc(\"{1}\") = {2}")
    @MethodSource("arguments")
    @DisplayName("Should calculate ASCII value correctly")
    void shouldCalculateAsciiValue(String testName, String input, Integer expected) {
        when(stringCalc.evaluate(evaluator)).thenReturn(input);

        Integer result = ascCalc.evaluate(evaluator);

        assertThat(result).isEqualTo(expected);
    }

    static Stream<Arguments> arguments() {
        return Stream.of(Arguments.of("letter A", "A", 65), Arguments.of("letter a", "a", 97),
                Arguments.of("digit 0", "0", 48), Arguments.of("digit 9", "9", 57),
                Arguments.of("space character", " ", 32), Arguments.of("exclamation mark", "!", 33),
                Arguments.of("at symbol", "@", 64), Arguments.of("first char of string", "Hello", 72),
                Arguments.of("special character #", "#", 35), Arguments.of("dollar sign", "$", 36),
                Arguments.of("percent sign", "%", 37), Arguments.of("ampersand", "&", 38),
                Arguments.of("asterisk", "*", 42), Arguments.of("plus sign", "+", 43), Arguments.of("comma", ",", 44),
                Arguments.of("hyphen", "-", 45), Arguments.of("period", ".", 46),
                Arguments.of("forward slash", "/", 47), Arguments.of("colon", ":", 58),
                Arguments.of("semicolon", ";", 59), Arguments.of("less than", "<", 60), Arguments.of("equals", "=", 61),
                Arguments.of("greater than", ">", 62), Arguments.of("question mark", "?", 63));
    }
}