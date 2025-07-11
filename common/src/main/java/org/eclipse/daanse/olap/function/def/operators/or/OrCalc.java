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

package org.eclipse.daanse.olap.function.def.operators.or;

import org.eclipse.daanse.olap.api.Evaluator;
import org.eclipse.daanse.olap.api.calc.BooleanCalc;
import org.eclipse.daanse.olap.api.calc.DoubleCalc;
import org.eclipse.daanse.olap.api.type.Type;
import org.eclipse.daanse.olap.calc.base.nested.AbstractProfilingNestedBooleanCalc;
import org.eclipse.daanse.olap.calc.base.nested.AbstractProfilingNestedDoubleCalc;
import org.eclipse.daanse.olap.fun.FunUtil;

public class OrCalc extends AbstractProfilingNestedBooleanCalc {

    protected OrCalc(Type type, final BooleanCalc calc0, final BooleanCalc calc1) {
        super(type, calc0, calc1);
    }

    @Override
    public Boolean evaluate(Evaluator evaluator) {
        boolean b0 = getChildCalc(0, BooleanCalc.class).evaluate(evaluator);
        // don't short-circuit evaluation if we're evaluating
        // the axes; that way, we can combine all measures
        // referenced in the OR expression in a single query
        if (!evaluator.isEvalAxes() && b0) {
            return true;
        }
        boolean b1 = getChildCalc(1, BooleanCalc.class).evaluate(evaluator);
        return b0 || b1;
    }
}
