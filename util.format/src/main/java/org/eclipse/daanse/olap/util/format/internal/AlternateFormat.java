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
import java.math.RoundingMode;
import java.time.LocalDateTime;

/**
 * AlternateFormat is an implementation of {@link BasicFormat} which allows a
 * different format to be used for different kinds of values. If there are 4
 * formats, purposes are as follows:
 * <ol>
 * <li>positive numbers</li>
 * <li>negative numbers</li>
 * <li>zero</li>
 * <li>null values</li>
 * </ol>
 *
 * If there are fewer than 4 formats, the first is used as a fall-back. See the
 * <a href="http://apostate.com/vb-format-syntax">the visual basic format
 * specification</a> for more details.
 */
public final class AlternateFormat extends BasicFormat {
    final FormatAlternates alternates;

    public AlternateFormat(FormatAlternates alternates) {
        this.alternates = alternates;
    }

    @Override
    public void formatNull(StringBuilder sb) {
        if (alternates.nullFormat().isPresent()) {
            alternates.nullFormat().get().format(0, sb);
        } else {
            super.formatNull(sb);
        }
    }

    @Override
    public void format(double n, StringBuilder sb) {
        BasicFormat positiveFormat = alternates.positiveFormat();

        if (n == 0 && alternates.zeroFormat().isPresent()) {
            alternates.zeroFormat().get().format(n, sb);
            return;
        }

        if (n < 0) {
            if (alternates.negativeFormat().isPresent()) {
                BasicFormat negativeFormat = alternates.negativeFormat().get();
                if (negativeFormat.isApplicableTo(n)) {
                    negativeFormat.format(-n, sb);
                } else {
                    // Does not fit into the negative mask, so use the
                    // zero mask, if there is one. For example,
                    // "#.0;(#.0);Nil" formats -0.0001 as "Nil".
                    BasicFormat fallback = alternates.zeroFormat().orElse(positiveFormat);
                    fallback.format(n, sb);
                }
            } else {
                if (positiveFormat.isApplicableTo(n)) {
                    // Special case for format strings with style,
                    // like "|#|style='red'". JPivot expects the
                    // '-' to immediately precede the digits, viz
                    // "|-6|style='red'|", not "-|6|style='red'|".
                    //
                    // But for other formats, we want '-' to precede
                    // literals, viz '-$6' not '$-6'. This is SSAS
                    // 2005's behavior too.
                    int size = sb.length();
                    sb.append('-');
                    positiveFormat.format(-n, sb);
                    if (sb.substring(size, size + 2).equals("-|")) {
                        sb.setCharAt(size, '|');
                        sb.setCharAt(size + 1, '-');
                    }
                } else {
                    positiveFormat.format(0d, sb);
                }
            }
            return;
        }

        positiveFormat.format(n, sb);
    }

    @Override
    public void format(long n, StringBuilder sb) {
        BasicFormat positiveFormat = alternates.positiveFormat();

        if (n == 0 && alternates.zeroFormat().isPresent()) {
            alternates.zeroFormat().get().format(n, sb);
            return;
        }

        if (n < 0) {
            if (alternates.negativeFormat().isPresent()) {
                BasicFormat negativeFormat = alternates.negativeFormat().get();
                if (negativeFormat.isApplicableTo(n)) {
                    negativeFormat.format(-n, sb);
                } else {
                    // Does not fit into the negative mask, so use the
                    // zero mask, if there is one. For example,
                    // "#.0;(#.0);Nil" formats -0.0001 as "Nil".
                    BasicFormat fallback = alternates.zeroFormat().orElse(positiveFormat);
                    fallback.format(n, sb);
                }
            } else {
                if (positiveFormat.isApplicableTo(n)) {
                    // Special case for format strings with style,
                    // like "|#|style='red'". JPivot expects the
                    // '-' to immediately precede the digits, viz
                    // "|-6|style='red'|", not "-|6|style='red'|".
                    //
                    // But for other formats, we want '-' to precede
                    // literals, viz '-$6' not '$-6'. This is SSAS
                    // 2005's behavior too.
                    final int size = sb.length();
                    sb.append('-');
                    positiveFormat.format(-n, sb);
                    if (sb.substring(size, size + 2).equals("-|")) {
                        sb.setCharAt(size, '|');
                        sb.setCharAt(size + 1, '-');
                    }
                } else {
                    positiveFormat.format(0L, sb);
                }
            }
            return;
        }

        positiveFormat.format(n, sb);
    }

    @Override
    public void format(String s, StringBuilder sb) {
        // since it is not a number, ignore all format strings
        sb.append(s);
    }

    @Override
    public void format(LocalDateTime ldt, StringBuilder sb) {
        // We're passing a date to a numeric format string. Convert it to
        // the number of days since 1900.
        BigDecimal bigDecimal = daysSince1900(ldt);
        format(bigDecimal.doubleValue(), sb);
    }

    static BigDecimal daysSince1900(LocalDateTime ldt) {
        final long dayOfYear = ldt.getDayOfYear();
        final long year = ldt.getYear();
        long yearForLeap = year;
        if (ldt.getMonthValue() < 3) {
            --yearForLeap;
        }
        final long leapDays = (yearForLeap - 1900) / 4 - (yearForLeap - 1900) / 100 + (yearForLeap - 2000) / 400;
        final long days = (year - 1900) * 365 + leapDays + dayOfYear + 2; // kludge factor to agree with Excel
        final long millis = ldt.getHour() * 3600000L + ldt.getMinute() * 60000 + ldt.getSecond() * 1000
                + ldt.getNano() / 1_000_000;
        return BigDecimal.valueOf(days)
                .add(BigDecimal.valueOf(millis).divide(BigDecimal.valueOf(86400000), 8, RoundingMode.FLOOR));
    }
}
