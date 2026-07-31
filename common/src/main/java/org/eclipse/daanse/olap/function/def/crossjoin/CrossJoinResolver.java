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
package org.eclipse.daanse.olap.function.def.crossjoin;

import static org.eclipse.daanse.olap.function.core.FunctionParameterR.param;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.eclipse.daanse.mdx.model.api.expression.operation.FunctionOperationAtom;
import org.eclipse.daanse.mdx.model.api.expression.operation.OperationAtom;
import org.eclipse.daanse.olap.api.DataType;
import org.eclipse.daanse.olap.api.function.FunctionDefinition;
import org.eclipse.daanse.olap.api.function.FunctionMetaData;
import org.eclipse.daanse.olap.api.function.FunctionResolutionResult;
import org.eclipse.daanse.olap.api.function.FunctionResolver;
import org.eclipse.daanse.olap.api.query.Validator;
import org.eclipse.daanse.olap.api.query.component.Expression;
import org.eclipse.daanse.olap.function.core.FunctionMetaDataR;
import org.eclipse.daanse.olap.function.core.resolver.FunctionResolutionResultR;
import org.eclipse.daanse.olap.function.core.resolver.NoExpressionRequiredFunctionResolver;
import org.eclipse.daanse.olap.query.base.Expressions;
import org.osgi.service.component.annotations.Component;

@Component(service = FunctionResolver.class)
public class CrossJoinResolver  extends NoExpressionRequiredFunctionResolver {
    static OperationAtom functionAtom = new FunctionOperationAtom("Crossjoin");

    @Override
    public Optional<FunctionResolutionResult> resolve(
            Expression[] args,
            Validator validator)
    {
      List<Conversion> conversions = new ArrayList<>();
      if (args.length < 2) {
        return Optional.empty();
      } else {
        for (int i = 0; i < args.length; i++) {
          if (!validator.canConvert(
                  i, args[i], org.eclipse.daanse.olap.api.DataType.SET, conversions)) {
            return Optional.empty();
          }
        }


        FunctionMetaData functionMetaData = new FunctionMetaDataR(functionAtom, "Returns the cross product of two sets.",
                org.eclipse.daanse.olap.api.DataType.SET, Expressions.functionParameterOf(args));
        return Optional.of(FunctionResolutionResultR.of(new CrossJoinFunDef(functionMetaData), conversions));
      }
    }

    protected FunctionDefinition createFunDef(Expression[] args, FunctionMetaData functionMetaData) {
      return new CrossJoinFunDef(functionMetaData);
    }

    @Override
    public OperationAtom getFunctionAtom() {
        return functionAtom;
    }

    private static final List<FunctionMetaData> REPRESENTATIVE_METADATAS = List.<FunctionMetaData>of(
        FunctionMetaDataR.of(functionAtom, "Returns the cross product of two sets.", DataType.SET,
            param(DataType.SET, "Set1"),
            param(DataType.SET, "Set2").repeatable(1)));

    @Override
    public List<FunctionMetaData> getRepresentativeFunctionMetaDatas() {
        return REPRESENTATIVE_METADATAS;
    }
  }
