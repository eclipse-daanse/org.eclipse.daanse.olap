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
import java.util.concurrent.CopyOnWriteArrayList;

import org.eclipse.daanse.olap.api.action.Action;
import org.eclipse.daanse.olap.api.action.ReportAction;
import org.eclipse.daanse.olap.api.action.UrlAction;
import org.eclipse.daanse.olap.xmla.connector.discover.MdSchemaDiscover;
import org.eclipse.daanse.olap.xmla.connector.ContextListSupplyer;
import org.eclipse.daanse.xmla.api.RowsetProvider;
import org.eclipse.daanse.xmla.api.RowsetScope;
import org.eclipse.emf.ecore.EObject;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ServiceScope;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

/**
 * {@code MDSCHEMA_ACTIONS}, as a whiteboard service - and a whiteboard of its
 * own: the registered {@link UrlAction}, {@link ReportAction} and drill-through
 * actions bind here, where they are used, rather than at the connector.
 */
@Component(service = RowsetProvider.class, scope = ServiceScope.PROTOTYPE, property = RowsetProvider.PROPERTY_REQUEST_TYPE
        + "=MDSCHEMA_ACTIONS")
public class MdschemaActionsProvider implements RowsetProvider<ContextListSupplyer> {

    private final List<Action> actions = new CopyOnWriteArrayList<>();

    @Reference(cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC)
    void bindUrlAction(UrlAction action) {
        actions.add(action);
    }

    void unbindUrlAction(UrlAction action) {
        actions.remove(action);
    }

    @Reference(cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC)
    void bindReportAction(ReportAction action) {
        actions.add(action);
    }

    void unbindReportAction(ReportAction action) {
        actions.remove(action);
    }

    @Reference(cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC)
    void bindDrillThroughAction(org.eclipse.daanse.olap.api.action.DrillThroughAction action) {
        actions.add(action);
    }

    void unbindDrillThroughAction(org.eclipse.daanse.olap.api.action.DrillThroughAction action) {
        actions.remove(action);
    }

    @Override
    public List<EObject> rows(RowsetScope<ContextListSupplyer> scope) {
        return new MdSchemaDiscover(scope.backend(), actions).actions(scope.restrictions(), scope.caller());
    }
}
