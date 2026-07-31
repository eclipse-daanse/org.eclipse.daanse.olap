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
package org.eclipse.daanse.olap.function.def.set;

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
import org.eclipse.daanse.olap.function.core.resolver.NoExpressionRequiredFunctionResolver;
import org.osgi.service.component.annotations.Component;

@Component(service = FunctionResolver.class)
public class SetResolver  extends NoExpressionRequiredFunctionResolver {

    @Override
    public Optional<FunctionResolutionResult> resolve(
        Expression[] args,
        Validator validator)
    {
        List<Conversion> conversions = new ArrayList<>();
        FunctionParameterR[] parameterTypes = new FunctionParameterR[args.length];
        for (int i = 0; i < args.length; i++) {
            if (validator.canConvert(
                    i, args[i], DataType.MEMBER, conversions))
            {
                parameterTypes[i] = FunctionParameterR.param(DataType.MEMBER);
                continue;
            }
            if (validator.canConvert(
                    i, args[i], DataType.TUPLE, conversions))
            {
                parameterTypes[i] = FunctionParameterR.param(DataType.TUPLE);
                continue;
            }
            if (validator.canConvert(
                    i, args[i], DataType.SET, conversions))
            {
                parameterTypes[i] = FunctionParameterR.param(DataType.SET);
                continue;
            }
            return Optional.empty();
        }

        return Optional.of(FunctionResolutionResultR.of(new SetFunDef(parameterTypes), conversions));
    }

    @Override
    public OperationAtom getFunctionAtom() {
        return SetFunDef.functionAtom;
    }

    private static final List<FunctionMetaData> REPRESENTATIVE_METADATAS = List.<FunctionMetaData>of(
        FunctionMetaDataR.of(SetFunDef.functionAtom, SetFunDef.DESCRIPTION, DataType.SET,
            param(DataType.VALUE, "Member_or_Set").asOptional().repeatable(1)));

    @Override
    public List<FunctionMetaData> getRepresentativeFunctionMetaDatas() {
        return REPRESENTATIVE_METADATAS;
    }
}
