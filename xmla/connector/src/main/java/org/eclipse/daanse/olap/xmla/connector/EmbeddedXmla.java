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
package org.eclipse.daanse.olap.xmla.connector;

import java.util.Map;

import org.eclipse.daanse.olap.api.ContextGroup;
import org.eclipse.daanse.olap.xmla.connector.rowset.provider.DbschemaCatalogsProvider;
import org.eclipse.daanse.olap.xmla.connector.rowset.provider.DbschemaColumnsProvider;
import org.eclipse.daanse.olap.xmla.connector.rowset.provider.DbschemaProviderTypesProvider;
import org.eclipse.daanse.olap.xmla.connector.rowset.provider.DbschemaSchemataProvider;
import org.eclipse.daanse.olap.xmla.connector.rowset.provider.DbschemaSourceTablesProvider;
import org.eclipse.daanse.olap.xmla.connector.rowset.provider.DbschemaTablesInfoProvider;
import org.eclipse.daanse.olap.xmla.connector.rowset.provider.DbschemaTablesProvider;
import org.eclipse.daanse.olap.xmla.connector.rowset.provider.DiscoverCsdlMetadataProvider;
import org.eclipse.daanse.olap.xmla.connector.rowset.provider.DiscoverDatasourcesProvider;
import org.eclipse.daanse.olap.xmla.connector.rowset.provider.DiscoverEnumeratorsProvider;
import org.eclipse.daanse.olap.xmla.connector.rowset.provider.DiscoverKeywordsProvider;
import org.eclipse.daanse.olap.xmla.connector.rowset.provider.DiscoverSessionsProvider;
import org.eclipse.daanse.olap.xmla.connector.rowset.provider.DiscoverLiteralsProvider;
import org.eclipse.daanse.olap.xmla.connector.rowset.provider.DiscoverPropertiesProvider;
import org.eclipse.daanse.olap.xmla.connector.rowset.provider.DiscoverSchemaRowsetsProvider;
import org.eclipse.daanse.olap.xmla.connector.rowset.provider.DiscoverXmlMetadataProvider;
import org.eclipse.daanse.olap.xmla.connector.rowset.provider.MdschemaActionsProvider;
import org.eclipse.daanse.olap.xmla.connector.rowset.provider.MdschemaCubesProvider;
import org.eclipse.daanse.olap.xmla.connector.rowset.provider.MdschemaDimensionsProvider;
import org.eclipse.daanse.olap.xmla.connector.rowset.provider.MdschemaFunctionsProvider;
import org.eclipse.daanse.olap.xmla.connector.rowset.provider.MdschemaHierarchiesProvider;
import org.eclipse.daanse.olap.xmla.connector.rowset.provider.MdschemaKpisProvider;
import org.eclipse.daanse.olap.xmla.connector.rowset.provider.MdschemaLevelsProvider;
import org.eclipse.daanse.olap.xmla.connector.rowset.provider.MdschemaMeasuregroupDimensionsProvider;
import org.eclipse.daanse.olap.xmla.connector.rowset.provider.MdschemaMeasuregroupsProvider;
import org.eclipse.daanse.olap.xmla.connector.rowset.provider.MdschemaMeasuresProvider;
import org.eclipse.daanse.olap.xmla.connector.rowset.provider.MdschemaMembersProvider;
import org.eclipse.daanse.olap.xmla.connector.rowset.provider.MdschemaPropertiesProvider;
import org.eclipse.daanse.olap.xmla.connector.rowset.provider.MdschemaSetsProvider;
import org.eclipse.daanse.olap.xmla.connector.api.ocd.SessionConfig;
import org.eclipse.daanse.xmla.api.RowsetProvider;
import org.osgi.framework.ServiceReference;
import org.osgi.service.component.ComponentServiceObjects;

/**
 * The connector, assembled by hand for embedded use.
 * <p>
 * In an OSGi runtime the whiteboard assembles itself: every provider registers
 * as a service and the connector binds whatever appears. An embedded host - a
 * test harness, an in-process endpoint - has no registry, so this factory
 * performs the same assembly in plain Java: the same 27 providers, the same
 * dispatch, the same refusals. What it cannot offer is the dynamism; an
 * embedded connector serves exactly this set.
 */
public final class EmbeddedXmla {

    private EmbeddedXmla() {
        // static access only
    }

    public static OlapXmlaConnector connector(ContextGroup contextGroup) {
        OlapXmlaConnector connector = new OlapXmlaConnector();
        connector.bindContextGroup(contextGroup);
        providers().forEach((requestType, provider) -> connector.bindRowsetProvider(fixed(provider),
                Map.of(RowsetProvider.PROPERTY_REQUEST_TYPE, requestType)));
        // The only provider that needs the connector itself: it reports the connector's
        // own sessions.
        connector.bindRowsetProvider(fixed(new DiscoverSessionsProvider(connector)),
                Map.of(RowsetProvider.PROPERTY_REQUEST_TYPE, "DISCOVER_SESSIONS"));
        // Embedded: no configuration admin, so the annotation's own defaults stand.
        connector.activate(defaults());
        return connector;
    }

    /** What the metatype declares, for an embedding that has no configuration. */
    private static SessionConfig defaults() {
        return (SessionConfig) java.lang.reflect.Proxy.newProxyInstance(SessionConfig.class.getClassLoader(),
                new Class<?>[] { SessionConfig.class }, (proxy, method, args) -> method.getDefaultValue());
    }

    /**
     * The 27 rowset services, spelled out with the request types their component
     * annotations name - the annotations are class-retained and unreadable here,
     * and a wrong pairing fails the whiteboard tests immediately.
     */
    private static Map<String, RowsetProvider<ContextListSupplyer>> providers() {
        Map<String, RowsetProvider<ContextListSupplyer>> providers = new java.util.LinkedHashMap<>();
        providers.put("DISCOVER_PROPERTIES", new DiscoverPropertiesProvider());
        providers.put("DISCOVER_SCHEMA_ROWSETS", new DiscoverSchemaRowsetsProvider());
        providers.put("DISCOVER_DATASOURCES", new DiscoverDatasourcesProvider());
        providers.put("DISCOVER_ENUMERATORS", new DiscoverEnumeratorsProvider());
        providers.put("DISCOVER_KEYWORDS", new DiscoverKeywordsProvider());
        providers.put("DISCOVER_LITERALS", new DiscoverLiteralsProvider());
        providers.put("DISCOVER_XML_METADATA", new DiscoverXmlMetadataProvider());
        providers.put("DISCOVER_CSDL_METADATA", new DiscoverCsdlMetadataProvider());
        providers.put("DBSCHEMA_CATALOGS", new DbschemaCatalogsProvider());
        providers.put("DBSCHEMA_SCHEMATA", new DbschemaSchemataProvider());
        providers.put("DBSCHEMA_TABLES", new DbschemaTablesProvider());
        providers.put("DBSCHEMA_COLUMNS", new DbschemaColumnsProvider());
        providers.put("DBSCHEMA_PROVIDER_TYPES", new DbschemaProviderTypesProvider());
        providers.put("DBSCHEMA_TABLES_INFO", new DbschemaTablesInfoProvider());
        providers.put("DBSCHEMA_SOURCE_TABLES", new DbschemaSourceTablesProvider());
        providers.put("MDSCHEMA_ACTIONS", new MdschemaActionsProvider());
        providers.put("MDSCHEMA_CUBES", new MdschemaCubesProvider());
        providers.put("MDSCHEMA_DIMENSIONS", new MdschemaDimensionsProvider());
        providers.put("MDSCHEMA_FUNCTIONS", new MdschemaFunctionsProvider());
        providers.put("MDSCHEMA_HIERARCHIES", new MdschemaHierarchiesProvider());
        providers.put("MDSCHEMA_KPIS", new MdschemaKpisProvider());
        providers.put("MDSCHEMA_LEVELS", new MdschemaLevelsProvider());
        providers.put("MDSCHEMA_MEASUREGROUP_DIMENSIONS", new MdschemaMeasuregroupDimensionsProvider());
        providers.put("MDSCHEMA_MEASUREGROUPS", new MdschemaMeasuregroupsProvider());
        providers.put("MDSCHEMA_MEASURES", new MdschemaMeasuresProvider());
        providers.put("MDSCHEMA_MEMBERS", new MdschemaMembersProvider());
        providers.put("MDSCHEMA_PROPERTIES", new MdschemaPropertiesProvider());
        providers.put("MDSCHEMA_SETS", new MdschemaSetsProvider());
        return providers;
    }

    /**
     * A fixed instance behind the ComponentServiceObjects the whiteboard consumes.
     */
    private static ComponentServiceObjects<RowsetProvider<ContextListSupplyer>> fixed(
            RowsetProvider<ContextListSupplyer> provider) {
        return new ComponentServiceObjects<>() {
            @Override
            public RowsetProvider<ContextListSupplyer> getService() {
                return provider;
            }

            @Override
            public void ungetService(RowsetProvider<ContextListSupplyer> service) {
                // a fixed instance is not pooled
            }

            @Override
            public ServiceReference<RowsetProvider<ContextListSupplyer>> getServiceReference() {
                throw new UnsupportedOperationException("no registry behind an embedded connector");
            }
        };
    }
}
