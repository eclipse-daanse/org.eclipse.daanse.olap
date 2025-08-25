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

import org.eclipse.daanse.olap.api.Evaluator;
import org.eclipse.daanse.olap.api.calc.DoubleCalc;
import org.eclipse.daanse.olap.api.type.NumericType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class AtanhCalcTest {

    private AtanhCalc atanhCalc;
    private DoubleCalc doubleCalc;
    private Evaluator evaluator;

    @BeforeEach
    void setUp() {
        doubleCalc = mock(DoubleCalc.class);
        evaluator = mock(Evaluator.class);
        atanhCalc = new AtanhCalc(NumericType.INSTANCE, doubleCalc);
    }

    @ParameterizedTest(name = "{0}: Atanh({1}) = {2}")
    @MethodSource("validArguments")
    @DisplayName("Should calculate inverse hyperbolic tangent correctly")
    void shouldCalculateInverseHyperbolicTangent(String testName, Double input, Double expected) {
        when(doubleCalc.evaluate(evaluator)).thenReturn(input);

        Double result = atanhCalc.evaluate(evaluator);

        assertThat(result).isCloseTo(expected, org.assertj.core.data.Offset.offset(1e-10));
    }

    static Stream<Arguments> validArguments() {
        return Stream.of(Arguments.of("atanh of 0", 0.0, 0.0),
                Arguments.of("atanh of 0.5", 0.5, 0.5 * Math.log(1.5 / 0.5)),
                Arguments.of("atanh of -0.5", -0.5, 0.5 * Math.log(0.5 / 1.5)),
                Arguments.of("atanh of 0.9", 0.9, 0.5 * Math.log(1.9 / 0.1)),
                Arguments.of("atanh of -0.9", -0.9, 0.5 * Math.log(0.1 / 1.9)),
                Arguments.of("atanh of tanh(1)", Math.tanh(1.0), 1.0),
                Arguments.of("atanh of tanh(-1)", Math.tanh(-1.0), -1.0),
                Arguments.of("small positive value", 0.01, 0.5 * Math.log(1.01 / 0.99)),
                Arguments.of("small negative value", -0.01, 0.5 * Math.log(0.99 / 1.01)));
    }

    @ParameterizedTest(name = "{0}: Atanh({1}) = infinity or NaN")
    @MethodSource("boundaryArguments")
    @DisplayName("Should handle boundary values correctly")
    void shouldHandleBoundaryValues(String testName, Double input, Double expected) {
        when(doubleCalc.evaluate(evaluator)).thenReturn(input);

        Double result = atanhCalc.evaluate(evaluator);

        if (Double.isInfinite(expected)) {
            assertThat(result).isEqualTo(expected);
        } else if (Double.isNaN(expected)) {
            assertThat(result).isNaN();
        } else {
            assertThat(result).isCloseTo(expected, org.assertj.core.data.Offset.offset(1e-10));
        }
    }

    static Stream<Arguments> boundaryArguments() {
        return Stream.of(Arguments.of("atanh of 1 (positive infinity)", 1.0, Double.POSITIVE_INFINITY),
                Arguments.of("atanh of -1 (negative infinity)", -1.0, Double.NEGATIVE_INFINITY),
                Arguments.of("atanh of value > 1 (NaN)", 1.1, Double.NaN),
                Arguments.of("atanh of value < -1 (NaN)", -1.1, Double.NaN),
                Arguments.of("atanh of NaN", Double.NaN, Double.NaN));
    }

    @Test
    @DisplayName("Should preserve type information")
    void shouldPreserveTypeInformation() {
        assertThat(atanhCalc.getType()).isEqualTo(NumericType.INSTANCE);
    }
}