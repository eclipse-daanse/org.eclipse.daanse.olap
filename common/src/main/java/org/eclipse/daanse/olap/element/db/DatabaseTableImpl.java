/*
 * Copyright (c) 2024 Contributors to the Eclipse Foundation.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   SmartCity Jena, Stefan Bischof - initial
 *
 */
package org.eclipse.daanse.olap.element.db;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.eclipse.daanse.olap.api.element.db.DatabaseColumn;
import org.eclipse.daanse.olap.api.element.db.DatabaseForeignKey;
import org.eclipse.daanse.olap.api.element.db.DatabaseKey;
import org.eclipse.daanse.olap.api.element.db.DatabaseTable;

/**
 * A table of the catalog's database schema. Built single-threaded while the
 * catalog loads and read-only afterwards; the setters are for the loader.
 */
public class DatabaseTableImpl implements DatabaseTable {

	private String name;
	private String description;

	private List<DatabaseColumn> dbColumns;
	private DatabaseKey primaryKey;
	private final List<DatabaseForeignKey> foreignKeys = new ArrayList<>();

	public List<DatabaseColumn> getDbColumns() {
		return dbColumns;
	}

	public void setDbColumns(List<DatabaseColumn> dbColumns) {
		this.dbColumns = dbColumns;
	}

	@Override
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	@Override
	public Optional<DatabaseKey> getPrimaryKey() {
		return Optional.ofNullable(primaryKey);
	}

	public void setPrimaryKey(DatabaseKey primaryKey) {
		this.primaryKey = primaryKey;
	}

	/** Sets the primary key only when none is known yet; a declared key wins over a derived one. */
	public void setPrimaryKeyIfAbsent(DatabaseKey primaryKey) {
		if (this.primaryKey == null) {
			this.primaryKey = primaryKey;
		}
	}

	@Override
	public List<DatabaseForeignKey> getForeignKeys() {
		return List.copyOf(foreignKeys);
	}

	/**
	 * Adds a foreign key unless one with the same referenced table and the same
	 * column names is already known: a declared and a derived key for one join
	 * are one relationship.
	 */
	public void addForeignKey(DatabaseForeignKey foreignKey) {
		for (DatabaseForeignKey known : foreignKeys) {
			if (known.getReferencedTable() == foreignKey.getReferencedTable()
					&& sameColumns(known.getColumns(), foreignKey.getColumns())) {
				return;
			}
		}
		foreignKeys.add(foreignKey);
	}

	private static boolean sameColumns(List<DatabaseColumn> a, List<DatabaseColumn> b) {
		if (a.size() != b.size()) {
			return false;
		}
		for (int i = 0; i < a.size(); i++) {
			if (!a.get(i).getName().equals(b.get(i).getName())) {
				return false;
			}
		}
		return true;
	}

}
