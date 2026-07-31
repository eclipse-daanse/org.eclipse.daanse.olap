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
package org.eclipse.daanse.olap.function.def.vba.ipmt;

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
public class IPmtResolver extends AbstractFunctionDefinitionMultiResolver {

    private static FunctionOperationAtom atom = new FunctionOperationAtom("IPmt");
    private static String DESCRIPTION = """
            Returns a Double specifying the interest payment for a given period
            of an annuity based on periodic, fixed payments and a fixed
            interest rate.""";

    private static FunctionParameterR[] params = { FunctionParameterR.param(DataType.NUMERIC, "Rate"),
            FunctionParameterR.param(DataType.NUMERIC, "Per"), FunctionParameterR.param(DataType.NUMERIC, "NPer"),
            FunctionParameterR.param(DataType.NUMERIC, "Pv"),
            FunctionParameterR.param(DataType.NUMERIC, "Fv").asOptional(),
            FunctionParameterR.param(DataType.LOGICAL, "due").asOptional() };

    private static FunctionMetaData functionMetaData = new FunctionMetaDataR(atom, DESCRIPTION,
            DataType.NUMERIC, params);

    public IPmtResolver() {
        super(List.of(new IPmtFunDef(functionMetaData)));
    }
}