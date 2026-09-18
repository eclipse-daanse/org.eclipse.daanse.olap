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
package org.eclipse.daanse.olap.function.def.logical;

import java.util.List;

import org.eclipse.daanse.mdx.model.api.expression.operation.InfixOperationAtom;
import org.eclipse.daanse.mdx.model.api.expression.operation.OperationAtom;
import org.eclipse.daanse.olap.api.DataType;
import org.eclipse.daanse.olap.api.function.FunctionMetaData;
import org.eclipse.daanse.olap.api.function.FunctionResolver;
import org.eclipse.daanse.olap.function.core.FunctionMetaDataR;
import org.eclipse.daanse.olap.function.core.FunctionParameterR;
import org.eclipse.daanse.olap.function.core.resolver.AbstractFunctionDefinitionMultiResolver;
import org.osgi.service.component.annotations.Component;

@Component(service = FunctionResolver.class)
public class IsResolver extends AbstractFunctionDefinitionMultiResolver {

    private static String DESCRIPTION = "Returns whether two objects are the same";
    private static OperationAtom atom = new InfixOperationAtom("IS");
    //{"ibmm", "ibll", "ibhh", "ibdd", "ibtt"}

    private static FunctionMetaData functionMetaDataWithMember = new FunctionMetaDataR(atom, DESCRIPTION,
            DataType.LOGICAL, new FunctionParameterR[] { FunctionParameterR.param(DataType.MEMBER, "Member1").describedAs("Member 1"), FunctionParameterR.param(DataType.MEMBER, "Member2").describedAs("Member 2")})
            .withTextKey("Is.Member.Member").caption("IS Operation with Member and Member");

    private static FunctionMetaData functionMetaDataWithLevel = new FunctionMetaDataR(atom, DESCRIPTION,
            DataType.LOGICAL, new FunctionParameterR[] { FunctionParameterR.param(DataType.LEVEL, "Level1").describedAs("Level 1"), FunctionParameterR.param(DataType.LEVEL, "Level2").describedAs("Level 2") })
            .withTextKey("Is.Level.Level").caption("IS Operation");

    private static FunctionMetaData functionMetaDataWithHierrchy = new FunctionMetaDataR(atom, DESCRIPTION,
            DataType.LOGICAL, new FunctionParameterR[] { FunctionParameterR.param(DataType.HIERARCHY, "Hierarchy1").describedAs("Hierarchy 1"), FunctionParameterR.param(DataType.HIERARCHY, "Hierarchy2").describedAs("Hierarchy 2") })
            .withTextKey("Is.Hierarchy.Hierarchy").caption("IS Operation");

    private static FunctionMetaData functionMetaDataWithDimension = new FunctionMetaDataR(atom, DESCRIPTION,
            DataType.LOGICAL, new FunctionParameterR[] { FunctionParameterR.param(DataType.DIMENSION, "Dimension1").describedAs("Dimension 1"), FunctionParameterR.param(DataType.DIMENSION, "Dimension2").describedAs("Dimension 2") })
            .withTextKey("Is.Dimension.Dimension").caption("IS Operation");

    private static FunctionMetaData functionMetaDataWithTuple = new FunctionMetaDataR(atom, DESCRIPTION,
            DataType.LOGICAL, new FunctionParameterR[] { FunctionParameterR.param(DataType.TUPLE, "Tuple1").describedAs("Tuple 1"), FunctionParameterR.param(DataType.TUPLE, "Tuple2").describedAs("Tuple 2") })
            .withTextKey("Is.Tuple.Tuple").caption("IS Operation");

    public IsResolver() {
        super(List.of(new IsFunDef(functionMetaDataWithMember), new IsFunDef(functionMetaDataWithLevel),
                new IsFunDef(functionMetaDataWithHierrchy), new IsFunDef(functionMetaDataWithDimension), new IsFunDef(functionMetaDataWithTuple)));
    }

}
