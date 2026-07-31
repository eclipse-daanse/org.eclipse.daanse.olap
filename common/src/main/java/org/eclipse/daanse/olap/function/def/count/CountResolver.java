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
package org.eclipse.daanse.olap.function.def.count;

import java.util.List;
import java.util.Optional;

import org.eclipse.daanse.mdx.model.api.expression.operation.FunctionOperationAtom;
import org.eclipse.daanse.olap.api.DataType;
import org.eclipse.daanse.olap.api.function.FunctionInterface;
import org.eclipse.daanse.olap.api.function.FunctionMetaData;
import org.eclipse.daanse.olap.api.function.FunctionResolver;
import org.eclipse.daanse.olap.function.core.FunctionMetaDataR;
import org.eclipse.daanse.olap.function.core.FunctionParameterR;
import org.eclipse.daanse.olap.function.core.resolver.AbstractFunctionDefinitionMultiResolver;
import org.osgi.service.component.annotations.Component;

@Component(service = FunctionResolver.class)
public class CountResolver extends AbstractFunctionDefinitionMultiResolver {
    static final List<String> ReservedWords =  List.of( "INCLUDEEMPTY", "EXCLUDEEMPTY" );
    private static FunctionOperationAtom atom = new FunctionOperationAtom("Count");
    private static String DESCRIPTION = "Returns the number of tuples in a set, empty cells included unless the optional EXCLUDEEMPTY flag is used.";

    private static FunctionMetaData functionMetaData = new FunctionMetaDataR(atom, DESCRIPTION,
            DataType.NUMERIC, new FunctionParameterR[] { FunctionParameterR.param(DataType.SET),
                    new FunctionParameterR(DataType.SYMBOL, "Empty_Flag", Optional.of(ReservedWords))
                            .describedAs("INCLUDEEMPTY (default) counts empty cells, EXCLUDEEMPTY skips them.")
                            .asOptional() }).interfaceName(FunctionInterface.STATISTICAL);

    public CountResolver() {
        super(List.of(new CountFunDef(functionMetaData)));
    }

    @Override
    public List<String> getReservedWords() {
        return ReservedWords;
    }
}
