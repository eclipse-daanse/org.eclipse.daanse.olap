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
package org.eclipse.daanse.olap.function.def.drilldownleveltopbottom;

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
public class DrilldownLevelTopResolver extends AbstractFunctionDefinitionMultiResolver {
    private static FunctionOperationAtom atom = new FunctionOperationAtom("DrilldownLevelTop");
    private static String DESCRIPTION = "Drills down the topmost members of a set, at a specified level, to one level below.";
    private static FunctionParameterR[] xn = { FunctionParameterR.param(DataType.SET),
            FunctionParameterR.param(DataType.NUMERIC, "Count") };
    private static FunctionParameterR[] xnl = { FunctionParameterR.param(DataType.SET),
            FunctionParameterR.param(DataType.NUMERIC, "Count"), FunctionParameterR.param(DataType.LEVEL) };
    private static FunctionParameterR[] xnln = { FunctionParameterR.param(DataType.SET),
            FunctionParameterR.param(DataType.NUMERIC, "Count"), FunctionParameterR.param(DataType.LEVEL),
            FunctionParameterR.param(DataType.NUMERIC, "Numeric Expression") };
    private static FunctionParameterR[] xnen = { FunctionParameterR.param(DataType.SET),
            FunctionParameterR.param(DataType.NUMERIC, "Count"), FunctionParameterR.param(DataType.EMPTY),
            FunctionParameterR.param(DataType.NUMERIC, "Numeric Expression") };
    // {"fxxn", "fxxnl", "fxxnln", "fxxnen"}

    private static FunctionMetaData functionMetaData1 = new FunctionMetaDataR(atom, DESCRIPTION,
            DataType.SET, xn);
    private static FunctionMetaData functionMetaData2 = new FunctionMetaDataR(atom, DESCRIPTION,
            DataType.SET, xnl);
    private static FunctionMetaData functionMetaData3 = new FunctionMetaDataR(atom, DESCRIPTION,
            DataType.SET, xnln);
    private static FunctionMetaData functionMetaData4 = new FunctionMetaDataR(atom, DESCRIPTION,
            DataType.SET, xnen);

    public DrilldownLevelTopResolver() {
        super(List.of(new DrilldownLevelTopBottomFunDef(functionMetaData1, true),
                new DrilldownLevelTopBottomFunDef(functionMetaData2, true),
                new DrilldownLevelTopBottomFunDef(functionMetaData3, true),
                new DrilldownLevelTopBottomFunDef(functionMetaData4, true)));
    }
}
