/*
 * Copyright (c) 2025 Contributors to the Eclipse Foundation.
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
package org.eclipse.daanse.olap.function.def.vba.sln;

import org.eclipse.daanse.olap.api.Evaluator;
import org.eclipse.daanse.olap.api.calc.DoubleCalc;
import org.eclipse.daanse.olap.api.type.Type;
import org.eclipse.daanse.olap.calc.base.nested.AbstractProfilingNestedDoubleCalc;

public class SLNCalc extends AbstractProfilingNestedDoubleCalc {

    protected SLNCalc(Type type, final DoubleCalc costCalc, final DoubleCalc salvageCalc, final DoubleCalc lifeCalc) {
        super(type, costCalc, salvageCalc, lifeCalc);
    }

    @Override
    public Double evaluate(Evaluator evaluator) {
        Double cost = getChildCalc(0, DoubleCalc.class).evaluate(evaluator);
        Double salvage = getChildCalc(1, DoubleCalc.class).evaluate(evaluator);
        Double life = getChildCalc(2, DoubleCalc.class).evaluate(evaluator);

        return (cost - salvage) / life;
    }

}
