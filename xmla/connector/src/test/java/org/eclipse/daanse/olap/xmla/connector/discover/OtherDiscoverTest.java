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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.eclipse.daanse.olap.api.Context;
import org.eclipse.daanse.olap.xmla.connector.ContextListSupplyer;
import org.eclipse.daanse.xmla.api.RowsetProvider;
import org.eclipse.daanse.xmla.api.RowsetScope;
import org.eclipse.daanse.xmla.model.rowset.core.DiscoverDatasourcesRow;
import org.eclipse.daanse.xmla.model.rowset.core.DiscoverKeywordsRow;
import org.eclipse.daanse.xmla.model.rowset.core.DiscoverLiteralsRow;
import org.eclipse.daanse.xmla.model.rowset.core.DiscoverPropertiesRow;
import org.eclipse.daanse.xmla.api.XmlaRequest;
import org.eclipse.emf.ecore.EObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

/**
 * The bridge's {@code OtherDiscoverServiceTest}, ported onto the connector's
 * EMF rows.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class OtherDiscoverTest {

    @Mock
    private Context<?> context1;
    @Mock
    private Context<?> context2;
    @Mock
    private ContextListSupplyer contexts;

    private final XmlaRequest anonymous = XmlaRequest.anonymous();

    @BeforeEach
    void setup() {
    }

    /**
     * One rowset through its provider - the whiteboard's dispatch, minus OSGi: the
     * provider for this request type, called with the scope the connector would
     * have built.
     */
    private List<EObject> discover(String requestType, Map<String, String> restrictions) {
        RowsetProvider<ContextListSupplyer> provider = Providers.of(requestType);
        return provider.rows(
                RowsetScope.of(Requests.discover(requestType, restrictions), anonymous, contexts, Providers.served()));
    }

    @Test
    void dataSources() {
        when(contexts.getContexts()).thenReturn(List.of(context1, context2));
        when(context1.getName()).thenReturn("foo");
        when(context1.getDescription()).thenReturn(Optional.of("fooDescription"));
        when(context2.getName()).thenReturn("bar");
        when(context2.getDescription()).thenReturn(Optional.empty());

        List<EObject> rows = discover("DISCOVER_DATASOURCES", Map.of());
        assertThat(rows).hasSize(2);
        DiscoverDatasourcesRow row = (DiscoverDatasourcesRow) rows.get(0);
        assertThat(row.getDataSourceName()).isEqualTo("DataSource of foo");
        assertThat(row.getDataSourceDescription()).isEqualTo("fooDescription");
        assertThat(row.getProviderName()).isEqualTo("Daanse");
        assertThat(row.getProviderType()).containsExactly("MDP");
        assertThat(row.getAuthenticationMode()).isEqualTo("Unauthenticated");
    }

    @Test
    void keywordsComeFromTheFirstContext() {
        when(contexts.getContexts()).thenReturn(List.of(context1, context2));
        when(context1.getKeywordList()).thenReturn(List.of("SELECT", "FROM"));

        List<EObject> rows = discover("DISCOVER_KEYWORDS", Map.of());
        assertThat(rows).extracting(row -> ((DiscoverKeywordsRow) row).getKeyword()).containsExactly("SELECT", "FROM");
    }

    @Test
    void literalsCarryTheDbliteralPrefix() {
        List<EObject> rows = discover("DISCOVER_LITERALS", Map.of());
        assertThat(rows).isNotEmpty();
        assertThat(rows)
                .allSatisfy(row -> assertThat(((DiscoverLiteralsRow) row).getLiteralName()).startsWith("DBLITERAL_"));
    }

    @Test
    void enumeratorsAreServed() {
        assertThat(discover("DISCOVER_ENUMERATORS", Map.of())).isNotEmpty();
    }

    @Test
    void propertiesAnswerTheCatalogWithTheCurrentOne() {
        when(contexts.getContexts()).thenReturn(List.of(context1));
        when(context1.getName()).thenReturn("foo");

        List<EObject> rows = discover("DISCOVER_PROPERTIES", Map.of("PropertyName", "Catalog"));
        assertThat(rows).hasSize(1);
        DiscoverPropertiesRow row = (DiscoverPropertiesRow) rows.get(0);
        assertThat(row.getPropertyName()).isEqualTo("Catalog");
        assertThat(row.getValue()).isEqualTo("foo");
    }

    /**
     * DISCOVER_XML_METADATA answers the engine model's DDL, serialized into its own
     * namespace - the hand-written template used to inherit the rowset namespace,
     * which no live server does.
     */
    @Test
    void theServerDefinitionIsTheEngineModelsInItsNamespace() {
        org.eclipse.daanse.olap.api.element.Catalog catalog = org.mockito.Mockito
                .mock(org.eclipse.daanse.olap.api.element.Catalog.class);
        when(catalog.getName()).thenReturn("FoodMart");
        when(contexts.get(org.mockito.ArgumentMatchers.any(org.eclipse.daanse.xmla.api.XmlaRequest.class)))
                .thenReturn(List.of(catalog));

        List<EObject> rows = discover("DISCOVER_XML_METADATA", Map.of());
        assertThat(rows).hasSize(1);
        String document = (String) rows.get(0).eGet(rows.get(0).eClass().getEStructuralFeature("metaData"));
        assertThat(document).contains("xmlns=\"http://schemas.microsoft.com/analysisservices/2003/engine\"")
                .contains("<Name>FoodMart</Name>").contains("<Version>11.0.7001.0</Version>")
                .contains("<Edition>Enterprise64</Edition>").contains("<EditionID>1804890536</EditionID>")
                .contains("<Databases>");
    }

    /**
     * A DatabaseID restriction selects that Database as the root, the way SSMS
     * asks.
     */
    @Test
    void aDatabaseIdMakesTheDatabaseTheRoot() {
        org.eclipse.daanse.olap.api.element.Catalog catalog = org.mockito.Mockito
                .mock(org.eclipse.daanse.olap.api.element.Catalog.class);
        when(catalog.getName()).thenReturn("FoodMart");
        when(contexts.tryGetFirstByName(org.mockito.ArgumentMatchers.eq("FoodMart"),
                org.mockito.ArgumentMatchers.any(org.eclipse.daanse.xmla.api.XmlaRequest.class)))
                .thenReturn(java.util.Optional.of(catalog));

        List<EObject> rows = discover("DISCOVER_XML_METADATA", Map.of("DatabaseID", "FoodMart"));
        String document = (String) rows.get(0).eGet(rows.get(0).eClass().getEStructuralFeature("metaData"));
        assertThat(document).startsWith("<Database").doesNotContain("<Server");
    }

    /** The reference expansions leave the Databases children out. */
    @Test
    void referenceOnlyCarriesNoDatabases() {
        org.eclipse.daanse.olap.api.element.Catalog catalog = org.mockito.Mockito
                .mock(org.eclipse.daanse.olap.api.element.Catalog.class);
        when(catalog.getName()).thenReturn("FoodMart");
        when(contexts.get(org.mockito.ArgumentMatchers.any(org.eclipse.daanse.xmla.api.XmlaRequest.class)))
                .thenReturn(List.of(catalog));

        List<EObject> rows = discover("DISCOVER_XML_METADATA", Map.of("ObjectExpansion", "ReferenceOnly"));
        String document = (String) rows.get(0).eGet(rows.get(0).eClass().getEStructuralFeature("metaData"));
        assertThat(document).contains("<Server").doesNotContain("<Databases>");
    }

    @Test
    void theSelfDescriptionNamesExactlyTheRegisteredRowsets() {
        List<EObject> rows = discover("DISCOVER_SCHEMA_ROWSETS", Map.of());

        // Generated from the whiteboard's registrations, not from the model's full
        // list:
        // what the server announces is what it can be asked.
        assertThat(rows).hasSize(Providers.served().size());
        assertThat(rows).extracting(row -> row.eGet(row.eClass().getEStructuralFeature("schemaName")))
                .containsExactlyInAnyOrderElementsOf(Providers.served());
    }

    @Test
    void theSelfDescriptionCarriesTheModelsRestrictionsAndMask() {
        List<EObject> rows = discover("DISCOVER_SCHEMA_ROWSETS", Map.of("SchemaName", "MDSCHEMA_MEMBERS"));

        assertThat(rows).hasSize(1);
        EObject row = rows.get(0);
        assertThat(row.eGet(row.eClass().getEStructuralFeature("restrictionsMask")))
                .isEqualTo(org.eclipse.daanse.xmla.model.io.RowsetCatalog
                        .restrictionsMaskOf(org.eclipse.daanse.xmla.model.io.RowsetCatalog
                                .forRequestType("MDSCHEMA_MEMBERS").orElseThrow()));
        @SuppressWarnings("unchecked")
        List<EObject> restrictions = (List<EObject>) row.eGet(row.eClass().getEStructuralFeature("restrictions"));
        assertThat(restrictions).isNotEmpty();
        assertThat(restrictions).extracting(one -> one.eGet(one.eClass().getEStructuralFeature("name")))
                .contains("CATALOG_NAME", "TREE_OP");
    }
}
