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
package org.eclipse.daanse.olap.xmla.connector.embedded;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

import org.eclipse.daanse.olap.xmla.connector.ContextListSupplyer;
import org.eclipse.daanse.xmla.api.RowsetProvider;
import org.eclipse.daanse.olap.xmla.connector.rowset.provider.core.*;
import org.eclipse.daanse.olap.xmla.connector.rowset.provider.multidimensional.*;
import org.eclipse.daanse.olap.xmla.connector.rowset.provider.relational.*;
import org.eclipse.daanse.olap.xmla.connector.rowset.provider.core.DiscoverCsdlMetadataProvider;
import org.eclipse.daanse.olap.xmla.connector.rowset.provider.core.DiscoverDatasourcesProvider;
import org.eclipse.daanse.olap.xmla.connector.rowset.provider.core.DiscoverEnumeratorsProvider;
import org.eclipse.daanse.olap.xmla.connector.rowset.provider.core.DiscoverKeywordsProvider;
import org.eclipse.daanse.olap.xmla.connector.rowset.provider.core.DiscoverLiteralsProvider;
import org.eclipse.daanse.olap.xmla.connector.rowset.provider.core.DiscoverPropertiesProvider;
import org.eclipse.daanse.olap.xmla.connector.rowset.provider.core.DiscoverSchemaRowsetsProvider;
import org.eclipse.daanse.olap.xmla.connector.rowset.provider.core.DiscoverXmlMetadataProvider;
import org.eclipse.daanse.olap.xmla.connector.rowset.provider.multidimensional.MdschemaActionsProvider;
import org.eclipse.daanse.olap.xmla.connector.rowset.provider.multidimensional.MdschemaCubesProvider;
import org.eclipse.daanse.olap.xmla.connector.rowset.provider.multidimensional.MdschemaDimensionsProvider;
import org.eclipse.daanse.olap.xmla.connector.rowset.provider.multidimensional.MdschemaFunctionsProvider;
import org.eclipse.daanse.olap.xmla.connector.rowset.provider.multidimensional.MdschemaHierarchiesProvider;
import org.eclipse.daanse.olap.xmla.connector.rowset.provider.multidimensional.MdschemaKpisProvider;
import org.eclipse.daanse.olap.xmla.connector.rowset.provider.multidimensional.MdschemaLevelsProvider;
import org.eclipse.daanse.olap.xmla.connector.rowset.provider.multidimensional.MdschemaMeasuregroupDimensionsProvider;
import org.eclipse.daanse.olap.xmla.connector.rowset.provider.multidimensional.MdschemaInputDatasourcesProvider;
import org.eclipse.daanse.olap.xmla.connector.rowset.provider.multidimensional.MdschemaMeasuregroupsProvider;
import org.eclipse.daanse.olap.xmla.connector.rowset.provider.multidimensional.MdschemaMeasuresProvider;
import org.eclipse.daanse.olap.xmla.connector.rowset.provider.multidimensional.MdschemaMembersProvider;
import org.eclipse.daanse.olap.xmla.connector.rowset.provider.multidimensional.MdschemaPropertiesProvider;
import org.eclipse.daanse.olap.xmla.connector.rowset.provider.multidimensional.MdschemaSetsProvider;
import org.eclipse.daanse.olap.xmla.connector.rowset.provider.relational.oledb.DbschemaAssertionsProvider;
import org.eclipse.daanse.olap.xmla.connector.rowset.provider.relational.DbschemaCatalogsProvider;
import org.eclipse.daanse.olap.xmla.connector.rowset.provider.relational.oledb.DbschemaCharacterSetsProvider;
import org.eclipse.daanse.olap.xmla.connector.rowset.provider.relational.oledb.DbschemaCheckConstraintsByTableProvider;
import org.eclipse.daanse.olap.xmla.connector.rowset.provider.relational.oledb.DbschemaCheckConstraintsProvider;
import org.eclipse.daanse.olap.xmla.connector.rowset.provider.relational.oledb.DbschemaCollationsProvider;
import org.eclipse.daanse.olap.xmla.connector.rowset.provider.relational.oledb.DbschemaColumnDomainUsageProvider;
import org.eclipse.daanse.olap.xmla.connector.rowset.provider.relational.oledb.DbschemaColumnPrivilegesProvider;
import org.eclipse.daanse.olap.xmla.connector.rowset.provider.relational.DbschemaColumnsProvider;
import org.eclipse.daanse.olap.xmla.connector.rowset.provider.relational.oledb.DbschemaConstraintColumnUsageProvider;
import org.eclipse.daanse.olap.xmla.connector.rowset.provider.relational.oledb.DbschemaConstraintTableUsageProvider;
import org.eclipse.daanse.olap.xmla.connector.rowset.provider.relational.oledb.DbschemaForeignKeysProvider;
import org.eclipse.daanse.olap.xmla.connector.rowset.provider.relational.oledb.DbschemaIndexesProvider;
import org.eclipse.daanse.olap.xmla.connector.rowset.provider.relational.oledb.DbschemaKeyColumnUsageProvider;
import org.eclipse.daanse.olap.xmla.connector.rowset.provider.relational.oledb.DbschemaPrimaryKeysProvider;
import org.eclipse.daanse.olap.xmla.connector.rowset.provider.relational.oledb.DbschemaProcedureColumnsProvider;
import org.eclipse.daanse.olap.xmla.connector.rowset.provider.relational.oledb.DbschemaProcedureParametersProvider;
import org.eclipse.daanse.olap.xmla.connector.rowset.provider.relational.oledb.DbschemaProceduresProvider;
import org.eclipse.daanse.olap.xmla.connector.rowset.provider.relational.DbschemaProviderTypesProvider;
import org.eclipse.daanse.olap.xmla.connector.rowset.provider.relational.oledb.DbschemaReferentialConstraintsProvider;
import org.eclipse.daanse.olap.xmla.connector.rowset.provider.relational.DbschemaSchemataProvider;
import org.eclipse.daanse.olap.xmla.connector.rowset.provider.relational.DbschemaSourceTablesProvider;
import org.eclipse.daanse.olap.xmla.connector.rowset.provider.relational.oledb.DbschemaSqlLanguagesProvider;
import org.eclipse.daanse.olap.xmla.connector.rowset.provider.relational.oledb.DbschemaStatisticsProvider;
import org.eclipse.daanse.olap.xmla.connector.rowset.provider.relational.oledb.DbschemaTableConstraintsProvider;
import org.eclipse.daanse.olap.xmla.connector.rowset.provider.relational.oledb.DbschemaTablePrivilegesProvider;
import org.eclipse.daanse.olap.xmla.connector.rowset.provider.relational.oledb.DbschemaTableStatisticsProvider;
import org.eclipse.daanse.olap.xmla.connector.rowset.provider.relational.DbschemaTablesInfoProvider;
import org.eclipse.daanse.olap.xmla.connector.rowset.provider.relational.DbschemaTablesProvider;
import org.eclipse.daanse.olap.xmla.connector.rowset.provider.relational.oledb.DbschemaTranslationsProvider;
import org.eclipse.daanse.olap.xmla.connector.rowset.provider.relational.oledb.DbschemaTrusteeProvider;
import org.eclipse.daanse.olap.xmla.connector.rowset.provider.relational.oledb.DbschemaUsagePrivilegesProvider;
import org.eclipse.daanse.olap.xmla.connector.rowset.provider.relational.oledb.DbschemaViewColumnUsageProvider;
import org.eclipse.daanse.olap.xmla.connector.rowset.provider.relational.oledb.DbschemaViewTableUsageProvider;
import org.eclipse.daanse.olap.xmla.connector.rowset.provider.relational.oledb.DbschemaViewsProvider;

/**
 * The whiteboard's registrations, as a test would have them: the same mapping
 * of request type to provider that OSGi builds from the service properties,
 * without a framework.
 */
final class Providers {

    private static final Map<String, Supplier<RowsetProvider<ContextListSupplyer>>> BY_REQUEST_TYPE = new LinkedHashMap<>();

    static {
        BY_REQUEST_TYPE.put("DISCOVER_PROPERTIES", DiscoverPropertiesProvider::new);
        BY_REQUEST_TYPE.put("DISCOVER_SCHEMA_ROWSETS", DiscoverSchemaRowsetsProvider::new);
        BY_REQUEST_TYPE.put("DISCOVER_DATASOURCES", DiscoverDatasourcesProvider::new);
        BY_REQUEST_TYPE.put("DISCOVER_ENUMERATORS", DiscoverEnumeratorsProvider::new);
        BY_REQUEST_TYPE.put("DISCOVER_KEYWORDS", DiscoverKeywordsProvider::new);
        BY_REQUEST_TYPE.put("DISCOVER_LITERALS", DiscoverLiteralsProvider::new);
        BY_REQUEST_TYPE.put("DISCOVER_XML_METADATA", DiscoverXmlMetadataProvider::new);
        BY_REQUEST_TYPE.put("DISCOVER_CSDL_METADATA", DiscoverCsdlMetadataProvider::new);
        BY_REQUEST_TYPE.put("DBSCHEMA_CATALOGS", DbschemaCatalogsProvider::new);
        BY_REQUEST_TYPE.put("DBSCHEMA_SCHEMATA", DbschemaSchemataProvider::new);
        BY_REQUEST_TYPE.put("DBSCHEMA_TABLES", DbschemaTablesProvider::new);
        BY_REQUEST_TYPE.put("DBSCHEMA_COLUMNS", DbschemaColumnsProvider::new);
        BY_REQUEST_TYPE.put("DBSCHEMA_PROVIDER_TYPES", DbschemaProviderTypesProvider::new);
        BY_REQUEST_TYPE.put("DBSCHEMA_TABLES_INFO", DbschemaTablesInfoProvider::new);
        BY_REQUEST_TYPE.put("DBSCHEMA_SOURCE_TABLES", DbschemaSourceTablesProvider::new);

        // The OLE DB schema rowsets beyond the four [MS-SSAS] names. They answer no
        // rows yet, but they are registered so the advertised list and the dispatch
        // agree - an unregistered request type is a fault, an empty rowset is not.
        BY_REQUEST_TYPE.put("DBSCHEMA_ASSERTIONS", DbschemaAssertionsProvider::new);
        BY_REQUEST_TYPE.put("DBSCHEMA_CHARACTER_SETS", DbschemaCharacterSetsProvider::new);
        BY_REQUEST_TYPE.put("DBSCHEMA_CHECK_CONSTRAINTS", DbschemaCheckConstraintsProvider::new);
        BY_REQUEST_TYPE.put("DBSCHEMA_CHECK_CONSTRAINTS_BY_TABLE", DbschemaCheckConstraintsByTableProvider::new);
        BY_REQUEST_TYPE.put("DBSCHEMA_COLLATIONS", DbschemaCollationsProvider::new);
        BY_REQUEST_TYPE.put("DBSCHEMA_COLUMN_DOMAIN_USAGE", DbschemaColumnDomainUsageProvider::new);
        BY_REQUEST_TYPE.put("DBSCHEMA_COLUMN_PRIVILEGES", DbschemaColumnPrivilegesProvider::new);
        BY_REQUEST_TYPE.put("DBSCHEMA_CONSTRAINT_COLUMN_USAGE", DbschemaConstraintColumnUsageProvider::new);
        BY_REQUEST_TYPE.put("DBSCHEMA_CONSTRAINT_TABLE_USAGE", DbschemaConstraintTableUsageProvider::new);
        BY_REQUEST_TYPE.put("DBSCHEMA_FOREIGN_KEYS", DbschemaForeignKeysProvider::new);
        BY_REQUEST_TYPE.put("DBSCHEMA_INDEXES", DbschemaIndexesProvider::new);
        BY_REQUEST_TYPE.put("DBSCHEMA_KEY_COLUMN_USAGE", DbschemaKeyColumnUsageProvider::new);
        BY_REQUEST_TYPE.put("DBSCHEMA_PRIMARY_KEYS", DbschemaPrimaryKeysProvider::new);
        BY_REQUEST_TYPE.put("DBSCHEMA_PROCEDURES", DbschemaProceduresProvider::new);
        BY_REQUEST_TYPE.put("DBSCHEMA_PROCEDURE_COLUMNS", DbschemaProcedureColumnsProvider::new);
        BY_REQUEST_TYPE.put("DBSCHEMA_PROCEDURE_PARAMETERS", DbschemaProcedureParametersProvider::new);
        BY_REQUEST_TYPE.put("DBSCHEMA_REFERENTIAL_CONSTRAINTS", DbschemaReferentialConstraintsProvider::new);
        BY_REQUEST_TYPE.put("DBSCHEMA_SQL_LANGUAGES", DbschemaSqlLanguagesProvider::new);
        BY_REQUEST_TYPE.put("DBSCHEMA_STATISTICS", DbschemaStatisticsProvider::new);
        BY_REQUEST_TYPE.put("DBSCHEMA_TABLE_CONSTRAINTS", DbschemaTableConstraintsProvider::new);
        BY_REQUEST_TYPE.put("DBSCHEMA_TABLE_PRIVILEGES", DbschemaTablePrivilegesProvider::new);
        BY_REQUEST_TYPE.put("DBSCHEMA_TABLE_STATISTICS", DbschemaTableStatisticsProvider::new);
        BY_REQUEST_TYPE.put("DBSCHEMA_TRANSLATIONS", DbschemaTranslationsProvider::new);
        BY_REQUEST_TYPE.put("DBSCHEMA_USAGE_PRIVILEGES", DbschemaUsagePrivilegesProvider::new);
        BY_REQUEST_TYPE.put("DBSCHEMA_VIEWS", DbschemaViewsProvider::new);
        BY_REQUEST_TYPE.put("DBSCHEMA_VIEW_COLUMN_USAGE", DbschemaViewColumnUsageProvider::new);
        BY_REQUEST_TYPE.put("DBSCHEMA_VIEW_TABLE_USAGE", DbschemaViewTableUsageProvider::new);
        // Beyond Appendix B: a later OLE DB addition, documented only through
        // System.Data.OleDb.
        BY_REQUEST_TYPE.put("DBSCHEMA_TRUSTEE", DbschemaTrusteeProvider::new);

        BY_REQUEST_TYPE.put("MDSCHEMA_ACTIONS", MdschemaActionsProvider::new);
        BY_REQUEST_TYPE.put("MDSCHEMA_CUBES", MdschemaCubesProvider::new);
        BY_REQUEST_TYPE.put("MDSCHEMA_DIMENSIONS", MdschemaDimensionsProvider::new);
        BY_REQUEST_TYPE.put("MDSCHEMA_HIERARCHIES", MdschemaHierarchiesProvider::new);
        BY_REQUEST_TYPE.put("MDSCHEMA_LEVELS", MdschemaLevelsProvider::new);
        BY_REQUEST_TYPE.put("MDSCHEMA_MEASURES", MdschemaMeasuresProvider::new);
        BY_REQUEST_TYPE.put("MDSCHEMA_INPUT_DATASOURCES", MdschemaInputDatasourcesProvider::new);
        BY_REQUEST_TYPE.put("MDSCHEMA_MEASUREGROUPS", MdschemaMeasuregroupsProvider::new);
        BY_REQUEST_TYPE.put("MDSCHEMA_MEASUREGROUP_DIMENSIONS", MdschemaMeasuregroupDimensionsProvider::new);
        BY_REQUEST_TYPE.put("MDSCHEMA_MEMBERS", MdschemaMembersProvider::new);
        BY_REQUEST_TYPE.put("MDSCHEMA_PROPERTIES", MdschemaPropertiesProvider::new);
        BY_REQUEST_TYPE.put("MDSCHEMA_KPIS", MdschemaKpisProvider::new);
        BY_REQUEST_TYPE.put("MDSCHEMA_SETS", MdschemaSetsProvider::new);
        BY_REQUEST_TYPE.put("MDSCHEMA_FUNCTIONS", MdschemaFunctionsProvider::new);
    }

    private Providers() {
        // static access only
    }

    static RowsetProvider<ContextListSupplyer> of(String requestType) {
        Supplier<RowsetProvider<ContextListSupplyer>> supplier = BY_REQUEST_TYPE.get(requestType);
        if (supplier == null) {
            // A rowset nothing provides: the tests that ask for one want the empty answer,
            // the connector itself refuses - that difference is the whiteboard test's
            // subject.
            return scope -> java.util.List.of();
        }
        return supplier.get();
    }

    static Set<String> served() {
        return BY_REQUEST_TYPE.keySet();
    }
}
