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
package org.eclipse.daanse.olap.function.def.parameter;

import java.util.List;

import org.eclipse.daanse.mdx.model.api.expression.operation.FunctionOperationAtom;
import org.eclipse.daanse.olap.api.DataType;
import org.eclipse.daanse.olap.api.function.FunctionDefinition;
import org.eclipse.daanse.olap.api.function.FunctionMetaData;
import org.eclipse.daanse.olap.api.function.FunctionResolver;
import org.eclipse.daanse.olap.api.query.component.Expression;
import org.eclipse.daanse.olap.function.core.FunctionMetaDataR;
import org.eclipse.daanse.olap.function.core.FunctionParameterR;
import org.eclipse.daanse.olap.function.core.resolver.AbstractMetaDataMultiResolver;
import org.osgi.service.component.annotations.Component;

/**
 * Resolves calls to the ParamRef MDX function.
 */
@Component(service = FunctionResolver.class)
public class ParamRefResolver  extends AbstractMetaDataMultiResolver {

    private static FunctionOperationAtom atom = new FunctionOperationAtom("ParamRef");
    private static String DESCRIPTION = "Returns the current value of this parameter. If it is null, returns the default value.";
    
    private static FunctionParameterR[] S = { new FunctionParameterR(DataType.STRING, "Name") };
    //"fvS"
    
    private static FunctionMetaData functionMetaData = new FunctionMetaDataR(atom, DESCRIPTION,
            DataType.VALUE, S);

    public ParamRefResolver() {
        super(List.of(functionMetaData));
    }


    @Override
    protected FunctionDefinition createFunDef(Expression[] args, FunctionMetaData functionMetaData,
            FunctionMetaData fmdTarget) {
        String parameterName = ParameterFunDef.getParameterName(args);
        return new ParameterFunDef(
            functionMetaData, parameterName, null, DataType.UNKNOWN, null,
            null);
    }

}
