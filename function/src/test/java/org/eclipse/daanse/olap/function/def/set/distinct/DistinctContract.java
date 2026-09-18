/*
 * Copyright (c) 2026 Contributors to the Eclipse Foundation.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   SmartCity Jena - initial
 */
package org.eclipse.daanse.olap.function.def.set.distinct;

import org.eclipse.daanse.olap.testkit.function.FunctionContract.Promise;
import org.eclipse.daanse.olap.testkit.function.FunctionContract;
import static org.eclipse.daanse.olap.api.DataType.DIMENSION;
import static org.eclipse.daanse.olap.api.DataType.HIERARCHY;
import static org.eclipse.daanse.olap.api.DataType.LEVEL;
import static org.eclipse.daanse.olap.api.DataType.MEMBER;
import static org.eclipse.daanse.olap.api.DataType.NUMERIC;
import static org.eclipse.daanse.olap.api.DataType.SET;
import static org.eclipse.daanse.olap.api.DataType.TUPLE;

import org.eclipse.daanse.olap.api.calc.ResultStyle;

/** The contract of the MDX function {@code Distinct}. */
public final class DistinctContract {

    private DistinctContract() {
    }

    public static final FunctionContract CONTRACT = FunctionContract.of("Distinct")
            .signatures("<Set> Distinct(<Set>)")
            .returns(SET)
            .arity(1, 1)

            .resolvesTo(DistinctFunDef.class, SET)
            .resolvesWithCost(2, DistinctFunDef.class, MEMBER)   // Member -> Set
            .resolvesWithCost(2, DistinctFunDef.class, TUPLE)    // Tuple -> Set
            .resolvesWithCost(1, DistinctFunDef.class, LEVEL)    // Level -> Set
            .rejects(DIMENSION)   // Dimension converts to Member/Hierarchy/Level, never Set
            .rejects(HIERARCHY)   // Hierarchy converts to Member/Dimension/Tuple, never Set
            .rejects(NUMERIC)
            .rejects()             // arity 0
            .rejects(SET, SET)     // arity 2

            .autoEdgeCases()
            .edgeCaseMdx("empty set",              "Distinct({})")
            .edgeCaseMdx("already distinct",        "Distinct([Geo].Members)")
            .edgeCaseMdx("set with duplicates",     "Distinct({[Geo].[All Geo].[North], [Geo].[All Geo].[North], [Geo].[All Geo].[South]})")
            .edgeCaseMdx("member operand (lenient)", "Distinct([Geo].[All Geo].[North])")

            .value("Count(Distinct({[Geo].[All Geo].[North], [Geo].[All Geo].[North], [Geo].[All Geo].[South]}))", "2")
            .value("SetToStr(Distinct({[Geo].[All Geo].[North], [Geo].[All Geo].[North], [Geo].[All Geo].[South]}))",
                   "{[Geo].[All Geo].[North], [Geo].[All Geo].[South]}")
            .value("Count(Distinct({}))", "0")

            .dependsOn("Distinct([Geo].Members)")

            .resultStyle("Distinct([Geo].Members)", ResultStyle.MUTABLE_LIST, ResultStyle.MUTABLE_LIST)
            .resultStyle("Distinct([Geo].Members)", ResultStyle.ITERABLE, ResultStyle.MUTABLE_LIST)
            .independentMutableList("Distinct([Geo].Members)")

            .build();
}
