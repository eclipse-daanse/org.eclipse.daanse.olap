/*
* Copyright (c) 2026 Contributors to the Eclipse Foundation.
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
package org.eclipse.daanse.olap.api.element.db;

import java.util.List;

/**
 * A key of a {@link DatabaseTable}: a name and the columns it spans, in key
 * order.
 * <p>
 * A primary key is declared in the catalog's CWM schema, or derived from the
 * mapping: the column a hierarchy names as its primary key is one. Either way
 * a client asking the schema rowsets sees a key, which is what a tool needs to
 * draw a table with its identity.
 */
public interface DatabaseKey {

    /** The constraint name; never {@code null}, generated when the schema names none. */
    String getName();

    /** The columns of the key, in order. */
    List<DatabaseColumn> getColumns();
}
