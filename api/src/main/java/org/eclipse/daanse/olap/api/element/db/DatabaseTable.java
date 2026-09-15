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
package org.eclipse.daanse.olap.api.element.db;

import java.util.List;
import java.util.Optional;

public interface DatabaseTable {

    String getName();

    List<DatabaseColumn> getDbColumns();

    String getDescription();

    /**
     * The table's primary key, when the schema declares one or the mapping
     * implies one. A table with several candidate keys reports the first.
     */
    default Optional<DatabaseKey> getPrimaryKey() {
        return Optional.empty();
    }

    /** The foreign keys this table's columns hold, referencing other tables. */
    default List<DatabaseForeignKey> getForeignKeys() {
        return List.of();
    }
}
