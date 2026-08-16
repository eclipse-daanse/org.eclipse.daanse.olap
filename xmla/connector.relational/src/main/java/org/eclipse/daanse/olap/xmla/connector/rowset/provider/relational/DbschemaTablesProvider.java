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
 * {@code DBSCHEMA_TABLES}, as a whiteboard service.
 * <p>
 * Cubes as TABLEs and levels as SYSTEM TABLEs named cube:hierarchy:level - the
 * relational view of the OLAP metadata.
 * <p>
 * Unregistering this service takes the rowset out of the server: it stops being
 * advertised in DISCOVER_SCHEMA_ROWSETS, and a request for it is refused.
 */
@Component(service = RowsetProvider.class, scope = ServiceScope.PROTOTYPE, property = RowsetProvider.PROPERTY_REQUEST_TYPE
        + "=DBSCHEMA_TABLES")
public class DbschemaTablesProvider implements RowsetProvider<ContextListSupplyer> {

    @Override
    public List<EObject> rows(RowsetScope<ContextListSupplyer> scope) {
        return new DbSchemaDiscover(scope.backend()).tables(scope.restrictions(), scope.caller());
    }
}
