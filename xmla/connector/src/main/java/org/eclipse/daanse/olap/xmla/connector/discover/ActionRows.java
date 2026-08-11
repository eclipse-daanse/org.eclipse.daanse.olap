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

import java.util.List;
import java.util.Optional;

import org.eclipse.daanse.olap.api.action.Action;
import org.eclipse.daanse.olap.api.action.ReportAction;
import org.eclipse.daanse.olap.api.action.UrlAction;
import org.eclipse.daanse.olap.api.element.Catalog;
import org.eclipse.daanse.olap.api.element.Cube;
import org.eclipse.daanse.olap.api.element.DrillThroughAction;
import org.eclipse.daanse.olap.xmla.connector.DrillThroughUtils;
import org.eclipse.daanse.xmla.model.rowset.multidimensional.MdschemaActionsRow;
import org.eclipse.daanse.xmla.model.rowset.multidimensional.RowsetMultidimensionalFactory;
import org.eclipse.daanse.xmla.api.RestrictionValues;
import org.eclipse.emf.ecore.EObject;

/**
 * MDSCHEMA_ACTIONS: what a client may do with a cell.
 * <p>
 * Ported from the bridge's {@code ActionServiceImpl}, which answered from two
 * sources. A cube carries its own drill-through actions, answered when the
 * client asks for the CELL coordinate type and names a coordinate — the content
 * is the DRILLTHROUGH statement built for exactly that coordinate. And any
 * {@code UrlAction}, {@code ReportAction} or {@code DrillThroughAction}
 * registered as an OSGi service is offered against every matching catalog,
 * schema and cube.
 * <p>
 * The [MS-SSAS] constants are written out here because they are the wire:
 * MDACTION_TYPE_URL 0x01, REPORT 0x80, DRILLTHROUGH 0x100;
 * MDACTION_COORDINATE_CELL 6; MDACTION_INVOCATION_NORMAL_OPERATION 1.
 */
public final class ActionRows {

    private static final RowsetMultidimensionalFactory FACTORY = RowsetMultidimensionalFactory.eINSTANCE;

    private static final int TYPE_URL = 0x01;
    private static final int TYPE_REPORT = 0x80;
    private static final int TYPE_DRILLTHROUGH = 0x100;
    private static final int COORDINATE_CELL = 6;
    private static final int INVOCATION_NORMAL = 1;

    private ActionRows() {
        // static access only
    }

    public static void collect(Catalog catalog, List<Action> registered, RestrictionValues restrictions,
            List<EObject> result) {
        Optional<String> schemaName = restrictions.value("SCHEMA_NAME");
        Optional<String> cubeName = restrictions.value("CUBE_NAME");
        Optional<String> actionName = restrictions.value("ACTION_NAME");
        Optional<String> actionType = restrictions.value("ACTION_TYPE");
        Optional<String> coordinate = restrictions.value("COORDINATE");
        Optional<String> coordinateType = restrictions.value("COORDINATE_TYPE");

        boolean cell = coordinateType.isPresent() && Integer.parseInt(coordinateType.get()) == COORDINATE_CELL;
        if (!cell) {
            return;
        }

        for (Cube cube : catalog.getCubes()) {
            if (cubeName.isPresent() && !cubeName.get().equals(cube.getName())) {
                continue;
            }
            drillThroughRows(catalog.getName(), cube, actionName, coordinate, result);
        }
        registeredRows(catalog, registered, schemaName, cubeName, actionName, actionType, coordinate, result);
    }

    /**
     * The cube's own drill-through actions, one row per action, for a named
     * coordinate.
     */
    private static void drillThroughRows(String catalogName, Cube cube, Optional<String> actionName,
            Optional<String> coordinate, List<EObject> result) {
        List<DrillThroughAction> actions = cube.getDrillThroughActions();
        if (actions == null || coordinate.isEmpty()) {
            return;
        }
        List<String> coordinateElements = DrillThroughUtils.getCoordinateElements(coordinate.get());
        for (DrillThroughAction action : actions) {
            if (actionName.isPresent() && !actionName.get().equals(action.getName())) {
                continue;
            }
            String query = DrillThroughUtils.getDrillThroughQuery(coordinateElements, action.getOlapElements(), cube);
            MdschemaActionsRow row = FACTORY.createMdschemaActionsRow();
            row.setCatalogName(catalogName);
            row.setCubeName(cube.getName());
            row.setActionName(action.getName());
            row.setActionType(TYPE_DRILLTHROUGH);
            row.setCoordinate(coordinate.get());
            row.setCoordinateType(COORDINATE_CELL);
            if (action.getCaption() != null) {
                row.setActionCaption(action.getCaption());
            }
            if (action.getDescription() != null) {
                row.setDescription(action.getDescription());
            }
            row.setContent(query);
            row.setInvocation(INVOCATION_NORMAL);
            result.add(row);
        }
    }

    /**
     * The registered action services, filtered the way the bridge filtered them: an
     * action that does not name a catalog, schema or cube matches every one.
     */
    private static void registeredRows(Catalog catalog, List<Action> registered, Optional<String> schemaName,
            Optional<String> cubeName, Optional<String> actionName, Optional<String> actionType,
            Optional<String> coordinate, List<EObject> result) {
        for (Action action : registered) {
            if (namedAndDiffers(action.catalogName(), Optional.of(catalog.getName()))
                    || namedAndDiffers(action.schemaName(), schemaName)
                    || (cubeName.isPresent() && action.cubeName() != null && !cubeName.get().equals(action.cubeName()))
                    || namedAndDiffers(action.actionName(), actionName)) {
                continue;
            }
            int type = typeOf(action);
            if (actionType.isPresent() && Integer.parseInt(actionType.get()) != type) {
                continue;
            }
            MdschemaActionsRow row = FACTORY.createMdschemaActionsRow();
            action.catalogName().ifPresent(row::setCatalogName);
            action.schemaName().ifPresent(row::setSchemaName);
            if (action.cubeName() != null) {
                row.setCubeName(action.cubeName());
            }
            action.actionName().ifPresent(row::setActionName);
            row.setActionType(type);
            coordinate.ifPresent(row::setCoordinate);
            // The bridge's coordinate-type mapping discarded its own result and always sent
            // nothing; what clients have seen is no COORDINATE_TYPE column, so none is
            // sent.
            action.actionCaption().ifPresent(row::setActionCaption);
            action.description().ifPresent(row::setDescription);
            String content = action.content(coordinate.orElse(null), action.cubeName());
            if (content != null) {
                row.setContent(content);
            }
            row.setInvocation(INVOCATION_NORMAL);
            result.add(row);
        }
    }

    /** An action that names the thing and names it differently is filtered out. */
    private static boolean namedAndDiffers(Optional<String> named, Optional<String> wanted) {
        return named.isPresent() && wanted.isPresent() && !wanted.get().equals(named.get());
    }

    private static int typeOf(Action action) {
        if (action instanceof org.eclipse.daanse.olap.api.action.DrillThroughAction) {
            return TYPE_DRILLTHROUGH;
        }
        if (action instanceof ReportAction) {
            return TYPE_REPORT;
        }
        if (action instanceof UrlAction) {
            return TYPE_URL;
        }
        return 0;
    }
}
