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

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

import org.eclipse.daanse.olap.xmla.connector.ContextListSupplyer;
import org.eclipse.daanse.xmla.api.RowsetProvider;
import org.eclipse.daanse.olap.xmla.connector.rowset.provider.*;

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
        BY_REQUEST_TYPE.put("MDSCHEMA_ACTIONS", MdschemaActionsProvider::new);
        BY_REQUEST_TYPE.put("MDSCHEMA_CUBES", MdschemaCubesProvider::new);
        BY_REQUEST_TYPE.put("MDSCHEMA_DIMENSIONS", MdschemaDimensionsProvider::new);
        BY_REQUEST_TYPE.put("MDSCHEMA_HIERARCHIES", MdschemaHierarchiesProvider::new);
        BY_REQUEST_TYPE.put("MDSCHEMA_LEVELS", MdschemaLevelsProvider::new);
        BY_REQUEST_TYPE.put("MDSCHEMA_MEASURES", MdschemaMeasuresProvider::new);
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
