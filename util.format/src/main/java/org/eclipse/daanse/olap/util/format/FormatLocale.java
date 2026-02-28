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

import java.util.List;
import java.util.Locale;

/**
 * A FormatLocale contains all information necessary to format objects based
 * upon the locale of the end-user.
 *
 * <p>
 * Instances are created via {@link Format#createLocale(java.util.Locale)} or
 * {@link Format#createLocale(char, char, String, String, String, String, java.util.List, java.util.List, java.util.List, java.util.List, java.util.Locale)}.
 *
 * @since 0.0.1
 */
public record FormatLocale(char thousandSeparator, char decimalPlaceholder, String dateSeparator, String timeSeparator,
        String currencySymbol, String currencyFormat, List<String> daysOfWeekShort, List<String> daysOfWeekLong,
        List<String> monthsShort, List<String> monthsLong, List<String> amPmStrings, Locale locale) {

    // English defaults
    static final char THOUSAND_SEPARATOR_EN = ',';
    static final char DECIMAL_PLACEHOLDER_EN = '.';
    static final String DATE_SEPARATOR_EN = "/";
    static final String TIME_SEPARATOR_EN = ":";
    static final String CURRENCY_SYMBOL_EN = "$";
    static final String CURRENCY_FORMAT_EN = "$#,##0.00";
    static final List<String> DAYS_OF_WEEK_SHORT_EN = List.of("", "Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat");
    static final List<String> DAYS_OF_WEEK_LONG_EN = List.of("", "Sunday", "Monday", "Tuesday", "Wednesday", "Thursday",
            "Friday", "Saturday");
    static final List<String> MONTHS_SHORT_EN = List.of("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep",
            "Oct", "Nov", "Dec", "");
    static final List<String> MONTHS_LONG_EN = List.of("January", "February", "March", "April", "May", "June", "July",
            "August", "September", "October", "November", "December", "");
    static final List<String> AM_PM_STRINGS_EN = List.of("AM", "PM");

    public FormatLocale {
        if (thousandSeparator == '\0') {
            thousandSeparator = THOUSAND_SEPARATOR_EN;
        }
        if (decimalPlaceholder == '\0') {
            decimalPlaceholder = DECIMAL_PLACEHOLDER_EN;
        }
        if (dateSeparator == null) {
            dateSeparator = DATE_SEPARATOR_EN;
        }
        if (timeSeparator == null) {
            timeSeparator = TIME_SEPARATOR_EN;
        }
        if (currencySymbol == null) {
            currencySymbol = CURRENCY_SYMBOL_EN;
        }
        if (currencyFormat == null) {
            currencyFormat = CURRENCY_FORMAT_EN;
        }
        daysOfWeekShort = daysOfWeekShort != null ? List.copyOf(daysOfWeekShort) : DAYS_OF_WEEK_SHORT_EN;
        daysOfWeekLong = daysOfWeekLong != null ? List.copyOf(daysOfWeekLong) : DAYS_OF_WEEK_LONG_EN;
        monthsShort = monthsShort != null ? List.copyOf(monthsShort) : MONTHS_SHORT_EN;
        monthsLong = monthsLong != null ? List.copyOf(monthsLong) : MONTHS_LONG_EN;
        amPmStrings = amPmStrings != null ? List.copyOf(amPmStrings) : AM_PM_STRINGS_EN;
        if (daysOfWeekShort.size() != 8 || daysOfWeekLong.size() != 8 || monthsShort.size() != 13
                || monthsLong.size() != 13) {
            throw new IllegalArgumentException("Format: day or month array has incorrect length");
        }
    }
}
