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
package org.eclipse.daanse.olap.function.def.order;

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
import org.eclipse.daanse.olap.fun.sort.Sorter.SorterFlag;
import org.eclipse.daanse.olap.function.core.FunctionMetaDataR;
import org.eclipse.daanse.olap.function.core.FunctionParameterR;
import org.eclipse.daanse.olap.function.core.resolver.FunctionResolutionResultR;
import org.eclipse.daanse.olap.function.core.resolver.NoExpressionRequiredFunctionResolver;
import org.osgi.service.component.annotations.Component;

@Component(service = FunctionResolver.class)
public class OrderResolver  extends NoExpressionRequiredFunctionResolver {
    private final List<String> reservedWords;
    static FunctionParameterR[] argTypes;

    public OrderResolver() {

      this.reservedWords = SorterFlag.asReservedWords();
    }

    @Override
    public Optional<FunctionResolutionResult> resolve( Expression[] args, Validator validator ) {
      List<Conversion> conversions = new ArrayList<>();
      OrderResolver.argTypes = new FunctionParameterR[args.length];

      if ( args.length < 2 ) {
        return Optional.empty();
      }
      // first arg must be a set
      if ( !validator.canConvert( 0, args[0], DataType.SET, conversions ) ) {
        return Optional.empty();
      }
      OrderResolver.argTypes[0] = FunctionParameterR.param(DataType.SET);
      // after fist args, should be: value [, symbol]
      int i = 1;
      while ( i < args.length ) {
        if ( !validator.canConvert( i, args[i], DataType.VALUE, conversions ) ) {
          return Optional.empty();
        } else {
          OrderResolver.argTypes[i] = FunctionParameterR.param(DataType.VALUE);
          i++;
        }
        // if symbol is not specified, skip to the next
        if ( ( i == args.length ) ) {
          // done, will default last arg to ASC
        } else {
          if ( !validator.canConvert( i, args[i], DataType.SYMBOL, conversions ) ) {
            // continue, will default sort flag for prev arg to ASC
          } else {
            OrderResolver.argTypes[i] = FunctionParameterR.param(DataType.SYMBOL);
            i++;
          }
        }
      }

      return Optional.of(FunctionResolutionResultR.of(new OrderFunDef( OrderResolver.argTypes ), conversions));
    }

    @Override
    public List<String> getReservedWords() {
      if ( reservedWords != null ) {
        return reservedWords;
      }
      return super.getReservedWords();
    }

    @Override
    public OperationAtom getFunctionAtom() {
        return OrderFunDef.functionAtom;
    }

    private static final List<FunctionMetaData> REPRESENTATIVE_METADATAS = List.<FunctionMetaData>of(
        FunctionMetaDataR.of(OrderFunDef.functionAtom,
            "Arranges members of a set, optionally preserving or breaking the hierarchy.", DataType.SET,
            param(DataType.SET),
            param(DataType.VALUE, "Value_Expression").repeatable(1),
            param(DataType.SYMBOL, "Sort_Flag").repeatable(1).asSkippable()
                .reserved("ASC", "DESC", "BASC", "BDESC")
                .describedAs("ASC (default), DESC, BASC or BDESC — B-variants break the hierarchy.")));

    @Override
    public List<FunctionMetaData> getRepresentativeFunctionMetaDatas() {
        return REPRESENTATIVE_METADATAS;
    }
  }
