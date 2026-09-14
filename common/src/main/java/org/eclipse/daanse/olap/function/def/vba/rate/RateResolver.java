/*
* Copyright (c) 2025 Contributors to the Eclipse Foundation.
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
package org.eclipse.daanse.olap.function.def.vba.rate;

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
public class RateResolver extends AbstractFunctionDefinitionMultiResolver {

    private static FunctionOperationAtom atom = new FunctionOperationAtom("Rate");
    private static String DESCRIPTION = """
        Returns a Double specifying the interest rate per period for an
        annuity.""";

    private static FunctionParameterR[] params = { FunctionParameterR.param(DataType.NUMERIC, "NPer").describedAs("NPer"),
            FunctionParameterR.param(DataType.NUMERIC, "Pmt").describedAs("Pmt"), FunctionParameterR.param(DataType.NUMERIC, "Pv").describedAs("Pv"),
            FunctionParameterR.param(DataType.NUMERIC, "Fv").describedAs("Fv").asOptional(),
            FunctionParameterR.param(DataType.LOGICAL, "Due").describedAs("Due").asOptional(),
            FunctionParameterR.param(DataType.NUMERIC, "Guess").describedAs("Guess").asOptional() };

    private static FunctionMetaData functionMetaData = new FunctionMetaDataR(atom, DESCRIPTION,
            DataType.NUMERIC, params).withTextKey("Rate").caption("Rate Function");

    public RateResolver() {
        super(List.of(new RateFunDef(functionMetaData)));
    }
}
