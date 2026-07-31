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
package org.eclipse.daanse.olap.function.def.nonstandard;

import static org.eclipse.daanse.olap.function.core.FunctionParameterR.param;

import java.util.List;
import java.util.Optional;

import org.eclipse.daanse.mdx.model.api.expression.operation.OperationAtom;
import org.eclipse.daanse.olap.api.DataType;
import org.eclipse.daanse.olap.api.function.FunctionMetaData;
import org.eclipse.daanse.olap.api.function.FunctionResolutionResult;
import org.eclipse.daanse.olap.api.function.FunctionResolver;
import org.eclipse.daanse.olap.api.query.Validator;
import org.eclipse.daanse.olap.api.query.component.Expression;
import org.eclipse.daanse.olap.api.query.component.Literal;
import org.eclipse.daanse.olap.exceptions.CastInvalidTypeException;
import org.eclipse.daanse.olap.function.core.FunctionMetaDataR;
import org.eclipse.daanse.olap.function.core.resolver.FunctionResolutionResultR;
import org.eclipse.daanse.olap.function.core.resolver.NoExpressionRequiredFunctionResolver;
import org.eclipse.daanse.olap.query.base.Expressions;
import org.osgi.service.component.annotations.Component;

@Component(service = FunctionResolver.class)
public class CastResolver extends NoExpressionRequiredFunctionResolver {

    @Override
    public Optional<FunctionResolutionResult> resolve(
        Expression[] args, Validator validator)
    {
        if (args.length != 2) {
            return Optional.empty();
        }
        if (!(args[1] instanceof Literal literal)) {
            return Optional.empty();
        }
        String typeName = (String) literal.getValue();
        DataType returnCategory;
        if (typeName.equalsIgnoreCase("String")) {
            returnCategory = DataType.STRING;
        } else if (typeName.equalsIgnoreCase("Numeric")) {
            returnCategory = DataType.NUMERIC;
        } else if (typeName.equalsIgnoreCase("Boolean")) {
            returnCategory = DataType.LOGICAL;
        } else if (typeName.equalsIgnoreCase("Integer")) {
            returnCategory = DataType.INTEGER;
        } else {
            throw new CastInvalidTypeException(typeName);
        }


        FunctionMetaData functionMetaData = new FunctionMetaDataR(CastFunDef.functionAtom, "Converts values to another type.",
                returnCategory, Expressions.functionParameterOf(args));
        return Optional.of(FunctionResolutionResultR.of(new CastFunDef(functionMetaData), List.<Conversion>of()));
    }

    @Override
    public OperationAtom getFunctionAtom() {
        return CastFunDef.functionAtom;
    }

    private static final List<FunctionMetaData> REPRESENTATIVE_METADATAS = List.<FunctionMetaData>of(
        FunctionMetaDataR.of(CastFunDef.functionAtom, "Converts values to another type.", DataType.VALUE,
            param(DataType.VALUE),
            param(DataType.STRING, "Type_Name").reserved("String", "Numeric", "Integer", "Boolean")));

    @Override
    public List<FunctionMetaData> getRepresentativeFunctionMetaDatas() {
        return REPRESENTATIVE_METADATAS;
    }
}
