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
package org.eclipse.daanse.olap.function.def.udf.currentdatemember;

import java.util.List;
import java.util.Optional;

import org.eclipse.daanse.mdx.model.api.expression.operation.FunctionOperationAtom;
import org.eclipse.daanse.olap.api.DataType;
import org.eclipse.daanse.olap.api.function.FunctionInterface;
import org.eclipse.daanse.olap.api.function.FunctionMetaData;
import org.eclipse.daanse.olap.api.function.FunctionOrigin;
import org.eclipse.daanse.olap.api.function.FunctionResolver;
import org.eclipse.daanse.olap.function.core.FunctionMetaDataR;
import org.eclipse.daanse.olap.function.core.FunctionParameterR;
import org.eclipse.daanse.olap.function.core.resolver.AbstractFunctionDefinitionMultiResolver;
import org.osgi.service.component.annotations.Component;

@Component(service = FunctionResolver.class)
public class CurrentDateMemberResolver extends AbstractFunctionDefinitionMultiResolver {
    private final static FunctionOperationAtom atom = new FunctionOperationAtom("CurrentDateMember");
    private final static List<String> reservedWords = List.of("EXACT", "BEFORE", "AFTER");
    private final static String DESCRIPTION = """
            Returns the closest or exact member within the specified
            dimension corresponding to the current date, in the format
            specified by the format parameter.
            Format strings are the same as used by the MDX Format function,
            namely the Visual Basic format strings.
            See http://www.apostate.com/programming/vb-format.html.""";

    private static FunctionParameterR[] fp = { FunctionParameterR.param(DataType.HIERARCHY),
            FunctionParameterR.param(DataType.STRING, "Format"),
            new FunctionParameterR(DataType.SYMBOL, "MatchType", Optional.of(reservedWords))
                    .describedAs("EXACT (default), BEFORE or AFTER — how the current date is matched to a member.")
                    .asOptional() };

    private static FunctionMetaData functionMetaData = new FunctionMetaDataR(atom, DESCRIPTION,
            DataType.MEMBER, fp).interfaceName(FunctionInterface.DATETIME).origin(FunctionOrigin.UDF).library("daanse.udf");

    @Override
    public List<String> getReservedWords() {
        return reservedWords;
    }

    public CurrentDateMemberResolver() {
        super(List.of(new CurrentDateMemberFunDef(functionMetaData)));
    }

}
