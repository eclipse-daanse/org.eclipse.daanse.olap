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

import java.util.List;
import java.util.Optional;

/**
 * Holds the up-to-four alternate formats used by {@link AlternateFormat}.
 *
 * <p>
 * In a VB-style format string separated by semicolons, the sections have these
 * meanings:
 * <ol>
 * <li><b>positive</b> – format for positive numbers (always present)</li>
 * <li><b>negative</b> – format for negative numbers (optional)</li>
 * <li><b>zero</b> – format for zero values (optional)</li>
 * <li><b>null</b> – format for null values (optional)</li>
 * </ol>
 *
 * <p>
 * When fewer than four sections are given, the positive format is used as a
 * fallback.
 */
public record FormatAlternates(BasicFormat positiveFormat, Optional<BasicFormat> negativeFormat,
        Optional<BasicFormat> zeroFormat, Optional<BasicFormat> nullFormat) {

    /**
     * Creates a {@code FormatAlternates} from an ordered list of formats. The list
     * must have at least one element. Elements beyond the first may be {@code null}
     * to indicate "not specified".
     */
    public static FormatAlternates of(List<BasicFormat> formats) {
        if (formats.isEmpty()) {
            throw new IllegalArgumentException("formats list must not be empty");
        }
        return new FormatAlternates(formats.get(0), optionalAt(formats, 1), optionalAt(formats, 2),
                optionalAt(formats, 3));
    }

    private static Optional<BasicFormat> optionalAt(List<BasicFormat> list, int index) {
        return index < list.size() ? Optional.ofNullable(list.get(index)) : Optional.empty();
    }
}
