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
package org.eclipse.daanse.olap.function.def.vba.instr;

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
public class InStrResolver extends AbstractFunctionDefinitionMultiResolver {

    private static FunctionOperationAtom atom = new FunctionOperationAtom("InStr");
    private static String DESCRIPTION = """
        Returns a Variant (Long) specifying the position of the first
        occurrence of one string within another.""";

    private static FunctionParameterR[] p1 = { FunctionParameterR.param(DataType.STRING, "String_Check").describedAs("String Check"), FunctionParameterR.param(DataType.STRING, "String_Match").describedAs("String Match") };
    private static FunctionParameterR[] p2 = { FunctionParameterR.param(DataType.INTEGER, "Start").describedAs("Start"), FunctionParameterR.param(DataType.STRING, "String_Check").describedAs("String Check"),
            FunctionParameterR.param(DataType.STRING, "String_Match").describedAs("String Match") };
    private static FunctionParameterR[] p3 = { FunctionParameterR.param(DataType.INTEGER, "Start").describedAs("Start"), FunctionParameterR.param(DataType.STRING, "String_Check").describedAs("String Check"),
            FunctionParameterR.param(DataType.STRING, "String_Match").describedAs("String Match"),
            FunctionParameterR.param(DataType.INTEGER, "Compare").describedAs("Compare") };

    private static FunctionMetaData functionMetaData1 = new FunctionMetaDataR(atom, DESCRIPTION,
            DataType.INTEGER, p1).withTextKey("InStr.Basic").caption("InStr Function");
    private static FunctionMetaData functionMetaData2 = new FunctionMetaDataR(atom, DESCRIPTION,
            DataType.INTEGER, p2).withTextKey("InStr.WithStart").caption("InStr Function");
    private static FunctionMetaData functionMetaData3 = new FunctionMetaDataR(atom, DESCRIPTION,
            DataType.INTEGER, p3).withTextKey("InStr.WithCompare").caption("InStr Function");

    public InStrResolver() {
        super(List.of(new InStrFunDef(functionMetaData1), new InStrFunDef(functionMetaData2), new InStrFunDef(functionMetaData3)));
    }
}
