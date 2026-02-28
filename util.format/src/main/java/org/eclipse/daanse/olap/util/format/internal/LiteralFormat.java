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

import static org.eclipse.daanse.olap.util.format.internal.FormatConstants.FORMAT_LITERAL;

import java.time.LocalDateTime;

/**
 * LiteralFormat is an implementation of {@link BasicFormat} which prints a
 * constant value, regardless of the value to be formatted.
 *
 * @see CompoundFormat
 */
public final class LiteralFormat extends BasicFormat {
    private final String s;

    public LiteralFormat(String s) {
        this(FORMAT_LITERAL, s);
    }

    public LiteralFormat(int code, String s) {
        super(code);
        this.s = s;
    }

    public String getLiteral() {
        return s;
    }

    @Override
    public void format(double d, StringBuilder sb) {
        sb.append(s);
    }

    @Override
    public void format(long n, StringBuilder sb) {
        sb.append(s);
    }

    @Override
    public void format(String str, StringBuilder sb) {
        sb.append(s);
    }

    @Override
    public void format(LocalDateTime ldt, StringBuilder sb) {
        sb.append(s);
    }
}
