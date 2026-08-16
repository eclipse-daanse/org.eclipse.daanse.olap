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

import java.util.Map;

import org.eclipse.daanse.olap.api.ContextGroup;
import org.eclipse.daanse.olap.xmla.connector.rowset.provider.relational.DbschemaCatalogsProvider;
import org.eclipse.daanse.olap.xmla.connector.rowset.provider.relational.DbschemaColumnsProvider;
import org.eclipse.daanse.olap.xmla.connector.rowset.provider.relational.DbschemaProviderTypesProvider;
import org.eclipse.daanse.olap.xmla.connector.rowset.provider.relational.DbschemaSchemataProvider;
import org.eclipse.daanse.olap.xmla.connector.rowset.provider.relational.DbschemaSourceTablesProvider;
import org.eclipse.daanse.olap.xmla.connector.rowset.provider.relational.DbschemaTablesInfoProvider;
import org.eclipse.daanse.olap.xmla.connector.rowset.provider.relational.DbschemaTablesProvider;
import org.eclipse.daanse.olap.xmla.connector.rowset.provider.core.DiscoverCsdlMetadataProvider;
import org.eclipse.daanse.olap.xmla.connector.rowset.provider.core.DiscoverDatasourcesProvider;
import org.eclipse.daanse.olap.xmla.connector.rowset.provider.core.DiscoverEnumeratorsProvider;
import org.eclipse.daanse.olap.xmla.connector.rowset.provider.core.DiscoverKeywordsProvider;
import org.eclipse.daanse.olap.xmla.connector.rowset.provider.core.DiscoverSessionsProvider;
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
import org.eclipse.daanse.olap.xmla.connector.api.ocd.SessionConfig;
import org.eclipse.daanse.xmla.api.RowsetProvider;
import org.osgi.framework.ServiceReference;
import org.osgi.service.component.ComponentServiceObjects;
import org.eclipse.daanse.olap.xmla.connector.rowset.provider.relational.oledb.DbschemaAssertionsProvider;
import org.eclipse.daanse.olap.xmla.connector.rowset.provider.relational.oledb.DbschemaCharacterSetsProvider;
import org.eclipse.daanse.olap.xmla.connector.rowset.provider.relational.oledb.DbschemaCheckConstraintsByTableProvider;
import org.eclipse.daanse.olap.xmla.connector.rowset.provider.relational.oledb.DbschemaCheckConstraintsProvider;
import org.eclipse.daanse.olap.xmla.connector.rowset.provider.relational.oledb.DbschemaCollationsProvider;
import org.eclipse.daanse.olap.xmla.connector.rowset.provider.relational.oledb.DbschemaColumnDomainUsageProvider;
import org.eclipse.daanse.olap.xmla.connector.rowset.provider.relational.oledb.DbschemaColumnPrivilegesProvider;
import org.eclipse.daanse.olap.xmla.connector.rowset.provider.relational.oledb.DbschemaConstraintColumnUsageProvider;
import org.eclipse.daanse.olap.xmla.connector.rowset.provider.relational.oledb.DbschemaConstraintTableUsageProvider;
import org.eclipse.daanse.olap.xmla.connector.rowset.provider.relational.oledb.DbschemaForeignKeysProvider;
import org.eclipse.daanse.olap.xmla.connector.rowset.provider.relational.oledb.DbschemaIndexesProvider;
import org.eclipse.daanse.olap.xmla.connector.rowset.provider.relational.oledb.DbschemaKeyColumnUsageProvider;
import org.eclipse.daanse.olap.xmla.connector.rowset.provider.relational.oledb.DbschemaPrimaryKeysProvider;
import org.eclipse.daanse.olap.xmla.connector.rowset.provider.relational.oledb.DbschemaProcedureColumnsProvider;
import org.eclipse.daanse.olap.xmla.connector.rowset.provider.relational.oledb.DbschemaProcedureParametersProvider;
import org.eclipse.daanse.olap.xmla.connector.rowset.provider.relational.oledb.DbschemaProceduresProvider;
import org.eclipse.daanse.olap.xmla.connector.rowset.provider.relational.oledb.DbschemaReferentialConstraintsProvider;
import org.eclipse.daanse.olap.xmla.connector.rowset.provider.relational.oledb.DbschemaSqlLanguagesProvider;
import org.eclipse.daanse.olap.xmla.connector.rowset.provider.relational.oledb.DbschemaStatisticsProvider;
import org.eclipse.daanse.olap.xmla.connector.rowset.provider.relational.oledb.DbschemaTableConstraintsProvider;
import org.eclipse.daanse.olap.xmla.connector.rowset.provider.relational.oledb.DbschemaTablePrivilegesProvider;
import org.eclipse.daanse.olap.xmla.connector.rowset.provider.relational.oledb.DbschemaTableStatisticsProvider;
import org.eclipse.daanse.olap.xmla.connector.rowset.provider.relational.oledb.DbschemaTranslationsProvider;
import org.eclipse.daanse.olap.xmla.connector.rowset.provider.relational.oledb.DbschemaTrusteeProvider;
import org.eclipse.daanse.olap.xmla.connector.rowset.provider.relational.oledb.DbschemaUsagePrivilegesProvider;
import org.eclipse.daanse.olap.xmla.connector.rowset.provider.relational.oledb.DbschemaViewColumnUsageProvider;
import org.eclipse.daanse.olap.xmla.connector.rowset.provider.relational.oledb.DbschemaViewTableUsageProvider;
import org.eclipse.daanse.olap.xmla.connector.rowset.provider.relational.oledb.DbschemaViewsProvider;
import org.eclipse.daanse.olap.xmla.connector.ContextListSupplyer;
import org.eclipse.daanse.olap.xmla.connector.OlapXmlaConnector;

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
        // Embedded: no configuration admin, so the annotation's own defaults stand.
        return OlapXmlaConnector.assemble(contextGroup, providers(), DiscoverSessionsProvider::new, defaults());
    }

    /** What the metatype declares, for an embedding that has no configuration. */
    private static SessionConfig defaults() {
        return (SessionConfig) java.lang.reflect.Proxy.newProxyInstance(SessionConfig.class.getClassLoader(),
                new Class<?>[] { SessionConfig.class }, (proxy, method, args) -> method.getDefaultValue());
    }

    /**
     * The rowset services, spelled out with the request types their component
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
        providers.put("MDSCHEMA_INPUT_DATASOURCES", new MdschemaInputDatasourcesProvider());
        providers.put("MDSCHEMA_MEASUREGROUPS", new MdschemaMeasuregroupsProvider());
        providers.put("MDSCHEMA_MEASURES", new MdschemaMeasuresProvider());
        providers.put("MDSCHEMA_MEMBERS", new MdschemaMembersProvider());
        providers.put("MDSCHEMA_PROPERTIES", new MdschemaPropertiesProvider());
        providers.put("MDSCHEMA_SETS", new MdschemaSetsProvider());

        // The OLE DB schema rowsets beyond the four [MS-SSAS] names. They answer no
        // rows yet; they are registered so an embedded server offers the same request
        // types an OSGi one does.
        providers.put("DBSCHEMA_ASSERTIONS", new DbschemaAssertionsProvider());
        providers.put("DBSCHEMA_CHARACTER_SETS", new DbschemaCharacterSetsProvider());
        providers.put("DBSCHEMA_CHECK_CONSTRAINTS", new DbschemaCheckConstraintsProvider());
        providers.put("DBSCHEMA_CHECK_CONSTRAINTS_BY_TABLE", new DbschemaCheckConstraintsByTableProvider());
        providers.put("DBSCHEMA_COLLATIONS", new DbschemaCollationsProvider());
        providers.put("DBSCHEMA_COLUMN_DOMAIN_USAGE", new DbschemaColumnDomainUsageProvider());
        providers.put("DBSCHEMA_COLUMN_PRIVILEGES", new DbschemaColumnPrivilegesProvider());
        providers.put("DBSCHEMA_CONSTRAINT_COLUMN_USAGE", new DbschemaConstraintColumnUsageProvider());
        providers.put("DBSCHEMA_CONSTRAINT_TABLE_USAGE", new DbschemaConstraintTableUsageProvider());
        providers.put("DBSCHEMA_FOREIGN_KEYS", new DbschemaForeignKeysProvider());
        providers.put("DBSCHEMA_INDEXES", new DbschemaIndexesProvider());
        providers.put("DBSCHEMA_KEY_COLUMN_USAGE", new DbschemaKeyColumnUsageProvider());
        providers.put("DBSCHEMA_PRIMARY_KEYS", new DbschemaPrimaryKeysProvider());
        providers.put("DBSCHEMA_PROCEDURES", new DbschemaProceduresProvider());
        providers.put("DBSCHEMA_PROCEDURE_COLUMNS", new DbschemaProcedureColumnsProvider());
        providers.put("DBSCHEMA_PROCEDURE_PARAMETERS", new DbschemaProcedureParametersProvider());
        providers.put("DBSCHEMA_REFERENTIAL_CONSTRAINTS", new DbschemaReferentialConstraintsProvider());
        providers.put("DBSCHEMA_SQL_LANGUAGES", new DbschemaSqlLanguagesProvider());
        providers.put("DBSCHEMA_STATISTICS", new DbschemaStatisticsProvider());
        providers.put("DBSCHEMA_TABLE_CONSTRAINTS", new DbschemaTableConstraintsProvider());
        providers.put("DBSCHEMA_TABLE_PRIVILEGES", new DbschemaTablePrivilegesProvider());
        providers.put("DBSCHEMA_TABLE_STATISTICS", new DbschemaTableStatisticsProvider());
        providers.put("DBSCHEMA_TRANSLATIONS", new DbschemaTranslationsProvider());
        providers.put("DBSCHEMA_TRUSTEE", new DbschemaTrusteeProvider());
        providers.put("DBSCHEMA_USAGE_PRIVILEGES", new DbschemaUsagePrivilegesProvider());
        providers.put("DBSCHEMA_VIEWS", new DbschemaViewsProvider());
        providers.put("DBSCHEMA_VIEW_COLUMN_USAGE", new DbschemaViewColumnUsageProvider());
        providers.put("DBSCHEMA_VIEW_TABLE_USAGE", new DbschemaViewTableUsageProvider());

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
