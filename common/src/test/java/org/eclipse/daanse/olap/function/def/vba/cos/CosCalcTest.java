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
package org.eclipse.daanse.olap.function.def.vba.cos;

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

class CosCalcTest {

    private CosCalc cosCalc;
    private DoubleCalc doubleCalc;
    private Evaluator evaluator;

    @BeforeEach
    void setUp() {
        doubleCalc = mock(DoubleCalc.class);
        evaluator = mock(Evaluator.class);
        cosCalc = new CosCalc(NumericType.INSTANCE, doubleCalc);
    }

    @ParameterizedTest(name = "{0}: cos({1}) = {2}")
    @MethodSource("arguments")
    @DisplayName("Should calculate cosine correctly")
    void shouldCalculateCosine(String testName, Double input, Double expected) {
        when(doubleCalc.evaluate(evaluator)).thenReturn(input);

        Double result = cosCalc.evaluate(evaluator);

        assertThat(result).isCloseTo(expected, org.assertj.core.data.Offset.offset(1e-10));
    }

    static Stream<Arguments> arguments() {
        return Stream.of(Arguments.of("zero", 0.0, 1.0), Arguments.of("pi/2", Math.PI / 2, 0.0),
                Arguments.of("pi", Math.PI, -1.0), Arguments.of("3pi/2", 3 * Math.PI / 2, 0.0),
                Arguments.of("2pi", 2 * Math.PI, 1.0), Arguments.of("pi/4", Math.PI / 4, Math.sqrt(2) / 2),
                Arguments.of("pi/3", Math.PI / 3, 0.5), Arguments.of("pi/6", Math.PI / 6, Math.sqrt(3) / 2),
                Arguments.of("negative pi/2", -Math.PI / 2, 0.0), Arguments.of("negative pi", -Math.PI, -1.0),
                Arguments.of("positive infinity", Double.POSITIVE_INFINITY, Double.NaN),
                Arguments.of("negative infinity", Double.NEGATIVE_INFINITY, Double.NaN),
                Arguments.of("not a number", Double.NaN, Double.NaN));
    }

    @Test
    @DisplayName("Should preserve type information")
    void shouldPreserveTypeInformation() {
        assertThat(cosCalc.getType()).isEqualTo(NumericType.INSTANCE);
    }
}