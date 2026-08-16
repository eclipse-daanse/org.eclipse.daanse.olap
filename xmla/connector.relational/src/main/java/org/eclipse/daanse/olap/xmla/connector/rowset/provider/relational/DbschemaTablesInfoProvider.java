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
package org.eclipse.daanse.olap.xmla.connector.rowset.provider.relational;

import java.util.List;

import org.eclipse.daanse.olap.xmla.connector.relational.DbSchemaDiscover;
import org.eclipse.daanse.olap.xmla.connector.ContextListSupplyer;
import org.eclipse.daanse.xmla.api.RowsetProvider;
import org.eclipse.daanse.xmla.api.RowsetScope;
import org.eclipse.emf.ecore.EObject;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ServiceScope;

/**
 * {@code DBSCHEMA_TABLES_INFO}, as a whiteboard service.
 * <p>
 * The same tables as DBSCHEMA_TABLES with the cardinality and capability
 * columns an OLE DB client uses to plan a query.
 * <p>
 * Unregistering this service takes the rowset out of the server: it stops being
 * advertised in DISCOVER_SCHEMA_ROWSETS, and a request for it is refused.
 */
@Component(service = RowsetProvider.class, scope = ServiceScope.PROTOTYPE, property = RowsetProvider.PROPERTY_REQUEST_TYPE
        + "=DBSCHEMA_TABLES_INFO")
public class DbschemaTablesInfoProvider implements RowsetProvider<ContextListSupplyer> {

    @Override
    public List<EObject> rows(RowsetScope<ContextListSupplyer> scope) {
        return new DbSchemaDiscover(scope.backend()).tablesInfo(scope.restrictions(), scope.caller());
    }
}
