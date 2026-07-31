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
package org.eclipse.daanse.olap.function.def.set.setitem;

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
import org.eclipse.daanse.olap.api.type.SetType;
import org.eclipse.daanse.olap.common.Util;
import org.eclipse.daanse.olap.function.core.FunctionMetaDataR;
import org.eclipse.daanse.olap.function.core.resolver.FunctionResolutionResultR;
import org.eclipse.daanse.olap.function.core.resolver.NoExpressionRequiredFunctionResolver;
import org.eclipse.daanse.olap.query.base.Expressions;
import org.osgi.service.component.annotations.Component;

@Component(service = FunctionResolver.class)
public class SetItemStringResolver extends NoExpressionRequiredFunctionResolver {
    @Override
    public Optional<FunctionResolutionResult> resolve(Expression[] args, Validator validator) {
        List<Conversion> conversions = new ArrayList<>();
        if (args.length < 1) {
            return Optional.empty();
        }
        final Expression setExp = args[0];
        if (!(setExp.getType() instanceof SetType)) {
            return Optional.empty();
        }
        final SetType setType = (SetType) setExp.getType();
        final int arity = setType.getArity();
        // All args must be strings.
        for (int i = 1; i < args.length; i++) {
            if (!validator.canConvert(i, args[i], DataType.STRING, conversions)) {
                return Optional.empty();
            }
        }
        if (args.length - 1 != arity) {
            throw Util.newError("Argument count does not match set's cardinality " + arity);
        }
        final DataType category = arity == 1 ? DataType.MEMBER : DataType.TUPLE;

        FunctionMetaData functionMetaData = new FunctionMetaDataR(SetItemFunDef.functionAtom,
                "Returns a tuple from the set specified in <Set>. The tuple to be returned is specified by the member name (or names) in <String>.",
                category, Expressions.functionParameterOf(args));

        return Optional.of(FunctionResolutionResultR.of(new SetItemFunDef(functionMetaData), conversions));
    }

    @Override
    public OperationAtom getFunctionAtom() {
        return SetItemFunDef.functionAtom;
    }

    private static final List<FunctionMetaData> REPRESENTATIVE_METADATAS = List.<FunctionMetaData>of(
            FunctionMetaDataR.of(SetItemFunDef.functionAtom,
                    "Returns a tuple from the set specified in <Set>. The tuple to be returned is specified by the member name (or names) in <String>.",
                    DataType.TUPLE,
                    param(DataType.SET),
                    param(DataType.STRING, "Member_Name").repeatable(1)));

    @Override
    public List<FunctionMetaData> getRepresentativeFunctionMetaDatas() {
        return REPRESENTATIVE_METADATAS;
    }

}
