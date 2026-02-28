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

import static org.eclipse.daanse.olap.util.format.internal.FormatConstants.FORMAT_AMPM;
import static org.eclipse.daanse.olap.util.format.internal.FormatConstants.FORMAT_C;
import static org.eclipse.daanse.olap.util.format.internal.FormatConstants.FORMAT_D;
import static org.eclipse.daanse.olap.util.format.internal.FormatConstants.FORMAT_DD;
import static org.eclipse.daanse.olap.util.format.internal.FormatConstants.FORMAT_DDD;
import static org.eclipse.daanse.olap.util.format.internal.FormatConstants.FORMAT_DDDD;
import static org.eclipse.daanse.olap.util.format.internal.FormatConstants.FORMAT_DDDDD;
import static org.eclipse.daanse.olap.util.format.internal.FormatConstants.FORMAT_DDDDDD;
import static org.eclipse.daanse.olap.util.format.internal.FormatConstants.FORMAT_H;
import static org.eclipse.daanse.olap.util.format.internal.FormatConstants.FORMAT_HH;
import static org.eclipse.daanse.olap.util.format.internal.FormatConstants.FORMAT_HH_UPPER;
import static org.eclipse.daanse.olap.util.format.internal.FormatConstants.FORMAT_LOWER_AM_SOLIDUS_PM;
import static org.eclipse.daanse.olap.util.format.internal.FormatConstants.FORMAT_LOWER_A_SOLIDUS_P;
import static org.eclipse.daanse.olap.util.format.internal.FormatConstants.FORMAT_M;
import static org.eclipse.daanse.olap.util.format.internal.FormatConstants.FORMAT_MM;
import static org.eclipse.daanse.olap.util.format.internal.FormatConstants.FORMAT_MMMMM_LOWER;
import static org.eclipse.daanse.olap.util.format.internal.FormatConstants.FORMAT_MMMMM_UPPER;
import static org.eclipse.daanse.olap.util.format.internal.FormatConstants.FORMAT_MMMM_LOWER;
import static org.eclipse.daanse.olap.util.format.internal.FormatConstants.FORMAT_MMMM_UPPER;
import static org.eclipse.daanse.olap.util.format.internal.FormatConstants.FORMAT_MMM_LOWER;
import static org.eclipse.daanse.olap.util.format.internal.FormatConstants.FORMAT_MMM_UPPER;
import static org.eclipse.daanse.olap.util.format.internal.FormatConstants.FORMAT_MM_UPPER;
import static org.eclipse.daanse.olap.util.format.internal.FormatConstants.FORMAT_M_UPPER;
import static org.eclipse.daanse.olap.util.format.internal.FormatConstants.FORMAT_N;
import static org.eclipse.daanse.olap.util.format.internal.FormatConstants.FORMAT_NN;
import static org.eclipse.daanse.olap.util.format.internal.FormatConstants.FORMAT_Q;
import static org.eclipse.daanse.olap.util.format.internal.FormatConstants.FORMAT_S;
import static org.eclipse.daanse.olap.util.format.internal.FormatConstants.FORMAT_SS;
import static org.eclipse.daanse.olap.util.format.internal.FormatConstants.FORMAT_TTTTT;
import static org.eclipse.daanse.olap.util.format.internal.FormatConstants.FORMAT_UPPER_AM_SOLIDUS_PM;
import static org.eclipse.daanse.olap.util.format.internal.FormatConstants.FORMAT_UPPER_A_SOLIDUS_P;
import static org.eclipse.daanse.olap.util.format.internal.FormatConstants.FORMAT_W;
import static org.eclipse.daanse.olap.util.format.internal.FormatConstants.FORMAT_WW;
import static org.eclipse.daanse.olap.util.format.internal.FormatConstants.FORMAT_Y;
import static org.eclipse.daanse.olap.util.format.internal.FormatConstants.FORMAT_YY;
import static org.eclipse.daanse.olap.util.format.internal.FormatConstants.FORMAT_YYYY;

import java.time.LocalDateTime;
import java.time.temporal.WeekFields;

import org.eclipse.daanse.olap.util.format.FormatLocale;

/**
 * VbDateFormat is an element of a {@link CompoundFormat} which has a value when
 * applied to a {@link LocalDateTime} object. (Values of type
 * {@link java.util.Date} and {@link java.util.Calendar} are automatically
 * converted to {@link LocalDateTime} by
 * {@link BasicFormat#format(java.util.Date, StringBuilder)} and
 * {@link BasicFormat#format(java.util.Calendar, StringBuilder)}.)
 *
 * In a typical use of this class, a format string such as "m/d/yy" is parsed
 * into VbDateFormat objects for "m", "d", and "yy", and {@link LiteralFormat}
 * objects for "/". A {@link CompoundFormat} object is created to bind them
 * together.
 *
 * Renamed from DateFormat to VbDateFormat to avoid clash with
 * {@link java.text.DateFormat}.
 */
public final class VbDateFormat extends FallbackFormat {
    private final FormatLocale locale;
    private boolean twelveHourClock;

    public VbDateFormat(int code, String s, FormatLocale locale, boolean twelveHourClock) {
        super(code, s);
        this.locale = locale;
        this.twelveHourClock = twelveHourClock;
    }

    @Override
    public FormatType getFormatType() {
        return FormatType.DATE;
    }

    public void setTwelveHourClock(boolean twelveHourClock) {
        this.twelveHourClock = twelveHourClock;
    }

    @Override
    public void format(LocalDateTime ldt, StringBuilder sb) {
        format(code, ldt, sb);
    }

    /**
     * Converts a java.time DayOfWeek (Monday=1 .. Sunday=7) to Calendar-style
     * day-of-week (Sunday=1 .. Saturday=7), which matches the FormatLocale
     * daysOfWeek array indexing.
     */
    private static int calendarStyleDow(LocalDateTime ldt) {
        return ldt.getDayOfWeek().getValue() % 7 + 1;
    }

    private int hour(LocalDateTime ldt) {
        return twelveHourClock ? ldt.getHour() % 12 : ldt.getHour();
    }

    private static boolean isAm(LocalDateTime ldt) {
        return ldt.getHour() < 12;
    }

    private static void appendPadded(StringBuilder sb, int value) {
        if (value < 10) {
            sb.append('0');
        }
        sb.append(value);
    }

    private void format(int code, LocalDateTime ldt, StringBuilder sb) {
        switch (code) {
        case FORMAT_C -> {
            boolean dateSet = true; // LocalDateTime always has a date part
            boolean timeSet = !(ldt.getHour() == 0 && ldt.getMinute() == 0 && ldt.getSecond() == 0);
            if (dateSet) {
                format(FORMAT_DDDDD, ldt, sb);
            }
            if (dateSet && timeSet) {
                sb.append(' ');
            }
            if (timeSet) {
                format(FORMAT_TTTTT, ldt, sb);
            }
        }
        case FORMAT_D -> sb.append(ldt.getDayOfMonth());
        case FORMAT_DD -> appendPadded(sb, ldt.getDayOfMonth());
        case FORMAT_DDD -> sb.append(locale.daysOfWeekShort().get(calendarStyleDow(ldt)));
        case FORMAT_DDDD -> sb.append(locale.daysOfWeekLong().get(calendarStyleDow(ldt)));
        case FORMAT_DDDDD -> {
            // Short date: m/d/yy
            format(FORMAT_M, ldt, sb);
            sb.append(locale.dateSeparator());
            format(FORMAT_D, ldt, sb);
            sb.append(locale.dateSeparator());
            format(FORMAT_YY, ldt, sb);
        }
        case FORMAT_DDDDDD -> {
            // Long date: mmmm dd, yyyy
            format(FORMAT_MMMM_UPPER, ldt, sb);
            sb.append(" ");
            format(FORMAT_DD, ldt, sb);
            sb.append(", ");
            format(FORMAT_YYYY, ldt, sb);
        }
        case FORMAT_W -> sb.append(calendarStyleDow(ldt));
        case FORMAT_WW -> sb.append(ldt.get(WeekFields.SUNDAY_START.weekOfYear()));
        case FORMAT_M, FORMAT_M_UPPER -> sb.append(ldt.getMonthValue());
        case FORMAT_MM, FORMAT_MM_UPPER -> appendPadded(sb, ldt.getMonthValue());
        case FORMAT_MMM_LOWER, FORMAT_MMM_UPPER -> sb.append(locale.monthsShort().get(ldt.getMonthValue() - 1)); // 0-based
                                                                                                                 // array
        case FORMAT_MMMM_LOWER, FORMAT_MMMM_UPPER, FORMAT_MMMMM_LOWER, FORMAT_MMMMM_UPPER ->
            sb.append(locale.monthsLong().get(ldt.getMonthValue() - 1)); // 0-based array
        case FORMAT_Q -> sb.append((ldt.getMonthValue() - 1) / 3 + 1);
        case FORMAT_Y -> sb.append(ldt.getDayOfYear());
        case FORMAT_YY -> appendPadded(sb, ldt.getYear() % 100);
        case FORMAT_YYYY -> sb.append(ldt.getYear());
        case FORMAT_H -> sb.append(hour(ldt));
        case FORMAT_HH, FORMAT_HH_UPPER -> appendPadded(sb, hour(ldt));
        case FORMAT_N -> sb.append(ldt.getMinute());
        case FORMAT_NN -> appendPadded(sb, ldt.getMinute());
        case FORMAT_S -> sb.append(ldt.getSecond());
        case FORMAT_SS -> appendPadded(sb, ldt.getSecond());
        case FORMAT_TTTTT -> {
            // Default time: h:mm:ss
            format(FORMAT_H, ldt, sb);
            sb.append(locale.timeSeparator());
            format(FORMAT_NN, ldt, sb);
            sb.append(locale.timeSeparator());
            format(FORMAT_SS, ldt, sb);
        }
        case FORMAT_AMPM -> sb.append(isAm(ldt) ? locale.amPmStrings().get(0) : locale.amPmStrings().get(1));
        case FORMAT_UPPER_AM_SOLIDUS_PM -> sb.append(isAm(ldt) ? "AM" : "PM");
        case FORMAT_LOWER_AM_SOLIDUS_PM -> sb.append(isAm(ldt) ? "am" : "pm");
        case FORMAT_UPPER_A_SOLIDUS_P -> sb.append(isAm(ldt) ? "A" : "P");
        case FORMAT_LOWER_A_SOLIDUS_P -> sb.append(isAm(ldt) ? "a" : "p");
        default -> throw new IllegalStateException("Unexpected format code");
        }
    }
}
