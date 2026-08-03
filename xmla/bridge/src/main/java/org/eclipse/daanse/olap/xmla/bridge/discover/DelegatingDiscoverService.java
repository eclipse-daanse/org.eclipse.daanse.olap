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

import java.util.List;

import org.eclipse.daanse.olap.xmla.bridge.ActionService;
import org.eclipse.daanse.olap.xmla.bridge.ContextGroupXmlaServiceConfig;
import org.eclipse.daanse.olap.xmla.bridge.ContextListSupplyer;
import org.eclipse.daanse.xmla.api.RequestMetaData;
import org.eclipse.daanse.xmla.api.UserRolePrincipal;
import org.eclipse.daanse.xmla.api.discover.DiscoverService;
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
import org.eclipse.daanse.xmla.api.discover.discover.csdlmetadata.DiscoverCsdlMetaDataRequest;
import org.eclipse.daanse.xmla.api.discover.discover.csdlmetadata.DiscoverCsdlMetaDataResponseRow;
import org.eclipse.daanse.xmla.api.discover.discover.datasources.DiscoverDataSourcesRequest;
import org.eclipse.daanse.xmla.api.discover.discover.datasources.DiscoverDataSourcesResponseRow;
import org.eclipse.daanse.xmla.api.discover.discover.enumerators.DiscoverEnumeratorsRequest;
import org.eclipse.daanse.xmla.api.discover.discover.enumerators.DiscoverEnumeratorsResponseRow;
import org.eclipse.daanse.xmla.api.discover.discover.keywords.DiscoverKeywordsRequest;
import org.eclipse.daanse.xmla.api.discover.discover.keywords.DiscoverKeywordsResponseRow;
import org.eclipse.daanse.xmla.api.discover.discover.literals.DiscoverLiteralsRequest;
import org.eclipse.daanse.xmla.api.discover.discover.literals.DiscoverLiteralsResponseRow;
import org.eclipse.daanse.xmla.api.discover.discover.properties.DiscoverPropertiesRequest;
import org.eclipse.daanse.xmla.api.discover.discover.properties.DiscoverPropertiesResponseRow;
import org.eclipse.daanse.xmla.api.discover.discover.schemarowsets.DiscoverSchemaRowsetsRequest;
import org.eclipse.daanse.xmla.api.discover.discover.schemarowsets.DiscoverSchemaRowsetsResponseRow;
import org.eclipse.daanse.xmla.api.discover.discover.xmlmetadata.DiscoverXmlMetaDataRequest;
import org.eclipse.daanse.xmla.api.discover.discover.xmlmetadata.DiscoverXmlMetaDataResponseRow;
import org.eclipse.daanse.xmla.api.discover.mdschema.actions.MdSchemaActionsRequest;
import org.eclipse.daanse.xmla.api.discover.mdschema.actions.MdSchemaActionsResponseRow;
import org.eclipse.daanse.xmla.api.discover.mdschema.cubes.MdSchemaCubesRequest;
import org.eclipse.daanse.xmla.api.discover.mdschema.cubes.MdSchemaCubesResponseRow;
import org.eclipse.daanse.xmla.api.discover.mdschema.dimensions.MdSchemaDimensionsRequest;
import org.eclipse.daanse.xmla.api.discover.mdschema.dimensions.MdSchemaDimensionsResponseRow;
import org.eclipse.daanse.xmla.api.discover.mdschema.functions.MdSchemaFunctionsRequest;
import org.eclipse.daanse.xmla.api.discover.mdschema.functions.MdSchemaFunctionsResponseRow;
import org.eclipse.daanse.xmla.api.discover.mdschema.hierarchies.MdSchemaHierarchiesRequest;
import org.eclipse.daanse.xmla.api.discover.mdschema.hierarchies.MdSchemaHierarchiesResponseRow;
import org.eclipse.daanse.xmla.api.discover.mdschema.kpis.MdSchemaKpisRequest;
import org.eclipse.daanse.xmla.api.discover.mdschema.kpis.MdSchemaKpisResponseRow;
import org.eclipse.daanse.xmla.api.discover.mdschema.levels.MdSchemaLevelsRequest;
import org.eclipse.daanse.xmla.api.discover.mdschema.levels.MdSchemaLevelsResponseRow;
import org.eclipse.daanse.xmla.api.discover.mdschema.measuregroupdimensions.MdSchemaMeasureGroupDimensionsRequest;
import org.eclipse.daanse.xmla.api.discover.mdschema.measuregroupdimensions.MdSchemaMeasureGroupDimensionsResponseRow;
import org.eclipse.daanse.xmla.api.discover.mdschema.measuregroups.MdSchemaMeasureGroupsRequest;
import org.eclipse.daanse.xmla.api.discover.mdschema.measuregroups.MdSchemaMeasureGroupsResponseRow;
import org.eclipse.daanse.xmla.api.discover.mdschema.measures.MdSchemaMeasuresRequest;
import org.eclipse.daanse.xmla.api.discover.mdschema.measures.MdSchemaMeasuresResponseRow;
import org.eclipse.daanse.xmla.api.discover.mdschema.members.MdSchemaMembersRequest;
import org.eclipse.daanse.xmla.api.discover.mdschema.members.MdSchemaMembersResponseRow;
import org.eclipse.daanse.xmla.api.discover.mdschema.properties.MdSchemaPropertiesRequest;
import org.eclipse.daanse.xmla.api.discover.mdschema.properties.MdSchemaPropertiesResponseRow;
import org.eclipse.daanse.xmla.api.discover.mdschema.sets.MdSchemaSetsRequest;
import org.eclipse.daanse.xmla.api.discover.mdschema.sets.MdSchemaSetsResponseRow;

/*
 * Delegates to a other class that share same kind of information.
 * Encapsulates the Logic.
 */
public class DelegatingDiscoverService implements DiscoverService {

    private DBSchemaDiscoverService dbSchemaService;
    private MDSchemaDiscoverService mdSchemaService;
    private OtherDiscoverService otherSchemaService;

    public DelegatingDiscoverService(ContextListSupplyer contextsListSupplyer, ActionService actionService,
            ContextGroupXmlaServiceConfig config) {
        this.dbSchemaService = new DBSchemaDiscoverService(contextsListSupplyer);
        this.mdSchemaService = new MDSchemaDiscoverService(contextsListSupplyer, actionService);
        this.otherSchemaService = new OtherDiscoverService(contextsListSupplyer, config);
    }

    @Override
    public List<DiscoverDataSourcesResponseRow> dataSources(DiscoverDataSourcesRequest request,
            RequestMetaData metaData, UserRolePrincipal userRolePrincipal) {
        return otherSchemaService.dataSources(request, metaData);
    }

    @Override
    public List<DbSchemaCatalogsResponseRow> dbSchemaCatalogs(DbSchemaCatalogsRequest request, RequestMetaData metaData, UserRolePrincipal userRolePrincipal) {

        return dbSchemaService.dbSchemaCatalogs(request, metaData);
    }

    @Override
    public List<DbSchemaColumnsResponseRow> dbSchemaColumns(DbSchemaColumnsRequest request, RequestMetaData metaData, UserRolePrincipal userRolePrincipal) {

        return dbSchemaService.dbSchemaColumns(request, metaData);
    }

    @Override
    public List<DbSchemaProviderTypesResponseRow> dbSchemaProviderTypes(DbSchemaProviderTypesRequest request,
            RequestMetaData metaData, UserRolePrincipal userRolePrincipal) {

        return dbSchemaService.dbSchemaProviderTypes(request, metaData);
    }

    @Override
    public List<DbSchemaSchemataResponseRow> dbSchemaSchemata(DbSchemaSchemataRequest request, RequestMetaData metaData, UserRolePrincipal userRolePrincipal) {

        return dbSchemaService.dbSchemaSchemata(request, metaData);
    }

    @Override
    public List<DbSchemaSourceTablesResponseRow> dbSchemaSourceTables(DbSchemaSourceTablesRequest request,
            RequestMetaData metaData, UserRolePrincipal userRolePrincipal) {

        return dbSchemaService.dbSchemaSourceTables(request, metaData);
    }

    @Override
    public List<DbSchemaTablesResponseRow> dbSchemaTables(DbSchemaTablesRequest request, RequestMetaData metaData, UserRolePrincipal userRolePrincipal) {

        return dbSchemaService.dbSchemaTables(request, metaData);
    }

    @Override
    public List<DbSchemaTablesInfoResponseRow> dbSchemaTablesInfo(DbSchemaTablesInfoRequest request,
            RequestMetaData metaData, UserRolePrincipal userRolePrincipal) {

        return dbSchemaService.dbSchemaTablesInfo(request, metaData);
    }

    @Override
    public List<DiscoverEnumeratorsResponseRow> discoverEnumerators(DiscoverEnumeratorsRequest request,
            RequestMetaData metaData, UserRolePrincipal userRolePrincipal) {

        return otherSchemaService.discoverEnumerators(request, metaData);
    }

    @Override
    public List<DiscoverKeywordsResponseRow> discoverKeywords(DiscoverKeywordsRequest request, RequestMetaData metaData, UserRolePrincipal userRolePrincipal) {

        return otherSchemaService.discoverKeywords(request, metaData);
    }

    @Override
    public List<DiscoverLiteralsResponseRow> discoverLiterals(DiscoverLiteralsRequest request, RequestMetaData metaData, UserRolePrincipal userRolePrincipal) {

        return otherSchemaService.discoverLiterals(request, metaData);
    }

    @Override
    public List<DiscoverPropertiesResponseRow> discoverProperties(DiscoverPropertiesRequest request,
            RequestMetaData metaData, UserRolePrincipal userRolePrincipal) {

        return otherSchemaService.discoverProperties(request, metaData);
    }

    @Override
    public List<DiscoverSchemaRowsetsResponseRow> discoverSchemaRowsets(DiscoverSchemaRowsetsRequest request,
            RequestMetaData metaData, UserRolePrincipal userRolePrincipal) {

        return otherSchemaService.discoverSchemaRowsets(request, metaData);
    }

    @Override
    public List<MdSchemaActionsResponseRow> mdSchemaActions(MdSchemaActionsRequest request, RequestMetaData metaData, UserRolePrincipal userRolePrincipal) {

        return mdSchemaService.mdSchemaActions(request, metaData);
    }

    @Override
    public List<MdSchemaCubesResponseRow> mdSchemaCubes(MdSchemaCubesRequest request, RequestMetaData metaData, UserRolePrincipal userRolePrincipal) {

        return mdSchemaService.mdSchemaCubes(request, metaData);
    }

    @Override
    public List<MdSchemaDimensionsResponseRow> mdSchemaDimensions(MdSchemaDimensionsRequest request,
            RequestMetaData metaData, UserRolePrincipal userRolePrincipal) {

        return mdSchemaService.mdSchemaDimensions(request, metaData);
    }

    @Override
    public List<MdSchemaFunctionsResponseRow> mdSchemaFunctions(MdSchemaFunctionsRequest request,
            RequestMetaData metaData, UserRolePrincipal userRolePrincipal) {

        return mdSchemaService.mdSchemaFunctions(request, metaData);
    }

    @Override
    public List<MdSchemaHierarchiesResponseRow> mdSchemaHierarchies(MdSchemaHierarchiesRequest request,
            RequestMetaData metaData, UserRolePrincipal userRolePrincipal) {

        return mdSchemaService.mdSchemaHierarchies(request, metaData);
    }

    @Override
    public List<MdSchemaKpisResponseRow> mdSchemaKpis(MdSchemaKpisRequest request, RequestMetaData metaData, UserRolePrincipal userRolePrincipal) {

        return mdSchemaService.mdSchemaKpis(request, metaData);
    }

    @Override
    public List<MdSchemaLevelsResponseRow> mdSchemaLevels(MdSchemaLevelsRequest request, RequestMetaData metaData, UserRolePrincipal userRolePrincipal) {

        return mdSchemaService.mdSchemaLevels(request, metaData);
    }

    @Override
    public List<MdSchemaMeasureGroupDimensionsResponseRow> mdSchemaMeasureGroupDimensions(
            MdSchemaMeasureGroupDimensionsRequest request, RequestMetaData metaData, UserRolePrincipal userRolePrincipal) {

        return mdSchemaService.mdSchemaMeasureGroupDimensions(request, metaData);
    }

    @Override
    public List<MdSchemaMeasureGroupsResponseRow> mdSchemaMeasureGroups(MdSchemaMeasureGroupsRequest request,
            RequestMetaData metaData, UserRolePrincipal userRolePrincipal) {

        return mdSchemaService.mdSchemaMeasureGroups(request, metaData);
    }

    @Override
    public List<MdSchemaMeasuresResponseRow> mdSchemaMeasures(MdSchemaMeasuresRequest request, RequestMetaData metaData, UserRolePrincipal userRolePrincipal) {

        return mdSchemaService.mdSchemaMeasures(request, metaData);
    }

    @Override
    public List<MdSchemaMembersResponseRow> mdSchemaMembers(MdSchemaMembersRequest request, RequestMetaData metaData, UserRolePrincipal userRolePrincipal) {

        return mdSchemaService.mdSchemaMembers(request, metaData);
    }

    @Override
    public List<MdSchemaPropertiesResponseRow> mdSchemaProperties(MdSchemaPropertiesRequest request,
            RequestMetaData metaData, UserRolePrincipal userRolePrincipal) {

        return mdSchemaService.mdSchemaProperties(request, metaData);
    }

    @Override
    public List<MdSchemaSetsResponseRow> mdSchemaSets(MdSchemaSetsRequest request, RequestMetaData metaData, UserRolePrincipal userRolePrincipal) {

        return mdSchemaService.mdSchemaSets(request, metaData);
    }

    @Override
    public List<DiscoverXmlMetaDataResponseRow> xmlMetaData(DiscoverXmlMetaDataRequest request,
            RequestMetaData metaData, UserRolePrincipal userRolePrincipal) {

        return otherSchemaService.xmlMetaData(request, metaData);
    }

    @Override
    public List<DiscoverCsdlMetaDataResponseRow> csdlMetaData(DiscoverCsdlMetaDataRequest request,
            RequestMetaData metaData, UserRolePrincipal userRolePrincipal) {

        return otherSchemaService.csdlMetaData(request, metaData);
    }

    @Override
    public List<DbSchemaAssertionsResponseRow> dbSchemaAssertions(DbSchemaAssertionsRequest request, RequestMetaData metaData,
            UserRolePrincipal userRolePrincipal) {
        return dbSchemaService.dbSchemaAssertions(request, metaData);
    }

    @Override
    public List<DbSchemaCharacterSetsResponseRow> dbSchemaCharacterSets(DbSchemaCharacterSetsRequest request,
            RequestMetaData metaData, UserRolePrincipal userRolePrincipal) {
        return dbSchemaService.dbSchemaCharacterSets(request, metaData);
    }

    @Override
    public List<DbSchemaCheckConstraintsResponseRow> dbSchemaCheckConstraints(DbSchemaCheckConstraintsRequest request,
            RequestMetaData metaData, UserRolePrincipal userRolePrincipal) {
        return dbSchemaService.dbSchemaCheckConstraints(request, metaData);
    }

    @Override
    public List<DbSchemaCheckConstraintsByTableResponseRow> dbSchemaCheckConstraintsByTable(
            DbSchemaCheckConstraintsByTableRequest request, RequestMetaData metaData, UserRolePrincipal userRolePrincipal) {
        return dbSchemaService.dbSchemaCheckConstraintsByTable(request, metaData);
    }

    @Override
    public List<DbSchemaCollationsResponseRow> dbSchemaCollations(DbSchemaCollationsRequest request, RequestMetaData metaData,
            UserRolePrincipal userRolePrincipal) {
        return dbSchemaService.dbSchemaCollations(request, metaData);
    }

    @Override
    public List<DbSchemaColumnDomainUsageResponseRow> dbSchemaColumnDomainUsage(DbSchemaColumnDomainUsageRequest request,
            RequestMetaData metaData, UserRolePrincipal userRolePrincipal) {
        return dbSchemaService.dbSchemaColumnDomainUsage(request, metaData);
    }

    @Override
    public List<DbSchemaColumnPrivilegesResponseRow> dbSchemaColumnPrivileges(DbSchemaColumnPrivilegesRequest request,
            RequestMetaData metaData, UserRolePrincipal userRolePrincipal) {
        return dbSchemaService.dbSchemaColumnPrivileges(request, metaData);
    }

    @Override
    public List<DbSchemaConstraintColumnUsageResponseRow> dbSchemaConstraintColumnUsage(
            DbSchemaConstraintColumnUsageRequest request, RequestMetaData metaData, UserRolePrincipal userRolePrincipal) {
        return dbSchemaService.dbSchemaConstraintColumnUsage(request, metaData);
    }

    @Override
    public List<DbSchemaConstraintTableUsageResponseRow> dbSchemaConstraintTableUsage(
            DbSchemaConstraintTableUsageRequest request, RequestMetaData metaData, UserRolePrincipal userRolePrincipal) {
        return dbSchemaService.dbSchemaConstraintTableUsage(request, metaData);
    }

    @Override
    public List<DbSchemaForeignKeysResponseRow> dbSchemaForeignKeys(DbSchemaForeignKeysRequest request,
            RequestMetaData metaData, UserRolePrincipal userRolePrincipal) {
        return dbSchemaService.dbSchemaForeignKeys(request, metaData);
    }

    @Override
    public List<DbSchemaIndexesResponseRow> dbSchemaIndexes(DbSchemaIndexesRequest request, RequestMetaData metaData,
            UserRolePrincipal userRolePrincipal) {
        return dbSchemaService.dbSchemaIndexes(request, metaData);
    }

    @Override
    public List<DbSchemaKeyColumnUsageResponseRow> dbSchemaKeyColumnUsage(DbSchemaKeyColumnUsageRequest request, RequestMetaData metaData,
            UserRolePrincipal userRolePrincipal) {
            return dbSchemaService.dbSchemaKeyColumnUsage(request, metaData);
    }

    @Override
    public List<DbSchemaPrimaryKeysResponseRow> dbSchemaPrimaryKeys(DbSchemaPrimaryKeysRequest request,
            RequestMetaData metaData, UserRolePrincipal userRolePrincipal) {
        return dbSchemaService.dbSchemaPrimaryKeys(request, metaData);
    }

    @Override
    public List<DbSchemaProcedureColumnsResponseRow> dbSchemaProcedureColumns(DbSchemaProcedureColumnsRequest request,
            RequestMetaData metaData, UserRolePrincipal userRolePrincipal) {
        return dbSchemaService.dbSchemaProcedureColumns(request, metaData);
    }

    @Override
    public List<DbSchemaProcedureParametersResponseRow> dbSchemaProcedureParameters(
            DbSchemaProcedureParametersRequest request, RequestMetaData metaData, UserRolePrincipal userRolePrincipal) {
        return dbSchemaService.dbSchemaProcedureParameters(request, metaData);
    }

    @Override
    public List<DbSchemaProceduresResponseRow> dbSchemaProcedures(DbSchemaProceduresRequest request, RequestMetaData metaData,
            UserRolePrincipal userRolePrincipal) {
        return dbSchemaService.dbSchemaProcedures(request, metaData);
    }

    @Override
    public List<DbSchemaReferentialConstraintsResponseRow> dbSchemaReferentialConstraints(
            DbSchemaReferentialConstraintsRequest request, RequestMetaData metaData, UserRolePrincipal userRolePrincipal) {
        return dbSchemaService.dbSchemaReferentialConstraints(request, metaData);
    }

    @Override
    public List<DbSchemaSqlLanguagesResponseRow> dbSchemaSqlLanguages(DbSchemaSqlLanguagesRequest request,
            RequestMetaData metaData, UserRolePrincipal userRolePrincipal) {
        return dbSchemaService.dbSchemaSqlLanguages(request, metaData);
    }

    @Override
    public List<DbSchemaStatisticsResponseRow> dbSchemaStatistics(DbSchemaStatisticsRequest request, RequestMetaData metaData,
            UserRolePrincipal userRolePrincipal) {
        return dbSchemaService.dbSchemaStatistics(request, metaData);
    }

    @Override
    public List<DbSchemaTableConstraintsResponseRow> dbSchemaTableConstraints(DbSchemaTableConstraintsRequest request,
            RequestMetaData metaData, UserRolePrincipal userRolePrincipal) {
        return dbSchemaService.dbSchemaTableConstraints(request, metaData);
    }

    @Override
    public List<DbSchemaTablePrivilegesResponseRow> dbSchemaTablePrivileges(DbSchemaTablePrivilegesRequest request,
            RequestMetaData metaData, UserRolePrincipal userRolePrincipal) {
        return dbSchemaService.dbSchemaTablePrivileges(request, metaData);
    }

    @Override
    public List<DbSchemaTableStatisticsResponseRow> dbSchemaTableStatistics(DbSchemaTableStatisticsRequest request,
            RequestMetaData metaData, UserRolePrincipal userRolePrincipal) {
        return dbSchemaService.dbSchemaTableStatistics(request, metaData);
    }

    @Override
    public List<DbSchemaTranslationsResponseRow> dbSchemaTranslations(DbSchemaTranslationsRequest request,
            RequestMetaData metaData, UserRolePrincipal userRolePrincipal) {
        return dbSchemaService.dbSchemaTranslations(request, metaData);
    }

    @Override
    public List<DbSchemaUsagePrivilegesResponseRow> dbSchemaUsagePrivileges(DbSchemaUsagePrivilegesRequest request,
            RequestMetaData metaData, UserRolePrincipal userRolePrincipal) {
        return dbSchemaService.dbSchemaUsagePrivileges(request, metaData);
    }

    @Override
    public List<DbSchemaViewColumnUsageResponseRow> dbSchemaViewColumnUsage(DbSchemaViewColumnUsageRequest request,
            RequestMetaData metaData, UserRolePrincipal userRolePrincipal) {
        return dbSchemaService.dbSchemaViewColumnUsage(request, metaData);
    }

    @Override
    public List<DbSchemaViewTableUsageResponseRow> dbSchemaViewTableUsage(DbSchemaViewTableUsageRequest request,
            RequestMetaData metaData, UserRolePrincipal userRolePrincipal) {
        return dbSchemaService.dbSchemaViewTableUsage(request, metaData);
    }

    @Override
    public List<DbSchemaViewsResponseRow> dbSchemaViews(DbSchemaViewsRequest request, RequestMetaData metaData,
            UserRolePrincipal userRolePrincipal) {
        return dbSchemaService.dbSchemaViews(request, metaData);
    }

}
