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

package org.eclipse.daanse.olap.function.def.operators.multiply;

import org.eclipse.daanse.olap.api.Evaluator;
import org.eclipse.daanse.olap.api.calc.DoubleCalc;
import org.eclipse.daanse.olap.api.type.Type;
import org.eclipse.daanse.olap.calc.base.nested.AbstractProfilingNestedDoubleCalc;
import org.eclipse.daanse.olap.fun.FunUtil;

public class MultiplyCalc extends AbstractProfilingNestedDoubleCalc {

    protected MultiplyCalc(Type type, final DoubleCalc calc0, final DoubleCalc calc1) {
        super(type, calc0, calc1);
    }

    @Override
    public Double evaluate(Evaluator evaluator) {
        final Double v0 = getChildCalc(0, DoubleCalc.class).evaluate(evaluator);
        final Double v1 = getChildCalc(1, DoubleCalc.class).evaluate(evaluator);
        // Multiply and divide return null if EITHER arg is
        // null.
        if (v0 == FunUtil.DOUBLE_NULL || v1 == FunUtil.DOUBLE_NULL || v0 == null|| v1 == null) {
            return FunUtil.DOUBLE_NULL;
        } else {
            return v0 * v1;
        }
    }

}
