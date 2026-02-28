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

import java.time.LocalDateTime;
import java.util.Locale;

/**
 * StringFormat is an implementation of {@link BasicFormat} which formats values
 * as strings, optionally converting to upper or lower case.
 */
public final class StringFormat extends BasicFormat {
    final StringCase stringCase;
    final String literal;
    final JavaFormat javaFormat;

    public StringFormat(StringCase stringCase, String literal, Locale locale) {
        assert stringCase != null;
        this.stringCase = stringCase;
        this.literal = literal;
        this.javaFormat = new JavaFormat(locale);
    }

    @Override
    public void format(String s, StringBuilder sb) {
        s = switch (stringCase) {
        case UPPER -> s.toUpperCase();
        case LOWER -> s.toLowerCase();
        };
        sb.append(s);
    }

    @Override
    public void format(double d, StringBuilder sb) {
        final int x = sb.length();
        javaFormat.format(d, sb);
        String s = sb.substring(x);
        sb.setLength(x);
        format(s, sb);
    }

    @Override
    public void format(long n, StringBuilder sb) {
        final int x = sb.length();
        javaFormat.format(n, sb);
        String s = sb.substring(x);
        sb.setLength(x);
        format(s, sb);
    }

    @Override
    public void format(LocalDateTime ldt, StringBuilder sb) {
        final int x = sb.length();
        javaFormat.format(ldt, sb);
        String s = sb.substring(x);
        sb.setLength(x);
        format(s, sb);
    }
}
