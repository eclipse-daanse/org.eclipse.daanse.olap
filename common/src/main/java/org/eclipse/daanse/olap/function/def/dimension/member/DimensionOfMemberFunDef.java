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
package org.eclipse.daanse.olap.function.def.dimension.member;

import org.eclipse.daanse.mdx.model.api.expression.operation.OperationAtom;
import org.eclipse.daanse.mdx.model.api.expression.operation.PlainPropertyOperationAtom;
import org.eclipse.daanse.olap.api.DataType;
import org.eclipse.daanse.olap.api.calc.Calc;
import org.eclipse.daanse.olap.api.calc.MemberCalc;
import org.eclipse.daanse.olap.api.calc.compiler.ExpressionCompiler;
import org.eclipse.daanse.olap.api.function.FunctionMetaData;
import org.eclipse.daanse.olap.api.query.component.ResolvedFunCall;
import org.eclipse.daanse.olap.function.core.FunctionMetaDataR;
import org.eclipse.daanse.olap.function.core.FunctionParameterR;
import org.eclipse.daanse.olap.function.def.AbstractFunctionDefinition;

public class DimensionOfMemberFunDef extends AbstractFunctionDefinition {

	private static final OperationAtom atom = new PlainPropertyOperationAtom("Dimension");

	private static final FunctionMetaData functionMetaData = new FunctionMetaDataR(atom,
			"Returns the dimension that contains a specified member.", DataType.DIMENSION,
			new FunctionParameterR[] { new FunctionParameterR( DataType.MEMBER )});

	public DimensionOfMemberFunDef() {
		super(functionMetaData);
	}

	@Override
	public Calc<?> compileCall(ResolvedFunCall call, ExpressionCompiler compiler) {

		final MemberCalc memberCalc = compiler.compileMember(call.getArg(0));
		return new DimensionOfMemberCalc(call.getType(), memberCalc);
	}

}
