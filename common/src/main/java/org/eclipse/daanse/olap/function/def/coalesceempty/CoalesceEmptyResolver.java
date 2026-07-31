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
package org.eclipse.daanse.olap.function.def.coalesceempty;

import static org.eclipse.daanse.olap.function.core.FunctionParameterR.param;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.eclipse.daanse.mdx.model.api.expression.operation.OperationAtom;
import org.eclipse.daanse.olap.api.DataType;
import org.eclipse.daanse.olap.api.function.FunctionMetaData;
import org.eclipse.daanse.olap.api.function.FunctionResolutionResult;
import org.eclipse.daanse.olap.api.function.FunctionResolver;
import org.eclipse.daanse.olap.api.query.Validator;
import org.eclipse.daanse.olap.api.query.component.Expression;
import org.eclipse.daanse.olap.function.core.FunctionMetaDataR;
import org.eclipse.daanse.olap.function.core.FunctionParameterR;
import org.eclipse.daanse.olap.function.core.resolver.FunctionResolutionResultR;
import org.osgi.service.component.annotations.Component;

@Component(service = FunctionResolver.class)
public class CoalesceEmptyResolver implements FunctionResolver {

    @Override
    public Optional<FunctionResolutionResult> resolve(
        Expression[] args,
        Validator validator)
    {
        List<Conversion> conversions = new ArrayList<>();
        if (args.length < 1) {
            return Optional.empty();
        }
        final DataType[] types = {DataType.NUMERIC, DataType.STRING};
        final FunctionParameterR[] argTypes = new FunctionParameterR[args.length];
        for (DataType type : types) {
            int matchingArgs = 0;
            conversions.clear();
            for (int i = 0; i < args.length; i++) {
                if (validator.canConvert(i, args[i], type, conversions)) {
                    matchingArgs++;
                }
                argTypes[i] = new FunctionParameterR(type);
            }
            if (matchingArgs == args.length) {

                FunctionMetaData functionMetaData=new FunctionMetaDataR( CoalesceEmptyFunDef.functionAtom,
                    "Coalesces an empty cell value to a different value. All of the expressions must be of the same type (number or string).",
                    type, argTypes);
                return Optional.of(FunctionResolutionResultR.of(new CoalesceEmptyFunDef(functionMetaData), conversions));
            }
        }
        return Optional.empty();
    }

    @Override
    public boolean requiresScalarExpressionOnArgument(int k) {
        return true;
    }

    @Override
    public OperationAtom getFunctionAtom() {
        return CoalesceEmptyFunDef.functionAtom;
    }

    private static final String DESCRIPTION =
        "Coalesces an empty cell value to a different value. All of the expressions must be of the same type (number or string).";

    private static final List<FunctionMetaData> REPRESENTATIVE_METADATAS = List.<FunctionMetaData>of(
        FunctionMetaDataR.of(CoalesceEmptyFunDef.functionAtom, DESCRIPTION, DataType.NUMERIC,
            param(DataType.NUMERIC),
            param(DataType.NUMERIC).repeatable(1)),
        FunctionMetaDataR.of(CoalesceEmptyFunDef.functionAtom, DESCRIPTION, DataType.STRING,
            param(DataType.STRING),
            param(DataType.STRING).repeatable(1)));

    @Override
    public List<FunctionMetaData> getRepresentativeFunctionMetaDatas() {
        return REPRESENTATIVE_METADATAS;
    }
}
