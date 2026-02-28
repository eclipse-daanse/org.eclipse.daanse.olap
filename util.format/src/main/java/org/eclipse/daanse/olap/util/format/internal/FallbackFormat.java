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

/**
 * FallbackFormat catches un-handled datatypes and prints the original format
 * string. Better than giving an error. Abstract base class for
 * {@link VbDateFormat}.
 */
public sealed abstract class FallbackFormat extends BasicFormat permits VbDateFormat {
    final String token;

    public FallbackFormat(int code, String token) {
        super(code);
        this.token = token;
    }

    @Override
    public void format(double d, StringBuilder sb) {
        sb.append(token);
    }

    @Override
    public void format(long n, StringBuilder sb) {
        sb.append(token);
    }

    @Override
    public void format(String s, StringBuilder sb) {
        sb.append(token);
    }

    @Override
    public void format(LocalDateTime ldt, StringBuilder sb) {
        sb.append(token);
    }
}
