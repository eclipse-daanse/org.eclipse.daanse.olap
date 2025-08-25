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
package org.eclipse.daanse.olap.function.def.vba.abs;

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

class AbsCalcTest {

    private AbsCalc absCalc;
    private DoubleCalc doubleCalc;
    private Evaluator evaluator;

    @BeforeEach
    void setUp() {
        doubleCalc = mock(DoubleCalc.class);
        evaluator = mock(Evaluator.class);
        absCalc = new AbsCalc(NumericType.INSTANCE, doubleCalc);
    }

    @ParameterizedTest(name = "{0}: abs({1}) = {2}")
    @MethodSource("arguments")
    @DisplayName("Should calculate absolute value correctly")
    void shouldCalculateAbsoluteValue(String testName, Double input, Double expected) {
        when(doubleCalc.evaluate(evaluator)).thenReturn(input);

        Double result = absCalc.evaluate(evaluator);

        assertThat(result).isEqualTo(expected);
    }

    static Stream<Arguments> arguments() {
        return Stream.of(Arguments.of("positive number", 5.0, 5.0), Arguments.of("negative number", -5.0, 5.0),
                Arguments.of("zero", 0.0, 0.0), Arguments.of("negative zero", -0.0, 0.0),
                Arguments.of("positive decimal", 3.14, 3.14), Arguments.of("negative decimal", -3.14, 3.14),
                Arguments.of("large positive number", 999999.999, 999999.999),
                Arguments.of("large negative number", -999999.999, 999999.999),
                Arguments.of("very small positive", 0.0001, 0.0001),
                Arguments.of("very small negative", -0.0001, 0.0001),
                Arguments.of("positive infinity", Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY),
                Arguments.of("negative infinity", Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY),
                Arguments.of("maximum double value", Double.MAX_VALUE, Double.MAX_VALUE),
                Arguments.of("minimum double value", -Double.MAX_VALUE, Double.MAX_VALUE),
                Arguments.of("smallest positive double", Double.MIN_VALUE, Double.MIN_VALUE),
                Arguments.of("not a number", Double.NaN, Double.NaN));
    }

    @Test
    @DisplayName("Should preserve type information")
    void shouldPreserveTypeInformation() {
        assertThat(absCalc.getType()).isEqualTo(NumericType.INSTANCE);
    }
}