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
package org.eclipse.daanse.olap.function.def.vba.ipmt;

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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class IPmtCalcTest {

    private IPmtCalc ipmtCalc;
    private DoubleCalc rateCalc;
    private DoubleCalc perCalc;
    private DoubleCalc nPerCalc;
    private DoubleCalc pvCalc;
    private DoubleCalc fvCalc;
    private BooleanCalc dueCalc;
    private Evaluator evaluator;

    @BeforeEach
    void setUp() {
        rateCalc = mock(DoubleCalc.class);
        perCalc = mock(DoubleCalc.class);
        nPerCalc = mock(DoubleCalc.class);
        pvCalc = mock(DoubleCalc.class);
        fvCalc = mock(DoubleCalc.class);
        dueCalc = mock(BooleanCalc.class);
        evaluator = mock(Evaluator.class);
        ipmtCalc = new IPmtCalc(NumericType.INSTANCE, rateCalc, perCalc, nPerCalc, pvCalc, fvCalc, dueCalc);
    }

    @ParameterizedTest(name = "{0}: ipmt(rate={1}, per={2}, nper={3}, pv={4}, fv={5}, due={6})")
    @MethodSource("ipmtArguments")
    @DisplayName("Should calculate interest payment correctly")
    void shouldCalculateInterestPayment(String testName, Double rate, Double per, Double nPer, Double pv, Double fv,
            Boolean due) {
        when(rateCalc.evaluate(evaluator)).thenReturn(rate);
        when(perCalc.evaluate(evaluator)).thenReturn(per);
        when(nPerCalc.evaluate(evaluator)).thenReturn(nPer);
        when(pvCalc.evaluate(evaluator)).thenReturn(pv);
        when(fvCalc.evaluate(evaluator)).thenReturn(fv);
        when(dueCalc.evaluate(evaluator)).thenReturn(due);

        Double result = ipmtCalc.evaluate(evaluator);

        assertThat(result).isNotNull();
        // Interest portion should be negative for a loan (positive present value)
        if (pv > 0) {
            assertThat(result).isLessThan(0);
        }
    }

    static Stream<Arguments> ipmtArguments() {
        return Stream.of(Arguments.of("first payment", 0.06 / 12, 1.0, 360.0, 100000.0, 0.0, false),
                Arguments.of("middle payment", 0.06 / 12, 180.0, 360.0, 100000.0, 0.0, false),
                Arguments.of("last payment", 0.06 / 12, 360.0, 360.0, 100000.0, 0.0, false),
                Arguments.of("annual payment", 0.08, 1.0, 10.0, 10000.0, 0.0, false),
                Arguments.of("payment due beginning", 0.05 / 12, 1.0, 60.0, 20000.0, 0.0, true));
    }

    @Test
    @DisplayName("Should test static iPmt method directly")
    void shouldTestStaticMethod() {
        Double result = IPmtCalc.iPmt(0.06 / 12, 1.0, 360.0, 100000.0, 0.0, false);
        assertThat(result).isNotNull();
        assertThat(result).isLessThan(0); // Interest portion should be negative
    }

    @Test
    @DisplayName("Should test helper pmt method")
    void shouldTestPmtMethod() {
        Double pmt = IPmtCalc.pmt(0.06 / 12, 360.0, 100000.0, 0.0, false);
        assertThat(pmt).isNotNull();
        assertThat(pmt).isLessThan(0); // Payment should be negative for a loan
    }

    @Test
    @DisplayName("Should test helper pV method")
    void shouldTestPVMethod() {
        Double pv = IPmtCalc.pV(0.06 / 12, 360.0, -599.55, 0.0, false);
        assertThat(pv).isNotNull();
        assertThat(pv).isCloseTo(100000.0, within(1.0)); // Should be close to loan amount
    }

    @Test
    @DisplayName("Should handle zero rate")
    void shouldHandleZeroRate() {
        Double pmt = IPmtCalc.pmt(0.0, 12.0, 12000.0, 0.0, false);
        assertThat(pmt).isCloseTo(-1000.0, within(0.01)); // Simple division
    }

    @Test
    @DisplayName("Should handle payments due at beginning")
    void shouldHandlePaymentsDueAtBeginning() {
        Double pmtEnd = IPmtCalc.pmt(0.05, 10.0, 1000.0, 0.0, false);
        Double pmtBegin = IPmtCalc.pmt(0.05, 10.0, 1000.0, 0.0, true);

        // Payments due at beginning should be smaller in absolute value
        assertThat(Math.abs(pmtBegin)).isLessThan(Math.abs(pmtEnd));
    }

    @Test
    @DisplayName("Should preserve type information")
    void shouldPreserveTypeInformation() {
        assertThat(ipmtCalc.getType()).isEqualTo(NumericType.INSTANCE);
    }
}