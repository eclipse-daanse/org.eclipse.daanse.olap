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
package org.eclipse.daanse.olap.function.def.linreg;

import java.util.List;

import org.eclipse.daanse.mdx.model.api.expression.operation.FunctionOperationAtom;
import org.eclipse.daanse.olap.api.DataType;
import org.eclipse.daanse.olap.api.function.FunctionInterface;
import org.eclipse.daanse.olap.api.function.FunctionMetaData;
import org.eclipse.daanse.olap.api.function.FunctionResolver;
import org.eclipse.daanse.olap.function.core.FunctionMetaDataR;
import org.eclipse.daanse.olap.function.core.FunctionParameterR;
import org.eclipse.daanse.olap.function.core.resolver.AbstractFunctionDefinitionMultiResolver;
import org.osgi.service.component.annotations.Component;

@Component(service = FunctionResolver.class)
public class LinRegInterceptResolver extends AbstractFunctionDefinitionMultiResolver {
    private static FunctionOperationAtom atom = new FunctionOperationAtom("LinRegIntercept");
    private static String DESCRIPTION = "Calculates the linear regression of a set and returns the value of b in the regression line y = ax + b.";

    private static FunctionMetaData functionMetaData = new FunctionMetaDataR(atom, DESCRIPTION,
            DataType.NUMERIC, new FunctionParameterR[] { FunctionParameterR.param(DataType.SET),
                    FunctionParameterR.param(DataType.NUMERIC, "NumericY"),
                    FunctionParameterR.param(DataType.NUMERIC, "NumericX").asOptional() }).interfaceName(FunctionInterface.STATISTICAL);

    public LinRegInterceptResolver() {
        super(List.of(new LinRegFunDef(functionMetaData, LinRegFunDef.INTERCEPT)));
    }
}
