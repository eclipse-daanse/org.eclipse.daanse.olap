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
package org.eclipse.daanse.olap.function.def.vba.left;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.stream.Stream;

import org.eclipse.daanse.olap.api.Evaluator;
import org.eclipse.daanse.olap.api.calc.IntegerCalc;
import org.eclipse.daanse.olap.api.calc.StringCalc;
import org.eclipse.daanse.olap.api.type.StringType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class LeftCalcTest {

    private LeftCalc leftCalc;
    private StringCalc stringCalc;
    private IntegerCalc integerCalc;
    private Evaluator evaluator;

    @BeforeEach
    void setUp() {
        stringCalc = mock(StringCalc.class);
        integerCalc = mock(IntegerCalc.class);
        evaluator = mock(Evaluator.class);
        leftCalc = new LeftCalc(StringType.INSTANCE, stringCalc, integerCalc);
    }

    @ParameterizedTest(name = "{0}: left(\"{1}\", {2}) = \"{3}\"")
    @MethodSource("arguments")
    @DisplayName("Should extract left substring correctly")
    void shouldExtractLeftSubstring(String testName, String inputString, Integer length, String expected) {
        when(stringCalc.evaluate(evaluator)).thenReturn(inputString);
        when(integerCalc.evaluate(evaluator)).thenReturn(length);

        String result = leftCalc.evaluate(evaluator);

        assertThat(result).isEqualTo(expected);
    }

    static Stream<Arguments> arguments() {
        return Stream.of(Arguments.of("basic extraction", "Hello World", 5, "Hello"),
                Arguments.of("single character", "Hello World", 1, "H"),
                Arguments.of("zero length", "Hello World", 0, ""), Arguments.of("full string", "Hello", 5, "Hello"),
                Arguments.of("length greater than string", "Hello", 10, "Hello"),
                Arguments.of("empty string", "", 5, ""), Arguments.of("empty string zero length", "", 0, ""),
                Arguments.of("special characters", "Hello@#$%", 8, "Hello@#$"),
                Arguments.of("unicode characters", "Héllo Wørld", 6, "Héllo "),
                Arguments.of("numbers as string", "123456789", 3, "123"),
                Arguments.of("spaces", "   Hello   ", 6, "   Hel"), Arguments.of("single space", " ", 1, " "),
                Arguments.of("tabs and newlines", "\t\nHello", 3, "\t\nH"));
    }

    @Test
    @DisplayName("Should preserve type information")
    void shouldPreserveTypeInformation() {
        assertThat(leftCalc.getType()).isEqualTo(StringType.INSTANCE);
    }
}