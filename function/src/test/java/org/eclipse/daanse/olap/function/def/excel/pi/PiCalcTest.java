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
package org.eclipse.daanse.olap.function.def.excel.pi;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import org.eclipse.daanse.olap.api.evaluator.Evaluator;
import org.eclipse.daanse.olap.api.type.NumericType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class PiCalcTest {

    private PiCalc piCalc;
    private Evaluator evaluator;

    @BeforeEach
    void setUp() {
        evaluator = mock(Evaluator.class);
        piCalc = new PiCalc(NumericType.INSTANCE);
    }

    @Test
    @DisplayName("Should return Math.PI constant")
    void shouldReturnMathPi() {
        Double result = piCalc.evaluate(evaluator);

        assertThat(result).isEqualTo(Math.PI);
        assertThat(result).isCloseTo(3.141592653589793, org.assertj.core.data.Offset.offset(1e-15));
    }

    @Test
    @DisplayName("Should always return the same value")
    void shouldAlwaysReturnSameValue() {
        Double result1 = piCalc.evaluate(evaluator);
        Double result2 = piCalc.evaluate(evaluator);
        Double result3 = piCalc.evaluate(evaluator);

        assertThat(result1).isEqualTo(result2);
        assertThat(result2).isEqualTo(result3);
        assertThat(result1).isEqualTo(Math.PI);
    }

    @Test
    @DisplayName("Should return precise Pi value")
    void shouldReturnPrecisePiValue() {
        Double result = piCalc.evaluate(evaluator);

        // Verify it's the exact Math.PI constant
        assertThat(result).isEqualTo(Math.PI);

        // Verify it's approximately 3.14159...
        assertThat(result).isBetween(3.14159, 3.14160);

        // Verify high precision
        assertThat(result).isCloseTo(3.141592653589793238462643383279, org.assertj.core.data.Offset.offset(1e-15));
    }

    @Test
    @DisplayName("Should be mathematical constant properties")
    void shouldHaveMathematicalProperties() {
        Double pi = piCalc.evaluate(evaluator);

        // Pi is approximately 22/7
        assertThat(pi).isCloseTo(22.0 / 7.0, org.assertj.core.data.Offset.offset(0.01));

        // Pi * 2 should be 2π (full circle in radians)
        assertThat(pi * 2).isCloseTo(6.283185307179586, org.assertj.core.data.Offset.offset(1e-15));

        // Pi / 2 should be π/2 (90 degrees in radians)
        assertThat(pi / 2).isCloseTo(1.5707963267948966, org.assertj.core.data.Offset.offset(1e-15));
    }

    @Test
    @DisplayName("Should preserve type information")
    void shouldPreserveTypeInformation() {
        assertThat(piCalc.getType()).isEqualTo(NumericType.INSTANCE);
    }

    @Test
    @DisplayName("Should not depend on evaluator state")
    void shouldNotDependOnEvaluatorState() {
        Evaluator anotherEvaluator = mock(Evaluator.class);

        Double result1 = piCalc.evaluate(evaluator);
        Double result2 = piCalc.evaluate(anotherEvaluator);
        Double result3 = piCalc.evaluate(null); // Even with null evaluator

        assertThat(result1).isEqualTo(result2);
        assertThat(result2).isEqualTo(result3);
        assertThat(result1).isEqualTo(Math.PI);
    }
}