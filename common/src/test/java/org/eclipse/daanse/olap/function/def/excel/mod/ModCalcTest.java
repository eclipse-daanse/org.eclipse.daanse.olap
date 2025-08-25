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
package org.eclipse.daanse.olap.function.def.excel.mod;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.stream.Stream;

import org.eclipse.daanse.olap.api.Evaluator;
import org.eclipse.daanse.olap.api.calc.Calc;
import org.eclipse.daanse.olap.api.type.NumericType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

@SuppressWarnings("unchecked")
class ModCalcTest {

    private ModCalc modCalc;
    private Calc<Object> firstCalc;
    private Calc<Object> secondCalc;
    private Evaluator evaluator;

    @BeforeEach
    void setUp() {
        firstCalc = mock(Calc.class);
        secondCalc = mock(Calc.class);
        evaluator = mock(Evaluator.class);
        modCalc = new ModCalc(NumericType.INSTANCE, firstCalc, secondCalc);
    }

    @ParameterizedTest(name = "{0}: Mod({1}, {2}) = {3}")
    @MethodSource("validArguments")
    @DisplayName("Should calculate modulo correctly")
    void shouldCalculateModulo(String testName, Number first, Number second, Double expected) {
        when(firstCalc.evaluate(evaluator)).thenReturn(first);
        when(secondCalc.evaluate(evaluator)).thenReturn(second);

        Double result = modCalc.evaluate(evaluator);

        assertThat(result).isCloseTo(expected, org.assertj.core.data.Offset.offset(1e-10));
    }

    static Stream<Arguments> validArguments() {
        return Stream.of(Arguments.of("positive mod positive", 10.0, 3.0, 1.0),
                Arguments.of("negative mod positive", -10.0, 3.0, 2.0),
                Arguments.of("positive mod negative", 10.0, -3.0, -2.0),
                Arguments.of("negative mod negative", -10.0, -3.0, -1.0),
                Arguments.of("zero mod positive", 0.0, 5.0, 0.0), Arguments.of("smaller mod larger", 3.0, 5.0, 3.0),
                Arguments.of("exact division", 15.0, 3.0, 0.0), Arguments.of("decimal numbers", 7.5, 2.5, 0.0),
                Arguments.of("integer inputs", 17, 5, 2.0), Arguments.of("mixed types", 17.0, 5, 2.0),
                Arguments.of("fractional result", 5.7, 2.0, 1.7), Arguments.of("large numbers", 1000.0, 7.0, 6.0));
    }

    @Test
    @DisplayName("Should preserve type information")
    void shouldPreserveTypeInformation() {
        assertThat(modCalc.getType()).isEqualTo(NumericType.INSTANCE);
    }
}