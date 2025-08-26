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
import org.eclipse.daanse.xmla.api.discover.dbschema.catalogs.DbSchemaCatalogsRequest;
import org.eclipse.daanse.xmla.api.discover.dbschema.catalogs.DbSchemaCatalogsResponseRow;
import org.eclipse.daanse.xmla.api.discover.dbschema.columns.DbSchemaColumnsRequest;
import org.eclipse.daanse.xmla.api.discover.dbschema.columns.DbSchemaColumnsResponseRow;
import org.eclipse.daanse.xmla.api.discover.dbschema.providertypes.DbSchemaProviderTypesRequest;
import org.eclipse.daanse.xmla.api.discover.dbschema.providertypes.DbSchemaProviderTypesResponseRow;
import org.eclipse.daanse.xmla.api.discover.dbschema.schemata.DbSchemaSchemataRequest;
import org.eclipse.daanse.xmla.api.discover.dbschema.schemata.DbSchemaSchemataResponseRow;
import org.eclipse.daanse.xmla.api.discover.dbschema.sourcetables.DbSchemaSourceTablesRequest;
import org.eclipse.daanse.xmla.api.discover.dbschema.sourcetables.DbSchemaSourceTablesResponseRow;
import org.eclipse.daanse.xmla.api.discover.dbschema.tables.DbSchemaTablesRequest;
import org.eclipse.daanse.xmla.api.discover.dbschema.tables.DbSchemaTablesResponseRow;
import org.eclipse.daanse.xmla.api.discover.dbschema.tablesinfo.DbSchemaTablesInfoRequest;
import org.eclipse.daanse.xmla.api.discover.dbschema.tablesinfo.DbSchemaTablesInfoResponseRow;
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
import org.eclipse.daanse.xmla.api.discover.mdschema.demensions.MdSchemaDimensionsRequest;
import org.eclipse.daanse.xmla.api.discover.mdschema.demensions.MdSchemaDimensionsResponseRow;
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
        return otherSchemaService.dataSources(request, metaData, userRolePrincipal);
    }

    @Override
    public List<DbSchemaCatalogsResponseRow> dbSchemaCatalogs(DbSchemaCatalogsRequest request, RequestMetaData metaData,
            UserRolePrincipal userRolePrincipal) {

        return dbSchemaService.dbSchemaCatalogs(request, metaData, userRolePrincipal);
    }

    @Override
    public List<DbSchemaColumnsResponseRow> dbSchemaColumns(DbSchemaColumnsRequest request, RequestMetaData metaData,
            UserRolePrincipal userRolePrincipal) {

        return dbSchemaService.dbSchemaColumns(request, metaData, userRolePrincipal);
    }

    @Override
    public List<DbSchemaProviderTypesResponseRow> dbSchemaProviderTypes(DbSchemaProviderTypesRequest request,
            RequestMetaData metaData, UserRolePrincipal userRolePrincipal) {

        return dbSchemaService.dbSchemaProviderTypes(request, metaData, userRolePrincipal);
    }

    @Override
    public List<DbSchemaSchemataResponseRow> dbSchemaSchemata(DbSchemaSchemataRequest request, RequestMetaData metaData,
            UserRolePrincipal userRolePrincipal) {

        return dbSchemaService.dbSchemaSchemata(request, metaData, userRolePrincipal);
    }

    @Override
    public List<DbSchemaSourceTablesResponseRow> dbSchemaSourceTables(DbSchemaSourceTablesRequest request,
            RequestMetaData metaData, UserRolePrincipal userRolePrincipal) {

        return dbSchemaService.dbSchemaSourceTables(request, metaData, userRolePrincipal);
    }

    @Override
    public List<DbSchemaTablesResponseRow> dbSchemaTables(DbSchemaTablesRequest request, RequestMetaData metaData,
            UserRolePrincipal userRolePrincipal) {

        return dbSchemaService.dbSchemaTables(request, metaData, userRolePrincipal);
    }

    @Override
    public List<DbSchemaTablesInfoResponseRow> dbSchemaTablesInfo(DbSchemaTablesInfoRequest request,
            RequestMetaData metaData, UserRolePrincipal userRolePrincipal) {

        return dbSchemaService.dbSchemaTablesInfo(request, metaData, userRolePrincipal);
    }

    @Override
    public List<DiscoverEnumeratorsResponseRow> discoverEnumerators(DiscoverEnumeratorsRequest request,
            RequestMetaData metaData, UserRolePrincipal userRolePrincipal) {

        return otherSchemaService.discoverEnumerators(request, metaData, userRolePrincipal);
    }

    @Override
    public List<DiscoverKeywordsResponseRow> discoverKeywords(DiscoverKeywordsRequest request, RequestMetaData metaData,
            UserRolePrincipal userRolePrincipal) {

        return otherSchemaService.discoverKeywords(request, metaData, userRolePrincipal);
    }

    @Override
    public List<DiscoverLiteralsResponseRow> discoverLiterals(DiscoverLiteralsRequest request, RequestMetaData metaData,
            UserRolePrincipal userRolePrincipal) {

        return otherSchemaService.discoverLiterals(request, metaData, userRolePrincipal);
    }

    @Override
    public List<DiscoverPropertiesResponseRow> discoverProperties(DiscoverPropertiesRequest request,
            RequestMetaData metaData, UserRolePrincipal userRolePrincipal) {

        return otherSchemaService.discoverProperties(request, metaData, userRolePrincipal);
    }

    @Override
    public List<DiscoverSchemaRowsetsResponseRow> discoverSchemaRowsets(DiscoverSchemaRowsetsRequest request,
            RequestMetaData metaData, UserRolePrincipal userRolePrincipal) {

        return otherSchemaService.discoverSchemaRowsets(request, metaData, userRolePrincipal);
    }

    @Override
    public List<MdSchemaActionsResponseRow> mdSchemaActions(MdSchemaActionsRequest request, RequestMetaData metaData,
            UserRolePrincipal userRolePrincipal) {

        return mdSchemaService.mdSchemaActions(request, metaData, userRolePrincipal);
    }

    @Override
    public List<MdSchemaCubesResponseRow> mdSchemaCubes(MdSchemaCubesRequest request, RequestMetaData metaData,
            UserRolePrincipal userRolePrincipal) {

        return mdSchemaService.mdSchemaCubes(request, metaData, userRolePrincipal);
    }

    @Override
    public List<MdSchemaDimensionsResponseRow> mdSchemaDimensions(MdSchemaDimensionsRequest request,
            RequestMetaData metaData, UserRolePrincipal userRolePrincipal) {

        return mdSchemaService.mdSchemaDimensions(request, metaData, userRolePrincipal);
    }

    @Override
    public List<MdSchemaFunctionsResponseRow> mdSchemaFunctions(MdSchemaFunctionsRequest request,
            RequestMetaData metaData, UserRolePrincipal userRolePrincipal) {

        return mdSchemaService.mdSchemaFunctions(request, metaData, userRolePrincipal);
    }

    @Override
    public List<MdSchemaHierarchiesResponseRow> mdSchemaHierarchies(MdSchemaHierarchiesRequest request,
            RequestMetaData metaData, UserRolePrincipal userRolePrincipal) {

        return mdSchemaService.mdSchemaHierarchies(request, metaData, userRolePrincipal);
    }

    @Override
    public List<MdSchemaKpisResponseRow> mdSchemaKpis(MdSchemaKpisRequest request, RequestMetaData metaData,
            UserRolePrincipal userRolePrincipal) {

        return mdSchemaService.mdSchemaKpis(request, metaData, userRolePrincipal);
    }

    @Override
    public List<MdSchemaLevelsResponseRow> mdSchemaLevels(MdSchemaLevelsRequest request, RequestMetaData metaData,
            UserRolePrincipal userRolePrincipal) {

        return mdSchemaService.mdSchemaLevels(request, metaData, userRolePrincipal);
    }

    @Override
    public List<MdSchemaMeasureGroupDimensionsResponseRow> mdSchemaMeasureGroupDimensions(
            MdSchemaMeasureGroupDimensionsRequest request, RequestMetaData metaData, UserRolePrincipal userRolePrincipal) {

        return mdSchemaService.mdSchemaMeasureGroupDimensions(request, metaData, userRolePrincipal);
    }

    @Override
    public List<MdSchemaMeasureGroupsResponseRow> mdSchemaMeasureGroups(MdSchemaMeasureGroupsRequest request,
            RequestMetaData metaData, UserRolePrincipal userRolePrincipal) {

        return mdSchemaService.mdSchemaMeasureGroups(request, metaData, userRolePrincipal);
    }

    @Override
    public List<MdSchemaMeasuresResponseRow> mdSchemaMeasures(MdSchemaMeasuresRequest request, RequestMetaData metaData,
            UserRolePrincipal userRolePrincipal) {

        return mdSchemaService.mdSchemaMeasures(request, metaData, userRolePrincipal);
    }

    @Override
    public List<MdSchemaMembersResponseRow> mdSchemaMembers(MdSchemaMembersRequest request, RequestMetaData metaData,
            UserRolePrincipal userRolePrincipal) {

        return mdSchemaService.mdSchemaMembers(request, metaData, userRolePrincipal);
    }

    @Override
    public List<MdSchemaPropertiesResponseRow> mdSchemaProperties(MdSchemaPropertiesRequest request,
            RequestMetaData metaData, UserRolePrincipal userRolePrincipal) {

        return mdSchemaService.mdSchemaProperties(request, metaData, userRolePrincipal);
    }

    @Override
    public List<MdSchemaSetsResponseRow> mdSchemaSets(MdSchemaSetsRequest request, RequestMetaData metaData,
            UserRolePrincipal userRolePrincipal) {

        return mdSchemaService.mdSchemaSets(request, metaData, userRolePrincipal);
    }

    @Override
    public List<DiscoverXmlMetaDataResponseRow> xmlMetaData(DiscoverXmlMetaDataRequest request,
            RequestMetaData metaData, UserRolePrincipal userRolePrincipal) {

        return otherSchemaService.xmlMetaData(request, metaData, userRolePrincipal);
    }

}
