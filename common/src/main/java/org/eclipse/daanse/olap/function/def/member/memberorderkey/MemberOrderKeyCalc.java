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
package org.eclipse.daanse.olap.function.def.member.memberorderkey;

import org.eclipse.daanse.olap.api.Evaluator;
import org.eclipse.daanse.olap.api.calc.MemberCalc;
import org.eclipse.daanse.olap.api.type.Type;
import org.eclipse.daanse.olap.calc.base.AbstractProfilingNestedCalc;
import org.eclipse.daanse.olap.fun.sort.OrderKey;

public class MemberOrderKeyCalc extends AbstractProfilingNestedCalc<OrderKey> {
    /**
     * Creates a Calc
     *
     * @param type Type 
     * @param memberCalc Compiled expression to calculate member
     */
    public MemberOrderKeyCalc(Type type, MemberCalc memberCalc) {
        super(type, memberCalc);
    }

    @Override
    public OrderKey evaluate(Evaluator evaluator) {
        return new OrderKey(getChildCalc(0, MemberCalc.class).evaluate(evaluator));
    }

}
