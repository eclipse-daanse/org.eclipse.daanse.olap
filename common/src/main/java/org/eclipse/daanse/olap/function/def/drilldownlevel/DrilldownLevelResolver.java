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

import static org.eclipse.daanse.olap.function.core.FunctionParameterR.canonicalNameOf;

@Component(service = FunctionResolver.class)
public class DrilldownLevelResolver extends AbstractFunctionDefinitionMultiResolver {
    private static FunctionOperationAtom atom = new FunctionOperationAtom("DrilldownLevel");
    private static List<String> RESERVED_WORDS = List.of("INCLUDE_CALC_MEMBERS");
    private static String DESCRIPTION = "Drills down the members of a set, at a specified level, to one level below. Alternatively, drills down on a specified dimension in the set.";
    private static FunctionParameterR[] x = { FunctionParameterR.param(DataType.SET).describedAs("Set") };
    private static FunctionParameterR[] xl = { FunctionParameterR.param(DataType.SET).describedAs("Set"),
            FunctionParameterR.param(DataType.LEVEL).describedAs("Level").describedAs("Level") };
    private static FunctionParameterR[] xen = { FunctionParameterR.param(DataType.SET).describedAs("Set"),
            FunctionParameterR.param(DataType.EMPTY), FunctionParameterR.param(DataType.NUMERIC, "Index").describedAs("Index") };
    private static FunctionParameterR[] xeny = { FunctionParameterR.param(DataType.SET).describedAs("Set"),
            FunctionParameterR.param(DataType.EMPTY), FunctionParameterR.param(DataType.NUMERIC, "Index").describedAs("Index"),
            new FunctionParameterR(DataType.SYMBOL, "Include_Members", Optional.of(RESERVED_WORDS))
                    .describedAs("INCLUDE_CALC_MEMBERS includes calculated members in the drilled-down result.")};
    private static FunctionParameterR[] xeey = { FunctionParameterR.param(DataType.SET).describedAs("Set"),
            FunctionParameterR.param(DataType.EMPTY, canonicalNameOf(DataType.EMPTY) + 1), FunctionParameterR.param(DataType.EMPTY, canonicalNameOf(DataType.EMPTY) + 2),
            new FunctionParameterR(DataType.SYMBOL, "Include_Members", Optional.of(RESERVED_WORDS))
                    .describedAs("INCLUDE_CALC_MEMBERS includes calculated members in the drilled-down result.") };
    // {"fxx", "fxxl", "fxxen", "fxxeny", "fxxeey"}


    private static FunctionMetaData functionMetaData = new FunctionMetaDataR(atom, DESCRIPTION, DataType.SET,
            x).withTextKey("DrilldownLevel");
    private static FunctionMetaData functionMetaData1 = new FunctionMetaDataR(atom, DESCRIPTION,
            DataType.SET, xl).withTextKey("DrilldownLevel.withLevel");;
    private static FunctionMetaData functionMetaData2 = new FunctionMetaDataR(atom, DESCRIPTION,
            DataType.SET, xen).withTextKey("DrilldownLevel.withIndex");
    private static FunctionMetaData functionMetaData3 = new FunctionMetaDataR(atom, DESCRIPTION,
            DataType.SET, xeny).withTextKey("DrilldownLevel.withIndexIncludeMembers");
    private static FunctionMetaData functionMetaData4 = new FunctionMetaDataR(atom, DESCRIPTION,
            DataType.SET, xeey).withTextKey("DrilldownLevel.withIncludeMembers");

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
