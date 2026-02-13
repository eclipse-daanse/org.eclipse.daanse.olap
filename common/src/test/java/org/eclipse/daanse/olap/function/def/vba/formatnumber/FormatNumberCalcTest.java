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
package org.eclipse.daanse.olap.function.def.vba.formatnumber;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.stream.Stream;

import org.eclipse.daanse.olap.api.evaluator.Evaluator;
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

class FormatNumberCalcTest {

    private FormatNumberCalc formatNumberCalc;
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
        formatNumberCalc = new FormatNumberCalc(StringType.INSTANCE, expressionCalc, numDigitsAfterDecimalCalc,
                includeLeadingDigitCalc, useParensForNegativeNumbersCalc, groupDigitsCalc);
    }

    @ParameterizedTest(name = "{0}: formatNumber({1}) = {2}")
    @MethodSource("formatNumberArguments")
    @DisplayName("Should format numbers correctly")
    void shouldFormatNumber(String testName, Object value, Integer digits, Integer leadingDigit, Integer parens,
            Integer grouping, String expectedPattern) {
        when(expressionCalc.evaluate(evaluator)).thenReturn(value);
        when(numDigitsAfterDecimalCalc.evaluate(evaluator)).thenReturn(digits);
        when(includeLeadingDigitCalc.evaluate(evaluator)).thenReturn(leadingDigit);
        when(useParensForNegativeNumbersCalc.evaluate(evaluator)).thenReturn(parens);
        when(groupDigitsCalc.evaluate(evaluator)).thenReturn(grouping);

        String result = formatNumberCalc.evaluate(evaluator);

        assertThat(result).matches(expectedPattern);
    }

    static Stream<Arguments> formatNumberArguments() {
        return Stream.of(Arguments.of("basic positive", 123.456, 2, -1, -1, -1, "123\\.46"),
//                Arguments.of("basic negative", -123.456, 2, -1, -1, -1, "-123\\.46"),
                Arguments.of("zero digits after decimal", 123.456, 0, -1, -1, -1, "123"),
                Arguments.of("custom digits", 123.456, 3, -1, -1, -1, "123\\.456"),
                Arguments.of("zero value", 0.0, 2, -1, -1, -1, "0\\.00"),
                Arguments.of("large number with grouping", 1234567.89, 2, -1, -1, 1, "1,234,567\\.89"),
                Arguments.of("small decimal", 0.05, 2, -1, -1, -1, "0\\.05"));
    }

    @Test
    @DisplayName("Should test static formatNumber method directly")
    void shouldTestStaticMethod() {
        String result = FormatNumberCalc.formatNumber(123.45, 2, -1, -1, -1);
        assertThat(result).isEqualTo("123.45");
    }

    @Test
    @DisplayName("Should handle parentheses for negative numbers")
    void shouldHandleParenthesesForNegativeNumbers() {
        String result = FormatNumberCalc.formatNumber(-123.45, 2, -1, 1, -1);
        assertThat(result).isEqualTo("(123.45)");
    }

    @Test
    @DisplayName("Should handle leading digit settings")
    void shouldHandleLeadingDigitSettings() {
        // With leading digit enabled
        String withLeading = FormatNumberCalc.formatNumber(0.5, 1, 1, -1, -1);
        assertThat(withLeading).contains("0.5");

        // With leading digit disabled
        String withoutLeading = FormatNumberCalc.formatNumber(0.5, 1, 0, -1, -1);
        assertThat(withoutLeading).isEqualTo(".5");
    }

    @Test
    @DisplayName("Should handle grouping settings")
    void shouldHandleGroupingSettings() {
        // With grouping enabled
        String withGrouping = FormatNumberCalc.formatNumber(1234567.89, 2, -1, -1, 1);
        assertThat(withGrouping).contains(",");

        // With grouping disabled
        String withoutGrouping = FormatNumberCalc.formatNumber(1234567.89, 2, -1, -1, 0);
        assertThat(withoutGrouping).doesNotContain(",");
    }

    @Test
    @DisplayName("Should preserve type information")
    void shouldPreserveTypeInformation() {
        assertThat(formatNumberCalc.getType()).isEqualTo(StringType.INSTANCE);
    }

    @Test
    @DisplayName("Should handle null input")
    @Disabled
    void shouldHandleNullInput() {
        when(expressionCalc.evaluate(evaluator)).thenReturn(null);
        when(numDigitsAfterDecimalCalc.evaluate(evaluator)).thenReturn(2);
        when(includeLeadingDigitCalc.evaluate(evaluator)).thenReturn(-1);
        when(useParensForNegativeNumbersCalc.evaluate(evaluator)).thenReturn(-1);
        when(groupDigitsCalc.evaluate(evaluator)).thenReturn(-1);

        String result = formatNumberCalc.evaluate(evaluator);

        assertThat(result).isNotNull();
    }

    @Test
    @DisplayName("Should handle different number types")
    void shouldHandleDifferentNumberTypes() {
        assertThat(FormatNumberCalc.formatNumber(123, 2, -1, -1, -1)).isEqualTo("123.00");
        assertThat(FormatNumberCalc.formatNumber(123.0f, 2, -1, -1, -1)).isEqualTo("123.00");
        assertThat(FormatNumberCalc.formatNumber(123.0d, 2, -1, -1, -1)).isEqualTo("123.00");
    }
}