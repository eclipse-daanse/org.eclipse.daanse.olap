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
package org.eclipse.daanse.olap.function.def.dimension.current;

import org.eclipse.daanse.mdx.model.api.expression.operation.PlainPropertyOperationAtom;
import org.eclipse.daanse.olap.api.DataType;
import org.eclipse.daanse.olap.api.calc.Calc;
import org.eclipse.daanse.olap.api.calc.compiler.ExpressionCompiler;
import org.eclipse.daanse.olap.api.function.FunctionMetaData;
import org.eclipse.daanse.olap.api.query.component.ResolvedFunCall;
import org.eclipse.daanse.olap.function.core.FunctionMetaDataR;
import org.eclipse.daanse.olap.function.core.FunctionParameterR;
import org.eclipse.daanse.olap.function.def.AbstractFunctionDefinition;

public class CurrentFunDef extends AbstractFunctionDefinition {

    static PlainPropertyOperationAtom plainPropertyOperationAtom = new PlainPropertyOperationAtom("Current");
    static FunctionMetaData functionMetaData = new FunctionMetaDataR(plainPropertyOperationAtom, "Returns the current tuple from a set during an iteration.",
            DataType.TUPLE, new FunctionParameterR[] { new FunctionParameterR(  DataType.SET ) });

	public CurrentFunDef() {
		super(functionMetaData);
	}

    @Override
    public Calc<?> compileCall(ResolvedFunCall call, ExpressionCompiler compiler)
    {
        throw new UnsupportedOperationException();
    }

}
