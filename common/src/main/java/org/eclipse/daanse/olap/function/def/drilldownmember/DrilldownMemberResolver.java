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
package org.eclipse.daanse.olap.function.def.drilldownmember;

import java.util.List;
import java.util.Optional;

import org.eclipse.daanse.mdx.model.api.expression.operation.FunctionOperationAtom;
import org.eclipse.daanse.olap.api.DataType;
import org.eclipse.daanse.olap.api.function.FunctionMetaData;
import org.eclipse.daanse.olap.api.function.FunctionResolver;
import org.eclipse.daanse.olap.function.core.FunctionMetaDataR;
import org.eclipse.daanse.olap.function.core.FunctionParameterR;
import org.eclipse.daanse.olap.function.core.resolver.AbstractFunctionDefinitionMultiResolver;
import org.osgi.service.component.annotations.Component;

@Component(service = FunctionResolver.class)
public class DrilldownMemberResolver extends AbstractFunctionDefinitionMultiResolver {
    private static FunctionOperationAtom atom = new FunctionOperationAtom("DrilldownMember");
    private static List<String> reservedWords = List.of("RECURSIVE");
    private static String DESCRIPTION = "Drills down the members in a set that are present in a second specified set.";

    private static FunctionMetaData functionMetaData = new FunctionMetaDataR(atom, DESCRIPTION,
            DataType.SET, new FunctionParameterR[] { FunctionParameterR.param(DataType.SET, "Set1"),
                    FunctionParameterR.param(DataType.SET, "Set2"),
                    new FunctionParameterR(DataType.SYMBOL, "Recursive", Optional.of(reservedWords))
                            .describedAs("RECURSIVE drills down every matching member at every level, not only the first match.")
                            .asOptional() });

    @Override
    public List<String> getReservedWords() {
        return reservedWords;
    }


    public DrilldownMemberResolver() {
        super(List.of(new DrilldownMemberFunDef(functionMetaData)));
    }
}