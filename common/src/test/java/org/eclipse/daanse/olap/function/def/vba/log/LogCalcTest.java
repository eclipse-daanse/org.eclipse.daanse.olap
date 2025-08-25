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
package org.eclipse.daanse.olap.function.def.vba.log;

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

class LogCalcTest {

    private LogCalc logCalc;
    private DoubleCalc doubleCalc;
    private Evaluator evaluator;

    @BeforeEach
    void setUp() {
        doubleCalc = mock(DoubleCalc.class);
        evaluator = mock(Evaluator.class);
        logCalc = new LogCalc(NumericType.INSTANCE, doubleCalc);
    }

    @ParameterizedTest(name = "{0}: Log({1}) = {2}")
    @MethodSource("arguments")
    @DisplayName("Should calculate log correctly")
    void shouldCalculateLog(String testName, Double input, String expectedType, Double expectedValue) {
        when(doubleCalc.evaluate(evaluator)).thenReturn(input);

        Double result = logCalc.evaluate(evaluator);

        switch (expectedType) {
        case "exact":
            assertThat(result).isEqualTo(expectedValue);
            break;
        case "close":
            assertThat(result).isCloseTo(expectedValue, org.assertj.core.data.Offset.offset(1e-10));
            break;
        case "positive":
            assertThat(result).isPositive();
            assertThat(result).isCloseTo(expectedValue, org.assertj.core.data.Offset.offset(1e-10));
            break;
        case "negative":
            assertThat(result).isNegative();
            assertThat(result).isCloseTo(expectedValue, org.assertj.core.data.Offset.offset(1e-10));
            break;
        case "nan":
            assertThat(result).isNaN();
            break;
        case "infinity":
            assertThat(result).isEqualTo(expectedValue);
            break;
        case "negative_infinity":
            assertThat(result).isEqualTo(Double.NEGATIVE_INFINITY);
            break;
        case "very_negative":
            assertThat(result).isNegative();
            break;
        }
    }

    static Stream<Arguments> arguments() {
        return Stream
                .of(Arguments.of("zero for one", 1.0, "exact", 0.0), Arguments.of("one for E", Math.E, "close", 1.0),
                        Arguments.of("negative value for value between zero and one", 0.5, "negative", Math.log(0.5)),
                        Arguments.of("positive value for value greater than one", 10.0, "positive", Math.log(10.0)),
                        Arguments.of("negative infinity for zero", 0.0, "negative_infinity", null),
                        Arguments.of("NaN for negative value", -1.0, "nan", null),
                        Arguments.of("infinity for infinity", Double.POSITIVE_INFINITY, "infinity",
                                Double.POSITIVE_INFINITY),
                        Arguments.of("NaN for NaN", Double.NaN, "nan", null),
                        Arguments.of("very small positive value", Double.MIN_VALUE, "very_negative", null));
    }

    @Test
    @DisplayName("Should preserve type information")
    void shouldPreserveTypeInformation() {
        assertThat(logCalc.getType()).isEqualTo(NumericType.INSTANCE);
    }
}