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
package org.eclipse.daanse.olap.function.def.vba.isnull;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.stream.Stream;

import org.eclipse.daanse.olap.api.Evaluator;
import org.eclipse.daanse.olap.api.calc.Calc;
import org.eclipse.daanse.olap.api.type.BooleanType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class IsNullCalcTest {

    private IsNullCalc isNullCalc;
    private Calc<Object> varNameCalc;
    private Evaluator evaluator;

    @BeforeEach
    void setUp() {
        varNameCalc = mock(Calc.class);
        evaluator = mock(Evaluator.class);
        isNullCalc = new IsNullCalc(BooleanType.INSTANCE, varNameCalc);
    }

    @ParameterizedTest(name = "{0}: isNull({1}) = {2}")
    @MethodSource("isNullArguments")
    @DisplayName("Should identify null values correctly")
    void shouldIdentifyNullValues(String testName, Object input, Boolean expected) {
        when(varNameCalc.evaluate(evaluator)).thenReturn(input);

        Boolean result = isNullCalc.evaluate(evaluator);

        assertThat(result).isEqualTo(expected);
    }

    static Stream<Arguments> isNullArguments() {
        return Stream.of(Arguments.of("null value", null, true), Arguments.of("string value", "test", false),
                Arguments.of("integer value", 123, false), Arguments.of("double value", 123.45, false),
                Arguments.of("boolean value", true, false), Arguments.of("empty string", "", false),
                Arguments.of("zero integer", 0, false), Arguments.of("zero double", 0.0, false),
                Arguments.of("false boolean", false, false), Arguments.of("object value", new Object(), false));
    }

    @Test
    @DisplayName("Should preserve type information")
    void shouldPreserveTypeInformation() {
        assertThat(isNullCalc.getType()).isEqualTo(BooleanType.INSTANCE);
    }

    @Test
    @DisplayName("Should return true only for null values")
    void shouldReturnTrueOnlyForNull() {
        when(varNameCalc.evaluate(evaluator)).thenReturn(null);
        assertThat(isNullCalc.evaluate(evaluator)).isTrue();

        when(varNameCalc.evaluate(evaluator)).thenReturn("");
        assertThat(isNullCalc.evaluate(evaluator)).isFalse();

        when(varNameCalc.evaluate(evaluator)).thenReturn(0);
        assertThat(isNullCalc.evaluate(evaluator)).isFalse();

        when(varNameCalc.evaluate(evaluator)).thenReturn(false);
        assertThat(isNullCalc.evaluate(evaluator)).isFalse();
    }

    @Test
    @DisplayName("Should distinguish null from empty values")
    void shouldDistinguishNullFromEmptyValues() {
        // Null should return true
        when(varNameCalc.evaluate(evaluator)).thenReturn(null);
        assertThat(isNullCalc.evaluate(evaluator)).isTrue();

        // Empty string should return false
        when(varNameCalc.evaluate(evaluator)).thenReturn("");
        assertThat(isNullCalc.evaluate(evaluator)).isFalse();

        // Zero values should return false
        when(varNameCalc.evaluate(evaluator)).thenReturn(0);
        assertThat(isNullCalc.evaluate(evaluator)).isFalse();
    }
}