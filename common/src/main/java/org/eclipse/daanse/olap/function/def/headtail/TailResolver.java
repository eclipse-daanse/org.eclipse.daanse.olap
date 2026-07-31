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
package org.eclipse.daanse.olap.function.def.headtail;

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
public class TailResolver extends AbstractFunctionDefinitionMultiResolver {
    private static FunctionOperationAtom atom = new FunctionOperationAtom("Tail");
    private static String DESCRIPTION = "Returns a subset from the end of a set.";

    private static FunctionMetaData functionMetaData = new FunctionMetaDataR(atom, DESCRIPTION,
            DataType.SET, new FunctionParameterR[] { FunctionParameterR.param(DataType.SET),
                    FunctionParameterR.param(DataType.NUMERIC, "Count")
                            .describedAs("Number of members to return; defaults to 1.").asOptional() });

    public TailResolver() {
        super(List.of(new HeadTailFunDef(functionMetaData)));
    }
}
