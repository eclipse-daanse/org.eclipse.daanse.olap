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
package org.eclipse.daanse.olap.function.def.vba.cbool;

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

class CBoolCalcTest {

    private CBoolCalc cBoolCalc;
    private Calc<Object> calc;
    private Evaluator evaluator;

    @BeforeEach
    void setUp() {
        calc = mock(Calc.class);
        evaluator = mock(Evaluator.class);
        cBoolCalc = new CBoolCalc(BooleanType.INSTANCE, calc);
    }

    @ParameterizedTest(name = "{0}: CBool({1}) = {2}")
    @MethodSource("arguments")
    @DisplayName("Should convert values to boolean correctly")
    void shouldConvertToBoolean(String testName, Object input, Boolean expected) {
        when(calc.evaluate(evaluator)).thenReturn(input);

        Boolean result = cBoolCalc.evaluate(evaluator);

        assertThat(result).isEqualTo(expected);
    }

    static Stream<Arguments> arguments() {
        return Stream.of(Arguments.of("boolean true", true, true), Arguments.of("boolean false", false, false),
                Arguments.of("integer zero", 0, false), Arguments.of("integer one", 1, true),
                Arguments.of("positive integer", 42, true), Arguments.of("negative integer", -42, true),
                Arguments.of("double zero", 0.0, false), Arguments.of("negative double zero", -0.0, false),
                Arguments.of("positive double", 3.14, true), Arguments.of("negative double", -3.14, true),
                Arguments.of("string zero", "0", false), Arguments.of("string positive number", "42", true)
//                ,
//                Arguments.of("string negative number", "-42", true), Arguments.of("empty string", "", false),
//                Arguments.of("non-numeric string", "hello", false)
        );
    }

    @Test
    @DisplayName("Should preserve type information")
    void shouldPreserveTypeInformation() {
        assertThat(cBoolCalc.getType()).isEqualTo(BooleanType.INSTANCE);
    }
}