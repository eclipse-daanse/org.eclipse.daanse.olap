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
package org.eclipse.daanse.olap.function.def.vba.sqr;

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

class SqrCalcTest {

    private SqrCalc sqrCalc;
    private DoubleCalc doubleCalc;
    private Evaluator evaluator;

    @BeforeEach
    void setUp() {
        doubleCalc = mock(DoubleCalc.class);
        evaluator = mock(Evaluator.class);
        sqrCalc = new SqrCalc(NumericType.INSTANCE, doubleCalc);
    }

    @ParameterizedTest(name = "{0}: sqrt({1}) = {2}")
    @MethodSource("arguments")
    @DisplayName("Should calculate square root correctly")
    void shouldCalculateSquareRoot(String testName, Double input, Double expected) {
        when(doubleCalc.evaluate(evaluator)).thenReturn(input);

        Double result = sqrCalc.evaluate(evaluator);

        assertThat(result).isEqualTo(expected);
    }

    static Stream<Arguments> arguments() {
        return Stream.of(Arguments.of("zero", 0.0, 0.0), Arguments.of("one", 1.0, 1.0), Arguments.of("four", 4.0, 2.0),
                Arguments.of("nine", 9.0, 3.0), Arguments.of("sixteen", 16.0, 4.0),
                Arguments.of("twenty-five", 25.0, 5.0), Arguments.of("decimal", 2.25, 1.5),
                Arguments.of("small decimal", 0.0001, 0.01), Arguments.of("large number", 10000.0, 100.0),
                Arguments.of("very small positive", Double.MIN_VALUE, Math.sqrt(Double.MIN_VALUE)),
                Arguments.of("positive infinity", Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY),
                Arguments.of("negative number", -1.0, Double.NaN),
                Arguments.of("negative infinity", Double.NEGATIVE_INFINITY, Double.NaN),
                Arguments.of("not a number", Double.NaN, Double.NaN));
    }

    @Test
    @DisplayName("Should preserve type information")
    void shouldPreserveTypeInformation() {
        assertThat(sqrCalc.getType()).isEqualTo(NumericType.INSTANCE);
    }
}