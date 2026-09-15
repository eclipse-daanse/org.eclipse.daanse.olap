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
 * A foreign key: columns of one table that reference the key columns of
 * another.
 * <p>
 * Declared in the catalog's CWM schema, or derived from the mapping: a
 * dimension connector names the fact table's foreign key column, and the
 * dimension's hierarchy names the primary key column of the table it reads -
 * that pair is the join the engine runs, and so it is the relationship a
 * client is shown.
 */
public interface DatabaseForeignKey extends DatabaseKey {

    /** The table whose key is referenced. */
    DatabaseTable getReferencedTable();

    /** The referenced key's columns, positionally matching {@link #getColumns()}. */
    List<DatabaseColumn> getReferencedColumns();

    /** The referenced key's name, or {@code null} when the referenced table has none. */
    default String getReferencedKeyName() {
        return getReferencedTable() == null || getReferencedTable().getPrimaryKey().isEmpty() ? null
                : getReferencedTable().getPrimaryKey().get().getName();
    }

    /** The OLE DB rule text for updates of the referenced key: {@code NO ACTION}, {@code CASCADE}, ... */
    default String getUpdateRule() {
        return "NO ACTION";
    }

    /** The OLE DB rule text for deletes of the referenced row. */
    default String getDeleteRule() {
        return "NO ACTION";
    }
}
