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
package org.eclipse.daanse.olap.function.core.resolver;

import java.util.List;
import java.util.Optional;

import org.eclipse.daanse.olap.api.function.FunctionDefinition;
import org.eclipse.daanse.olap.api.function.FunctionMetaData;
import org.eclipse.daanse.olap.api.function.FunctionResolutionResult;
import org.eclipse.daanse.olap.api.query.Validator;
import org.eclipse.daanse.olap.api.query.component.Expression;

public class AbstractFunctionDefinitionMultiResolver extends AbstractMetaDataMultiResolver {

	private List<FunctionDefinition> functionDefinitions;

	public AbstractFunctionDefinitionMultiResolver(List<FunctionDefinition> functionDefinitions) {
		super(functionDefinitions.stream().map(FunctionDefinition::getFunctionMetaData).toList());
		this.functionDefinitions = functionDefinitions;
	}

	@Override
	public Optional<FunctionResolutionResult> resolve(Expression[] expressions, Validator validator) {
		for (FunctionDefinition functionDefinition : functionDefinitions) {
			FunctionMetaData functionMetaData = functionDefinition.getFunctionMetaData();
			Optional<FunctionMetaDataMatcher.Match> match = FunctionMetaDataMatcher.match(functionMetaData,
					expressions, validator);
			if (match.isPresent()) {
				// createFunDef stays the hook for resolvers that need a fresh,
				// per-call definition (e.g. NativizeSet holds per-call state).
				FunctionDefinition def = createFunDef(expressions, functionMetaData, functionMetaData);
				if (def != null) {
					return Optional.of(new FunctionResolutionResultR(def, functionMetaData,
							match.get().bindings(), match.get().conversions()));
				}
			}
		}
		return Optional.empty();
	}

	@Override
	protected FunctionDefinition createFunDef(Expression[] args, FunctionMetaData functionMetaData,
			FunctionMetaData fmdTarget) {

		if (functionMetaData == null) {
			return null;
		}
		// identity, not equals: the metadata instance comes from the matched
		// definition itself
		return functionDefinitions.stream().filter(fd -> fd.getFunctionMetaData() == functionMetaData).findAny()
				.orElse(null);
	}

}
