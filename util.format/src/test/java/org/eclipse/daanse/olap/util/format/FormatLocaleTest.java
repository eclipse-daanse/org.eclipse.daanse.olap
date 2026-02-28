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
package org.eclipse.daanse.olap.util.format;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Locale;

import org.junit.jupiter.api.Test;

class FormatLocaleTest {

    @Test
    void testCreateLocaleUS() {
        FormatLocale locale = Format.createLocale(Locale.US);
        assertThat(locale).isNotNull();
        assertThat(locale.thousandSeparator()).isEqualTo(',');
        assertThat(locale.decimalPlaceholder()).isEqualTo('.');
        assertThat(locale.currencySymbol()).isEqualTo("$");
    }

    @Test
    void testCreateLocaleGermany() {
        FormatLocale locale = Format.createLocale(Locale.GERMANY);
        assertThat(locale).isNotNull();
        assertThat(locale.thousandSeparator()).isEqualTo('.');
        assertThat(locale.decimalPlaceholder()).isEqualTo(',');
    }

    @Test
    void testCreateLocaleFrance() {
        FormatLocale locale = Format.createLocale(Locale.FRANCE);
        assertThat(locale).isNotNull();
        // French uses space as thousand separator and comma as decimal
        assertThat(locale.decimalPlaceholder()).isEqualTo(',');
    }

    @Test
    void testCreateLocaleJapan() {
        FormatLocale locale = Format.createLocale(Locale.JAPAN);
        assertThat(locale).isNotNull();
        assertThat(locale.daysOfWeekShort()).hasSize(8);
        assertThat(locale.daysOfWeekLong()).hasSize(8);
        assertThat(locale.monthsShort()).hasSize(13);
        assertThat(locale.monthsLong()).hasSize(13);
    }

    @Test
    void testCreateLocaleWithCharParams() {
        FormatLocale locale = Format.createLocale('.', ',', "-", ":", "EUR", "#,##0.00EUR", null, null, null, null,
                Locale.GERMAN);
        assertThat(locale).isNotNull();
        assertThat(locale.thousandSeparator()).isEqualTo('.');
        assertThat(locale.decimalPlaceholder()).isEqualTo(',');
        assertThat(locale.dateSeparator()).isEqualTo("-");
        assertThat(locale.timeSeparator()).isEqualTo(":");
        assertThat(locale.currencySymbol()).isEqualTo("EUR");
    }

    @Test
    void testCreateLocaleDefaultsForNullArrays() {
        FormatLocale locale = Format.createLocale('\0', '\0', null, null, null, null, null, null, null, null, null);
        assertThat(locale).isNotNull();
        // Should default to English
        assertThat(locale.thousandSeparator()).isEqualTo(',');
        assertThat(locale.decimalPlaceholder()).isEqualTo('.');
        assertThat(locale.dateSeparator()).isEqualTo("/");
        assertThat(locale.timeSeparator()).isEqualTo(":");
        assertThat(locale.currencySymbol()).isEqualTo("$");
    }

    @Test
    void testGetBestFormatLocaleNull() {
        FormatLocale locale = Format.getBestFormatLocale(null);
        assertThat(locale).isNotNull();
        // Should return US locale
        assertThat(locale.thousandSeparator()).isEqualTo(',');
    }

    @Test
    void testGetBestFormatLocaleUS() {
        FormatLocale locale = Format.getBestFormatLocale(Locale.US);
        assertThat(locale).isNotNull();
    }

    @Test
    void testAmPmStringsDefaultToEnglish() {
        FormatLocale locale = Format.createLocale('\0', '\0', null, null, null, null, null, null, null, null, null);
        assertThat(locale.amPmStrings()).containsExactly("AM", "PM");
    }

    @Test
    void testAmPmStringsFromJdkUS() {
        FormatLocale locale = Format.createLocale(Locale.US);
        assertThat(locale.amPmStrings()).containsExactly("AM", "PM");
    }

    @Test
    void testGermanDateSeparatorFromPattern() {
        FormatLocale locale = Format.createLocale(Locale.GERMANY);
        assertThat(locale.dateSeparator()).isEqualTo(".");
    }

    @Test
    void testGermanTimeSeparator() {
        FormatLocale locale = Format.createLocale(Locale.GERMANY);
        assertThat(locale.timeSeparator()).isEqualTo(":");
    }

    @Test
    void testLocaleUSInitializedViaJDK() {
        FormatLocale locale = Format.getBestFormatLocale(Locale.US);
        assertThat(locale.thousandSeparator()).isEqualTo(',');
        assertThat(locale.decimalPlaceholder()).isEqualTo('.');
        assertThat(locale.dateSeparator()).isEqualTo("/");
        assertThat(locale.timeSeparator()).isEqualTo(":");
        assertThat(locale.currencySymbol()).isEqualTo("$");
        assertThat(locale.amPmStrings()).containsExactly("AM", "PM");
    }

    @Test
    void testGetFormatLocaleRegistered() {
        // Create and register a locale
        FormatLocale created = Format.createLocale(Locale.ITALY);
        // Now retrieve it
        FormatLocale retrieved = Format.getFormatLocale(Locale.ITALY);
        assertThat(retrieved).isNotNull();
        assertThat(retrieved).isSameAs(created);
    }
}
