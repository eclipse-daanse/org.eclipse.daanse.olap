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
package org.eclipse.daanse.olap.xmla.connector.rowset.provider;

import java.util.List;

import org.eclipse.daanse.olap.xmla.connector.ContextListSupplyer;
import org.eclipse.daanse.xmla.api.RowsetProvider;
import org.eclipse.daanse.xmla.api.RowsetScope;
import org.eclipse.daanse.xmla.model.io.RowsetCatalog;
import org.eclipse.emf.ecore.EObject;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ServiceScope;

/**
 * {@code DISCOVER_SCHEMA_ROWSETS} — the server describing what it can be asked.
 * <p>
 * The answer is generated, not maintained: the whiteboard's registrations say
 * which rowsets are served, and the model says everything else about each one -
 * the schema GUID, the restrictions with their wire names and XSD types, and
 * the RestrictionsMask over their ordinals. Register a rowset service and it
 * appears here; unregister it and it is gone, which is what makes this the one
 * description a client can trust.
 * <p>
 * Required like {@code DISCOVER_PROPERTIES}: a client that cannot ask what the
 * server offers is reduced to guessing.
 */
@Component(service = RowsetProvider.class, scope = ServiceScope.PROTOTYPE, property = RowsetProvider.PROPERTY_REQUEST_TYPE
        + "=DISCOVER_SCHEMA_ROWSETS")
public class DiscoverSchemaRowsetsProvider implements RowsetProvider<ContextListSupplyer> {

    @Override
    public List<EObject> rows(RowsetScope<ContextListSupplyer> scope) {
        List<EObject> rows = RowsetCatalog.schemaRowsets(scope.served());
        // The client may ask about one rowset: SchemaName is this rowset's only
        // restriction.
        return scope.restrictions().value("SchemaName")
                .map(wanted -> rows.stream().filter(row -> wanted.equals(schemaNameOf(row))).toList()).orElse(rows);
    }

    private static String schemaNameOf(EObject row) {
        return (String) row.eGet(row.eClass().getEStructuralFeature("schemaName"));
    }
}
