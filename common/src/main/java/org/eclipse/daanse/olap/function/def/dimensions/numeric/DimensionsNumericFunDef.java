 /*
 * This software is subject to the terms of the Eclipse Public License v1.0
 * Agreement, available at the following URL:
 * http://www.eclipse.org/legal/epl-v10.html.
 * You must accept the terms of that agreement to use this software.
 *
 * Copyright (c) 2002-2017 Hitachi Vantara..  All rights reserved.
 * 
 * ---- All changes after Fork in 2023 ------------------------
 * 
 * Project: Eclipse daanse
 * 
 * Copyright (c) 2023 Contributors to the Eclipse Foundation.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors after Fork in 2023:
 *   SmartCity Jena - initial
 *   Stefan Bischof (bipolis.org) - initial
 */

package org.eclipse.daanse.olap.function.def.dimensions.numeric;

import org.eclipse.daanse.mdx.model.api.expression.operation.FunctionOperationAtom;
import org.eclipse.daanse.mdx.model.api.expression.operation.OperationAtom;
import org.eclipse.daanse.olap.api.DataType;
import org.eclipse.daanse.olap.api.Validator;
import org.eclipse.daanse.olap.api.calc.Calc;
import org.eclipse.daanse.olap.api.calc.IntegerCalc;
import org.eclipse.daanse.olap.api.calc.compiler.ExpressionCompiler;
import org.eclipse.daanse.olap.api.function.FunctionMetaData;
import org.eclipse.daanse.olap.api.query.component.Expression;
import org.eclipse.daanse.olap.api.query.component.ResolvedFunCall;
import org.eclipse.daanse.olap.api.type.HierarchyType;
import org.eclipse.daanse.olap.api.type.Type;
import org.eclipse.daanse.olap.function.core.FunctionMetaDataR;
import org.eclipse.daanse.olap.function.core.FunctionParameterR;
import org.eclipse.daanse.olap.function.def.AbstractFunctionDefinition;

/**
 *
 * Actually returns a hierarchy. NOT an DIMENSION. This is consistent with
 * Analysis Services.
 *
 */
public class DimensionsNumericFunDef extends AbstractFunctionDefinition {

	static final String DIMENSIONS_NUMERIC_FUN_DESCRIPTION = "Returns the hierarchy whose zero-based position within the cube is specified by a numeric expression.";
	static final OperationAtom atom = new FunctionOperationAtom("Dimensions");
	static final FunctionMetaData functionalMetaData = new FunctionMetaDataR(atom, DIMENSIONS_NUMERIC_FUN_DESCRIPTION,
			DataType.HIERARCHY, new FunctionParameterR[] { new FunctionParameterR(  DataType.NUMERIC, "Numeric Expression" ) });

	public DimensionsNumericFunDef() {
		super(functionalMetaData);
	}

	@Override
	public Type getResultType(Validator validator, Expression[] args) {
		return HierarchyType.Unknown;
	}

	@Override
	public Calc<?> compileCall(ResolvedFunCall call, ExpressionCompiler compiler) {
		final IntegerCalc integerCalc = compiler.compileInteger(call.getArg(0));
		return new DimensionNumericCalc(call.getType(), integerCalc);
	}

}
