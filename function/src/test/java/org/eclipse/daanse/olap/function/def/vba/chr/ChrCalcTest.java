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
package org.eclipse.daanse.olap.function.def.vba.chr;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.stream.Stream;

import org.eclipse.daanse.olap.api.evaluator.Evaluator;
import org.eclipse.daanse.olap.api.calc.IntegerCalc;
import org.eclipse.daanse.olap.api.type.StringType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class ChrCalcTest {

    private ChrCalc chrCalc;
    private IntegerCalc integerCalc;
    private Evaluator evaluator;

    @BeforeEach
    void setUp() {
        integerCalc = mock(IntegerCalc.class);
        evaluator = mock(Evaluator.class);
        chrCalc = new ChrCalc(StringType.INSTANCE, integerCalc);
    }

    @ParameterizedTest(name = "{0}: Chr({1}) = \"{2}\"")
    @MethodSource("arguments")
    @DisplayName("Should convert character codes to characters correctly")
    void shouldConvertCharacterCodesToCharacters(String testName, Integer charCode, String expected) {
        when(integerCalc.evaluate(evaluator)).thenReturn(charCode);

        String result = chrCalc.evaluate(evaluator);

        assertThat(result).isEqualTo(expected);
    }

    static Stream<Arguments> arguments() {
        return Stream.of(Arguments.of("letter A", 65, "A"), Arguments.of("letter a", 97, "a"),
                Arguments.of("digit 0", 48, "0"), Arguments.of("digit 9", 57, "9"),
                Arguments.of("space character", 32, " "), Arguments.of("exclamation mark", 33, "!"),
                Arguments.of("at symbol", 64, "@"), Arguments.of("hash symbol", 35, "#"),
                Arguments.of("dollar sign", 36, "$"), Arguments.of("percent sign", 37, "%"),
                Arguments.of("ampersand", 38, "&"), Arguments.of("asterisk", 42, "*"),
                Arguments.of("plus sign", 43, "+"), Arguments.of("comma", 44, ","), Arguments.of("hyphen", 45, "-"),
                Arguments.of("period", 46, "."), Arguments.of("forward slash", 47, "/"),
                Arguments.of("newline", 10, "\n"), Arguments.of("tab", 9, "\t"),
                Arguments.of("carriage return", 13, "\r"));
    }
}