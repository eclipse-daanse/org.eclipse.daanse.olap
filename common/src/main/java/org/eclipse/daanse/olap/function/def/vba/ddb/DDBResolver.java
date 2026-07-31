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
package org.eclipse.daanse.olap.function.def.vba.ddb;

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
public class DDBResolver extends AbstractFunctionDefinitionMultiResolver {
    
    private static FunctionOperationAtom atom = new FunctionOperationAtom("DDB");
    private static String DESCRIPTION = """
            Returns a Double specifying the depreciation of an asset for a
            specific time period using the double-declining balance method or
            some other method you specify.""";
    
    private static FunctionParameterR[] params = { FunctionParameterR.param(DataType.NUMERIC, "Cost"),
            FunctionParameterR.param(DataType.NUMERIC, "Salvage"), FunctionParameterR.param(DataType.NUMERIC, "Life"),
            FunctionParameterR.param(DataType.NUMERIC, "Period"),
            FunctionParameterR.param(DataType.NUMERIC, "Factor").asOptional() };

    private static FunctionMetaData functionMetaData = new FunctionMetaDataR(atom, DESCRIPTION,
            DataType.NUMERIC, params);

    public DDBResolver() {
        super(List.of(new DDBFunDef(functionMetaData)));
    }
}    