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
package org.eclipse.daanse.olap.function.def.vba.replace;

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
public class ReplaceResolver extends AbstractFunctionDefinitionMultiResolver {

    private static FunctionOperationAtom atom = new FunctionOperationAtom("Replace");
    private static String DESCRIPTION = """
        Returns a string in which a specified substring has been replaced
        with another substring a specified number of times.""";

    private static FunctionParameterR[] params = { FunctionParameterR.param(DataType.STRING, "expression"),
            FunctionParameterR.param(DataType.STRING, "find"), FunctionParameterR.param(DataType.STRING, "replace"),
            FunctionParameterR.param(DataType.INTEGER, "start").asOptional(),
            FunctionParameterR.param(DataType.INTEGER, "coun").asOptional(),
            FunctionParameterR.param(DataType.INTEGER, "compare").asOptional() }; // compare is currently ignored

    private static FunctionMetaData functionMetaData = new FunctionMetaDataR(atom, DESCRIPTION,
            DataType.NUMERIC, params);

    public ReplaceResolver() {
        super(List.of(new ReplaceFunDef(functionMetaData)));
    }
}
