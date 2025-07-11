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
package org.eclipse.daanse.olap.function.def.set.setitem;

import java.util.List;

import org.eclipse.daanse.olap.api.Evaluator;
import org.eclipse.daanse.olap.api.calc.Calc;
import org.eclipse.daanse.olap.api.calc.IntegerCalc;
import org.eclipse.daanse.olap.api.calc.todo.TupleListCalc;
import org.eclipse.daanse.olap.api.element.Member;
import org.eclipse.daanse.olap.api.type.Type;
import org.eclipse.daanse.olap.calc.base.nested.AbstractProfilingNestedMemberCalc;

public class SetItemCalc extends AbstractProfilingNestedMemberCalc{

    private final TupleListCalc tupleListCalc;
    private final IntegerCalc indexCalc;
    private final Member nullMember;
    
    protected SetItemCalc(Type type, Calc<?>[] calcs, TupleListCalc tupleListCalc, IntegerCalc indexCalc, Member nullMember) {
        super(type, calcs);
        this.tupleListCalc = tupleListCalc;
        this.indexCalc = indexCalc;
        this.nullMember = nullMember;
    }

    @Override
    public Member evaluate(Evaluator evaluator) {
        final int savepoint = evaluator.savepoint();
        final List<Member> list;
        try {
            evaluator.setNonEmpty(false);
            list =
                tupleListCalc.evaluate(evaluator).slice(0);
            assert list != null;
        } finally {
            evaluator.restore(savepoint);
        }
        try {
            final Integer index =
                indexCalc.evaluate(evaluator);
            int listSize = list.size();
            if (index >= listSize || index < 0) {
                return nullMember;
            } else {
                return list.get(index);
            }
        } finally {
            evaluator.restore(savepoint);
        }
    }

}
