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
package org.eclipse.daanse.olap.xmla.bridge.discover;

import static org.eclipse.daanse.olap.xmla.bridge.discover.Utils.getDbSchemaColumnsResponseRow;
import static org.eclipse.daanse.olap.xmla.bridge.discover.Utils.getDbSchemaSchemataResponseRow;
import static org.eclipse.daanse.olap.xmla.bridge.discover.Utils.getDbSchemaSourceTablesResponseRow;
import static org.eclipse.daanse.olap.xmla.bridge.discover.Utils.getDbSchemaTablesInfoResponseRow;
import static org.eclipse.daanse.olap.xmla.bridge.discover.Utils.getDbSchemaAssertionsResponseRow;
import static org.eclipse.daanse.olap.xmla.bridge.discover.Utils.getRoles;
import static org.eclipse.daanse.olap.xmla.bridge.discover.Utils.isDataTypeCond;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.eclipse.daanse.olap.api.Context;
import org.eclipse.daanse.olap.api.element.Catalog;
import org.eclipse.daanse.olap.xmla.bridge.ContextListSupplyer;
import org.eclipse.daanse.xmla.api.RequestMetaData;
import org.eclipse.daanse.xmla.api.XmlaConstants;
import org.eclipse.daanse.xmla.api.common.enums.ColumnOlapTypeEnum;
import org.eclipse.daanse.xmla.api.common.enums.LevelDbTypeEnum;
import org.eclipse.daanse.xmla.api.common.enums.TableTypeEnum;
import org.eclipse.daanse.xmla.api.discover.dbschema.assertions.DbSchemaAssertionsRequest;
import org.eclipse.daanse.xmla.api.discover.dbschema.assertions.DbSchemaAssertionsResponseRow;
import org.eclipse.daanse.xmla.api.discover.dbschema.catalogs.DbSchemaCatalogsRequest;
import org.eclipse.daanse.xmla.api.discover.dbschema.catalogs.DbSchemaCatalogsResponseRow;
import org.eclipse.daanse.xmla.api.discover.dbschema.charactersets.DbSchemaCharacterSetsRequest;
import org.eclipse.daanse.xmla.api.discover.dbschema.charactersets.DbSchemaCharacterSetsResponseRow;
import org.eclipse.daanse.xmla.api.discover.dbschema.checkconstraints.DbSchemaCheckConstraintsRequest;
import org.eclipse.daanse.xmla.api.discover.dbschema.checkconstraints.DbSchemaCheckConstraintsResponseRow;
import org.eclipse.daanse.xmla.api.discover.dbschema.checkconstraintsbytable.DbSchemaCheckConstraintsByTableRequest;
import org.eclipse.daanse.xmla.api.discover.dbschema.checkconstraintsbytable.DbSchemaCheckConstraintsByTableResponseRow;
import org.eclipse.daanse.xmla.api.discover.dbschema.collations.DbSchemaCollationsRequest;
import org.eclipse.daanse.xmla.api.discover.dbschema.collations.DbSchemaCollationsResponseRow;
import org.eclipse.daanse.xmla.api.discover.dbschema.columndomainusage.DbSchemaColumnDomainUsageRequest;
import org.eclipse.daanse.xmla.api.discover.dbschema.columndomainusage.DbSchemaColumnDomainUsageResponseRow;
import org.eclipse.daanse.xmla.api.discover.dbschema.columnprivileges.DbSchemaColumnPrivilegesRequest;
import org.eclipse.daanse.xmla.api.discover.dbschema.columnprivileges.DbSchemaColumnPrivilegesResponseRow;
import org.eclipse.daanse.xmla.api.discover.dbschema.columns.DbSchemaColumnsRequest;
import org.eclipse.daanse.xmla.api.discover.dbschema.columns.DbSchemaColumnsResponseRow;
import org.eclipse.daanse.xmla.api.discover.dbschema.constraintcolumnusage.DbSchemaConstraintColumnUsageRequest;
import org.eclipse.daanse.xmla.api.discover.dbschema.constraintcolumnusage.DbSchemaConstraintColumnUsageResponseRow;
import org.eclipse.daanse.xmla.api.discover.dbschema.constrainttableusage.DbSchemaConstraintTableUsageRequest;
import org.eclipse.daanse.xmla.api.discover.dbschema.constrainttableusage.DbSchemaConstraintTableUsageResponseRow;
import org.eclipse.daanse.xmla.api.discover.dbschema.foreignkeys.DbSchemaForeignKeysRequest;
import org.eclipse.daanse.xmla.api.discover.dbschema.foreignkeys.DbSchemaForeignKeysResponseRow;
import org.eclipse.daanse.xmla.api.discover.dbschema.indexes.DbSchemaIndexesRequest;
import org.eclipse.daanse.xmla.api.discover.dbschema.indexes.DbSchemaIndexesResponseRow;
import org.eclipse.daanse.xmla.api.discover.dbschema.keycolumnusage.DbSchemaKeyColumnUsageRequest;
import org.eclipse.daanse.xmla.api.discover.dbschema.keycolumnusage.DbSchemaKeyColumnUsageResponseRow;
import org.eclipse.daanse.xmla.api.discover.dbschema.primarykeys.DbSchemaPrimaryKeysRequest;
import org.eclipse.daanse.xmla.api.discover.dbschema.primarykeys.DbSchemaPrimaryKeysResponseRow;
import org.eclipse.daanse.xmla.api.discover.dbschema.procedurecolumns.DbSchemaProcedureColumnsRequest;
import org.eclipse.daanse.xmla.api.discover.dbschema.procedurecolumns.DbSchemaProcedureColumnsResponseRow;
import org.eclipse.daanse.xmla.api.discover.dbschema.procedureparameters.DbSchemaProcedureParametersRequest;
import org.eclipse.daanse.xmla.api.discover.dbschema.procedureparameters.DbSchemaProcedureParametersResponseRow;
import org.eclipse.daanse.xmla.api.discover.dbschema.procedures.DbSchemaProceduresRequest;
import org.eclipse.daanse.xmla.api.discover.dbschema.procedures.DbSchemaProceduresResponseRow;
import org.eclipse.daanse.xmla.api.discover.dbschema.providertypes.DbSchemaProviderTypesRequest;
import org.eclipse.daanse.xmla.api.discover.dbschema.providertypes.DbSchemaProviderTypesResponseRow;
import org.eclipse.daanse.xmla.api.discover.dbschema.referentialconstraints.DbSchemaReferentialConstraintsRequest;
import org.eclipse.daanse.xmla.api.discover.dbschema.referentialconstraints.DbSchemaReferentialConstraintsResponseRow;
import org.eclipse.daanse.xmla.api.discover.dbschema.schemata.DbSchemaSchemataRequest;
import org.eclipse.daanse.xmla.api.discover.dbschema.schemata.DbSchemaSchemataResponseRow;
import org.eclipse.daanse.xmla.api.discover.dbschema.sourcetables.DbSchemaSourceTablesRequest;
import org.eclipse.daanse.xmla.api.discover.dbschema.sourcetables.DbSchemaSourceTablesResponseRow;
import org.eclipse.daanse.xmla.api.discover.dbschema.sqllanguages.DbSchemaSqlLanguagesRequest;
import org.eclipse.daanse.xmla.api.discover.dbschema.sqllanguages.DbSchemaSqlLanguagesResponseRow;
import org.eclipse.daanse.xmla.api.discover.dbschema.statistics.DbSchemaStatisticsRequest;
import org.eclipse.daanse.xmla.api.discover.dbschema.statistics.DbSchemaStatisticsResponseRow;
import org.eclipse.daanse.xmla.api.discover.dbschema.tableconstraints.DbSchemaTableConstraintsRequest;
import org.eclipse.daanse.xmla.api.discover.dbschema.tableconstraints.DbSchemaTableConstraintsResponseRow;
import org.eclipse.daanse.xmla.api.discover.dbschema.tableprivileges.DbSchemaTablePrivilegesRequest;
import org.eclipse.daanse.xmla.api.discover.dbschema.tableprivileges.DbSchemaTablePrivilegesResponseRow;
import org.eclipse.daanse.xmla.api.discover.dbschema.tables.DbSchemaTablesRequest;
import org.eclipse.daanse.xmla.api.discover.dbschema.tables.DbSchemaTablesResponseRow;
import org.eclipse.daanse.xmla.api.discover.dbschema.tablesinfo.DbSchemaTablesInfoRequest;
import org.eclipse.daanse.xmla.api.discover.dbschema.tablesinfo.DbSchemaTablesInfoResponseRow;
import org.eclipse.daanse.xmla.api.discover.dbschema.tablestatistics.DbSchemaTableStatisticsRequest;
import org.eclipse.daanse.xmla.api.discover.dbschema.tablestatistics.DbSchemaTableStatisticsResponseRow;
import org.eclipse.daanse.xmla.api.discover.dbschema.translations.DbSchemaTranslationsRequest;
import org.eclipse.daanse.xmla.api.discover.dbschema.translations.DbSchemaTranslationsResponseRow;
import org.eclipse.daanse.xmla.api.discover.dbschema.usageprivileges.DbSchemaUsagePrivilegesRequest;
import org.eclipse.daanse.xmla.api.discover.dbschema.usageprivileges.DbSchemaUsagePrivilegesResponseRow;
import org.eclipse.daanse.xmla.api.discover.dbschema.viewcolumnusage.DbSchemaViewColumnUsageRequest;
import org.eclipse.daanse.xmla.api.discover.dbschema.viewcolumnusage.DbSchemaViewColumnUsageResponseRow;
import org.eclipse.daanse.xmla.api.discover.dbschema.views.DbSchemaViewsRequest;
import org.eclipse.daanse.xmla.api.discover.dbschema.views.DbSchemaViewsResponseRow;
import org.eclipse.daanse.xmla.api.discover.dbschema.viewtableusage.DbSchemaViewTableUsageRequest;
import org.eclipse.daanse.xmla.api.discover.dbschema.viewtableusage.DbSchemaViewTableUsageResponseRow;
import org.eclipse.daanse.xmla.model.record.discover.dbschema.catalogs.DbSchemaCatalogsResponseRowR;
import org.eclipse.daanse.xmla.model.record.discover.dbschema.providertypes.DbSchemaProviderTypesResponseRowR;

public class DBSchemaDiscoverService {

    private ContextListSupplyer contextsListSupplyer;

    public DBSchemaDiscoverService(ContextListSupplyer contextsListSupplyer) {
        this.contextsListSupplyer = contextsListSupplyer;
    }

    public List<DbSchemaCatalogsResponseRow> dbSchemaCatalogs(DbSchemaCatalogsRequest request, RequestMetaData metaData) {

        Optional<String> oCatalogName = request.restrictions().catalogName();
        if (oCatalogName.isPresent()) {
            Optional<Context> oContext = oCatalogName.flatMap(name -> contextsListSupplyer.getContext(name));
            if (oContext.isPresent()) {
                Context context = oContext.get();
                return List.of(dbSchemaCatalogsRow(context));
            }
        } else {
            return contextsListSupplyer.getContexts().stream().map(this::dbSchemaCatalogsRow).toList();
        }
        return List.of();
    }

    public DbSchemaCatalogsResponseRow dbSchemaCatalogsRow(Context catalog) {
        return new DbSchemaCatalogsResponseRowR(Optional.ofNullable(catalog.getName()), catalog.getDescription(),
                getRoles(catalog.getAccessRoles()), Optional.of(LocalDateTime.now()), Optional.empty(),
                Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(),
                Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty());
    }

    public List<DbSchemaColumnsResponseRow> dbSchemaColumns(DbSchemaColumnsRequest request, RequestMetaData metaData) {
        Optional<String> oCatalog = request.restrictions().tableCatalog();
        Optional<String> oTableSchema = request.restrictions().tableSchema();
        Optional<String> oTableName = request.restrictions().tableName();
        Optional<String> oColumnName = request.restrictions().columnName();
        Optional<ColumnOlapTypeEnum> oColumnOlapType = request.restrictions().columnOlapType();
        List<DbSchemaColumnsResponseRow> result = new ArrayList<>();
        if (oCatalog.isPresent()) {
            Optional<Catalog> oContext = oCatalog
                    .flatMap(name -> contextsListSupplyer.tryGetFirstByName(name, metaData.sessionId()));
            if (oContext.isPresent()) {
                Catalog catalog = oContext.get();
                result.addAll(
                        getDbSchemaColumnsResponseRow(catalog, oTableSchema, oTableName, oColumnName, oColumnOlapType));
            }
        } else {
            result.addAll(contextsListSupplyer.get(metaData.sessionId()).stream()
                    .map(c -> getDbSchemaColumnsResponseRow(c, oTableSchema, oTableName, oColumnName, oColumnOlapType))
                    .flatMap(Collection::stream).toList());
        }
        return result;
    }

    public List<DbSchemaProviderTypesResponseRow> dbSchemaProviderTypes(DbSchemaProviderTypesRequest request,
            RequestMetaData metaData) {
        List<DbSchemaProviderTypesResponseRow> result = new ArrayList<>();
        Optional<LevelDbTypeEnum> oLevelDbType = request.restrictions().dataType();

        if (isDataTypeCond(XmlaConstants.DBType.I4, oLevelDbType)) {
            result.add(new DbSchemaProviderTypesResponseRowR(Optional.of(XmlaConstants.DBType.I4.userName),
                    Optional.of(LevelDbTypeEnum.DBTYPE_I4), Optional.of(8), Optional.empty(), Optional.empty(),
                    Optional.empty(), Optional.of(true), Optional.empty(), Optional.empty(), Optional.of(false),
                    Optional.of(false), Optional.of(false), Optional.empty(), Optional.empty(), Optional.empty(),
                    Optional.empty(), Optional.empty(), Optional.empty(), Optional.of(false), Optional.of(true),
                    Optional.empty()));
        }

        // R8
        if (isDataTypeCond(XmlaConstants.DBType.R8, oLevelDbType)) {

            result.add(new DbSchemaProviderTypesResponseRowR(Optional.of(XmlaConstants.DBType.R8.userName),
                    Optional.of(LevelDbTypeEnum.DBTYPE_R8), Optional.of(16), Optional.empty(), Optional.empty(),
                    Optional.empty(), Optional.of(true), Optional.empty(), Optional.empty(), Optional.of(false),
                    Optional.of(false), Optional.of(false), Optional.empty(), Optional.empty(), Optional.empty(),
                    Optional.empty(), Optional.empty(), Optional.empty(), Optional.of(false), Optional.of(true),
                    Optional.empty()));
        }

        // CY
        if (isDataTypeCond(XmlaConstants.DBType.CY, oLevelDbType)) {
            result.add(new DbSchemaProviderTypesResponseRowR(Optional.of(XmlaConstants.DBType.CY.userName),
                    Optional.of(LevelDbTypeEnum.DBTYPE_CY), Optional.of(8), Optional.empty(), Optional.empty(),
                    Optional.empty(), Optional.of(true), Optional.empty(), Optional.empty(), Optional.of(false),
                    Optional.of(false), Optional.of(false), Optional.empty(), Optional.empty(), Optional.empty(),
                    Optional.empty(), Optional.empty(), Optional.empty(), Optional.of(false), Optional.of(true),
                    Optional.empty()));
        }

        // BOOL
        if (isDataTypeCond(XmlaConstants.DBType.BOOL, oLevelDbType)) {
            result.add(new DbSchemaProviderTypesResponseRowR(Optional.of(XmlaConstants.DBType.BOOL.userName),
                    Optional.of(LevelDbTypeEnum.DBTYPE_BOOL), Optional.of(1), Optional.empty(), Optional.empty(),
                    Optional.empty(), Optional.of(true), Optional.empty(), Optional.empty(), Optional.of(false),
                    Optional.of(false), Optional.of(false), Optional.empty(), Optional.empty(), Optional.empty(),
                    Optional.empty(), Optional.empty(), Optional.empty(), Optional.of(false), Optional.of(true),
                    Optional.empty()));
        }
        // I8
        if (isDataTypeCond(XmlaConstants.DBType.I8, oLevelDbType)) {
            result.add(new DbSchemaProviderTypesResponseRowR(Optional.of(XmlaConstants.DBType.I8.userName),
                    Optional.of(LevelDbTypeEnum.DBTYPE_I8), Optional.of(16), Optional.empty(), Optional.empty(),
                    Optional.empty(), Optional.of(true), Optional.empty(), Optional.empty(), Optional.of(false),
                    Optional.of(false), Optional.of(false), Optional.empty(), Optional.empty(), Optional.empty(),
                    Optional.empty(), Optional.empty(), Optional.empty(), Optional.of(false), Optional.of(true),
                    Optional.empty()));
        }

        // WSTR
        if (isDataTypeCond(XmlaConstants.DBType.WSTR, oLevelDbType)) {

            result.add(new DbSchemaProviderTypesResponseRowR(Optional.of(XmlaConstants.DBType.WSTR.userName),
                    Optional.of(LevelDbTypeEnum.DBTYPE_WSTR), Optional.of(255), Optional.of("\""), Optional.of("\""),
                    Optional.empty(), Optional.of(true), Optional.of(false), Optional.empty(), Optional.empty(),
                    Optional.of(false), Optional.of(false), Optional.empty(), Optional.empty(), Optional.empty(),
                    Optional.empty(), Optional.empty(), Optional.empty(), Optional.of(false), Optional.of(true),
                    Optional.empty()));
        }

        return result;
    }

    public List<DbSchemaSchemataResponseRow> dbSchemaSchemata(DbSchemaSchemataRequest request, RequestMetaData metaData) {
        String catalogName = request.restrictions().catalogName();
        String schemaName = request.restrictions().schemaName();
        String schemaOwner = request.restrictions().schemaOwner();
        List<DbSchemaSchemataResponseRow> result = new ArrayList<>();
        if (catalogName != null) {
            Optional<Catalog> oCatalog = contextsListSupplyer.tryGetFirstByName(catalogName, metaData.sessionId());
            if (oCatalog.isPresent()) {
                result.addAll(getDbSchemaSchemataResponseRow(oCatalog.get(), schemaName, schemaOwner));
            }
        } else {
            result.addAll(contextsListSupplyer.get(metaData.sessionId()).stream()
                    .map(c -> getDbSchemaSchemataResponseRow(c, schemaName, schemaOwner)).flatMap(Collection::stream)
                    .toList());
        }
        return result;
    }

    public List<DbSchemaSourceTablesResponseRow> dbSchemaSourceTables(DbSchemaSourceTablesRequest request,
            RequestMetaData metaData) {
        Optional<String> oCatalogName = request.restrictions().catalogName();
        Optional<String> oSchemaName = request.restrictions().schemaName();
        String tableName = request.restrictions().tableName();
        TableTypeEnum tableType = request.restrictions().tableType();

        if (oCatalogName.isPresent()) {
            Optional<Catalog> oCatalog = oCatalogName
                    .flatMap(name -> contextsListSupplyer.tryGetFirstByName(name, metaData.sessionId()));
            if (oCatalog.isPresent()) {
                return getDbSchemaSourceTablesResponseRow(oCatalog.get(), List.of(tableType.getValue()));
            }
        } else {
            return contextsListSupplyer.get(metaData.sessionId()).stream()
                    .map(c -> getDbSchemaSourceTablesResponseRow(c, List.of(tableType.getValue())))
                    .flatMap(Collection::stream).toList();
        }
        return List.of();
    }

    public List<DbSchemaTablesResponseRow> dbSchemaTables(DbSchemaTablesRequest request, RequestMetaData metaData) {
        Optional<String> oTableCatalog = request.restrictions().tableCatalog();
        Optional<String> oTableSchema = request.restrictions().tableSchema();
        Optional<String> oTableName = request.restrictions().tableName();
        Optional<String> oTableType = request.restrictions().tableType();

        if (oTableCatalog.isEmpty()) {
            oTableCatalog = request.properties().catalog();
        }

        if (oTableCatalog.isPresent()) {
            Optional<Catalog> oCatalog = oTableCatalog
                    .flatMap(name -> contextsListSupplyer.tryGetFirstByName(name, metaData.sessionId()));
            if (oCatalog.isPresent()) {
                return Utils.getDbSchemaTablesResponseRow(oCatalog.get(), oTableSchema, oTableName, oTableType);
            }
        } else {
            return contextsListSupplyer.get(metaData.sessionId()).stream()
                    .map(c -> Utils.getDbSchemaTablesResponseRow(c, oTableSchema, oTableName, oTableType))
                    .flatMap(Collection::stream).toList();
        }
        return List.of();
    }

    public List<DbSchemaTablesInfoResponseRow> dbSchemaTablesInfo(DbSchemaTablesInfoRequest request,
            RequestMetaData metaData) {
        Optional<String> oCatalogName = request.restrictions().catalogName();
        Optional<String> oSchemaName = request.restrictions().schemaName();
        String tableName = request.restrictions().tableName();
        TableTypeEnum tableType = request.restrictions().tableType();
        if (oCatalogName.isPresent()) {
            Optional<Catalog> oCatalog = oCatalogName
                    .flatMap(name -> contextsListSupplyer.tryGetFirstByName(name, metaData.sessionId()));
            if (oCatalog.isPresent()) {
                return getDbSchemaTablesInfoResponseRow(oCatalog.get(), oSchemaName, tableName, tableType);
            }
        } else {
            return contextsListSupplyer.get(metaData.sessionId()).stream()
                    .map(c -> getDbSchemaTablesInfoResponseRow(c, oSchemaName, tableName, tableType))
                    .flatMap(Collection::stream).toList();
        }
        return List.of();
    }

    public List<DbSchemaAssertionsResponseRow> dbSchemaAssertions(DbSchemaAssertionsRequest request,
            RequestMetaData metaData) {
        Optional<String> oCatalogName = request.restrictions().constraintCatalog();
        Optional<String> oSchemaName = request.restrictions().constraintSchema();
        Optional<String> oConstraintName = request.restrictions().constraintName();
        if (oCatalogName.isPresent()) {
            Optional<Catalog> oCatalog = oCatalogName
                    .flatMap(name -> contextsListSupplyer.tryGetFirstByName(name, metaData.sessionId()));
            if (oCatalog.isPresent()) {
                return getDbSchemaAssertionsResponseRow(oCatalog.get(), oSchemaName, oConstraintName);
            }
        } else {
            return contextsListSupplyer.get(metaData.sessionId()).stream()
                    .map(c -> getDbSchemaAssertionsResponseRow(c, oSchemaName, oConstraintName))
                    .flatMap(Collection::stream).toList();
        }
        return List.of();
    }

    public List<DbSchemaCharacterSetsResponseRow> dbSchemaCharacterSets(DbSchemaCharacterSetsRequest request,
            RequestMetaData metaData) {
        // TODO Auto-generated method stub
        return null;
    }

    public List<DbSchemaCheckConstraintsResponseRow> dbSchemaCheckConstraints(DbSchemaCheckConstraintsRequest request,
            RequestMetaData metaData) {
        Optional<String> oCatalogName = request.restrictions().constraintCatalog();
        Optional<String> oSchemaName = request.restrictions().constraintSchema();
        Optional<String> oConstraintName = request.restrictions().constraintName();
        if (oCatalogName.isPresent()) {
            Optional<Catalog> oCatalog = oCatalogName
                    .flatMap(name -> contextsListSupplyer.tryGetFirstByName(name, metaData.sessionId()));
            if (oCatalog.isPresent()) {
                return Utils.getDbSchemaCheckConstraints(oCatalog.get(), oSchemaName, oConstraintName);
            } else {
                return List.of();
            }
        } else {
            return contextsListSupplyer.get(metaData.sessionId()).stream()
                    .map(c -> Utils.getDbSchemaCheckConstraints(c, oSchemaName, oConstraintName))
                    .flatMap(Collection::stream).toList();
        }
    }

    public List<DbSchemaCheckConstraintsByTableResponseRow> dbSchemaCheckConstraintsByTable(
            DbSchemaCheckConstraintsByTableRequest request, RequestMetaData metaData) {
        Optional<String> oCatalogName = request.restrictions().constraintCatalog();
        Optional<String> oSchemaName = request.restrictions().constraintSchema();
        Optional<String> oConstraintName = request.restrictions().constraintName();
        Optional<String> oTableSchemaName = request.restrictions().tableSchema();
        Optional<String> oTableCatalogName = request.restrictions().tableCatalog();
        Optional<String> oTableName = request.restrictions().tableName();
        if (oCatalogName.isPresent()) {
            Optional<Catalog> oCatalog = oCatalogName
                    .flatMap(name -> contextsListSupplyer.tryGetFirstByName(name, metaData.sessionId()));
            if (oCatalog.isPresent()) {
                return Utils.getDbSchemaCheckConstraintsByTable(oCatalog.get(), oSchemaName, oConstraintName, oTableCatalogName, oTableSchemaName, oTableName);
            } else {
                return List.of();
            }
        } else {
            return contextsListSupplyer.get(metaData.sessionId()).stream()
                    .map(c -> Utils.getDbSchemaCheckConstraintsByTable(c, oSchemaName, oConstraintName, oTableCatalogName, oTableSchemaName, oTableName))
                    .flatMap(Collection::stream).toList();
        }
    }

    public List<DbSchemaCollationsResponseRow> dbSchemaCollations(DbSchemaCollationsRequest request,
            RequestMetaData metaData) {
        // TODO Auto-generated method stub
        return null;
    }

    public List<DbSchemaColumnDomainUsageResponseRow> dbSchemaColumnDomainUsage(
            DbSchemaColumnDomainUsageRequest request, RequestMetaData metaData) {
        // TODO Auto-generated method stub
        return null;
    }

    public List<DbSchemaColumnPrivilegesResponseRow> dbSchemaColumnPrivileges(DbSchemaColumnPrivilegesRequest request,
            RequestMetaData metaData) {
        // TODO Auto-generated method stub
        return null;
    }

    public List<DbSchemaConstraintColumnUsageResponseRow> dbSchemaConstraintColumnUsage(
            DbSchemaConstraintColumnUsageRequest request, RequestMetaData metaData) {
        // TODO Auto-generated method stub
        return null;
    }

    public List<DbSchemaConstraintTableUsageResponseRow> dbSchemaConstraintTableUsage(
            DbSchemaConstraintTableUsageRequest request, RequestMetaData metaData) {
        // TODO Auto-generated method stub
        return null;
    }

    public List<DbSchemaForeignKeysResponseRow> dbDbSchemaForeignKeys(DbSchemaForeignKeysRequest request,
            RequestMetaData metaData) {
        Optional<String> oFkCatalogName = request.restrictions().fkTableCatalog();
        Optional<String> oFkSchemaName = request.restrictions().fkTableSchema();
        Optional<String> oFkTableName = request.restrictions().fkTableName();
        Optional<String> oPkCatalogName = request.restrictions().pkTableCatalog();
        Optional<String> oPkSchemaName = request.restrictions().pkTableSchema();
        Optional<String> oPkTableName = request.restrictions().pkTableName();
        if (oFkCatalogName.isPresent()) {
            Optional<Catalog> oCatalog = oFkCatalogName
                    .flatMap(name -> contextsListSupplyer.tryGetFirstByName(name, metaData.sessionId()));
            if (oCatalog.isPresent()) {
                return Utils.getDbSchemaForeignKeys(oCatalog.get(), oFkSchemaName, oFkTableName, oPkCatalogName, oPkSchemaName, oPkTableName);
            }
        } else {
            return contextsListSupplyer.get(metaData.sessionId()).stream()
                    .map(c -> Utils.getDbSchemaForeignKeys(c, oFkSchemaName, oFkTableName, oPkCatalogName, oPkSchemaName, oPkTableName))
                    .flatMap(Collection::stream).toList();
        }
        return List.of();
    }

    public List<DbSchemaIndexesResponseRow> dbSchemaIndexes(DbSchemaIndexesRequest request, RequestMetaData metaData) {
        Optional<String> oCatalogName = request.restrictions().tableCatalog();
        Optional<String> oSchemaName = request.restrictions().tableSchema();
        Optional<String> oIndexName = request.restrictions().indexName();
        Optional<String> oTableName = request.restrictions().tableName();
        Optional<String> oType = request.restrictions().type();
        if (oCatalogName.isPresent()) {
            Optional<Catalog> oCatalog = oCatalogName
                    .flatMap(name -> contextsListSupplyer.tryGetFirstByName(name, metaData.sessionId()));
            if (oCatalog.isPresent()) {
                return Utils.getDbSchemaIndexesResponseRow(oCatalog.get(), oSchemaName, oTableName, oIndexName, oType);
            }
        } else {
            return contextsListSupplyer.get(metaData.sessionId()).stream()
                    .map(c -> Utils.getDbSchemaIndexesResponseRow(c, oSchemaName, oTableName, oIndexName, oType))
                    .flatMap(Collection::stream).toList();
        }
        return List.of();
    }

    public List<DbSchemaKeyColumnUsageResponseRow> dbSchemaKeyColumnUsage(DbSchemaKeyColumnUsageRequest request,
            RequestMetaData metaData) {
        // TODO Auto-generated method stub
        return null;
    }

    public List<DbSchemaPrimaryKeysResponseRow> dbSchemaPrimaryKeys(DbSchemaPrimaryKeysRequest request,
            RequestMetaData metaData) {
        Optional<String> oCatalogName = request.restrictions().tableCatalog();
        Optional<String> oSchemaName = request.restrictions().tableSchema();
        Optional<String> oTableName = request.restrictions().tableName();
        if (oCatalogName.isPresent()) {
            Optional<Catalog> oCatalog = oCatalogName
                    .flatMap(name -> contextsListSupplyer.tryGetFirstByName(name, metaData.sessionId()));
            if (oCatalog.isPresent()) {
                return Utils.getDbSchemaPrimaryKeysResponseRow(oCatalog.get(), oSchemaName, oTableName);
            }
        } else {
            return contextsListSupplyer.get(metaData.sessionId()).stream()
                    .map(c -> Utils.getDbSchemaPrimaryKeysResponseRow(c, oSchemaName, oTableName))
                    .flatMap(Collection::stream).toList();
        }
        return List.of();
    }

    public List<DbSchemaProcedureColumnsResponseRow> dbSchemaProcedureColumns(DbSchemaProcedureColumnsRequest request,
            RequestMetaData metaData) {
        // TODO Auto-generated method stub
        return null;
    }

    public List<DbSchemaProcedureParametersResponseRow> dbSchemaProcedureParameters(
            DbSchemaProcedureParametersRequest request, RequestMetaData metaData) {
        Optional<String> oCatalogName = request.restrictions().procedureCatalog();
        Optional<String> oSchemaName = request.restrictions().procedureSchema();
        Optional<String> oProcedureName = request.restrictions().procedureName();
        Optional<String> oParameterName = request.restrictions().parameterName();
        if (oCatalogName.isPresent()) {
            Optional<Catalog> oCatalog = oCatalogName
                    .flatMap(name -> contextsListSupplyer.tryGetFirstByName(name, metaData.sessionId()));
            if (oCatalog.isPresent()) {
                return Utils.getDbSchemaProcedureParametersResponseRow(oCatalog.get(), oSchemaName, oProcedureName, oParameterName);
            }
        } else {
            return contextsListSupplyer.get(metaData.sessionId()).stream()
                    .map(c -> Utils.getDbSchemaProcedureParametersResponseRow(c, oSchemaName, oProcedureName, oParameterName))
                    .flatMap(Collection::stream).toList();
        }
        return List.of();
    }

    public List<DbSchemaProceduresResponseRow> dbSchemaProcedures(DbSchemaProceduresRequest request,
            RequestMetaData metaData) {
        Optional<String> oCatalogName = request.restrictions().procedureCatalog();
        Optional<String> oSchemaName = request.restrictions().procedureSchema();
        Optional<String> oProcedureName = request.restrictions().procedureName();
        Optional<String> oProcedureType = request.restrictions().procedureType();
        if (oCatalogName.isPresent()) {
            Optional<Catalog> oCatalog = oCatalogName
                    .flatMap(name -> contextsListSupplyer.tryGetFirstByName(name, metaData.sessionId()));
            if (oCatalog.isPresent()) {
                return Utils.getDbSchemaProceduresResponseRow(oCatalog.get(), oSchemaName, oProcedureName, oProcedureType);
            }
        } else {
            return contextsListSupplyer.get(metaData.sessionId()).stream()
                    .map(c -> Utils.getDbSchemaProceduresResponseRow(c, oSchemaName, oProcedureName, oProcedureType))
                    .flatMap(Collection::stream).toList();
        }
        return List.of();
    }

    public List<DbSchemaReferentialConstraintsResponseRow> dbSchemaReferentialConstraints(
            DbSchemaReferentialConstraintsRequest request, RequestMetaData metaData) {
        // TODO Auto-generated method stub
        return null;
    }

    public List<DbSchemaSqlLanguagesResponseRow> dbSchemaSqlLanguages(DbSchemaSqlLanguagesRequest request,
            RequestMetaData metaData) {
        // TODO Auto-generated method stub
        return null;
    }

    public List<DbSchemaStatisticsResponseRow> dbSchemaStatistics(DbSchemaStatisticsRequest request,
            RequestMetaData metaData) {
        // TODO Auto-generated method stub
        return null;
    }

    public List<DbSchemaTableConstraintsResponseRow> dbSchemaTableConstraints(DbSchemaTableConstraintsRequest request,
            RequestMetaData metaData) {
        Optional<String> oCatalogName = request.restrictions().constraintCatalog();
        Optional<String> oSchemaName = request.restrictions().constraintSchema();
        Optional<String> oTableCatalogName = request.restrictions().tableCatalog();
        Optional<String> oTableSchemaName = request.restrictions().tableSchema();
        Optional<String> oConstraintName = request.restrictions().constraintName();
        Optional<String> oTableName = request.restrictions().tableName();
        Optional<String> oConstraintType = request.restrictions().constraintType();
        if (oCatalogName.isPresent()) {
            Optional<Catalog> oCatalog = oCatalogName
                    .flatMap(name -> contextsListSupplyer.tryGetFirstByName(name, metaData.sessionId()));
            if (oCatalog.isPresent()) {
                return Utils.getDbSchemaTableConstraintsResponseRow(oCatalog.get(), oSchemaName, oTableCatalogName, oTableSchemaName, oTableName, oConstraintName, oConstraintType);
            }
        } else {
            return contextsListSupplyer.get(metaData.sessionId()).stream()
                    .map(c -> Utils.getDbSchemaTableConstraintsResponseRow(c, oSchemaName, oTableCatalogName, oTableSchemaName, oTableName, oConstraintName, oConstraintType))
                    .flatMap(Collection::stream).toList();
        }
        return List.of();
    }

    public List<DbSchemaTablePrivilegesResponseRow> dbSchemaTablePrivileges(DbSchemaTablePrivilegesRequest request,
            RequestMetaData metaData) {
        // TODO Auto-generated method stub
        return null;
    }

    public List<DbSchemaTableStatisticsResponseRow> dbSchemaTableStatistics(DbSchemaTableStatisticsRequest request,
            RequestMetaData metaData) {
        // TODO Auto-generated method stub
        return null;
    }

    public List<DbSchemaTranslationsResponseRow> dbSchemaTranslations(DbSchemaTranslationsRequest request,
            RequestMetaData metaData) {
        // TODO Auto-generated method stub
        return null;
    }

    public List<DbSchemaUsagePrivilegesResponseRow> dbSchemaUsagePrivileges(DbSchemaUsagePrivilegesRequest request,
            RequestMetaData metaData) {
        // TODO Auto-generated method stub
        return null;
    }

    public List<DbSchemaViewColumnUsageResponseRow> dbSchemaViewColumnUsage(DbSchemaViewColumnUsageRequest request,
            RequestMetaData metaData) {
        Optional<String> oCatalogName = request.restrictions().viewCatalog();
        Optional<String> oSchemaName = request.restrictions().viewSchema();
        Optional<String> oViewName = request.restrictions().viewName();
        if (oCatalogName.isPresent()) {
            Optional<Catalog> oCatalog = oCatalogName
                    .flatMap(name -> contextsListSupplyer.tryGetFirstByName(name, metaData.sessionId()));
            if (oCatalog.isPresent()) {
                return Utils.getDbSchemaViewColumnUsageResponseRow(oCatalog.get(), oSchemaName, oViewName);
            }
        } else {
            return contextsListSupplyer.get(metaData.sessionId()).stream()
                    .map(c -> Utils.getDbSchemaViewColumnUsageResponseRow(c, oSchemaName, oViewName))
                    .flatMap(Collection::stream).toList();
        }
        return List.of();
    }

    public List<DbSchemaViewTableUsageResponseRow> dbSchemaViewTableUsage(DbSchemaViewTableUsageRequest request,
            RequestMetaData metaData) {
        Optional<String> oCatalogName = request.restrictions().viewCatalog();
        Optional<String> oSchemaName = request.restrictions().viewSchema();
        Optional<String> oViewName = request.restrictions().viewName();
        if (oCatalogName.isPresent()) {
            Optional<Catalog> oCatalog = oCatalogName
                    .flatMap(name -> contextsListSupplyer.tryGetFirstByName(name, metaData.sessionId()));
            if (oCatalog.isPresent()) {
                return Utils.getDbSchemaViewTableUsageResponseRow(oCatalog.get(), oSchemaName, oViewName);
            }
        } else {
            return contextsListSupplyer.get(metaData.sessionId()).stream()
                    .map(c -> Utils.getDbSchemaViewTableUsageResponseRow(c, oSchemaName, oViewName))
                    .flatMap(Collection::stream).toList();
        }
        return List.of();
    }

    public List<DbSchemaViewsResponseRow> dbSchemaViews(DbSchemaViewsRequest request, RequestMetaData metaData) {
        Optional<String> oCatalogName = request.restrictions().tableCatalog();
        Optional<String> oSchemaName = request.restrictions().tableSchema();
        Optional<String> oTableName = request.restrictions().tableName();
        if (oCatalogName.isPresent()) {
            Optional<Catalog> oCatalog = oCatalogName
                    .flatMap(name -> contextsListSupplyer.tryGetFirstByName(name, metaData.sessionId()));
            if (oCatalog.isPresent()) {
                return Utils.getDbSchemaViewsResponseRow(oCatalog.get(), oSchemaName, oTableName);
            }
        } else {
            return contextsListSupplyer.get(metaData.sessionId()).stream()
                    .map(c -> Utils.getDbSchemaViewsResponseRow(c, oSchemaName, oTableName))
                    .flatMap(Collection::stream).toList();
        }
        return List.of();
    }

}
