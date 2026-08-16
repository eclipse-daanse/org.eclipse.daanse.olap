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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.daanse.olap.xmla.connector.ContextListSupplyer;
import org.eclipse.daanse.olap.xmla.connector.rowset.provider.core.DiscoverSchemaRowsetsProvider;
import org.eclipse.daanse.xmla.model.xmla.Discover;
import org.eclipse.daanse.xmla.model.xmla.RequestTypeEnum;
import org.eclipse.daanse.xmla.model.xmla.XmlaFactory;
import org.eclipse.daanse.xmla.api.RowsetProvider;
import org.eclipse.daanse.xmla.api.RowsetScope;
import org.eclipse.daanse.xmla.api.XmlaRefusedException;
import org.eclipse.daanse.xmla.api.XmlaRequest;
import org.eclipse.emf.ecore.EObject;
import org.junit.jupiter.api.Test;

/**
 * What the whiteboard is for: rowsets come and go with their services, the
 * self-description follows immediately, and a rowset nothing provides is
 * refused rather than answered empty.
 * <p>
 * The dispatch is exercised as the connector performs it - lookup by request
 * type, scope with the served set - without an OSGi framework, because what is
 * worth pinning is the behaviour, not the container.
 */
class WhiteboardTest {

    private static Discover request(String requestType) {
        XmlaFactory factory = XmlaFactory.eINSTANCE;
        Discover discover = factory.createDiscover();
        discover.setRequestType(RequestTypeEnum.get(requestType));
        discover.setRestrictions(factory.createRestrictions());
        return discover;
    }

    /**
     * The connector's dispatch, in the small: a map of registrations and a refusal.
     */
    private static List<EObject> dispatch(Map<String, RowsetProvider<ContextListSupplyer>> registered,
            String requestType) {
        RowsetProvider<ContextListSupplyer> provider = registered.get(requestType);
        if (provider == null) {
            throw XmlaRefusedException.unknownRequestType(requestType);
        }
        return provider.rows(RowsetScope.of(request(requestType), XmlaRequest.anonymous(), (ContextListSupplyer) null,
                registered.keySet()));
    }

    @Test
    void aRowsetNothingProvidesIsRefusedInTheServersWords() {
        Map<String, RowsetProvider<ContextListSupplyer>> registered = Map.of("DISCOVER_SCHEMA_ROWSETS",
                new DiscoverSchemaRowsetsProvider());

        assertThatThrownBy(() -> dispatch(registered, "MDSCHEMA_CUBES")).isInstanceOf(XmlaRefusedException.class)
                .hasMessage("XML for Analysis parser: The 'MDSCHEMA_CUBES' request type was "
                        + "not recognized by the server.");
    }

    @Test
    void theSelfDescriptionFollowsTheRegistrations() {
        DiscoverSchemaRowsetsProvider self = new DiscoverSchemaRowsetsProvider();

        List<EObject> few = dispatch(Map.of("DISCOVER_SCHEMA_ROWSETS", self), "DISCOVER_SCHEMA_ROWSETS");
        assertThat(names(few)).containsExactly("DISCOVER_SCHEMA_ROWSETS");

        // One more service registers - and the server says so, with no list to
        // maintain.
        Map<String, RowsetProvider<ContextListSupplyer>> more = Map.of("DISCOVER_SCHEMA_ROWSETS", self,
                "MDSCHEMA_CUBES", (RowsetProvider<ContextListSupplyer>) scope -> List.of());
        assertThat(names(dispatch(more, "DISCOVER_SCHEMA_ROWSETS")))
                .containsExactlyInAnyOrder("DISCOVER_SCHEMA_ROWSETS", "MDSCHEMA_CUBES");
    }

    @Test
    void aRegisteredRowsetIsDispatchedToItsProvider() {
        EObject sentinel = org.eclipse.daanse.xmla.model.rowset.multidimensional.RowsetMultidimensionalFactory.eINSTANCE
                .createMdschemaCubesRow();
        Map<String, RowsetProvider<ContextListSupplyer>> registered = Map.of("MDSCHEMA_CUBES",
                (RowsetProvider<ContextListSupplyer>) scope -> List.of(sentinel));

        assertThat(dispatch(registered, "MDSCHEMA_CUBES")).containsExactly(sentinel);
    }

    private static Set<String> names(List<EObject> rows) {
        return rows.stream().map(row -> (String) row.eGet(row.eClass().getEStructuralFeature("schemaName")))
                .collect(java.util.stream.Collectors.toSet());
    }
}
