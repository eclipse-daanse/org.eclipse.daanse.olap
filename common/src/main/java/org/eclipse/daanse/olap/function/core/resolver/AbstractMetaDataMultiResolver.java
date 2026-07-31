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
import java.util.Objects;
import java.util.Optional;

import org.eclipse.daanse.mdx.model.api.expression.operation.OperationAtom;
import org.eclipse.daanse.olap.api.exception.OlapRuntimeException;
import org.eclipse.daanse.olap.api.function.FunctionDefinition;
import org.eclipse.daanse.olap.api.function.FunctionMetaData;
import org.eclipse.daanse.olap.api.function.FunctionResolutionResult;
import org.eclipse.daanse.olap.api.function.FunctionResolver;
import org.eclipse.daanse.olap.api.query.Validator;
import org.eclipse.daanse.olap.api.query.component.Expression;
import org.eclipse.daanse.olap.function.core.FunctionMetaDataR;
import org.eclipse.daanse.olap.query.base.Expressions;

public abstract class AbstractMetaDataMultiResolver implements FunctionResolver {

	private final List<String> reservedWords;
	private final OperationAtom operationAtom;
	private List<FunctionMetaData> fmds;

	public AbstractMetaDataMultiResolver(List<FunctionMetaData> fmds) {
		this(fmds, List.of());
	}

	public AbstractMetaDataMultiResolver(List<FunctionMetaData> fmds, List<String> reservedWords) {
		this.fmds = fmds;
		this.reservedWords = reservedWords == null ? List.of() : reservedWords;

		this.operationAtom = fmds.getFirst().operationAtom();
		for (FunctionMetaData fmd : fmds) {
			OperationAtom operationAtomTemp = fmd.operationAtom();
			if (!Objects.equals(operationAtom, operationAtomTemp)) {
				throw new OlapRuntimeException("all FunctionMetaData inside a Resolver must have same OperationAtom");
			}
		}
	}

	@Override
	public Optional<FunctionResolutionResult> resolve(Expression[] expressions, Validator validator) {
		for (FunctionMetaData functionMetaData : fmds) {
			Optional<FunctionMetaDataMatcher.Match> match = FunctionMetaDataMatcher.match(functionMetaData,
					expressions, validator);
			if (match.isEmpty()) {
				continue;
			}
			FunctionMetaData fmdTarget = new FunctionMetaDataR(operationAtom, functionMetaData.description(),
					functionMetaData.returnCategory(),
					Expressions.boundParametersOf(expressions, functionMetaData, match.get().bindings()));
			FunctionDefinition def = createFunDef(expressions, functionMetaData, fmdTarget);
			if (def != null) {
				return Optional.of(new FunctionResolutionResultR(def, functionMetaData, match.get().bindings(),
						match.get().conversions()));
			}
		}
		return Optional.empty();
	}

	@Override
	public List<FunctionMetaData> getRepresentativeFunctionMetaDatas() {
	    return List.copyOf(fmds);
	}

	@Override
	public boolean requiresScalarExpressionOnArgument(int k) {
		for (FunctionMetaData fmd : fmds) {
			if (FunctionMetaDataMatcher.setPossibleAt(fmd, k)) {
				return false;
			}
		}
		return true;
	}

	@Override
	public OperationAtom getFunctionAtom() {
		return operationAtom;
	}

	@Override
	public List<String> getReservedWords() {
		return reservedWords;
	}

	protected abstract FunctionDefinition createFunDef(Expression[] args, FunctionMetaData functionMetaData,
			FunctionMetaData fmdTarget);
}
