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
package org.eclipse.daanse.olap.xmla.connector.discover;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.eclipse.daanse.xmla.api.XmlaRequest;
import org.eclipse.daanse.olap.api.Context;
import org.eclipse.daanse.olap.api.element.Catalog;
import org.eclipse.daanse.olap.api.element.Cube;
import org.eclipse.daanse.olap.api.element.Dimension;
import org.eclipse.daanse.olap.api.element.Hierarchy;
import org.eclipse.daanse.olap.api.element.Level;
import org.eclipse.daanse.olap.api.element.Member;
import org.eclipse.daanse.olap.api.element.db.DatabaseSchema;
import org.eclipse.daanse.olap.api.element.db.DatabaseTable;
import org.eclipse.daanse.olap.xmla.connector.ContextListSupplyer;
import org.eclipse.daanse.xmla.model.rowset.relational.DbschemaCatalogsRow;
import org.eclipse.daanse.xmla.model.rowset.relational.DbschemaColumnsRow;
import org.eclipse.daanse.xmla.model.rowset.relational.DbschemaProviderTypesRow;
import org.eclipse.daanse.xmla.model.rowset.relational.DbschemaTablesInfoRow;
import org.eclipse.daanse.xmla.model.rowset.relational.DbschemaSchemataRow;
import org.eclipse.daanse.xmla.model.rowset.relational.DbschemaTablesRow;
import org.eclipse.daanse.xmla.model.rowset.relational.RowsetRelationalFactory;
import org.eclipse.daanse.xmla.api.RestrictionValues;
import org.eclipse.emf.ecore.EObject;

/**
 * The DBSCHEMA_* rowsets: the OLE DB relational surface.
 * <p>
 * Ported from the bridge's {@code DBSchemaDiscoverService} and the DBSCHEMA
 * half of its {@code Utils}. The classic rowsets — catalogs, schemata, tables —
 * read the OLAP metadata the way the bridge did: cubes as TABLEs, levels as
 * SYSTEM TABLEs named {@code cube:hierarchy:level}, database tables as SCHEMA
 * entries. The CWM relational rowsets (constraints, keys, indexes, procedures,
 * views) wait on {@code Catalog.getRelationalSchemas()} landing in the api.
 * Until then they are modelled but unserved, and an unserved rowset answers
 * empty rather than faulting.
 */
public class DbSchemaDiscover {

    private static final RowsetRelationalFactory FACTORY = RowsetRelationalFactory.eINSTANCE;

    private static final String TABLE = "TABLE";
    private static final String SYSTEM_TABLE = "SYSTEM TABLE";
    private static final String SCHEMA = "SCHEMA";

    private final ContextListSupplyer contexts;

    public DbSchemaDiscover(ContextListSupplyer contexts) {
        this.contexts = contexts;
    }

    // --- DBSCHEMA_COLUMNS, ported from Utils.getDbSchemaColumnsResponseRow ---

    public List<EObject> columns(RestrictionValues restrictions, XmlaRequest caller) {
        Optional<String> catalogName = restrictions.value("TABLE_CATALOG").or(restrictions::catalogProperty);
        Optional<String> tableName = restrictions.value("TABLE_NAME");
        Optional<String> columnName = restrictions.value("COLUMN_NAME");
        Optional<String> olapType = restrictions.value("COLUMN_OLAP_TYPE");

        List<EObject> result = new ArrayList<>();
        for (Catalog catalog : catalogsInScope(catalogName, caller)) {
            List<Cube> cubes = new ArrayList<>(catalog.getCubes());
            cubes.sort((left, right) -> left.getName().compareTo(right.getName()));
            for (Cube cube : cubes) {
                if (tableName.isEmpty() || tableName.get().equals(cube.getName())) {
                    if (olapType.isEmpty() || "ATTRIBUTE".equals(olapType.get())) {
                        for (Dimension dimension : cube.getDimensions()) {
                            hierarchyColumns(catalog.getName(), cube, dimension, result);
                        }
                    }
                    if (olapType.isEmpty() || "MEASURE".equals(olapType.get())) {
                        measureColumns(cube, columnName, result);
                    }
                }
                if (olapType.isEmpty() || "SCHEMA".equals(olapType.get())) {
                    schemaColumns(catalog, tableName, columnName, result);
                }
            }
        }
        return result;
    }

    /**
     * Two columns per hierarchy, {@code <hierarchy>:(All)!NAME} and
     * {@code ...!UNIQUE_NAME}. The ordinal restarts at one for every dimension -
     * the bridge passed a boxed Integer around and the increments never made it
     * back to the caller; the numbering is part of what clients have seen, so it
     * stays.
     */
    private static void hierarchyColumns(String catalogName, Cube cube, Dimension dimension, List<EObject> result) {
        int ordinal = 1;
        for (Hierarchy hierarchy : dimension.getHierarchies()) {
            for (String suffix : new String[] { ":(All)!NAME", ":(All)!UNIQUE_NAME" }) {
                DbschemaColumnsRow row = FACTORY.createDbschemaColumnsRow();
                row.setTableCatalog(catalogName);
                row.setTableName(cube.getName());
                row.setColumnName(hierarchy.getName() + suffix);
                row.setOrdinalPosition((long) ordinal++);
                row.setColumnHasDefault(Boolean.FALSE);
                row.setIsNullable(Boolean.FALSE);
                row.setDataType(OleDbType.WSTR.dbTypeOrdinal());
                row.setCharacterMaximumLength(0L);
                row.setCharacterOctetLength(0L);
                result.add(row);
            }
        }
    }

    private static void measureColumns(Cube cube, Optional<String> columnName, List<EObject> result) {
        int ordinal = 1;
        for (Member measure : cube.getMeasures()) {
            String name = "Measures:" + measure.getName();
            if (columnName.isPresent() && !columnName.get().equals(name)) {
                continue;
            }
            DbschemaColumnsRow row = FACTORY.createDbschemaColumnsRow();
            row.setTableCatalog(cube.getName());
            row.setTableName(cube.getName());
            row.setColumnName(name);
            row.setOrdinalPosition((long) ordinal++);
            row.setColumnHasDefault(Boolean.FALSE);
            row.setIsNullable(Boolean.FALSE);
            row.setDataType(OleDbType.R8.dbTypeOrdinal());
            row.setCharacterMaximumLength(0L);
            row.setCharacterOctetLength(0L);
            row.setNumericPrecision(16);
            row.setNumericScale((short) 255);
            result.add(row);
        }
    }

    private static void schemaColumns(Catalog catalog, Optional<String> tableName, Optional<String> columnName,
            List<EObject> result) {
        List<? extends DatabaseSchema> schemas = catalog.getDatabaseSchemas();
        if (schemas == null) {
            return;
        }
        int ordinal = 1;
        for (DatabaseSchema schema : schemas) {
            if (schema.getDbTables() == null) {
                continue;
            }
            for (DatabaseTable table : schema.getDbTables()) {
                if (tableName.isPresent() && !tableName.get().equals(table.getName())) {
                    continue;
                }
                if (table.getDbColumns() == null) {
                    continue;
                }
                for (org.eclipse.daanse.olap.api.element.db.DatabaseColumn column : table.getDbColumns()) {
                    if (columnName.isPresent() && !columnName.get().equals(column.getName())) {
                        continue;
                    }
                    DbschemaColumnsRow row = FACTORY.createDbschemaColumnsRow();
                    row.setTableCatalog(catalog.getName());
                    row.setTableSchema(schema.getName());
                    row.setTableName(table.getName());
                    row.setColumnName(column.getName());
                    row.setOrdinalPosition((long) ordinal++);
                    row.setColumnHasDefault(Boolean.FALSE);
                    row.setIsNullable(Boolean.FALSE);
                    row.setDataType(OleDbType.R8.dbTypeOrdinal());
                    row.setCharacterMaximumLength(0L);
                    row.setCharacterOctetLength(0L);
                    row.setNumericPrecision(16);
                    row.setNumericScale((short) 255);
                    row.setColumnOlapType(SCHEMA);
                    result.add(row);
                }
            }
        }
    }

    // --- DBSCHEMA_PROVIDER_TYPES: the six OLE DB types this server speaks ---

    public List<EObject> providerTypes(RestrictionValues restrictions) {
        Optional<String> dataType = restrictions.value("DATA_TYPE");
        List<EObject> result = new ArrayList<>();
        providerType(OleDbType.I4, 8L, dataType, result);
        providerType(OleDbType.R8, 16L, dataType, result);
        providerType(OleDbType.CY, 8L, dataType, result);
        providerType(OleDbType.BOOL, 1L, dataType, result);
        providerType(OleDbType.I8, 16L, dataType, result);
        providerType(OleDbType.WSTR, 255L, dataType, result);
        return result;
    }

    private static void providerType(OleDbType type, long columnSize, Optional<String> wanted, List<EObject> result) {
        if (wanted.isPresent() && Integer.parseInt(wanted.get()) != type.dbTypeOrdinal()) {
            return;
        }
        DbschemaProviderTypesRow row = FACTORY.createDbschemaProviderTypesRow();
        row.setTypeName(type.userName());
        row.setDataType(type.dbTypeOrdinal());
        row.setColumnSize(columnSize);
        row.setIsNullable(Boolean.TRUE);
        if (type == OleDbType.WSTR) {
            row.setLiteralPrefix("\"");
            row.setLiteralSuffix("\"");
            row.setCaseSensitive(Boolean.FALSE);
        } else {
            row.setUnsignedAttribute(Boolean.FALSE);
        }
        row.setFixedPrecScale(Boolean.FALSE);
        row.setAutoUniqueValue(Boolean.FALSE);
        row.setIsLong(Boolean.FALSE);
        row.setBestMatch(Boolean.TRUE);
        result.add(row);
    }

    // --- DBSCHEMA_TABLES_INFO: one row per cube, as the bridge answered it ---

    public List<EObject> tablesInfo(RestrictionValues restrictions, XmlaRequest caller) {
        Optional<String> catalogName = restrictions.value("TABLE_CATALOG").or(restrictions::catalogProperty);
        List<EObject> result = new ArrayList<>();
        for (Catalog catalog : catalogsInScope(catalogName, caller)) {
            List<Cube> cubes = new ArrayList<>(catalog.getCubes());
            cubes.sort((left, right) -> left.getName().compareTo(right.getName()));
            for (Cube cube : cubes) {
                String description = cube.getDescription();
                if (description == null) {
                    description = catalog.getName() + " - " + cube.getName() + " Cube";
                }
                DbschemaTablesInfoRow row = FACTORY.createDbschemaTablesInfoRow();
                row.setTableCatalog(catalog.getName());
                row.setTableName(cube.getName());
                row.setTableType(TABLE);
                row.setBookmarks(Boolean.FALSE);
                // SQL Server answers 1000000 for every table, and so did the bridge.
                row.setCardinality(BigInteger.valueOf(1_000_000L));
                row.setDescription(description);
                result.add(row);
            }
        }
        return result;
    }

    // --- DBSCHEMA_CATALOGS ---

    public List<EObject> catalogs(RestrictionValues restrictions) {
        Optional<String> catalogName = restrictions.value("CATALOG_NAME");

        List<EObject> result = new ArrayList<>();
        if (catalogName.isPresent()) {
            Optional<Context<?>> context = contexts.getContext(catalogName.get());
            if (context.isPresent()) {
                result.add(catalogRow(context.get()));
            }
        } else {
            for (Context<?> context : contexts.getContexts()) {
                result.add(catalogRow(context));
            }
        }
        return result;
    }

    private static EObject catalogRow(Context<?> context) {
        DbschemaCatalogsRow row = FACTORY.createDbschemaCatalogsRow();
        row.setCatalogName(context.getName());
        context.getDescription().ifPresent(row::setDescription);
        List<String> roles = context.getAccessRoles();
        if (roles != null) {
            row.setRoles(String.join(",", roles));
        }
        row.setDateModified(LocalDateTime.now());
        return row;
    }

    // --- DBSCHEMA_SCHEMATA ---

    public List<EObject> schemata(RestrictionValues restrictions, XmlaRequest caller) {
        Optional<String> catalogName = restrictions.value("CATALOG_NAME").or(restrictions::catalogProperty);
        Optional<String> schemaName = restrictions.value("SCHEMA_NAME");

        List<EObject> result = new ArrayList<>();
        for (Catalog catalog : catalogsInScope(catalogName, caller)) {
            List<? extends DatabaseSchema> schemas = catalog.getDatabaseSchemas();
            if (schemas == null) {
                continue;
            }
            for (DatabaseSchema schema : schemas) {
                if (schemaName.isPresent() && !schemaName.get().equals(schema.getName())) {
                    continue;
                }
                DbschemaSchemataRow row = FACTORY.createDbschemaSchemataRow();
                row.setCatalogName(catalog.getName());
                row.setSchemaName(schema.getName());
                row.setSchemaOwner("");
                result.add(row);
            }
        }
        return result;
    }

    // --- DBSCHEMA_TABLES, ported from Utils.getDbSchemaTablesResponseRow ---

    public List<EObject> tables(RestrictionValues restrictions, XmlaRequest caller) {
        Optional<String> catalogName = restrictions.value("TABLE_CATALOG").or(restrictions::catalogProperty);
        Optional<String> tableType = restrictions.value("TABLE_TYPE");

        List<EObject> result = new ArrayList<>();
        for (Catalog catalog : catalogsInScope(catalogName, caller)) {
            for (Cube cube : catalog.getCubes()) {
                String description = cube.getDescription();
                if (description == null) {
                    description = catalog.getName() + " - " + cube.getName() + " Cube";
                }
                if (isTableType(tableType, TABLE)) {
                    result.add(tableRow(catalog.getName(), null, cube.getName(), TABLE, description));
                }
                if (isTableType(tableType, SYSTEM_TABLE)) {
                    for (Dimension dimension : cube.getDimensions()) {
                        levelTables(catalog.getName(), cube, dimension, result);
                    }
                }
            }
            if (isTableType(tableType, SCHEMA)) {
                databaseTables(catalog, result);
            }
        }
        return result;
    }

    /**
     * Every level of every hierarchy, as a {@code cube:hierarchy:level} system
     * table.
     */
    private static void levelTables(String catalogName, Cube cube, Dimension dimension, List<EObject> result) {
        if (dimension == null) {
            return;
        }
        for (Hierarchy hierarchy : dimension.getHierarchies()) {
            if (hierarchy.getLevels() == null) {
                continue;
            }
            for (Level level : hierarchy.getLevels()) {
                String hierarchyName = hierarchyName(hierarchy.getName(), dimension.getName());
                String tableName = cube.getName() + ':' + hierarchyName + ':' + level.getName();
                String description = level.getDescription();
                if (description == null) {
                    description = catalogName + " - " + cube.getName() + " Cube - " + hierarchyName + " Hierarchy - "
                            + level.getName() + " Level";
                }
                result.add(tableRow(catalogName, null, tableName, SYSTEM_TABLE, description));
            }
        }
    }

    private static void databaseTables(Catalog catalog, List<EObject> result) {
        List<? extends DatabaseSchema> schemas = catalog.getDatabaseSchemas();
        if (schemas == null) {
            return;
        }
        for (DatabaseSchema schema : schemas) {
            List<? extends DatabaseTable> tables = schema.getDbTables();
            if (tables == null) {
                continue;
            }
            for (DatabaseTable table : tables) {
                result.add(
                        tableRow(catalog.getName(), schema.getName(), table.getName(), SCHEMA, table.getDescription()));
            }
        }
    }

    private static EObject tableRow(String catalogName, String schemaName, String tableName, String tableType,
            String description) {
        DbschemaTablesRow row = FACTORY.createDbschemaTablesRow();
        row.setTableCatalog(catalogName);
        if (schemaName != null) {
            row.setTableSchema(schemaName);
        }
        row.setTableName(tableName);
        row.setTableType(tableType);
        if (description != null) {
            row.setDescription(description);
        }
        return row;
    }

    private static boolean isTableType(Optional<String> wanted, String type) {
        return wanted.isEmpty() || wanted.get().equals(type);
    }

    private static String hierarchyName(String hierarchyName, String dimensionName) {
        if (!hierarchyName.equals(dimensionName)) {
            return dimensionName + "." + hierarchyName;
        }
        return hierarchyName;
    }

    /**
     * The catalogs in scope: the named one when the server has it, all of them
     * otherwise.
     */
    private List<Catalog> catalogsInScope(Optional<String> catalogName, XmlaRequest caller) {
        if (catalogName.isPresent()) {
            Optional<Catalog> catalog = contexts.tryGetFirstByName(catalogName.get(), caller);
            if (catalog.isPresent()) {
                return List.of(catalog.get());
            }
            return List.of();
        }
        return contexts.get(caller);
    }
}
