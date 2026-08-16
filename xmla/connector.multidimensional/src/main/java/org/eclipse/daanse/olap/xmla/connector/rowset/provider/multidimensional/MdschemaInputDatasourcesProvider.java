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
package org.eclipse.daanse.olap.xmla.connector.rowset.provider.multidimensional;

import java.util.List;

import org.eclipse.daanse.olap.xmla.connector.ContextListSupplyer;
import org.eclipse.daanse.olap.xmla.connector.multidimensional.MdSchemaDiscover;
import org.eclipse.daanse.xmla.api.RowsetProvider;
import org.eclipse.daanse.xmla.api.RowsetScope;
import org.eclipse.emf.ecore.EObject;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ServiceScope;

/**
 * {@code MDSCHEMA_INPUT_DATASOURCES}, as a whiteboard service.
 * <p>
 * What each catalog reads from: one row per catalog, naming its relational
 * store. Of the seventy rowsets a recorded Analysis Services advertises, this
 * was the last piece of <em>metadata</em> this server did not answer — the
 * remaining gaps are administrative rowsets and data mining.
 * <p>
 * Unregistering this service takes the rowset out of the server: it stops being
 * advertised in DISCOVER_SCHEMA_ROWSETS, and a request for it is refused.
 */
@Component(service = RowsetProvider.class, scope = ServiceScope.PROTOTYPE, property = RowsetProvider.PROPERTY_REQUEST_TYPE
        + "=MDSCHEMA_INPUT_DATASOURCES")
public class MdschemaInputDatasourcesProvider implements RowsetProvider<ContextListSupplyer> {

    @Override
    public List<EObject> rows(RowsetScope<ContextListSupplyer> scope) {
        return new MdSchemaDiscover(scope.backend(), List.of()).inputDatasources(scope.restrictions(), scope.caller());
    }
}
