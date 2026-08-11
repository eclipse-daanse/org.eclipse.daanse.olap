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
import org.eclipse.emf.ecore.EObject;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ServiceScope;

/**
 * {@code DBSCHEMA_SOURCE_TABLES}, as a whiteboard service — <strong>a
 * placeholder that answers no rows</strong>.
 * <p>
 * The rowset names the tables a catalog is built from: {@code TABLE_CATALOG},
 * {@code TABLE_SCHEMA}, {@code TABLE_NAME} and {@code TABLE_TYPE}, restrictable
 * by all four. The bridge answered it; this connector does not, and registering
 * an empty answer is the deliberate middle course — the rowset stays advertised
 * in DISCOVER_SCHEMA_ROWSETS and a client asking for it gets a well-formed
 * empty rowset rather than a fault.
 * <p>
 * <strong>To implement it</strong>, read the underlying JDBC connection of each
 * catalog in scope and ask {@code DatabaseMetaData.getTables(...)}; map its
 * {@code TABLE_CAT}, {@code TABLE_SCHEM}, {@code TABLE_NAME} and
 * {@code TABLE_TYPE} onto {@code DbschemaSourceTablesRow}, honouring the four
 * restrictions. The shape to follow is {@link DbschemaTablesProvider} and
 * {@code DbSchemaDiscover}: build the rows there, keep this class a one-line
 * delegation.
 * <p>
 * Until then it answers empty deliberately, not by accident. A deployment that
 * would rather the rowset were not offered at all takes it out by unregistering
 * this service.
 */
@Component(service = RowsetProvider.class, scope = ServiceScope.PROTOTYPE, property = RowsetProvider.PROPERTY_REQUEST_TYPE
        + "=DBSCHEMA_SOURCE_TABLES")
public class DbschemaSourceTablesProvider implements RowsetProvider<ContextListSupplyer> {

    @Override
    public List<EObject> rows(RowsetScope<ContextListSupplyer> scope) {
        return List.of();
    }
}
