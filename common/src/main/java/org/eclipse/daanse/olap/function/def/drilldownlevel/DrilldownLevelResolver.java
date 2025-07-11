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
package org.eclipse.daanse.olap.function.def.drilldownlevel;

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
public class DrilldownLevelResolver extends AbstractFunctionDefinitionMultiResolver {
    private static FunctionOperationAtom atom = new FunctionOperationAtom("DrilldownLevel");
    private static List<String> RESERVED_WORDS = List.of("INCLUDE_CALC_MEMBERS");
    private static String DESCRIPTION = "Drills down the members of a set, at a specified level, to one level below. Alternatively, drills down on a specified dimension in the set.";
    private static FunctionParameterR[] x = { new FunctionParameterR(DataType.SET) };
    private static FunctionParameterR[] xl = { new FunctionParameterR(DataType.SET),
            new FunctionParameterR(DataType.LEVEL) };
    private static FunctionParameterR[] xen = { new FunctionParameterR(DataType.SET),
            new FunctionParameterR(DataType.EMPTY), new FunctionParameterR(DataType.NUMERIC, "Index") };
    private static FunctionParameterR[] xeny = { new FunctionParameterR(DataType.SET),
            new FunctionParameterR(DataType.EMPTY), new FunctionParameterR(DataType.NUMERIC, "Index"), new FunctionParameterR(DataType.SYMBOL, "Include members", Optional.of(RESERVED_WORDS))};
    private static FunctionParameterR[] xeey = { new FunctionParameterR(DataType.SET),
            new FunctionParameterR(DataType.EMPTY), new FunctionParameterR(DataType.EMPTY), new FunctionParameterR(DataType.SYMBOL, "Include members", Optional.of(RESERVED_WORDS)) };
    // {"fxx", "fxxl", "fxxen", "fxxeny", "fxxeey"}


    private static FunctionMetaData functionMetaData = new FunctionMetaDataR(atom, DESCRIPTION, DataType.SET,
            x);
    private static FunctionMetaData functionMetaData1 = new FunctionMetaDataR(atom, DESCRIPTION,
            DataType.SET, xl);
    private static FunctionMetaData functionMetaData2 = new FunctionMetaDataR(atom, DESCRIPTION,
            DataType.SET, xen);
    private static FunctionMetaData functionMetaData3 = new FunctionMetaDataR(atom, DESCRIPTION,
            DataType.SET, xeny);
    private static FunctionMetaData functionMetaData4 = new FunctionMetaDataR(atom, DESCRIPTION,
            DataType.SET, xeey);

    @Override
    public List<String> getReservedWords() {
        return RESERVED_WORDS;
    }


    public DrilldownLevelResolver() {
        super(List.of(new DrilldownLevelFunDef(functionMetaData), new DrilldownLevelFunDef(functionMetaData1),
                new DrilldownLevelFunDef(functionMetaData2), new DrilldownLevelFunDef(functionMetaData3),
                new DrilldownLevelFunDef(functionMetaData4)));
    }
}
