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
package org.eclipse.daanse.olap.function.def.vba.fix;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.stream.Stream;

import org.eclipse.daanse.olap.api.evaluator.Evaluator;
import org.eclipse.daanse.olap.api.calc.Calc;
import org.eclipse.daanse.olap.api.type.NumericType;
import org.eclipse.daanse.olap.exceptions.InvalidArgumentException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class FixCalcTest {

    private FixCalc fixCalc;
    private Calc<Object> calc;
    private Evaluator evaluator;

    @BeforeEach
    void setUp() {
        calc = mock(Calc.class);
        evaluator = mock(Evaluator.class);
        fixCalc = new FixCalc(NumericType.INSTANCE, calc);
    }

    @ParameterizedTest(name = "{0}: Fix({1}) = {2}")
    @MethodSource("arguments")
    @DisplayName("Should calculate fix correctly")
    void shouldCalculateFix(String testName, Object input, Integer expected) {
        when(calc.evaluate(evaluator)).thenReturn(input);

        Integer result = fixCalc.evaluate(evaluator);

        assertThat(result).isEqualTo(expected);
    }

    static Stream<Arguments> arguments() {
        return Stream.of(Arguments.of("positive integer", 42.0, 42), Arguments.of("truncate positive double", 42.7, 42),
                Arguments.of("negative integer", -42.0, -42),
                Arguments.of("truncate towards zero for negative double", -42.7, -42), Arguments.of("zero", 0.0, 0),
                Arguments.of("negative zero", -0.0, 0), Arguments.of("small positive decimal", 0.99, 0),
                Arguments.of("small negative decimal", -0.99, 0), Arguments.of("integer type", 42, 42));
    }

    @Test
    @DisplayName("Should throw exception for non-number type")
    void shouldThrowExceptionForNonNumberType() {
        when(calc.evaluate(evaluator)).thenReturn("not a number");

        assertThatThrownBy(() -> fixCalc.evaluate(evaluator)).isInstanceOf(InvalidArgumentException.class)
                .hasMessageContaining("Invalid parameter").hasMessageContaining("must be of type number");
    }

    @Test
    @DisplayName("Should throw exception for null input")
    void shouldThrowExceptionForNullInput() {
        when(calc.evaluate(evaluator)).thenReturn(null);

        assertThatThrownBy(() -> fixCalc.evaluate(evaluator)).isInstanceOf(InvalidArgumentException.class);
    }

    @Test
    @DisplayName("Should preserve type information")
    void shouldPreserveTypeInformation() {
        assertThat(fixCalc.getType()).isEqualTo(NumericType.INSTANCE);
    }
}