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
package org.eclipse.daanse.olap.function.def.descendants;

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
public class DescendantsSetResolver extends AbstractFunctionDefinitionMultiResolver {
    private static String descFlagDescription = "Controls which levels relative to the member are included: SELF, AFTER, BEFORE, BEFORE_AND_AFTER, SELF_AND_AFTER, SELF_AND_BEFORE, SELF_BEFORE_AFTER, LEAVES.";
    private static FunctionOperationAtom atom = new FunctionOperationAtom("Descendants");
    private static String DESCRIPTION = "Returns the set of descendants of a set of members at a specified level, optionally including or excluding descendants in other levels.";
    private static FunctionParameterR[] x = { FunctionParameterR.param(DataType.SET).describedAs("Set") };
    private static FunctionParameterR[] xl = { FunctionParameterR.param(DataType.SET).describedAs("Set"),
            FunctionParameterR.param(DataType.LEVEL).describedAs("Level") };
    private static FunctionParameterR[] xly = { FunctionParameterR.param(DataType.SET).describedAs("Set"),
            FunctionParameterR.param(DataType.LEVEL).describedAs("Level"), new FunctionParameterR(DataType.SYMBOL, Optional.of("Desc_flag"),
                    Optional.of(descFlagDescription), Optional.of(Flag.asReservedWords())) };
    private static FunctionParameterR[] xn = { FunctionParameterR.param(DataType.SET).describedAs("Set"),
            FunctionParameterR.param(DataType.NUMERIC, "Limit").describedAs("Limit") };
    private static FunctionParameterR[] xny = { FunctionParameterR.param(DataType.SET).describedAs("Set"),
            FunctionParameterR.param(DataType.NUMERIC, "Limit").describedAs("Limit"), new FunctionParameterR(DataType.SYMBOL, "Desc_flag", descFlagDescription) };
    private static FunctionParameterR[] xey = { FunctionParameterR.param(DataType.SET).describedAs("Set"),
            FunctionParameterR.param(DataType.EMPTY).describedAs("Empty"), new FunctionParameterR(DataType.SYMBOL, "Desc_flag", descFlagDescription) };
    // {"fxx", "fxxl", "fxxly", "fxxn", "fxxny", "fxxey"}

    private static FunctionMetaData functionMetaData = new FunctionMetaDataR(atom, DESCRIPTION, DataType.SET,
            x).withTextKey("Descendants.Set.Function").caption("Descendants Function");
    private static FunctionMetaData functionMetaData1 = new FunctionMetaDataR(atom, DESCRIPTION,
            DataType.SET, xl).withTextKey("Descendants.Set.Level.Function").caption("Descendants Function");
    private static FunctionMetaData functionMetaData2 = new FunctionMetaDataR(atom, DESCRIPTION,
            DataType.SET, xly).withTextKey("Descendants.Set.Level.Symbol.Function").caption("Descendants Function");
    private static FunctionMetaData functionMetaData3 = new FunctionMetaDataR(atom, DESCRIPTION,
            DataType.SET, xn).withTextKey("Descendants.Set.Numeric.Function").caption("Descendants Function");
    private static FunctionMetaData functionMetaData4 = new FunctionMetaDataR(atom, DESCRIPTION,
            DataType.SET, xny).withTextKey("Descendants.Set.Numeric.Symbol.Function").caption("Descendants Function");
    private static FunctionMetaData functionMetaData5 = new FunctionMetaDataR(atom, DESCRIPTION,
            DataType.SET, xey).withTextKey("Descendants.Set.Empty.Symbol.Function").caption("Descendants Function");

    @Override
    public List<String> getReservedWords() {
        return Flag.asReservedWords();
    }


    public DescendantsSetResolver() {
        super(List.of(new DescendantsByLevelFunDef(functionMetaData), new DescendantsByLevelFunDef(functionMetaData1),
                new DescendantsByLevelFunDef(functionMetaData2), new DescendantsByLevelFunDef(functionMetaData3),
                new DescendantsByLevelFunDef(functionMetaData4), new DescendantsByLevelFunDef(functionMetaData5)));
    }
}
