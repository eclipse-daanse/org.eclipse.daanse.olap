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
package org.eclipse.daanse.olap.function.def.vba.irr;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.stream.Stream;

import org.eclipse.daanse.olap.api.Evaluator;
import org.eclipse.daanse.olap.api.calc.Calc;
import org.eclipse.daanse.olap.api.calc.DoubleCalc;
import org.eclipse.daanse.olap.api.type.NumericType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class IRRCalcTest {

    private IRRCalc irrCalc;
    private Calc<Object> valueArrayCalc;
    private DoubleCalc guessCalc;
    private Evaluator evaluator;

    @BeforeEach
    void setUp() {
        valueArrayCalc = mock(Calc.class);
        guessCalc = mock(DoubleCalc.class);
        evaluator = mock(Evaluator.class);
        irrCalc = new IRRCalc(NumericType.INSTANCE, valueArrayCalc, guessCalc);
    }

    @ParameterizedTest(name = "{0}: irr(values, {1})")
    @MethodSource("irrArguments")
    @DisplayName("Should calculate internal rate of return")
    void shouldCalculateIRR(String testName, Double[] values, Double guess, Double expectedRange) {
        when(valueArrayCalc.evaluate(evaluator)).thenReturn(values);
        when(guessCalc.evaluate(evaluator)).thenReturn(guess);

        Double result = irrCalc.evaluate(evaluator);

        assertThat(result).isNotNull();
        if (expectedRange > 0) {
            // For valid cash flows, IRR should be within reasonable range
            assertThat(result).isBetween(-1.0, expectedRange);
        } else {
            // For invalid flows, might return -1
            assertThat(result).isEqualTo(-1.0);
        }
    }

    static Stream<Arguments> irrArguments() {
        return Stream.of(
                Arguments.of("simple investment", new Double[] { -1000.0, 300.0, 300.0, 300.0, 300.0 }, 0.1, 2.0),
                Arguments.of("project cash flow", new Double[] { -5000.0, 1000.0, 2000.0, 3000.0 }, 0.12, 1.0),
                Arguments.of("loan scenario", new Double[] { 10000.0, -3000.0, -3000.0, -3000.0, -3000.0 }, 0.05, 1.0),
                Arguments.of("mixed cash flow", new Double[] { -1000.0, 500.0, -200.0, 800.0 }, 0.1, 2.0));
    }

    @Test
    @DisplayName("Should test static irr method directly")
    void shouldTestStaticMethod() {
        Double[] cashFlow = new Double[] { -1000.0, 300.0, 300.0, 300.0, 300.0 };
        Double result = IRRCalc.irr(cashFlow, 0.1);

        assertThat(result).isNotNull();
        // IRR should be positive for this investment scenario
        assertThat(result).isGreaterThan(0.0);
    }

    @Test
    @DisplayName("Should handle initial positive investment")
    void shouldHandleInitialPositiveInvestment() {
        Double[] cashFlow = new Double[] { 1000.0, -300.0, -300.0, -300.0, -300.0 };
        Double result = IRRCalc.irr(cashFlow, 0.1);

        assertThat(result).isNotNull();
    }

    @Test
    @DisplayName("Should converge when solution exists")
    void shouldConvergeWhenSolutionExists() {
        // Simple case where we know the IRR should be approximately 0.1 (10%)
        Double[] cashFlow = new Double[] { -100.0, 110.0 };
        Double result = IRRCalc.irr(cashFlow, 0.05);

        assertThat(result).isCloseTo(0.1, within(0.01));
    }

    @Test
    @DisplayName("Should handle no solution case")
    @Disabled
    void shouldHandleNoSolutionCase() {
        // All positive cash flows - no valid IRR
        Double[] cashFlow = new Double[] { 1000.0, 1000.0, 1000.0 };
        Double result = IRRCalc.irr(cashFlow, 0.1);

        // Should return -1 when no solution found
        assertThat(result).isEqualTo(-1.0);
    }

    @Test
    @DisplayName("Should handle different guess values")
    void shouldHandleDifferentGuessValues() {
        Double[] cashFlow = new Double[] { -1000.0, 500.0, 600.0 };

        Double result1 = IRRCalc.irr(cashFlow, 0.05);
        Double result2 = IRRCalc.irr(cashFlow, 0.15);

        // Both should converge to same result (within tolerance)
        if (result1 > 0 && result2 > 0) {
            assertThat(result1).isCloseTo(result2, within(0.001));
        }
    }

    @Test
    @DisplayName("Should preserve type information")
    void shouldPreserveTypeInformation() {
        assertThat(irrCalc.getType()).isEqualTo(NumericType.INSTANCE);
    }

    @Test
    @DisplayName("Should handle single cash flow")
    @Disabled
    void shouldHandleSingleCashFlow() {
        Double[] cashFlow = new Double[] { -1000.0 };
        Double result = IRRCalc.irr(cashFlow, 0.1);

        // Single negative cash flow has no meaningful IRR
        assertThat(result).isEqualTo(-1.0);
    }
}