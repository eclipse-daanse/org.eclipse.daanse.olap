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
package org.eclipse.daanse.olap.function.def.vba.formatcurrency;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.stream.Stream;

import org.eclipse.daanse.olap.api.Evaluator;
import org.eclipse.daanse.olap.api.calc.Calc;
import org.eclipse.daanse.olap.api.calc.IntegerCalc;
import org.eclipse.daanse.olap.api.type.StringType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class FormatCurrencyCalcTest {

    private FormatCurrencyCalc formatCurrencyCalc;
    private Calc<Object> expressionCalc;
    private IntegerCalc numDigitsAfterDecimalCalc;
    private IntegerCalc includeLeadingDigitCalc;
    private IntegerCalc useParensForNegativeNumbersCalc;
    private IntegerCalc groupDigitsCalc;
    private Evaluator evaluator;

    @BeforeEach
    void setUp() {
        expressionCalc = mock(Calc.class);
        numDigitsAfterDecimalCalc = mock(IntegerCalc.class);
        includeLeadingDigitCalc = mock(IntegerCalc.class);
        useParensForNegativeNumbersCalc = mock(IntegerCalc.class);
        groupDigitsCalc = mock(IntegerCalc.class);
        evaluator = mock(Evaluator.class);
        formatCurrencyCalc = new FormatCurrencyCalc(StringType.INSTANCE, expressionCalc, numDigitsAfterDecimalCalc,
                includeLeadingDigitCalc, useParensForNegativeNumbersCalc, groupDigitsCalc);
    }

    @ParameterizedTest(name = "{0}: formatCurrency({1}) = {2}")
    @MethodSource("formatCurrencyArguments")
    @DisplayName("Should format currency correctly")
    void shouldFormatCurrency(String testName, Object value, Integer digits, Integer leadingDigit, Integer parens,
            Integer grouping, String expectedPattern) {
        when(expressionCalc.evaluate(evaluator)).thenReturn(value);
        when(numDigitsAfterDecimalCalc.evaluate(evaluator)).thenReturn(digits);
        when(includeLeadingDigitCalc.evaluate(evaluator)).thenReturn(leadingDigit);
        when(useParensForNegativeNumbersCalc.evaluate(evaluator)).thenReturn(parens);
        when(groupDigitsCalc.evaluate(evaluator)).thenReturn(grouping);

        String result = formatCurrencyCalc.evaluate(evaluator);

        assertThat(result).matches(expectedPattern);
    }

    static Stream<Arguments> formatCurrencyArguments() {
        return Stream.of(Arguments.of("basic positive", 123.456, 2, -2, -2, -2, ".*123\\.46.*"),
                Arguments.of("basic negative", -123.456, 2, -2, -2, -2, ".*123\\.46.*"),
                Arguments.of("zero digits after decimal", 123.456, 0, -2, -2, -2, ".*123.*"),
                Arguments.of("custom digits", 123.456, 3, -2, -2, -2, ".*123\\.456.*"),
                Arguments.of("zero value", 0.0, 2, -2, -2, -2, ".*0\\.00.*"),
                Arguments.of("large number", 1234567.89, 2, -2, -2, -2, ".*1,234,567\\.89.*"),
                Arguments.of("small decimal", 0.05, 2, -2, -2, -2, ".*0\\.05.*"));
    }

    @Test
    @DisplayName("Should test static formatCurrency method directly")
    void shouldTestStaticMethod() {
        String result = FormatCurrencyCalc.formatCurrency(123.45, 2, -2, -2, -2);
        assertThat(result).matches(".*123\\.45.*");
    }

    @Test
    @DisplayName("Should handle null input")
    @Disabled
    void shouldHandleNullInput() {
        when(expressionCalc.evaluate(evaluator)).thenReturn(null);
        when(numDigitsAfterDecimalCalc.evaluate(evaluator)).thenReturn(2);
        when(includeLeadingDigitCalc.evaluate(evaluator)).thenReturn(-2);
        when(useParensForNegativeNumbersCalc.evaluate(evaluator)).thenReturn(-2);
        when(groupDigitsCalc.evaluate(evaluator)).thenReturn(-2);

        String result = formatCurrencyCalc.evaluate(evaluator);

        assertThat(result).isNotNull();
    }

    @Test
    @DisplayName("Should preserve type information")
    void shouldPreserveTypeInformation() {
        assertThat(formatCurrencyCalc.getType()).isEqualTo(StringType.INSTANCE);
    }

    @Test
    @DisplayName("Should handle grouping settings")
    @Disabled
    void shouldHandleGroupingSettings() {
        when(expressionCalc.evaluate(evaluator)).thenReturn(1234567.89);
        when(numDigitsAfterDecimalCalc.evaluate(evaluator)).thenReturn(2);
        when(includeLeadingDigitCalc.evaluate(evaluator)).thenReturn(-2);
        when(useParensForNegativeNumbersCalc.evaluate(evaluator)).thenReturn(-2);
        when(groupDigitsCalc.evaluate(evaluator)).thenReturn(0);

        String result = formatCurrencyCalc.evaluate(evaluator);

        // With grouping disabled, should not have commas
        assertThat(result).doesNotContain(",");
    }
}