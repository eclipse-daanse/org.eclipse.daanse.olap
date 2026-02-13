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
package org.eclipse.daanse.olap.function.def.vba.fv;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.stream.Stream;

import org.eclipse.daanse.olap.api.evaluator.Evaluator;
import org.eclipse.daanse.olap.api.calc.BooleanCalc;
import org.eclipse.daanse.olap.api.calc.DoubleCalc;
import org.eclipse.daanse.olap.api.type.NumericType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class FVCalcTest {

    private FVCalc fvCalc;
    private DoubleCalc rateCalc;
    private DoubleCalc nPerCalc;
    private DoubleCalc pmtCalc;
    private DoubleCalc pvCalc;
    private BooleanCalc typeCalc;
    private Evaluator evaluator;

    @BeforeEach
    void setUp() {
        rateCalc = mock(DoubleCalc.class);
        nPerCalc = mock(DoubleCalc.class);
        pmtCalc = mock(DoubleCalc.class);
        pvCalc = mock(DoubleCalc.class);
        typeCalc = mock(BooleanCalc.class);
        evaluator = mock(Evaluator.class);
        fvCalc = new FVCalc(NumericType.INSTANCE, rateCalc, nPerCalc, pmtCalc, pvCalc, typeCalc);
    }

    @ParameterizedTest(name = "{0}: fv(rate={1}, nper={2}, pmt={3}, pv={4}, type={5}) ≈ {6}")
    @MethodSource("fvArguments")
    @DisplayName("Should calculate future value correctly")
    void shouldCalculateFutureValue(String testName, Double rate, Double nPer, Double pmt, Double pv, Boolean type,
            Double expected) {
        when(rateCalc.evaluate(evaluator)).thenReturn(rate);
        when(nPerCalc.evaluate(evaluator)).thenReturn(nPer);
        when(pmtCalc.evaluate(evaluator)).thenReturn(pmt);
        when(pvCalc.evaluate(evaluator)).thenReturn(pv);
        when(typeCalc.evaluate(evaluator)).thenReturn(type);

        Double result = fvCalc.evaluate(evaluator);

        assertThat(result).isCloseTo(expected, within(0.01));
    }

    static Stream<Arguments> fvArguments() {
        return Stream.of(
//                Arguments.of("zero rate", 0.0, 10.0, -100.0, -1000.0, false, 0.0),
//                Arguments.of("simple savings", 0.05, 10.0, -100.0, -1000.0, false, 2885.65),
//                Arguments.of("loan payment", 0.06, 24.0, 300.0, 0.0, false, -10394.5),
//                Arguments.of("annuity due", 0.04, 20.0, -500.0, 0.0, true, 15474.66),
                Arguments.of("compound interest only", 0.08, 5.0, 0.0, -1000.0, false, 1469.33),
                Arguments.of("negative rate", -0.02, 10.0, 0.0, -1000.0, false, 817.07));
    }

    @Test
    @DisplayName("Should test static fV method directly")
    @Disabled
    void shouldTestStaticMethod() {
        Double result = FVCalc.fV(0.05, 10.0, -100.0, -1000.0, false);
        assertThat(result).isCloseTo(2885.65, within(0.01));
    }

    @Test
    @DisplayName("Should handle zero rate scenario")
    @Disabled
    void shouldHandleZeroRate() {
        Double result = FVCalc.fV(0.0, 10.0, -100.0, -1000.0, false);
        assertThat(result).isCloseTo(0.0, within(0.01));
    }

    @Test
    @DisplayName("Should handle payments at beginning vs end")
    void shouldHandlePaymentTiming() {
        // Payment at end of period
        Double fvEnd = FVCalc.fV(0.05, 10.0, -100.0, 0.0, false);

        // Payment at beginning of period
        Double fvBegin = FVCalc.fV(0.05, 10.0, -100.0, 0.0, true);

        // Beginning payments should result in higher future value
        assertThat(fvBegin).isGreaterThan(fvEnd);
    }

    @Test
    @DisplayName("Should preserve type information")
    void shouldPreserveTypeInformation() {
        assertThat(fvCalc.getType()).isEqualTo(NumericType.INSTANCE);
    }

    @Test
    @DisplayName("Should handle edge cases")
    void shouldHandleEdgeCases() {
        // No payments, no present value
        Double result1 = FVCalc.fV(0.05, 10.0, 0.0, 0.0, false);
        assertThat(result1).isCloseTo(0.0, within(0.01));

        // High interest rate
        Double result2 = FVCalc.fV(0.20, 5.0, -100.0, 0.0, false);
        assertThat(result2).isGreaterThan(0);
    }
}