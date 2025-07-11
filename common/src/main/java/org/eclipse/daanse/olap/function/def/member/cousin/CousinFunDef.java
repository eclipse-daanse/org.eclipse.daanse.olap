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
package org.eclipse.daanse.olap.function.def.member.cousin;

import org.eclipse.daanse.mdx.model.api.expression.operation.FunctionOperationAtom;
import org.eclipse.daanse.mdx.model.api.expression.operation.OperationAtom;
import org.eclipse.daanse.olap.api.DataType;
import org.eclipse.daanse.olap.api.calc.Calc;
import org.eclipse.daanse.olap.api.calc.MemberCalc;
import org.eclipse.daanse.olap.api.calc.compiler.ExpressionCompiler;
import org.eclipse.daanse.olap.api.function.FunctionMetaData;
import org.eclipse.daanse.olap.api.query.component.ResolvedFunCall;
import org.eclipse.daanse.olap.function.core.FunctionMetaDataR;
import org.eclipse.daanse.olap.function.core.FunctionParameterR;
import org.eclipse.daanse.olap.function.def.AbstractFunctionDefinition;

public class CousinFunDef extends AbstractFunctionDefinition {
    static OperationAtom functionAtomCousin = new FunctionOperationAtom("Cousin");
    static FunctionMetaData functionMetaData = new FunctionMetaDataR(functionAtomCousin,
            "Returns the member with the same relative position under <ancestor member> as the member specified.",
            DataType.MEMBER,
            new FunctionParameterR[] { new FunctionParameterR(  DataType.MEMBER, "Member1"), new FunctionParameterR( DataType.MEMBER, "Member2" ) });

    public CousinFunDef() {
        super(functionMetaData);
    }

    @Override
    public Calc<?> compileCall(ResolvedFunCall call, ExpressionCompiler compiler) {
        final MemberCalc memberCalc = compiler.compileMember(call.getArg(0));
        final MemberCalc ancestorMemberCalc = compiler.compileMember(call.getArg(1));
        return new CousinCalc(call.getType(), memberCalc, ancestorMemberCalc);
    }

}
