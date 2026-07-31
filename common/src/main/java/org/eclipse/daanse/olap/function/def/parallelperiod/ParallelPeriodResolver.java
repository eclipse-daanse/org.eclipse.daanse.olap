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
package org.eclipse.daanse.olap.function.def.parallelperiod;

import java.util.List;

import org.eclipse.daanse.mdx.model.api.expression.operation.FunctionOperationAtom;
import org.eclipse.daanse.mdx.model.api.expression.operation.OperationAtom;
import org.eclipse.daanse.olap.api.DataType;
import org.eclipse.daanse.olap.api.function.FunctionInterface;
import org.eclipse.daanse.olap.api.function.FunctionMetaData;
import org.eclipse.daanse.olap.api.function.FunctionResolver;
import org.eclipse.daanse.olap.function.core.FunctionMetaDataR;
import org.eclipse.daanse.olap.function.core.FunctionParameterR;
import org.eclipse.daanse.olap.function.core.resolver.AbstractFunctionDefinitionMultiResolver;
import org.osgi.service.component.annotations.Component;

@Component(service = FunctionResolver.class)
public class ParallelPeriodResolver extends AbstractFunctionDefinitionMultiResolver {

    private static OperationAtom atom = new FunctionOperationAtom("ParallelPeriod");
    private static String DESCRIPTION = "Returns a member from a prior period in the same relative position as a specified member.";
    // {"fm", "fml", "fmln", "fmlnm"}

    private static FunctionMetaData functionMetaData = new FunctionMetaDataR(atom, DESCRIPTION,
            DataType.MEMBER, new FunctionParameterR[] { FunctionParameterR.param(DataType.LEVEL).asOptional(),
                    FunctionParameterR.param(DataType.NUMERIC).asOptional(),
                    FunctionParameterR.param(DataType.MEMBER).asOptional() }).interfaceName(FunctionInterface.DATETIME);

    public ParallelPeriodResolver() {
        super(List.of(new ParallelPeriodFunDef(functionMetaData)));
    }
}
