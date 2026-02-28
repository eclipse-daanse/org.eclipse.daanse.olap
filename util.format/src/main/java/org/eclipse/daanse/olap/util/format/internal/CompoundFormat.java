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
import java.util.List;

/**
 * CompoundFormat is an implementation of {@link BasicFormat} where each value
 * is formatted by applying a sequence of format elements. Each format element
 * is itself a format.
 *
 * @see AlternateFormat
 */
public final class CompoundFormat extends BasicFormat {
    final List<BasicFormat> formats;

    public CompoundFormat(List<BasicFormat> formats) {
        this.formats = List.copyOf(formats);
        assert formats.size() >= 2;
    }

    @Override
    public void format(double v, StringBuilder sb) {
        for (BasicFormat format : formats) {
            format.format(v, sb);
        }
    }

    @Override
    public void format(long v, StringBuilder sb) {
        for (BasicFormat format : formats) {
            format.format(v, sb);
        }
    }

    @Override
    public void format(String v, StringBuilder sb) {
        for (BasicFormat format : formats) {
            format.format(v, sb);
        }
    }

    @Override
    public void format(LocalDateTime v, StringBuilder sb) {
        for (BasicFormat format : formats) {
            format.format(v, sb);
        }
    }

    @Override
    public boolean isApplicableTo(double n) {
        for (BasicFormat format : formats) {
            if (!format.isApplicableTo(n)) {
                return false;
            }
        }
        return true;
    }
}
