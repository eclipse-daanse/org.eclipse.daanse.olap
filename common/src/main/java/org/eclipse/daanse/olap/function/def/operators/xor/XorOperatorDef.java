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
package org.eclipse.daanse.olap.function.def.operators.xor;

import org.eclipse.daanse.mdx.model.api.expression.operation.InfixOperationAtom;
import org.eclipse.daanse.olap.api.DataType;
import org.eclipse.daanse.olap.api.calc.BooleanCalc;
import org.eclipse.daanse.olap.api.calc.Calc;
import org.eclipse.daanse.olap.api.calc.compiler.ExpressionCompiler;
import org.eclipse.daanse.olap.api.function.FunctionMetaData;
import org.eclipse.daanse.olap.api.query.component.ResolvedFunCall;
import org.eclipse.daanse.olap.function.core.FunctionMetaDataR;
import org.eclipse.daanse.olap.function.core.FunctionParameterR;
import org.eclipse.daanse.olap.function.def.AbstractFunctionDefinition;

public class XorOperatorDef extends AbstractFunctionDefinition {

    // <Logical Expression> XOR <Logical Expression>
    static InfixOperationAtom infixOperationAtom = new InfixOperationAtom("XOR");
    static FunctionMetaData functionMetaData = new FunctionMetaDataR(infixOperationAtom,
            "Returns whether two conditions are mutually exclusive.", DataType.LOGICAL,
            new FunctionParameterR[] { new FunctionParameterR( DataType.LOGICAL, "Condition1" ), new FunctionParameterR( DataType.LOGICAL, "Condition1" ) });

    public XorOperatorDef() {
        super(functionMetaData);
    }

    @Override
    public Calc<?> compileCall(ResolvedFunCall call, ExpressionCompiler compiler) {
        final BooleanCalc calc0 = compiler.compileBoolean(call.getArg(0));
        final BooleanCalc calc1 = compiler.compileBoolean(call.getArg(1));
        return new XorCalc(call.getType(), calc0, calc1);
    }

}
