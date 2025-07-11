/*
* Copyright (c) 2024 Contributors to the Eclipse Foundation.
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
package org.eclipse.daanse.olap.calc.base.type.dimension;

import org.eclipse.daanse.olap.api.Evaluator;
import org.eclipse.daanse.olap.api.calc.HierarchyCalc;
import org.eclipse.daanse.olap.api.element.Dimension;
import org.eclipse.daanse.olap.api.element.Hierarchy;
import org.eclipse.daanse.olap.api.type.Type;
import org.eclipse.daanse.olap.calc.base.nested.AbstractProfilingNestedDimensionCalc;

public class DimensionOfHierarchyCalc extends AbstractProfilingNestedDimensionCalc {

	public DimensionOfHierarchyCalc(Type type, HierarchyCalc hierarchyCalc) {
		super(type, hierarchyCalc);
	}

	@Override
	public Dimension evaluate(Evaluator evaluator) {
		Hierarchy hierarchy = getChildCalc(0, HierarchyCalc.class).evaluate(evaluator);
		return hierarchy.getDimension();
	}
}
