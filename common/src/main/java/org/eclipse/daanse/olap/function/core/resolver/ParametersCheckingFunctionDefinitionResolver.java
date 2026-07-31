/*
* Copyright (c) 2023 Contributors to the Eclipse Foundation.
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

package org.eclipse.daanse.olap.function.core.resolver;

import java.util.List;
import java.util.Optional;

import org.eclipse.daanse.mdx.model.api.expression.operation.OperationAtom;
import org.eclipse.daanse.olap.api.DataType;
import org.eclipse.daanse.olap.api.function.FunctionDefinition;
import org.eclipse.daanse.olap.api.function.FunctionMetaData;
import org.eclipse.daanse.olap.api.function.FunctionResolutionResult;
import org.eclipse.daanse.olap.api.function.FunctionResolver;
import org.eclipse.daanse.olap.api.query.Validator;
import org.eclipse.daanse.olap.api.query.component.Expression;

public class ParametersCheckingFunctionDefinitionResolver implements FunctionResolver {
	private FunctionDefinition functionDefinition;

	private ParametersCheckingFunctionDefinitionResolver() {
	}

	public ParametersCheckingFunctionDefinitionResolver(FunctionDefinition functionDefinition) {
		this();
		this.functionDefinition = functionDefinition;
	}

	@Override
	public List<FunctionMetaData> getRepresentativeFunctionMetaDatas() {
		FunctionMetaData functionMetaData = functionDefinition.getFunctionMetaData();
		if (functionMetaData != null) {
			return List.of(functionMetaData);
		}

		return List.of();
	}

	@Override
	public Optional<FunctionResolutionResult> resolve(Expression[] expressions, Validator validator) {
		FunctionMetaData functionMetaData = functionDefinition.getFunctionMetaData();
		Optional<FunctionMetaDataMatcher.Match> match = FunctionMetaDataMatcher.match(functionMetaData, expressions,
				validator);
		if (match.isEmpty() || !checkExpressions(expressions)) {
			return Optional.empty();
		}
		return Optional.of(new FunctionResolutionResultR(functionDefinition, functionMetaData,
				match.get().bindings(), match.get().conversions()));
	}

	protected boolean checkExpressions(Expression[] expressions) {
		return true;
	}

	@Override
	public boolean requiresScalarExpressionOnArgument(int k) {
		return !FunctionMetaDataMatcher.setPossibleAt(functionDefinition.getFunctionMetaData(), k);
	}

	@Override
	public OperationAtom getFunctionAtom() {
		return functionDefinition.getFunctionMetaData().operationAtom();
	}
}
