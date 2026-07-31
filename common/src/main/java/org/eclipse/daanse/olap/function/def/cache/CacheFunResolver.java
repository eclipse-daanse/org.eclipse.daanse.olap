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
package org.eclipse.daanse.olap.function.def.cache;

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
import org.eclipse.daanse.olap.function.core.FunctionMetaDataR;
import org.eclipse.daanse.olap.function.core.resolver.FunctionResolutionResultR;
import org.eclipse.daanse.olap.function.core.resolver.NoExpressionRequiredFunctionResolver;
import org.osgi.service.component.annotations.Component;

@Component(service = FunctionResolver.class)
public class CacheFunResolver extends NoExpressionRequiredFunctionResolver {

    @Override
    public Optional<FunctionResolutionResult> resolve(
        Expression[] args,
        Validator validator)
    {
        if (args.length != 1) {
            return Optional.empty();
        }
        final Expression exp = args[0];
        return Optional.of(FunctionResolutionResultR.of(new CacheFunDef(exp.getCategory()), List.<Conversion>of()));
    }

    @Override
    public OperationAtom getFunctionAtom() {
        return CacheFunDef.functionAtom;
    }

    private static final List<FunctionMetaData> REPRESENTATIVE_METADATAS = List.<FunctionMetaData>of(
            FunctionMetaDataR.of(CacheFunDef.functionAtom,
                    "Evaluates and returns its sole argument, applying statement-level caching", DataType.VALUE,
                    param(DataType.VALUE)));

    @Override
    public List<FunctionMetaData> getRepresentativeFunctionMetaDatas() {
        return REPRESENTATIVE_METADATAS;
    }

}
