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
package org.eclipse.daanse.olap.util.format.internal;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Locale;

/**
 * JavaFormat is an implementation of {@link BasicFormat} which prints values
 * using Java's default formatting for their type. null values appear as an
 * empty string.
 */
public sealed class JavaFormat extends BasicFormat permits NumericFormat {
    private final NumberFormat numberFormat;
    private final DateTimeFormatter dateTimeFormatter;

    public JavaFormat(Locale locale) {
        this.numberFormat = NumberFormat.getNumberInstance(locale);
        this.dateTimeFormatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT, FormatStyle.MEDIUM)
                .withLocale(locale).withZone(ZoneId.systemDefault());
    }

    @Override
    public void format(double d, StringBuilder sb) {
        sb.append(numberFormat.format(d));
    }

    @Override
    public void format(BigDecimal d, StringBuilder sb) {
        sb.append(numberFormat.format(d));
    }

    @Override
    public void format(long n, StringBuilder sb) {
        sb.append(numberFormat.format(n));
    }

    @Override
    public void format(String s, StringBuilder sb) {
        sb.append(s);
    }

    @Override
    public void format(LocalDateTime ldt, StringBuilder sb) {
        sb.append(dateTimeFormatter.format(ldt));
    }
}
