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
package org.eclipse.daanse.olap.function.def.set.level;

import org.eclipse.daanse.olap.api.Evaluator;
import org.eclipse.daanse.olap.api.calc.LevelCalc;
import org.eclipse.daanse.olap.api.calc.todo.TupleList;
import org.eclipse.daanse.olap.api.element.Level;
import org.eclipse.daanse.olap.api.type.Type;
import org.eclipse.daanse.olap.calc.base.type.tuplebase.AbstractProfilingNestedTupleListCalc;
import org.eclipse.daanse.olap.fun.FunUtil;

public class AllMembersCalc extends AbstractProfilingNestedTupleListCalc {

    protected AllMembersCalc(Type type, final LevelCalc levelCalc) {
        super(type, levelCalc);
    }

    @Override
    public TupleList evaluate(Evaluator evaluator) {
        Level level = getChildCalc(0, LevelCalc.class).evaluate(evaluator);
        return FunUtil.levelMembers(level, evaluator, true);
    }

}
