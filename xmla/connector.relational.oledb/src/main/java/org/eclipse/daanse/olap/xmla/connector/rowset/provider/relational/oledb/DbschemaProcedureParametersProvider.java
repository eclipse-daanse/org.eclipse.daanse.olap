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
package org.eclipse.daanse.olap.xmla.connector.rowset.provider.relational.oledb;

import java.util.List;

import org.eclipse.daanse.olap.xmla.connector.ContextListSupplyer;
import org.eclipse.daanse.xmla.api.RowsetProvider;
import org.eclipse.daanse.xmla.api.RowsetScope;
import org.eclipse.emf.ecore.EObject;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ServiceScope;

/**
 * {@code DBSCHEMA_PROCEDURE_PARAMETERS}, as a whiteboard service — <strong>answering no rows
 * yet</strong>.
 * <p>
 * The parameters of the catalog's procedures. One of the OLE DB schema rowsets (Appendix B), which [MS-SSAS] does
 * not list among the request types an XMLA server must answer; it is offered so
 * that a consumer speaking OLE DB over XMLA finds it under the name and GUID it
 * knows.
 * <p>
 * <strong>To fill it</strong>, the catalog needs routine metadata. The mapping model
 * reaches only as far as {@code DatabaseSchema}, {@code DatabaseTable} and
 * {@code DatabaseColumn} — name, type, nullability, size and scale — so there
 * is nothing here to report yet. Registering an empty answer is the deliberate
 * middle course: the rowset stays advertised in DISCOVER_SCHEMA_ROWSETS with
 * its columns, restrictions and mask, and a client asking for it gets a
 * well-formed empty rowset rather than a fault.
 * <p>
 * A deployment that would rather it were not offered takes it out by
 * unregistering this service.
 */
@Component(service = RowsetProvider.class, scope = ServiceScope.PROTOTYPE, property = RowsetProvider.PROPERTY_REQUEST_TYPE
        + "=DBSCHEMA_PROCEDURE_PARAMETERS")
public class DbschemaProcedureParametersProvider implements RowsetProvider<ContextListSupplyer> {

    @Override
    public List<EObject> rows(RowsetScope<ContextListSupplyer> scope) {
        return List.of();
    }
}
