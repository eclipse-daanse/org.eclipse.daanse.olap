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
package org.eclipse.daanse.olap.element.db;

import java.util.List;

import org.eclipse.daanse.olap.api.element.db.DatabaseColumn;
import org.eclipse.daanse.olap.api.element.db.DatabaseKey;

/** An immutable key: a name and the columns it spans, in key order. */
public class DatabaseKeyImpl implements DatabaseKey {

    private final String name;
    private final List<DatabaseColumn> columns;

    public DatabaseKeyImpl(String name, List<DatabaseColumn> columns) {
        this.name = name;
        this.columns = List.copyOf(columns);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public List<DatabaseColumn> getColumns() {
        return columns;
    }
}
