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
package org.eclipse.daanse.olap.function.def.vba.ddb;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;
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

class DDBCalcTest {

    private DDBCalc ddbCalc;
    private DoubleCalc costCalc;
    private DoubleCalc salvageCalc;
    private DoubleCalc lifeCalc;
    private DoubleCalc periodCalc;
    private DoubleCalc factorCalc;
    private Evaluator evaluator;

    @BeforeEach
    void setUp() {
        costCalc = mock(DoubleCalc.class);
        salvageCalc = mock(DoubleCalc.class);
        lifeCalc = mock(DoubleCalc.class);
        periodCalc = mock(DoubleCalc.class);
        factorCalc = mock(DoubleCalc.class);
        evaluator = mock(Evaluator.class);
        ddbCalc = new DDBCalc(NumericType.INSTANCE, costCalc, salvageCalc, lifeCalc, periodCalc, factorCalc);
    }

    @ParameterizedTest(name = "{0}: DDB({1}, {2}, {3}, {4}, {5}) = {6}")
    @MethodSource("ddbCalculationArguments")
    @DisplayName("Should calculate double declining balance depreciation correctly")
    void shouldCalculateDoubleBalanceDepreciationCorrectly(String testName, Double cost, Double salvage, Double life,
            Double period, Double factor, Double expectedResult) {
        when(costCalc.evaluate(evaluator)).thenReturn(cost);
        when(salvageCalc.evaluate(evaluator)).thenReturn(salvage);
        when(lifeCalc.evaluate(evaluator)).thenReturn(life);
        when(periodCalc.evaluate(evaluator)).thenReturn(period);
        when(factorCalc.evaluate(evaluator)).thenReturn(factor);

        Double result = ddbCalc.evaluate(evaluator);

        assertThat(result).isCloseTo(expectedResult, within(0.01));
    }

    static Stream<Arguments> ddbCalculationArguments() {
        // Formula: ((cost - salvage) * factor) / life * period
        return Stream.of(Arguments.of("standard depreciation", 10000.0, 1000.0, 5.0, 1.0, 2.0, 3600.0),
                Arguments.of("zero salvage", 10000.0, 0.0, 5.0, 1.0, 2.0, 4000.0),
                Arguments.of("half period", 10000.0, 1000.0, 5.0, 0.5, 2.0, 1800.0),
                Arguments.of("double period", 10000.0, 1000.0, 5.0, 2.0, 2.0, 7200.0),
                Arguments.of("factor of 1", 10000.0, 1000.0, 5.0, 1.0, 1.0, 1800.0),
                Arguments.of("factor of 3", 10000.0, 1000.0, 5.0, 1.0, 3.0, 5400.0),
                Arguments.of("high salvage", 10000.0, 9000.0, 5.0, 1.0, 2.0, 400.0),
                Arguments.of("long life", 10000.0, 1000.0, 10.0, 1.0, 2.0, 1800.0));
    }

    @Test
    @DisplayName("Should handle zero cost correctly")
    void shouldHandleZeroCostCorrectly() {
        when(costCalc.evaluate(evaluator)).thenReturn(0.0);
        when(salvageCalc.evaluate(evaluator)).thenReturn(0.0);
        when(lifeCalc.evaluate(evaluator)).thenReturn(5.0);
        when(periodCalc.evaluate(evaluator)).thenReturn(1.0);
        when(factorCalc.evaluate(evaluator)).thenReturn(2.0);

        Double result = ddbCalc.evaluate(evaluator);

        assertThat(result).isEqualTo(0.0);
    }

    @Test
    @DisplayName("Should handle zero period correctly")
    void shouldHandleZeroPeriodCorrectly() {
        when(costCalc.evaluate(evaluator)).thenReturn(10000.0);
        when(salvageCalc.evaluate(evaluator)).thenReturn(1000.0);
        when(lifeCalc.evaluate(evaluator)).thenReturn(5.0);
        when(periodCalc.evaluate(evaluator)).thenReturn(0.0);
        when(factorCalc.evaluate(evaluator)).thenReturn(2.0);

        Double result = ddbCalc.evaluate(evaluator);

        assertThat(result).isEqualTo(0.0);
    }

    @Test
    @DisplayName("Should handle zero factor correctly")
    void shouldHandleZeroFactorCorrectly() {
        when(costCalc.evaluate(evaluator)).thenReturn(10000.0);
        when(salvageCalc.evaluate(evaluator)).thenReturn(1000.0);
        when(lifeCalc.evaluate(evaluator)).thenReturn(5.0);
        when(periodCalc.evaluate(evaluator)).thenReturn(1.0);
        when(factorCalc.evaluate(evaluator)).thenReturn(0.0);

        Double result = ddbCalc.evaluate(evaluator);

        assertThat(result).isEqualTo(0.0);
    }

    @Test
    @DisplayName("Should handle cost equal to salvage correctly")
    void shouldHandleCostEqualToSalvageCorrectly() {
        when(costCalc.evaluate(evaluator)).thenReturn(5000.0);
        when(salvageCalc.evaluate(evaluator)).thenReturn(5000.0);
        when(lifeCalc.evaluate(evaluator)).thenReturn(5.0);
        when(periodCalc.evaluate(evaluator)).thenReturn(1.0);
        when(factorCalc.evaluate(evaluator)).thenReturn(2.0);

        Double result = ddbCalc.evaluate(evaluator);

        assertThat(result).isEqualTo(0.0);
    }

    @Test
    @DisplayName("Should handle fractional inputs correctly")
    void shouldHandleFractionalInputsCorrectly() {
        when(costCalc.evaluate(evaluator)).thenReturn(1000.5);
        when(salvageCalc.evaluate(evaluator)).thenReturn(100.5);
        when(lifeCalc.evaluate(evaluator)).thenReturn(4.5);
        when(periodCalc.evaluate(evaluator)).thenReturn(0.5);
        when(factorCalc.evaluate(evaluator)).thenReturn(1.5);

        Double result = ddbCalc.evaluate(evaluator);

        // ((1000.5 - 100.5) * 1.5) / 4.5 * 0.5 = 150.0
        assertThat(result).isCloseTo(150.0, within(0.01));
    }

    @Test
    @DisplayName("Should handle large numbers correctly")
    void shouldHandleLargeNumbersCorrectly() {
        when(costCalc.evaluate(evaluator)).thenReturn(1000000.0);
        when(salvageCalc.evaluate(evaluator)).thenReturn(100000.0);
        when(lifeCalc.evaluate(evaluator)).thenReturn(10.0);
        when(periodCalc.evaluate(evaluator)).thenReturn(1.0);
        when(factorCalc.evaluate(evaluator)).thenReturn(2.0);

        Double result = ddbCalc.evaluate(evaluator);

        // ((1000000 - 100000) * 2) / 10 * 1 = 180000.0
        assertThat(result).isCloseTo(180000.0, within(0.01));
    }

    @Test
    @DisplayName("Should handle negative salvage correctly")
    void shouldHandleNegativeSalvageCorrectly() {
        when(costCalc.evaluate(evaluator)).thenReturn(10000.0);
        when(salvageCalc.evaluate(evaluator)).thenReturn(-1000.0);
        when(lifeCalc.evaluate(evaluator)).thenReturn(5.0);
        when(periodCalc.evaluate(evaluator)).thenReturn(1.0);
        when(factorCalc.evaluate(evaluator)).thenReturn(2.0);

        Double result = ddbCalc.evaluate(evaluator);

        // ((10000 - (-1000)) * 2) / 5 * 1 = 4400.0
        assertThat(result).isCloseTo(4400.0, within(0.01));
    }

    @Test
    @DisplayName("Should handle very small life correctly")
    void shouldHandleVerySmallLifeCorrectly() {
        when(costCalc.evaluate(evaluator)).thenReturn(1000.0);
        when(salvageCalc.evaluate(evaluator)).thenReturn(100.0);
        when(lifeCalc.evaluate(evaluator)).thenReturn(0.1);
        when(periodCalc.evaluate(evaluator)).thenReturn(1.0);
        when(factorCalc.evaluate(evaluator)).thenReturn(2.0);

        Double result = ddbCalc.evaluate(evaluator);

        // ((1000 - 100) * 2) / 0.1 * 1 = 18000.0
        assertThat(result).isCloseTo(18000.0, within(0.01));
    }

    @Test
    @DisplayName("Should preserve type information")
    void shouldPreserveTypeInformation() {
        assertThat(ddbCalc.getType()).isEqualTo(NumericType.INSTANCE);
    }

    @Test
    @DisplayName("Should be consistent across multiple calls")
    void shouldBeConsistentAcrossMultipleCalls() {
        when(costCalc.evaluate(evaluator)).thenReturn(10000.0);
        when(salvageCalc.evaluate(evaluator)).thenReturn(1000.0);
        when(lifeCalc.evaluate(evaluator)).thenReturn(5.0);
        when(periodCalc.evaluate(evaluator)).thenReturn(1.0);
        when(factorCalc.evaluate(evaluator)).thenReturn(2.0);

        Double first = ddbCalc.evaluate(evaluator);
        Double second = ddbCalc.evaluate(evaluator);

        assertThat(first).isEqualTo(second);
    }

    @Test
    @DisplayName("Should handle multiple periods calculation")
    void shouldHandleMultiplePeriodsCalculation() {
        when(costCalc.evaluate(evaluator)).thenReturn(10000.0);
        when(salvageCalc.evaluate(evaluator)).thenReturn(1000.0);
        when(lifeCalc.evaluate(evaluator)).thenReturn(5.0);
        when(periodCalc.evaluate(evaluator)).thenReturn(3.0);
        when(factorCalc.evaluate(evaluator)).thenReturn(2.0);

        Double result = ddbCalc.evaluate(evaluator);

        // ((10000 - 1000) * 2) / 5 * 3 = 10800.0
        assertThat(result).isCloseTo(10800.0, within(0.01));
    }
}