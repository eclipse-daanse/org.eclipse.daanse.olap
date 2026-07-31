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
package org.eclipse.daanse.olap.function.def.tuple;

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
import org.eclipse.daanse.olap.api.type.MemberType;
import org.eclipse.daanse.olap.function.core.FunctionMetaDataR;
import org.eclipse.daanse.olap.function.core.FunctionParameterR;
import org.eclipse.daanse.olap.function.core.resolver.FunctionResolutionResultR;
import org.eclipse.daanse.olap.function.core.resolver.NoExpressionRequiredFunctionResolver;
import org.eclipse.daanse.olap.function.def.crossjoin.CrossJoinFunDef;
import org.eclipse.daanse.olap.function.def.parentheses.ParenthesesFunDef;
import org.eclipse.daanse.olap.query.base.Expressions;
import org.osgi.service.component.annotations.Component;

@Component(service = FunctionResolver.class)
public class TupleResolver extends NoExpressionRequiredFunctionResolver {

    @Override
    public OperationAtom getFunctionAtom() {
        return TupleFunDef.functionAtom;
    }
    @Override
    public Optional<FunctionResolutionResult> resolve(
        Expression[] args,
        Validator validator)
    {
        List<Conversion> conversions = new ArrayList<>();
        // Compare with TupleFunDef.getReturnCategory().  For example,
        //   ([Gender].members) is a set,
        //   ([Gender].[M]) is a member,
        //   (1 + 2) is a numeric,
        // but
        //   ([Gender].[M], [Marital Status].[S]) is a tuple.
        if (args.length == 1 && !(args[0].getType() instanceof MemberType)) {
            return Optional.of(FunctionResolutionResultR.of(new ParenthesesFunDef(args[0].getCategory()), conversions));
        } else {
            final FunctionParameterR[] argTypes = new FunctionParameterR[args.length];
            boolean hasSet = false;
            for (int i = 0; i < args.length; i++) {
                // Arg must be a member:
                //  OK: ([Gender].[S], [Time].[1997])   (member, member)
                //  OK: ([Gender], [Time])           (dimension, dimension)
                // Not OK:
                //  ([Gender].[S], [Store].[Store City]) (member, level)
                if (validator.canConvert(
                        i, args[i], DataType.MEMBER, conversions)) {
                    argTypes[i] = FunctionParameterR.param(DataType.MEMBER);
                } else if(validator.canConvert(
                        i, args[i], DataType.SET, conversions)){
                    hasSet = true;
                    argTypes[i] = FunctionParameterR.param(DataType.SET);
                }
                else {
                    return Optional.empty();
                }
            }
            if(hasSet){

                FunctionMetaData functionMetaData = new FunctionMetaDataR(TupleFunDef.functionAtom,"Parenthesis operator constructs a tuple.  If there is only one member, the expression is equivalent to the member expression.",
                          DataType.SET, Expressions.functionParameterOf(args));


                return Optional.of(FunctionResolutionResultR.of(new CrossJoinFunDef(functionMetaData), conversions));
            }
            else {


                FunctionMetaData functionMetaData = new FunctionMetaDataR(TupleFunDef.functionAtom,"Parenthesis operator constructs a tuple.  If there is only one member, the expression is equivalent to the member expression.",
                          DataType.TUPLE, argTypes);

                return Optional.of(FunctionResolutionResultR.of(new TupleFunDef(functionMetaData), conversions));
            }
        }
    }

    private static final List<FunctionMetaData> REPRESENTATIVE_METADATAS = List.<FunctionMetaData>of(
        FunctionMetaDataR.of(TupleFunDef.functionAtom,
            "Parenthesis operator constructs a tuple.  If there is only one member, the expression is equivalent to the member expression.",
            DataType.TUPLE,
            param(DataType.MEMBER, "Member").repeatable(1)));

    @Override
    public List<FunctionMetaData> getRepresentativeFunctionMetaDatas() {
        return REPRESENTATIVE_METADATAS;
    }
}
