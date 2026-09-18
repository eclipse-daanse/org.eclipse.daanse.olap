/*
 * Copyright (c) 2026 Contributors to the Eclipse Foundation.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Stefan Bischof (bipolis.org) - initial
 */
package org.eclipse.daanse.olap.function.def.set.ascendants;

import org.eclipse.daanse.olap.testkit.function.FunctionContract.Promise;
import org.eclipse.daanse.olap.testkit.function.FunctionContract;
import static org.eclipse.daanse.olap.api.DataType.DIMENSION;
import static org.eclipse.daanse.olap.api.DataType.HIERARCHY;
import static org.eclipse.daanse.olap.api.DataType.LEVEL;
import static org.eclipse.daanse.olap.api.DataType.MEMBER;
import static org.eclipse.daanse.olap.api.DataType.NUMERIC;
import static org.eclipse.daanse.olap.api.DataType.SET;
import static org.eclipse.daanse.olap.api.DataType.STRING;
import static org.eclipse.daanse.olap.api.DataType.TUPLE;

import org.eclipse.daanse.olap.api.calc.ResultStyle;

/** The contract of the MDX function {@code Ascendants}. */
public final class AscendantsContract {

    private AscendantsContract() {
    }

    public static final FunctionContract CONTRACT = FunctionContract.of("Ascendants")
            .signatures("<Set> Ascendants(<Member>)")
            .returns(SET)
            .arity(1, 1)

            .resolvesTo(AscendantsFunDef.class, MEMBER)
            .resolvesWithCost(1, AscendantsFunDef.class, HIERARCHY)   // Hierarchy -> Member
            .resolvesWithCost(2, AscendantsFunDef.class, DIMENSION)   // Dimension -> Member
            .rejects(LEVEL)     // Level converts to Hierarchy/Set/Dimension, never Member
            .rejects(SET)
            .rejects(TUPLE)
            .rejects(NUMERIC)
            .rejects(STRING)
            .rejects()                     // arity 0
            .rejects(MEMBER, MEMBER)       // arity 2

            .autoEdgeCases()
            .edgeCaseMdx("leaf member",           "Ascendants([Geo].[All Geo].[North])")
            .edgeCaseMdx("top-of-hierarchy member", "Ascendants([Geo].[All Geo].[North].Parent)")
            .edgeCaseMdx("null member",            "Ascendants([Geo].[All Geo].[North].Parent.Parent)")

            // The member itself always comes first.
            .valueKnownDefect("Ascendants([Geo].[All Geo].[North]).Item(0).Name", "F",
                            "a method call that takes an argument does not resolve: the"
                            + " argument is lost and the receiver is typed as a numeric"
                            + " expression. The no-argument forms work. The defect is in the MDX"
                            + " parser, org.eclipse.daanse.mdx, not here")
            // .Parent.Parent from a member of a shallow (one real level, +/- an All member)
            // hierarchy always bottoms out at the null member; AscendantsCalc returns {} for it
            // regardless of whether [Geo] happens to have an All level.
            .value("Count(Ascendants([Geo].[All Geo].[North].Parent.Parent))", "0")

            .dependsOn("Ascendants([Geo].[All Geo].[North])")
            .dependsOn("Ascendants([Geo].[All Geo].[North].Parent)")

            .resultStyle("Ascendants([Geo].[All Geo].[North])", ResultStyle.MUTABLE_LIST, ResultStyle.MUTABLE_LIST)
            .resultStyle("Ascendants([Geo].[All Geo].[North])", ResultStyle.ITERABLE, ResultStyle.MUTABLE_LIST)
            .independentMutableList("Ascendants([Geo].[All Geo].[North])")
            .build();
}
