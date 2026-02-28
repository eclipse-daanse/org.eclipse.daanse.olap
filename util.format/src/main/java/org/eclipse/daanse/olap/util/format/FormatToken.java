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

import static org.eclipse.daanse.olap.util.format.internal.FormatConstants.DATE;
import static org.eclipse.daanse.olap.util.format.internal.FormatConstants.NUMERIC;
import static org.eclipse.daanse.olap.util.format.internal.FormatConstants.SPECIAL;
import static org.eclipse.daanse.olap.util.format.internal.FormatConstants.STRING;

import org.eclipse.daanse.olap.util.format.internal.FormatType;

/**
 * Represents a parsed token from a VB-style format string. Tokens identify
 * format elements such as digit placeholders, date components, string
 * modifiers, and literal text.
 *
 * <p>
 * Instances are obtained via {@link Format#getTokenList()}.
 *
 * @since 0.0.1
 */
public record FormatToken(int code, int flags, String tokenValue, FormatType formatType) {

    public FormatToken(int code, int flags, String tokenValue) {
        this(code, flags, tokenValue, computeFormatType(flags));
    }

    private static FormatType computeFormatType(int flags) {
        if ((flags & NUMERIC) == NUMERIC) {
            return FormatType.NUMERIC;
        } else if ((flags & DATE) == DATE) {
            return FormatType.DATE;
        } else if ((flags & STRING) == STRING) {
            return FormatType.STRING;
        }
        return null;
    }

    public boolean compatibleWith(FormatType formatType) {
        return formatType == null || this.formatType == null || formatType == this.formatType;
    }

    boolean isSpecial() {
        return (flags & SPECIAL) == SPECIAL;
    }

    public boolean isNumeric() {
        return (flags & NUMERIC) == NUMERIC;
    }

    public boolean isDate() {
        return (flags & DATE) == DATE;
    }

    public boolean isString() {
        return (flags & STRING) == STRING;
    }
}
