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
package org.eclipse.daanse.olap.function.def.excel.log10;

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

class Log10CalcTest {

    private Log10Calc log10Calc;
    private DoubleCalc doubleCalc;
    private Evaluator evaluator;

    @BeforeEach
    void setUp() {
        doubleCalc = mock(DoubleCalc.class);
        evaluator = mock(Evaluator.class);
        log10Calc = new Log10Calc(NumericType.INSTANCE, doubleCalc);
    }

    @ParameterizedTest(name = "{0}: Log10({1}) = {2}")
    @MethodSource("validArguments")
    @DisplayName("Should calculate base-10 logarithm correctly")
    void shouldCalculateLog10(String testName, Double input, Double expected) {
        when(doubleCalc.evaluate(evaluator)).thenReturn(input);

        Double result = log10Calc.evaluate(evaluator);

        assertThat(result).isCloseTo(expected, org.assertj.core.data.Offset.offset(1e-10));
    }

    static Stream<Arguments> validArguments() {
        return Stream.of(Arguments.of("log10 of 1", 1.0, 0.0), Arguments.of("log10 of 10", 10.0, 1.0),
                Arguments.of("log10 of 100", 100.0, 2.0), Arguments.of("log10 of 1000", 1000.0, 3.0),
                Arguments.of("log10 of 0.1", 0.1, -1.0), Arguments.of("log10 of 0.01", 0.01, -2.0),
                Arguments.of("log10 of 0.001", 0.001, -3.0), Arguments.of("log10 of e", Math.E, Math.log10(Math.E)),
                Arguments.of("log10 of π", Math.PI, Math.log10(Math.PI)),
                Arguments.of("log10 of 2", 2.0, Math.log10(2.0)), Arguments.of("log10 of 5", 5.0, Math.log10(5.0)));
    }

    @ParameterizedTest(name = "{0}: Log10({1}) = NaN")
    @MethodSource("invalidArguments")
    @DisplayName("Should return NaN for non-positive values")
    void shouldReturnNaNForInvalidValues(String testName, Double input) {
        when(doubleCalc.evaluate(evaluator)).thenReturn(input);

        Double result = log10Calc.evaluate(evaluator);

        assertThat(result).isNaN();
    }

    static Stream<Arguments> invalidArguments() {
        return Stream.of(Arguments.of("log10 of negative value", -1.0), Arguments.of("log10 of large negative", -10.0),
                Arguments.of("log10 of NaN", Double.NaN),
                Arguments.of("log10 of negative infinity", Double.NEGATIVE_INFINITY));
    }

    @Test
    @DisplayName("Should preserve type information")
    void shouldPreserveTypeInformation() {
        assertThat(log10Calc.getType()).isEqualTo(NumericType.INSTANCE);
    }
}