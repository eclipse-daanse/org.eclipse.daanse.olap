/*
 * Copyright (c) 2026 Contributors to the Eclipse Foundation.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Stefan Bischof (bipolis.org) - initial
 */
package org.eclipse.daanse.olap.api.function;

/**
 * Marks the function that lifts a measure out of a virtual cube's unrelated dimensions
 * ({@code ValidMeasure}).
 *
 * <p>The core needs to recognise such a call without knowing the concrete definition:
 * a calculated member containing one may not be rolled up like an ordinary measure.
 * Implementing this interface is the whole contract; it declares no methods.
 */
public interface ValidMeasureFunction extends FunctionDefinition {
    // marker
}
