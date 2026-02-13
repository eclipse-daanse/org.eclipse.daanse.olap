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
package org.eclipse.daanse.olap.function.def.vba.cbyte;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.stream.Stream;

import org.eclipse.daanse.olap.api.evaluator.Evaluator;
import org.eclipse.daanse.olap.api.calc.Calc;
import org.eclipse.daanse.olap.api.type.NumericType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class CByteCalcTest {

    private CByteCalc cByteCalc;
    private Calc<Object> calc;
    private Evaluator evaluator;

    @BeforeEach
    void setUp() {
        calc = mock(Calc.class);
        evaluator = mock(Evaluator.class);
        cByteCalc = new CByteCalc(NumericType.INSTANCE, calc);
    }

    @ParameterizedTest(name = "{0}: CByte({1}) = {2}")
    @MethodSource("arguments")
    @DisplayName("Should convert values to byte correctly")
    void shouldConvertToByte(String testName, Object input, Byte expected) {
        when(calc.evaluate(evaluator)).thenReturn(input);

        Byte result = cByteCalc.evaluate(evaluator);

        assertThat(result).isEqualTo(expected);
    }

    static Stream<Arguments> arguments() {
        return Stream.of(Arguments.of("byte value", (byte) 42, (byte) 42), Arguments.of("integer zero", 0, (byte) 0),
                Arguments.of("integer one", 1, (byte) 1), Arguments.of("integer max byte", 127, (byte) 127),
                Arguments.of("integer min byte", -128, (byte) -128), Arguments.of("double value", 25.7, (byte) 26),
                Arguments.of("negative double", -10.3, (byte) -10), Arguments.of("string number", "15", (byte) 15),
                Arguments.of("string negative", "-25", (byte) -25)
//                ,
//                Arguments.of("boolean true", true, (byte) -1),
//                Arguments.of("boolean false", false, (byte) 0)
        );
    }
}