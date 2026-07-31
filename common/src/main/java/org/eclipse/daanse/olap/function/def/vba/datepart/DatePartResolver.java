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
package org.eclipse.daanse.olap.function.def.vba.datepart;

import java.util.List;

import org.eclipse.daanse.mdx.model.api.expression.operation.FunctionOperationAtom;
import org.eclipse.daanse.olap.api.DataType;
import org.eclipse.daanse.olap.api.function.FunctionMetaData;
import org.eclipse.daanse.olap.api.function.FunctionResolver;
import org.eclipse.daanse.olap.function.core.FunctionMetaDataR;
import org.eclipse.daanse.olap.function.core.FunctionParameterR;
import org.eclipse.daanse.olap.function.core.resolver.AbstractFunctionDefinitionMultiResolver;
import org.osgi.service.component.annotations.Component;

@Component(service = FunctionResolver.class)
public class DatePartResolver extends AbstractFunctionDefinitionMultiResolver {
    
    private static FunctionOperationAtom atom = new FunctionOperationAtom("DatePart");
    private static String DESCRIPTION = """
            Returns a Variant (Integer) containing the specified part of a given
            date.""";
    private static FunctionParameterR[] params = { FunctionParameterR.param(DataType.STRING, "IntervalName"),
            FunctionParameterR.param(DataType.DATE_TIME, "Date1"), FunctionParameterR.param(DataType.DATE_TIME, "Date2"),
            FunctionParameterR.param(DataType.INTEGER, "First Day Of Week").asOptional(),
            FunctionParameterR.param(DataType.INTEGER, "First Week Of Year").asOptional() };

    private static FunctionMetaData functionMetaData = new FunctionMetaDataR(atom, DESCRIPTION,
            DataType.NUMERIC, params);

    public DatePartResolver() {
        super(List.of(new DatePartFunDef(functionMetaData)));
    }
}    