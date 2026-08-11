/*
 * Copyright (c) 2022 Contributors to the Eclipse Foundation.
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
package org.eclipse.daanse.olap.xmla.connector;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.daanse.olap.api.element.Cube;
import org.eclipse.daanse.olap.api.element.CubeLevel;
import org.eclipse.daanse.olap.api.element.LevelProperty;
import org.eclipse.daanse.olap.api.element.OlapElement;
import org.eclipse.daanse.olap.api.element.PhysicalCubeMeasure;

/**
 * The MDX a drillthrough action turns into.
 * <p>
 * MDSCHEMA_ACTIONS answers a drillthrough action with the statement a client
 * should run, not with a description of it — so the row's content is a query
 * built here from the action's coordinate and the elements it returns.
 */
public final class DrillThroughUtils {

    private DrillThroughUtils() {
        // static access only
    }

    /**
     * The members of a coordinate restriction, stripped of the parentheses a client
     * wraps a tuple in.
     */
    public static List<String> getCoordinateElements(String coordinate) {
        List<String> result = new ArrayList<>();
        if (coordinate != null) {
            String[] r = coordinate.split(",");
            for (String s : r) {
                result.add(s.replace(")", "").replace("(", ""));
            }
        }
        return result;
    }

    /**
     * The DRILLTHROUGH statement for one action.
     * <p>
     * The coordinate becomes the axis, and the action's measures, levels and level
     * properties become the RETURN clause — anything else it carries is not a
     * column a drillthrough can produce and is left out.
     */
    public static String getDrillThroughQuery(List<String> coordinateElements, List<OlapElement> olapElements, Cube c) {
        return getDrillThroughQuery(coordinateElements, olapElements, c.getName());
    }

    private static String getDrillThroughQuery(List<String> coordinateElements, List<OlapElement> olapElements,
            String cubeName) {
        StringBuilder sb = new StringBuilder("DRILLTHROUGH MAXROWS 1000 SELECT ");
        if (!coordinateElements.isEmpty()) {
            sb.append("(");
            boolean flag = true;
            for (String element : coordinateElements) {
                if (flag) {
                    flag = false;
                } else {
                    sb.append(",");
                }
                sb.append(element);
            }
            sb.append(") ON 0 ");
        }
        sb.append("FROM ").append(cubeName);
        boolean flag = true;
        for (OlapElement olapElement : olapElements) {
            if (olapElement instanceof PhysicalCubeMeasure || olapElement instanceof CubeLevel
                    || olapElement instanceof LevelProperty) {
                if (flag) {
                    flag = false;
                    sb.append(" RETURN ");
                } else {
                    sb.append(",");
                }
                sb.append(olapElement.getUniqueName());
            }
        }
        return sb.toString();
    }
}
