/*
 * Copyright (c) 2026 Contributors to the Eclipse Foundation.
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
package org.eclipse.daanse.olap.function.def.kpi;

import org.eclipse.daanse.mdx.model.api.expression.operation.FunctionOperationAtom;
import org.eclipse.daanse.mdx.model.api.expression.operation.OperationAtom;
import org.eclipse.daanse.olap.api.DataType;
import org.eclipse.daanse.olap.api.calc.Calc;
import org.eclipse.daanse.olap.api.calc.StringCalc;
import org.eclipse.daanse.olap.api.calc.compiler.ExpressionCompiler;
import org.eclipse.daanse.olap.api.function.FunctionMetaData;
import org.eclipse.daanse.olap.api.query.component.ResolvedFunCall;
import org.eclipse.daanse.olap.function.core.FunctionMetaDataR;
import org.eclipse.daanse.olap.function.core.FunctionParameterR;
import org.eclipse.daanse.olap.function.def.AbstractFunctionDefinition;

public class KPICurrentTimeMemberFunDef extends AbstractFunctionDefinition {

    static final OperationAtom KPI_CURRENT_TIME_MEMBER_INSTANCE_FUNCTION_ATOM = new FunctionOperationAtom("KPICurrentTimeMember");
    static FunctionParameterR[] params = { new FunctionParameterR(DataType.STRING, "Kpi") };
    static final FunctionMetaData FUNCTION_META_DATA = new FunctionMetaDataR(KPI_CURRENT_TIME_MEMBER_INSTANCE_FUNCTION_ATOM, "Returns KPI current time member.",
    		DataType.MEMBER, params);
    // KPITrend(<String>)

    public KPICurrentTimeMemberFunDef() {
        super(FUNCTION_META_DATA);
    }

    @Override
    public Calc<?> compileCall( ResolvedFunCall call, ExpressionCompiler compiler) {
        final StringCalc stringCalc =
            compiler.compileString(call.getArg(0));
        return new KPICurrentTimeMemberCalc(call.getType(), stringCalc);
    }

}
