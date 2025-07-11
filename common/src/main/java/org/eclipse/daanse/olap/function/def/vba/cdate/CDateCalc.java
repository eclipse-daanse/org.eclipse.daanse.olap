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
package org.eclipse.daanse.olap.function.def.vba.cdate;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.eclipse.daanse.olap.api.Evaluator;
import org.eclipse.daanse.olap.api.calc.Calc;
import org.eclipse.daanse.olap.api.type.Type;
import org.eclipse.daanse.olap.calc.base.nested.AbstractProfilingNestedDateTimeCalc;
import org.eclipse.daanse.olap.common.InvalidArgumentException;

public class CDateCalc extends AbstractProfilingNestedDateTimeCalc {

    protected CDateCalc(Type type, Calc<?> doubleCalc) {
        super(type, doubleCalc);
    }

    @Override
    public Date evaluate(Evaluator evaluator) {
        Object expression = getChildCalc(0, Calc.class).evaluate(evaluator);
        String str = String.valueOf(expression);
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
        if (expression instanceof Date date) {
            return date;
        } else if (expression == null) {
            return null;
        } else {
            // note that this currently only supports a limited set of dates and
            // times
            // "October 19, 1962"
            // "4:35:47 PM"
            try {
                return sdf.parse(str);
            } catch (ParseException ex0) {
                try {
                    return DateFormat.getDateTimeInstance().parse(str);
                } catch (ParseException ex1) {
                    try {
                        return DateFormat.getDateInstance().parse(str);
                    } catch (ParseException ex2) {
                        throw new InvalidArgumentException(
                            new StringBuilder("Invalid parameter. ")
                            .append("expression parameter of CDate function must be ")
                            .append("formatted correctly (")
                            .append(String.valueOf(expression)).append(")").toString());
                    }
                }
            }
        }
    }

}
