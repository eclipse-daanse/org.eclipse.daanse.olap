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
package org.eclipse.daanse.olap.function.def.set.hierarchy;

import org.eclipse.daanse.olap.api.Evaluator;
import org.eclipse.daanse.olap.api.calc.HierarchyCalc;
import org.eclipse.daanse.olap.api.calc.todo.TupleList;
import org.eclipse.daanse.olap.api.element.Hierarchy;
import org.eclipse.daanse.olap.api.type.Type;
import org.eclipse.daanse.olap.calc.base.type.tuplebase.AbstractProfilingNestedTupleListCalc;
import org.eclipse.daanse.olap.fun.FunUtil;

public class AllMembersCalc extends AbstractProfilingNestedTupleListCalc {

    protected AllMembersCalc(Type type, final HierarchyCalc hierarchyCalc) {
        super(type, hierarchyCalc);
    }

    @Override
    public TupleList evaluate(Evaluator evaluator) {
        Hierarchy hierarchy = getChildCalc(0, HierarchyCalc.class).evaluate(evaluator);
        return FunUtil.hierarchyMembers(hierarchy, evaluator, true);
    }

}
