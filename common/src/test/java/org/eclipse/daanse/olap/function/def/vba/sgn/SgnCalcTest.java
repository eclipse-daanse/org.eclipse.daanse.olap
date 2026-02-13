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
package org.eclipse.daanse.olap.function.def.vba.sgn;

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

class SgnCalcTest {

    private SgnCalc sgnCalc;
    private DoubleCalc doubleCalc;
    private Evaluator evaluator;

    @BeforeEach
    void setUp() {
        doubleCalc = mock(DoubleCalc.class);
        evaluator = mock(Evaluator.class);
        sgnCalc = new SgnCalc(NumericType.INSTANCE, doubleCalc);
    }

    @ParameterizedTest(name = "{0}: Sgn({1}) = {2}")
    @MethodSource("arguments")
    @DisplayName("Should calculate sgn correctly")
    void shouldCalculateSgn(String testName, Double input, Integer expected) {
        when(doubleCalc.evaluate(evaluator)).thenReturn(input);

        Integer result = sgnCalc.evaluate(evaluator);

        assertThat(result).isEqualTo(expected);
    }

    static Stream<Arguments> arguments() {
        return Stream.of(Arguments.of("zero for zero", 0.0, 0), Arguments.of("zero for negative zero", -0.0, 0),
                Arguments.of("one for positive number", 42.5, 1),
                Arguments.of("negative one for negative number", -42.5, -1),
                Arguments.of("one for small positive number", Double.MIN_VALUE, 1),
                Arguments.of("negative one for small negative number", -Double.MIN_VALUE, -1),
                Arguments.of("one for large positive number", Double.MAX_VALUE, 1),
                Arguments.of("negative one for large negative number", -Double.MAX_VALUE, -1),
                Arguments.of("one for positive infinity", Double.POSITIVE_INFINITY, 1),
                Arguments.of("negative one for negative infinity", Double.NEGATIVE_INFINITY, -1),
                Arguments.of("zero for NaN", Double.NaN, 0));
    }

    @Test
    @DisplayName("Should preserve type information")
    void shouldPreserveTypeInformation() {
        assertThat(sgnCalc.getType()).isEqualTo(NumericType.INSTANCE);
    }
}