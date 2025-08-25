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
package org.eclipse.daanse.olap.function.def.vba.space;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.stream.Stream;

import org.eclipse.daanse.olap.api.Evaluator;
import org.eclipse.daanse.olap.api.calc.IntegerCalc;
import org.eclipse.daanse.olap.api.type.StringType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class SpaceCalcTest {

    private SpaceCalc spaceCalc;
    private IntegerCalc integerCalc;
    private Evaluator evaluator;

    @BeforeEach
    void setUp() {
        integerCalc = mock(IntegerCalc.class);
        evaluator = mock(Evaluator.class);
        spaceCalc = new SpaceCalc(StringType.INSTANCE, integerCalc);
    }

    @ParameterizedTest(name = "{0}: Space({1}) = {2}")
    @MethodSource("arguments")
    @DisplayName("Should calculate space correctly")
    void shouldCalculateSpace(String testName, Integer input, String expected) {
        when(integerCalc.evaluate(evaluator)).thenReturn(input);

        String result = spaceCalc.evaluate(evaluator);

        assertThat(result).isEqualTo(expected);
        if (expected.length() > 0) {
            assertThat(result).hasSize(expected.length());
            assertThat(result).matches("^ {" + expected.length() + "}$");
        }
    }

    static Stream<Arguments> arguments() {
        return Stream.of(Arguments.of("empty string for zero spaces", 0, ""),
                Arguments.of("single space for one", 1, " "), Arguments.of("multiple spaces for five", 5, "     "),
                Arguments.of("large number of spaces", 100, " ".repeat(100)));
    }

    @Test
    @DisplayName("Should throw exception for negative number")
    void shouldThrowExceptionForNegativeNumber() {
        when(integerCalc.evaluate(evaluator)).thenReturn(-1);

        assertThatThrownBy(() -> spaceCalc.evaluate(evaluator)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("Should throw exception for large negative number")
    void shouldThrowExceptionForLargeNegativeNumber() {
        when(integerCalc.evaluate(evaluator)).thenReturn(-10);

        assertThatThrownBy(() -> spaceCalc.evaluate(evaluator)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("Should preserve type information")
    void shouldPreserveTypeInformation() {
        assertThat(spaceCalc.getType()).isEqualTo(StringType.INSTANCE);
    }
}