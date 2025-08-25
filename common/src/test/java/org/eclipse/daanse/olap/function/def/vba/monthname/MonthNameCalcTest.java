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
package org.eclipse.daanse.olap.function.def.vba.monthname;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.text.DateFormatSymbols;
import java.util.Locale;
import java.util.stream.Stream;

import org.eclipse.daanse.olap.api.Evaluator;
import org.eclipse.daanse.olap.api.calc.BooleanCalc;
import org.eclipse.daanse.olap.api.calc.IntegerCalc;
import org.eclipse.daanse.olap.api.type.StringType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class MonthNameCalcTest {

    private MonthNameCalc monthNameCalc;
    private IntegerCalc monthCalc;
    private BooleanCalc abbreviateCalc;
    private Evaluator evaluator;
    private static final DateFormatSymbols symbols = new DateFormatSymbols(Locale.getDefault());

    @BeforeEach
    void setUp() {
        monthCalc = mock(IntegerCalc.class);
        abbreviateCalc = mock(BooleanCalc.class);
        evaluator = mock(Evaluator.class);
        monthNameCalc = new MonthNameCalc(StringType.INSTANCE, monthCalc, abbreviateCalc);
    }

    @ParameterizedTest(name = "{0}: MonthName({1}, {2}) = {3}")
    @MethodSource("monthNameArguments")
    @DisplayName("Should return correct month names")
    void shouldReturnCorrectMonthNames(String testName, Integer month, Boolean abbreviate, String expectedName) {
        when(monthCalc.evaluate(evaluator)).thenReturn(month);
        when(abbreviateCalc.evaluate(evaluator)).thenReturn(abbreviate);

        String result = monthNameCalc.evaluate(evaluator);

        assertThat(result).isEqualTo(expectedName);
    }

    static Stream<Arguments> monthNameArguments() {
        DateFormatSymbols symbols = new DateFormatSymbols(Locale.getDefault());
        String[] fullMonths = symbols.getMonths();
        String[] shortMonths = symbols.getShortMonths();

        return Stream.of(
                // Full month names
                Arguments.of("January full", 1, false, fullMonths[0]),
                Arguments.of("February full", 2, false, fullMonths[1]),
                Arguments.of("March full", 3, false, fullMonths[2]),
                Arguments.of("April full", 4, false, fullMonths[3]), Arguments.of("May full", 5, false, fullMonths[4]),
                Arguments.of("June full", 6, false, fullMonths[5]), Arguments.of("July full", 7, false, fullMonths[6]),
                Arguments.of("August full", 8, false, fullMonths[7]),
                Arguments.of("September full", 9, false, fullMonths[8]),
                Arguments.of("October full", 10, false, fullMonths[9]),
                Arguments.of("November full", 11, false, fullMonths[10]),
                Arguments.of("December full", 12, false, fullMonths[11]),

                // Abbreviated month names
                Arguments.of("January abbreviated", 1, true, shortMonths[0]),
                Arguments.of("February abbreviated", 2, true, shortMonths[1]),
                Arguments.of("March abbreviated", 3, true, shortMonths[2]),
                Arguments.of("April abbreviated", 4, true, shortMonths[3]),
                Arguments.of("May abbreviated", 5, true, shortMonths[4]),
                Arguments.of("June abbreviated", 6, true, shortMonths[5]),
                Arguments.of("July abbreviated", 7, true, shortMonths[6]),
                Arguments.of("August abbreviated", 8, true, shortMonths[7]),
                Arguments.of("September abbreviated", 9, true, shortMonths[8]),
                Arguments.of("October abbreviated", 10, true, shortMonths[9]),
                Arguments.of("November abbreviated", 11, true, shortMonths[10]),
                Arguments.of("December abbreviated", 12, true, shortMonths[11]));
    }

    @Test
    @DisplayName("Should handle out of range month values")
    void shouldHandleOutOfRangeMonthValues() {
        when(monthCalc.evaluate(evaluator)).thenReturn(0); // Month before January
        when(abbreviateCalc.evaluate(evaluator)).thenReturn(false);

        assertThatThrownBy(() -> monthNameCalc.evaluate(evaluator)).isInstanceOf(ArrayIndexOutOfBoundsException.class);
    }

    @Test
    @DisplayName("Should handle month 13")
    @Disabled
    void shouldHandleMonth13() {
        when(monthCalc.evaluate(evaluator)).thenReturn(13); // Month after December
        when(abbreviateCalc.evaluate(evaluator)).thenReturn(false);

        assertThatThrownBy(() -> monthNameCalc.evaluate(evaluator)).isInstanceOf(ArrayIndexOutOfBoundsException.class);
    }

    @Test
    @DisplayName("Should handle negative month values")
    void shouldHandleNegativeMonthValues() {
        when(monthCalc.evaluate(evaluator)).thenReturn(-1);
        when(abbreviateCalc.evaluate(evaluator)).thenReturn(false);

        assertThatThrownBy(() -> monthNameCalc.evaluate(evaluator)).isInstanceOf(ArrayIndexOutOfBoundsException.class);
    }

    @Test
    @DisplayName("Should test static monthName method directly")
    void shouldTestStaticMonthNameMethodDirectly() {
        String fullName = MonthNameCalc.monthName(1, false);
        String abbreviatedName = MonthNameCalc.monthName(1, true);

        assertThat(fullName).isEqualTo(symbols.getMonths()[0]);
        assertThat(abbreviatedName).isEqualTo(symbols.getShortMonths()[0]);
        assertThat(abbreviatedName.length()).isLessThanOrEqualTo(fullName.length());
    }

    @Test
    @DisplayName("Should verify all months are accessible")
    void shouldVerifyAllMonthsAreAccessible() {
        String[] fullMonths = symbols.getMonths();
        String[] shortMonths = symbols.getShortMonths();

        for (int month = 1; month <= 12; month++) {
            when(monthCalc.evaluate(evaluator)).thenReturn(month);

            // Test full names
            when(abbreviateCalc.evaluate(evaluator)).thenReturn(false);
            String fullResult = monthNameCalc.evaluate(evaluator);
            assertThat(fullResult).isEqualTo(fullMonths[month - 1]);
            assertThat(fullResult).isNotEmpty();

            // Test abbreviated names
            when(abbreviateCalc.evaluate(evaluator)).thenReturn(true);
            String shortResult = monthNameCalc.evaluate(evaluator);
            assertThat(shortResult).isEqualTo(shortMonths[month - 1]);
            assertThat(shortResult).isNotEmpty();
        }
    }

    @Test
    @DisplayName("Should handle null month")
    void shouldHandleNullMonth() {
        when(monthCalc.evaluate(evaluator)).thenReturn(null);
        when(abbreviateCalc.evaluate(evaluator)).thenReturn(false);

        assertThatThrownBy(() -> monthNameCalc.evaluate(evaluator)).isInstanceOf(NullPointerException.class);
    }

    @Test
    @DisplayName("Should handle null abbreviate flag")
    void shouldHandleNullAbbreviateFlag() {
        when(monthCalc.evaluate(evaluator)).thenReturn(1);
        when(abbreviateCalc.evaluate(evaluator)).thenReturn(null);

        assertThatThrownBy(() -> monthNameCalc.evaluate(evaluator)).isInstanceOf(NullPointerException.class);
    }

    @Test
    @DisplayName("Should preserve type information")
    void shouldPreserveTypeInformation() {
        assertThat(monthNameCalc.getType()).isEqualTo(StringType.INSTANCE);
    }

    @Test
    @DisplayName("Should verify month name consistency")
    void shouldVerifyMonthNameConsistency() {
        // Verify that the same month always returns the same name
        when(monthCalc.evaluate(evaluator)).thenReturn(7); // July
        when(abbreviateCalc.evaluate(evaluator)).thenReturn(false);

        String result1 = monthNameCalc.evaluate(evaluator);
        String result2 = monthNameCalc.evaluate(evaluator);

        assertThat(result1).isEqualTo(result2);
        assertThat(result1).isEqualTo(symbols.getMonths()[6]);
    }

    @Test
    @DisplayName("Should verify abbreviated names are shorter or equal")
    void shouldVerifyAbbreviatedNamesAreShorterOrEqual() {
        for (int month = 1; month <= 12; month++) {
            String fullName = MonthNameCalc.monthName(month, false);
            String shortName = MonthNameCalc.monthName(month, true);

            assertThat(shortName.length()).isLessThanOrEqualTo(fullName.length());
            assertThat(shortName).isNotEmpty();
            assertThat(fullName).isNotEmpty();
        }
    }

    @Test
    @DisplayName("Should verify month names are locale dependent")
    void shouldVerifyMonthNamesAreLocaleDependent() {
        // This test verifies that month names come from DateFormatSymbols
        // The actual names depend on the system locale
        String januaryFull = MonthNameCalc.monthName(1, false);
        String januaryShort = MonthNameCalc.monthName(1, true);

        assertThat(januaryFull).isNotNull();
        assertThat(januaryShort).isNotNull();
        assertThat(januaryFull).isNotEmpty();
        assertThat(januaryShort).isNotEmpty();

        // Verify they match what DateFormatSymbols provides
        assertThat(januaryFull).isEqualTo(symbols.getMonths()[0]);
        assertThat(januaryShort).isEqualTo(symbols.getShortMonths()[0]);
    }
}