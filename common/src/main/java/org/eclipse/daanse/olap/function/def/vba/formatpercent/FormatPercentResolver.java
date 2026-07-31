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
package org.eclipse.daanse.olap.function.def.vba.formatpercent;

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
public class FormatPercentResolver extends AbstractFunctionDefinitionMultiResolver {

    private static FunctionOperationAtom atom = new FunctionOperationAtom("FormatPercent");
    private static String DESCRIPTION = """
        Returns an expression formatted as a percentage (multipled by 100)
        with a trailing % character.""";

    private static FunctionParameterR[] params = { FunctionParameterR.param(DataType.VALUE, "Expression"),
            FunctionParameterR.param(DataType.INTEGER, "Digits After Decimal").asOptional(),
            FunctionParameterR.param(DataType.INTEGER, "Include Leading Digit").asOptional(),
            FunctionParameterR.param(DataType.INTEGER, "Use Parens For Negative Numbers").asOptional(),
            FunctionParameterR.param(DataType.INTEGER, "Group Digits").asOptional() };

    private static FunctionMetaData functionMetaData = new FunctionMetaDataR(atom, DESCRIPTION,
            DataType.STRING, params);

    public FormatPercentResolver() {
        super(List.of(new FormatPercentFunDef(functionMetaData)));
    }
}
