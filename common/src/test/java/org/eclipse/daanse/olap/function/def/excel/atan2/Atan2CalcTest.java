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
package org.eclipse.daanse.olap.function.def.excel.atan2;

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

class Atan2CalcTest {

    private Atan2Calc atan2Calc;
    private DoubleCalc doubleCalc1;
    private DoubleCalc doubleCalc2;
    private Evaluator evaluator;

    @BeforeEach
    void setUp() {
        doubleCalc1 = mock(DoubleCalc.class);
        doubleCalc2 = mock(DoubleCalc.class);
        evaluator = mock(Evaluator.class);
        atan2Calc = new Atan2Calc(NumericType.INSTANCE, doubleCalc1, doubleCalc2);
    }

    @ParameterizedTest(name = "{0}: Atan2({1}, {2}) = {3}")
    @MethodSource("validArguments")
    @DisplayName("Should calculate two-argument arctangent correctly")
    void shouldCalculateAtan2(String testName, Double x, Double y, Double expected) {
        when(doubleCalc1.evaluate(evaluator)).thenReturn(x);
        when(doubleCalc2.evaluate(evaluator)).thenReturn(y);

        Double result = atan2Calc.evaluate(evaluator);

        assertThat(result).isCloseTo(expected, org.assertj.core.data.Offset.offset(1e-10));
    }

    static Stream<Arguments> validArguments() {
        return Stream.of(Arguments.of("positive x, positive y (first quadrant)", 1.0, 1.0, Math.PI / 4),
                Arguments.of("negative x, positive y (second quadrant)", -1.0, 1.0, 3 * Math.PI / 4),
                Arguments.of("negative x, negative y (third quadrant)", -1.0, -1.0, -3 * Math.PI / 4),
                Arguments.of("positive x, negative y (fourth quadrant)", 1.0, -1.0, -Math.PI / 4),
                Arguments.of("positive x axis", 1.0, 0.0, 0.0), Arguments.of("negative x axis", -1.0, 0.0, Math.PI),
                Arguments.of("positive y axis", 0.0, 1.0, Math.PI / 2),
                Arguments.of("negative y axis", 0.0, -1.0, -Math.PI / 2), Arguments.of("origin case", 0.0, 0.0, 0.0),
                Arguments.of("large values", 1000.0, 1000.0, Math.PI / 4),
                Arguments.of("small values", 0.001, 0.001, Math.PI / 4));
    }

    @Test
    @DisplayName("Should preserve type information")
    void shouldPreserveTypeInformation() {
        assertThat(atan2Calc.getType()).isEqualTo(NumericType.INSTANCE);
    }
}