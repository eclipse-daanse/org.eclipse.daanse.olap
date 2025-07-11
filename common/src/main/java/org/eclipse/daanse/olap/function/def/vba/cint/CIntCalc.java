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
package org.eclipse.daanse.olap.function.def.vba.cint;

import org.eclipse.daanse.olap.api.Evaluator;
import org.eclipse.daanse.olap.api.calc.Calc;
import org.eclipse.daanse.olap.api.type.Type;
import org.eclipse.daanse.olap.calc.base.nested.AbstractProfilingNestedIntegerCalc;

public class CIntCalc extends AbstractProfilingNestedIntegerCalc {

    protected CIntCalc(Type type, Calc<?> calc) {
        super(type, calc);
    }

    @Override
    public Integer evaluate(Evaluator evaluator) {
        Object expression = getChildCalc(0, Calc.class).evaluate(evaluator);
        return cInt(expression);
    }

    public static int cInt(Object expression) {
        if (expression instanceof Number number) {
            final int intValue = number.intValue();
            if (number instanceof Float || number instanceof Double) {
                final double doubleValue = number.doubleValue();
                if (doubleValue == (double) intValue) {
                    // Number is already an integer
                    return intValue;
                }
                final double doubleDouble = doubleValue * 2d;
                if (doubleDouble == Math.floor(doubleDouble)) {
                    // Number ends in .5 - round towards even required
                    return (int) Math.round(doubleValue / 2d) * 2;
                }
                return (int) Math.round(doubleValue);
            }
            return intValue;
        } else {
            // Try to parse as integer before parsing as double. More
            // efficient, and avoids loss of precision.
            final String s = String.valueOf(expression);
            try {
                return Integer.parseInt(s);
            } catch (NumberFormatException e) {
                return Double.valueOf(s).intValue();
            }
        }
    }


}
