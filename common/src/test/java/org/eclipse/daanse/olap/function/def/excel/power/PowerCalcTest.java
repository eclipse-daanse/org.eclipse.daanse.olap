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
package org.eclipse.daanse.olap.function.def.excel.power;

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

class PowerCalcTest {

    private PowerCalc powerCalc;
    private DoubleCalc baseCalc;
    private DoubleCalc exponentCalc;
    private Evaluator evaluator;

    @BeforeEach
    void setUp() {
        baseCalc = mock(DoubleCalc.class);
        exponentCalc = mock(DoubleCalc.class);
        evaluator = mock(Evaluator.class);
        powerCalc = new PowerCalc(NumericType.INSTANCE, baseCalc, exponentCalc);
    }

    @ParameterizedTest(name = "{0}: Power({1}, {2}) = {3}")
    @MethodSource("validArguments")
    @DisplayName("Should calculate power correctly")
    void shouldCalculatePower(String testName, Double base, Double exponent, Double expected) {
        when(baseCalc.evaluate(evaluator)).thenReturn(base);
        when(exponentCalc.evaluate(evaluator)).thenReturn(exponent);

        Double result = powerCalc.evaluate(evaluator);

        assertThat(result).isCloseTo(expected, org.assertj.core.data.Offset.offset(1e-10));
    }

    static Stream<Arguments> validArguments() {
        return Stream.of(Arguments.of("2 to power of 3", 2.0, 3.0, 8.0),
                Arguments.of("any number to power of 0", 5.0, 0.0, 1.0),
                Arguments.of("any number to power of 1", 7.0, 1.0, 7.0),
                Arguments.of("1 to any power", 1.0, 100.0, 1.0), Arguments.of("square root (power 0.5)", 9.0, 0.5, 3.0),
                Arguments.of("cube root (power 1/3)", 8.0, 1.0 / 3.0, 2.0),
                Arguments.of("negative base, even exponent", -2.0, 2.0, 4.0),
                Arguments.of("negative base, odd exponent", -2.0, 3.0, -8.0),
                Arguments.of("fractional base", 0.5, 3.0, 0.125), Arguments.of("negative exponent", 2.0, -3.0, 0.125),
                Arguments.of("zero base, positive exponent", 0.0, 5.0, 0.0),
                Arguments.of("base 10, exponent 2", 10.0, 2.0, 100.0),
                Arguments.of("e to natural log", Math.E, Math.log(5), 5.0));
    }

    @Test
    @DisplayName("Should handle special infinity cases")
    void shouldHandleSpecialInfinityCases() {
        // Positive infinity base
        when(baseCalc.evaluate(evaluator)).thenReturn(Double.POSITIVE_INFINITY);
        when(exponentCalc.evaluate(evaluator)).thenReturn(2.0);

        Double result = powerCalc.evaluate(evaluator);
        assertThat(result).isEqualTo(Double.POSITIVE_INFINITY);

        // Large number to large power approaches infinity
        when(baseCalc.evaluate(evaluator)).thenReturn(10.0);
        when(exponentCalc.evaluate(evaluator)).thenReturn(100.0);

        result = powerCalc.evaluate(evaluator);
        assertThat(result).isEqualTo(Math.pow(10.0, 100.0));
    }

    @Test
    @DisplayName("Should handle NaN cases")
    void shouldHandleNaNCases() {
        // NaN base
        when(baseCalc.evaluate(evaluator)).thenReturn(Double.NaN);
        when(exponentCalc.evaluate(evaluator)).thenReturn(2.0);

        Double result = powerCalc.evaluate(evaluator);
        assertThat(result).isNaN();

        // Negative base with fractional exponent
        when(baseCalc.evaluate(evaluator)).thenReturn(-4.0);
        when(exponentCalc.evaluate(evaluator)).thenReturn(0.5);

        result = powerCalc.evaluate(evaluator);
        assertThat(result).isNaN();
    }

    @Test
    @DisplayName("Should handle zero base with zero exponent")
    void shouldHandleZeroBaseZeroExponent() {
        when(baseCalc.evaluate(evaluator)).thenReturn(0.0);
        when(exponentCalc.evaluate(evaluator)).thenReturn(0.0);

        Double result = powerCalc.evaluate(evaluator);

        assertThat(result).isEqualTo(Math.pow(0.0, 0.0)); // Should return 1.0
    }

    @Test
    @DisplayName("Should handle zero base with negative exponent")
    void shouldHandleZeroBaseNegativeExponent() {
        when(baseCalc.evaluate(evaluator)).thenReturn(0.0);
        when(exponentCalc.evaluate(evaluator)).thenReturn(-1.0);

        Double result = powerCalc.evaluate(evaluator);

        assertThat(result).isEqualTo(Double.POSITIVE_INFINITY);
    }

    @Test
    @DisplayName("Should preserve type information")
    void shouldPreserveTypeInformation() {
        assertThat(powerCalc.getType()).isEqualTo(NumericType.INSTANCE);
    }
}