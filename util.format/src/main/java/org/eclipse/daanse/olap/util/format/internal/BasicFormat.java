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
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.Date;

/**
 * BasicFormat is the interface implemented by the classes which do all the
 * work. Whereas {@link org.eclipse.daanse.olap.util.Format} has only one method
 * for formatting, this class provides methods for several primitive types. To
 * make it easy to combine formatting objects, all methods write to a
 * {@link StringBuilder}.
 *
 * The base implementation of most of these methods throws; there is no
 * requirement that a derived class implements all of these methods. It is up to
 * the format parser to ensure that, for example, the
 * {@link #format(double, StringBuilder)} method is never called for date
 * formats.
 */
public sealed class BasicFormat
        permits JavaFormat, LiteralFormat, FallbackFormat, StringFormat, CompoundFormat, AlternateFormat {
    public final int code;

    public BasicFormat() {
        this(0);
    }

    public BasicFormat(int code) {
        this.code = code;
    }

    public FormatType getFormatType() {
        return null;
    }

    public void formatNull(StringBuilder sb) {
        // SSAS formats null values as the empty string. However, SQL Server
        // Management Studio's pivot table formats them as "(null)", so many
        // people believe that this is the server's behavior.
    }

    public void format(double d, StringBuilder sb) {
        throw new UnsupportedOperationException("Cannot format double with " + getClass().getSimpleName());
    }

    public void format(long n, StringBuilder sb) {
        throw new UnsupportedOperationException("Cannot format long with " + getClass().getSimpleName());
    }

    public void format(String s, StringBuilder sb) {
        throw new UnsupportedOperationException("Cannot format String with " + getClass().getSimpleName());
    }

    public void format(Date date, StringBuilder sb) {
        format(toLocalDateTime(date), sb);
    }

    public void format(BigDecimal bigDecimal, StringBuilder sb) {
        format(bigDecimal.doubleValue(), sb);
    }

    public void format(Calendar calendar, StringBuilder sb) {
        format(toLocalDateTime(calendar), sb);
    }

    public void format(LocalDate localDate, StringBuilder sb) {
        format(localDate.atStartOfDay(), sb);
    }

    public void format(Instant instant, StringBuilder sb) {
        format(toLocalDateTime(instant), sb);
    }

    /**
     * Formats a {@link LocalDateTime} value. This is the canonical date/time
     * formatting method — {@link #format(Date, StringBuilder)},
     * {@link #format(Calendar, StringBuilder)} and
     * {@link #format(Instant, StringBuilder)} all convert to {@code LocalDateTime}
     * and delegate here.
     */
    public void format(LocalDateTime localDateTime, StringBuilder sb) {
        throw new UnsupportedOperationException("Cannot format LocalDateTime with " + getClass().getSimpleName());
    }

    /**
     * Returns whether this format can handle a given value.
     *
     * Usually returns true; one notable exception is a format for negative numbers
     * which causes the number to be underflow to zero and therefore be ineligible
     * for the negative format.
     *
     * @param n value
     * @return Whether this format is applicable for a given value
     */
    public boolean isApplicableTo(double n) {
        return true;
    }

    /**
     * Returns whether this format can handle a given value.
     *
     * Usually returns true; one notable exception is a format for negative numbers
     * which causes the number to be underflow to zero and therefore be ineligible
     * for the negative format.
     *
     * @param n value
     * @return Whether this format is applicable for a given value
     */
    public boolean isApplicableTo(long n) {
        return true;
    }

    /**
     * Converts a {@link Date} (including {@code java.sql.Date},
     * {@code java.sql.Time}, {@code java.sql.Timestamp}) to {@link LocalDateTime}
     * using the system default time zone.
     */
    static LocalDateTime toLocalDateTime(Date date) {
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(date.getTime()), ZoneId.systemDefault());
    }

    /**
     * Converts a {@link Calendar} to {@link LocalDateTime} using the calendar's
     * time zone.
     */
    static LocalDateTime toLocalDateTime(Calendar calendar) {
        return LocalDateTime.ofInstant(calendar.toInstant(), calendar.getTimeZone().toZoneId());
    }

    /**
     * Converts an {@link Instant} to {@link LocalDateTime} using the system default
     * time zone.
     */
    static LocalDateTime toLocalDateTime(Instant instant) {
        return LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
    }
}
