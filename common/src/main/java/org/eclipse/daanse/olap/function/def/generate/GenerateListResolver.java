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
package org.eclipse.daanse.olap.function.def.generate;

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
public class GenerateListResolver extends AbstractFunctionDefinitionMultiResolver {
    private static FunctionOperationAtom atom = new FunctionOperationAtom("Generate");
    private static String DESCRIPTION = "Applies a set to each member of another set and joins the resulting sets by union.";

    private static FunctionMetaData functionMetaData = new FunctionMetaDataR(atom, DESCRIPTION,
            DataType.SET, new FunctionParameterR[] { FunctionParameterR.param(DataType.SET, "Set1"),
                    FunctionParameterR.param(DataType.SET, "Set2"),
                    new FunctionParameterR(DataType.SYMBOL, "All", Optional.of(GenerateFunDef.ReservedWords))
                            .describedAs("ALL retains duplicates produced by the iteration; without it duplicates are removed.")
                            .asOptional() });

    public GenerateListResolver() {
        super(List.of(new GenerateFunDef(functionMetaData)));
    }
}