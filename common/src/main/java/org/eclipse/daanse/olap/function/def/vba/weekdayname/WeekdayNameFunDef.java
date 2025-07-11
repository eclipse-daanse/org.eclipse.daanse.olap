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
package org.eclipse.daanse.olap.function.def.vba.weekdayname;

import org.eclipse.daanse.mdx.model.api.expression.operation.FunctionOperationAtom;
import org.eclipse.daanse.olap.api.DataType;
import org.eclipse.daanse.olap.api.calc.BooleanCalc;
import org.eclipse.daanse.olap.api.calc.Calc;
import org.eclipse.daanse.olap.api.calc.IntegerCalc;
import org.eclipse.daanse.olap.api.calc.compiler.ExpressionCompiler;
import org.eclipse.daanse.olap.api.function.FunctionMetaData;
import org.eclipse.daanse.olap.api.query.component.ResolvedFunCall;
import org.eclipse.daanse.olap.function.core.FunctionMetaDataR;
import org.eclipse.daanse.olap.function.core.FunctionParameterR;
import org.eclipse.daanse.olap.function.def.AbstractFunctionDefinition;

public class WeekdayNameFunDef  extends AbstractFunctionDefinition {

    static FunctionOperationAtom atom = new FunctionOperationAtom("WeekdayName");
    static String description = """
        Returns a string indicating the specified day of the week.""";
    static FunctionMetaData functionMetaData = new FunctionMetaDataR(atom, description,
            DataType.STRING, new FunctionParameterR[] { new FunctionParameterR( DataType.INTEGER, "Weekday" ),
                    new FunctionParameterR( DataType.LOGICAL, "abbreviate" ), new FunctionParameterR( DataType.INTEGER, "First day of week" )});

    public WeekdayNameFunDef() {
        super(functionMetaData);
    }

    @Override
    public Calc<?> compileCall(ResolvedFunCall call, ExpressionCompiler compiler) {
        final IntegerCalc weekdayCalc = compiler.compileInteger(call.getArg(0));
        final BooleanCalc abbreviateCalc = compiler.compileBoolean(call.getArg(1));
        final IntegerCalc firstDayOfWeekCalc = compiler.compileInteger(call.getArg(2));
        return new WeekdayNameCalc(call.getType(), weekdayCalc, abbreviateCalc, firstDayOfWeekCalc);
    }

}
