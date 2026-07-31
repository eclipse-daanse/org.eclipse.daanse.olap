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
package org.eclipse.daanse.olap.function.def.aggregate.median;

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
public class MedianResolver extends AbstractFunctionDefinitionMultiResolver {
    private static FunctionOperationAtom atom = new FunctionOperationAtom("Median");
    private static String DESCRIPTION = "Returns the median value of a numeric expression evaluated over a set.";

    private static FunctionMetaData functionMetaData = new FunctionMetaDataR(atom, DESCRIPTION,
            DataType.NUMERIC, new FunctionParameterR[] { FunctionParameterR.param(DataType.SET),
                    FunctionParameterR.param(DataType.NUMERIC, "Percentile").asOptional() }).interfaceName(FunctionInterface.STATISTICAL);

    public MedianResolver() {
        super(List.of(new MedianFunDef(functionMetaData)));
    }
}
