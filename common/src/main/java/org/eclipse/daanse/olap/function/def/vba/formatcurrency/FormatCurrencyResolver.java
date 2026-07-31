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
package org.eclipse.daanse.olap.function.def.vba.formatcurrency;

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
public class FormatCurrencyResolver extends AbstractFunctionDefinitionMultiResolver {

    private static FunctionOperationAtom atom = new FunctionOperationAtom("FormatCurrency");
    private static String DESCRIPTION = """
        Returns an expression formatted as a currency value using the
        currency symbol defined in the system control panel.""";

    private static FunctionParameterR[] params = { FunctionParameterR.param(DataType.VALUE, "expression"),
            FunctionParameterR.param(DataType.INTEGER, "numDigitsAfterDecimal").asOptional(),
            FunctionParameterR.param(DataType.INTEGER, "includeLeadingDigit").asOptional(),
            FunctionParameterR.param(DataType.INTEGER, "useParensForNegativeNumbers").asOptional(),
            FunctionParameterR.param(DataType.INTEGER, "groupDigits").asOptional() };

    private static FunctionMetaData functionMetaData = new FunctionMetaDataR(atom, DESCRIPTION,
            DataType.STRING, params);

    public FormatCurrencyResolver() {
        super(List.of(new FormatCurrencyFunDef(functionMetaData)));
    }
}
