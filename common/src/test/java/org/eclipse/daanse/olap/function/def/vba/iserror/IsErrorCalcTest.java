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
package org.eclipse.daanse.olap.function.def.vba.iserror;

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

class IsErrorCalcTest {

    private IsErrorCalc isErrorCalc;
    private Calc<Object> varNameCalc;
    private Evaluator evaluator;

    @BeforeEach
    void setUp() {
        varNameCalc = mock(Calc.class);
        evaluator = mock(Evaluator.class);
        isErrorCalc = new IsErrorCalc(BooleanType.INSTANCE, varNameCalc);
    }

    @ParameterizedTest(name = "{0}: isError({1}) = {2}")
    @MethodSource("isErrorArguments")
    @DisplayName("Should identify error objects correctly")
    void shouldIdentifyErrors(String testName, Object input, Boolean expected) {
        when(varNameCalc.evaluate(evaluator)).thenReturn(input);

        Boolean result = isErrorCalc.evaluate(evaluator);

        assertThat(result).isEqualTo(expected);
    }

    static Stream<Arguments> isErrorArguments() {
        return Stream.of(Arguments.of("runtime exception", new RuntimeException("test"), true),
                Arguments.of("null pointer exception", new NullPointerException(), true),
                Arguments.of("illegal argument exception", new IllegalArgumentException("test"), true),
                Arguments.of("exception", new Exception("test"), true), Arguments.of("error", new Error("test"), true),
                Arguments.of("throwable", new Throwable("test"), true), Arguments.of("string value", "test", false),
                Arguments.of("integer value", 123, false), Arguments.of("double value", 123.45, false),
                Arguments.of("boolean value", true, false), Arguments.of("null value", null, false),
                Arguments.of("empty string", "", false), Arguments.of("object value", new Object(), false));
    }

    @Test
    @DisplayName("Should preserve type information")
    void shouldPreserveTypeInformation() {
        assertThat(isErrorCalc.getType()).isEqualTo(BooleanType.INSTANCE);
    }

    @Test
    @DisplayName("Should return true for all throwable subtypes")
    void shouldReturnTrueForAllThrowableSubtypes() {
        when(varNameCalc.evaluate(evaluator)).thenReturn(new ArithmeticException("division by zero"));
        assertThat(isErrorCalc.evaluate(evaluator)).isTrue();

        when(varNameCalc.evaluate(evaluator)).thenReturn(new ClassCastException("cast error"));
        assertThat(isErrorCalc.evaluate(evaluator)).isTrue();

        when(varNameCalc.evaluate(evaluator)).thenReturn(new OutOfMemoryError("memory"));
        assertThat(isErrorCalc.evaluate(evaluator)).isTrue();
    }

    @Test
    @DisplayName("Should return false for non-throwable objects")
    void shouldReturnFalseForNonThrowableObjects() {
        when(varNameCalc.evaluate(evaluator)).thenReturn("not an error");
        assertThat(isErrorCalc.evaluate(evaluator)).isFalse();

        when(varNameCalc.evaluate(evaluator)).thenReturn(42);
        assertThat(isErrorCalc.evaluate(evaluator)).isFalse();

        when(varNameCalc.evaluate(evaluator)).thenReturn(new StringBuilder());
        assertThat(isErrorCalc.evaluate(evaluator)).isFalse();
    }
}