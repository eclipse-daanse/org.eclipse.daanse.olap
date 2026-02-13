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
package org.eclipse.daanse.olap.function.def.vba.atn;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.stream.Stream;

import org.eclipse.daanse.olap.api.evaluator.Evaluator;
import org.eclipse.daanse.olap.api.calc.DoubleCalc;
import org.eclipse.daanse.olap.api.type.NumericType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class AtnCalcTest {

    private AtnCalc atnCalc;
    private DoubleCalc doubleCalc;
    private Evaluator evaluator;

    @BeforeEach
    void setUp() {
        doubleCalc = mock(DoubleCalc.class);
        evaluator = mock(Evaluator.class);
        atnCalc = new AtnCalc(NumericType.INSTANCE, doubleCalc);
    }

    @ParameterizedTest(name = "{0}: atan({1}) = {2}")
    @MethodSource("arguments")
    @DisplayName("Should calculate arctangent correctly")
    void shouldCalculateArctangent(String testName, Double input, Double expected) {
        when(doubleCalc.evaluate(evaluator)).thenReturn(input);

        Double result = atnCalc.evaluate(evaluator);

        assertThat(result).isCloseTo(expected, org.assertj.core.data.Offset.offset(1e-10));
    }

    static Stream<Arguments> arguments() {
        return Stream.of(Arguments.of("zero", 0.0, 0.0), Arguments.of("one", 1.0, Math.PI / 4),
                Arguments.of("negative one", -1.0, -Math.PI / 4), Arguments.of("sqrt(3)", Math.sqrt(3), Math.PI / 3),
                Arguments.of("negative sqrt(3)", -Math.sqrt(3), -Math.PI / 3),
                Arguments.of("1/sqrt(3)", 1.0 / Math.sqrt(3), Math.PI / 6),
                Arguments.of("negative 1/sqrt(3)", -1.0 / Math.sqrt(3), -Math.PI / 6),
                Arguments.of("large positive", 1000.0, Math.atan(1000.0)),
                Arguments.of("large negative", -1000.0, Math.atan(-1000.0)),
                Arguments.of("positive infinity", Double.POSITIVE_INFINITY, Math.PI / 2),
                Arguments.of("negative infinity", Double.NEGATIVE_INFINITY, -Math.PI / 2),
                Arguments.of("not a number", Double.NaN, Double.NaN));
    }

    @Test
    @DisplayName("Should preserve type information")
    void shouldPreserveTypeInformation() {
        assertThat(atnCalc.getType()).isEqualTo(NumericType.INSTANCE);
    }
}