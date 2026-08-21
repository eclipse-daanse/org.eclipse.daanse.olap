/*
 * Copyright (c) 2022 Contributors to the Eclipse Foundation.
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
package org.eclipse.daanse.olap.api.query.component;

import org.eclipse.daanse.mdx.model.api.select.Allocation;

public non-sealed interface UpdateClause extends QueryComponent {

    Expression getTupleExp();

    Expression getValueExp();

    Allocation getAllocation();

    /**
     * The {@code BY} expression a weighted allocation is to use, or {@code null}
     * where the statement named none - which is every statement a recorded client
     * sends.
     * <p>
     * Carried rather than applied: the allocator weights by the cells' existing
     * values, which is what a weighted allocation without {@code BY} means. A
     * statement that does name one is accepted and allocated as if it had not.
     */
    Expression getWeight();

}
