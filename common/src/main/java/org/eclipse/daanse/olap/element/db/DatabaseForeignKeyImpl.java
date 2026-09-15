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
import org.eclipse.daanse.olap.api.element.db.DatabaseForeignKey;
import org.eclipse.daanse.olap.api.element.db.DatabaseTable;

/**
 * An immutable foreign key. The referenced key name and the update and delete
 * rules fall back to the interface defaults when not given.
 */
public class DatabaseForeignKeyImpl extends DatabaseKeyImpl implements DatabaseForeignKey {

    private final DatabaseTable referencedTable;
    private final List<DatabaseColumn> referencedColumns;
    private final String referencedKeyName;
    private final String updateRule;
    private final String deleteRule;

    public DatabaseForeignKeyImpl(String name, List<DatabaseColumn> columns, DatabaseTable referencedTable,
            List<DatabaseColumn> referencedColumns) {
        this(name, columns, referencedTable, referencedColumns, null, null, null);
    }

    public DatabaseForeignKeyImpl(String name, List<DatabaseColumn> columns, DatabaseTable referencedTable,
            List<DatabaseColumn> referencedColumns, String referencedKeyName) {
        this(name, columns, referencedTable, referencedColumns, referencedKeyName, null, null);
    }

    public DatabaseForeignKeyImpl(String name, List<DatabaseColumn> columns, DatabaseTable referencedTable,
            List<DatabaseColumn> referencedColumns, String referencedKeyName, String updateRule, String deleteRule) {
        super(name, columns);
        this.referencedTable = referencedTable;
        this.referencedColumns = List.copyOf(referencedColumns);
        this.referencedKeyName = referencedKeyName;
        this.updateRule = updateRule;
        this.deleteRule = deleteRule;
    }

    @Override
    public DatabaseTable getReferencedTable() {
        return referencedTable;
    }

    @Override
    public List<DatabaseColumn> getReferencedColumns() {
        return referencedColumns;
    }

    @Override
    public String getReferencedKeyName() {
        return referencedKeyName != null ? referencedKeyName : DatabaseForeignKey.super.getReferencedKeyName();
    }

    @Override
    public String getUpdateRule() {
        return updateRule != null ? updateRule : DatabaseForeignKey.super.getUpdateRule();
    }

    @Override
    public String getDeleteRule() {
        return deleteRule != null ? deleteRule : DatabaseForeignKey.super.getDeleteRule();
    }
}
