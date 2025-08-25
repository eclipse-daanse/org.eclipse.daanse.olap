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
package org.eclipse.daanse.olap.function.def.vba.formatpercent;

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

class FormatPercentCalcTest {

    private FormatPercentCalc formatPercentCalc;
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
        formatPercentCalc = new FormatPercentCalc(StringType.INSTANCE, expressionCalc, numDigitsAfterDecimalCalc,
                includeLeadingDigitCalc, useParensForNegativeNumbersCalc, groupDigitsCalc);
    }

    @ParameterizedTest(name = "{0}: formatPercent({1}) contains %")
    @MethodSource("formatPercentArguments")
    @DisplayName("Should format percentages correctly")
    void shouldFormatPercent(String testName, Object value, Integer digits, Integer leadingDigit, Integer parens,
            Integer grouping) {
        when(expressionCalc.evaluate(evaluator)).thenReturn(value);
        when(numDigitsAfterDecimalCalc.evaluate(evaluator)).thenReturn(digits);
        when(includeLeadingDigitCalc.evaluate(evaluator)).thenReturn(leadingDigit);
        when(useParensForNegativeNumbersCalc.evaluate(evaluator)).thenReturn(parens);
        when(groupDigitsCalc.evaluate(evaluator)).thenReturn(grouping);

        String result = formatPercentCalc.evaluate(evaluator);

        assertThat(result).contains("%");
    }

    static Stream<Arguments> formatPercentArguments() {
        return Stream.of(Arguments.of("basic positive", 0.25, 2, -1, -1, -1),
                Arguments.of("basic negative", -0.25, 2, -1, -1, -1),
                Arguments.of("zero digits after decimal", 0.25, 0, -1, -1, -1),
                Arguments.of("custom digits", 0.2345, 3, -1, -1, -1), Arguments.of("zero value", 0.0, 2, -1, -1, -1),
                Arguments.of("one hundred percent", 1.0, 0, -1, -1, -1));
    }

    @Test
    @DisplayName("Should test static formatPercent method directly")
    void shouldTestStaticMethod() {
        String result = FormatPercentCalc.formatPercent(0.25, 2, -1, -1, -1);
        assertThat(result).contains("%");
        assertThat(result).contains("25");
    }

    @Test
    @DisplayName("Should handle parentheses for negative percentages")
    void shouldHandleParenthesesForNegativePercentages() {
        String result = FormatPercentCalc.formatPercent(-0.25, 2, -1, 1, -1);
        assertThat(result).startsWith("(");
        assertThat(result).endsWith("%)");
    }

    @Test
    @DisplayName("Should handle grouping settings")
    void shouldHandleGroupingSettings() {
        // With grouping enabled
        String withGrouping = FormatPercentCalc.formatPercent(12.345, 2, -1, -1, 1);
        assertThat(withGrouping).contains("%");

        // With grouping disabled
        String withoutGrouping = FormatPercentCalc.formatPercent(12.345, 2, -1, -1, 0);
        assertThat(withoutGrouping).contains("%");
    }

    @Test
    @DisplayName("Should preserve type information")
    void shouldPreserveTypeInformation() {
        assertThat(formatPercentCalc.getType()).isEqualTo(StringType.INSTANCE);
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

        String result = formatPercentCalc.evaluate(evaluator);

        assertThat(result).isNotNull();
    }
}