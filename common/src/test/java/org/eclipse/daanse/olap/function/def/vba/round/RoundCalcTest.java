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
package org.eclipse.daanse.olap.function.def.vba.round;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.stream.Stream;

import org.eclipse.daanse.olap.api.Evaluator;
import org.eclipse.daanse.olap.api.calc.DoubleCalc;
import org.eclipse.daanse.olap.api.calc.IntegerCalc;
import org.eclipse.daanse.olap.api.type.NumericType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class RoundCalcTest {

    private RoundCalc roundCalc;
    private DoubleCalc doubleCalc;
    private IntegerCalc integerCalc;
    private Evaluator evaluator;

    @BeforeEach
    void setUp() {
        doubleCalc = mock(DoubleCalc.class);
        integerCalc = mock(IntegerCalc.class);
        evaluator = mock(Evaluator.class);
        roundCalc = new RoundCalc(NumericType.INSTANCE, doubleCalc, integerCalc);
    }

    @ParameterizedTest(name = "{0}: Round({1}, {2}) = {3}")
    @MethodSource("arguments")
    @DisplayName("Should calculate round correctly")
    void shouldCalculateRound(String testName, Double value, Integer digits, String assertionType, Double expected) {
        when(doubleCalc.evaluate(evaluator)).thenReturn(value);
        when(integerCalc.evaluate(evaluator)).thenReturn(digits);

        Double result = roundCalc.evaluate(evaluator);

        if ("exact".equals(assertionType)) {
            assertThat(result).isEqualTo(expected);
        } else if ("close".equals(assertionType)) {
            assertThat(result).isCloseTo(expected, org.assertj.core.data.Offset.offset(1e-10));
        } else if ("close_loose".equals(assertionType)) {
            assertThat(result).isCloseTo(expected, org.assertj.core.data.Offset.offset(1e-3));
        }
    }

    static Stream<Arguments> arguments() {
        return Stream.of(Arguments.of("round to nearest integer when digits is zero", 42.7, 0, "exact", 43.0),
                Arguments.of("round halfway value when digits is zero", 42.5, 0, "exact", 43.0),
                Arguments.of("round to one decimal place", 42.76, 1, "close", 42.8),
                Arguments.of("round to two decimal places", 42.765, 2, "close", 42.77),
                Arguments.of("handle negative numbers", -42.76, 1, "close", -42.8),
                Arguments.of("round to negative decimal places", 1234.56, -1, "close", 1230.0),
                Arguments.of("handle zero", 0.0, 2, "exact", 0.0),
                Arguments.of("handle large numbers", 123456789.123456789, 3, "close_loose", 123456789.123));
    }

    @Test
    @DisplayName("Should preserve type information")
    void shouldPreserveTypeInformation() {
        assertThat(roundCalc.getType()).isEqualTo(NumericType.INSTANCE);
    }
}