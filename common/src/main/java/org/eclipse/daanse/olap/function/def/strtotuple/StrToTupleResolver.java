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
package org.eclipse.daanse.olap.function.def.strtotuple;

import java.util.List;
import java.util.Optional;

import org.eclipse.daanse.mdx.model.api.expression.operation.OperationAtom;
import org.eclipse.daanse.olap.api.DataType;
import org.eclipse.daanse.olap.api.function.FunctionMetaData;
import org.eclipse.daanse.olap.api.function.FunctionResolutionResult;
import org.eclipse.daanse.olap.api.function.FunctionResolver;
import org.eclipse.daanse.olap.api.query.Validator;
import org.eclipse.daanse.olap.api.query.component.DimensionExpression;
import org.eclipse.daanse.olap.api.query.component.Expression;
import org.eclipse.daanse.olap.api.type.NullType;
import org.eclipse.daanse.olap.api.type.StringType;
import org.eclipse.daanse.olap.api.type.Type;
import org.eclipse.daanse.olap.function.core.FunctionMetaDataR;
import org.eclipse.daanse.olap.function.core.FunctionParameterR;
import org.eclipse.daanse.olap.function.core.resolver.FunctionResolutionResultR;
import org.eclipse.daanse.olap.function.core.resolver.NoExpressionRequiredFunctionResolver;
import org.eclipse.daanse.olap.query.component.HierarchyExpressionImpl;
import org.osgi.service.component.annotations.Component;

@Component(service = FunctionResolver.class)
public class StrToTupleResolver extends NoExpressionRequiredFunctionResolver {


    @Override
    public Optional<FunctionResolutionResult> resolve(
        Expression[] args,
        Validator validator)
    {
        if (args.length < 1) {
            return Optional.empty();
        }
        Type type = args[0].getType();
        if (!(type instanceof StringType)
            && !(type instanceof NullType))
        {
            return Optional.empty();
        }
        for (int i = 1; i < args.length; i++) {
            Expression exp = args[i];
            if (!(exp instanceof DimensionExpression
                || exp instanceof HierarchyExpressionImpl))
            {
                return Optional.empty();
            }
        }
        FunctionParameterR[] argTypes = new FunctionParameterR[args.length];
        argTypes[0] = FunctionParameterR.param(DataType.STRING);
        for (int i = 1; i < argTypes.length; i++) {
            argTypes[i] = FunctionParameterR.param(DataType.HIERARCHY);
        }
        return Optional.of(FunctionResolutionResultR.of(new StrToTupleFunDef(functionMetaDataFor(argTypes)), List.<Conversion>of()));
    }


    @Override
    public List<FunctionMetaData> getRepresentativeFunctionMetaDatas() {
        return List.of( functionMetaDataFor(new FunctionParameterR[] {FunctionParameterR.param(DataType.STRING)}));
    }

    private FunctionMetaData functionMetaDataFor(FunctionParameterR[] argTypes) {
        FunctionMetaData functionMetaData = new FunctionMetaDataR(StrToTupleFunDef.functionAtom,
                "Constructs a tuple from a string.",
                 DataType.TUPLE, argTypes);
        return functionMetaData;
    }


    @Override
    public OperationAtom getFunctionAtom() {
        return StrToTupleFunDef.functionAtom;
    }

}
