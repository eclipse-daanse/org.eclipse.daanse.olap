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
package org.eclipse.daanse.olap.function.def.headtail;

import org.eclipse.daanse.olap.testkit.function.FunctionContract.Promise;
import org.eclipse.daanse.olap.testkit.function.FunctionContract;
import static org.eclipse.daanse.olap.api.DataType.INTEGER;
import static org.eclipse.daanse.olap.api.DataType.MEMBER;
import static org.eclipse.daanse.olap.api.DataType.NUMERIC;
import static org.eclipse.daanse.olap.api.DataType.SET;

import org.eclipse.daanse.olap.api.calc.ResultStyle;

/**
 * The contract of the MDX function {@code Tail} — {@link HeadContract}'s mirror image, sharing
 * the same {@code HeadTailFunDef} (which switches on {@code operationAtom().name()} to pick
 * "first N" vs "last N"). Unlike {@link BottomCountContract}'s {@code TopBottomCountCalc},
 * {@code TailCalc.tail(Integer count, ...)} checks {@code count == null || count <= 0} before
 * ever unboxing or using {@code count} arithmetically, so every INTEGER boundary here —
 * including a negative count and {@code Integer.MIN_VALUE} — is safe.
 */
public final class TailContract {

    private TailContract() {
    }

    public static final FunctionContract CONTRACT = FunctionContract.of("Tail")
            .signatures("<Set> Tail(<Set>, <Numeric Expression>)")
            .returns(SET)
            .arity(1, 2)                          // the second parameter is .asOptional()

            .resolvesTo(HeadTailFunDef.class, SET)
            .resolvesTo(HeadTailFunDef.class, SET, NUMERIC)
            .resolvesTo(HeadTailFunDef.class, SET, INTEGER)
            .resolvesWithCost(2, HeadTailFunDef.class, MEMBER, NUMERIC)   // Member -> Set
            .rejects(NUMERIC, SET)
            .rejects(SET, NUMERIC, NUMERIC)
            .rejects()

            .autoEdgeCases()
            .edgeCaseMdx("empty set",           "Tail({}, 2)")
            .edgeCaseMdx("empty set, no count", "Tail({})")
            .edgeCaseMdx("count zero",          "Tail([Geo].Members, 0)")
            .edgeCaseMdx("count one",           "Tail([Geo].Members, 1)")
            .edgeCaseMdx("count negative",      "Tail([Geo].Members, -1)")
            .edgeCaseMdx("count beyond end",    "Tail([Geo].Members, 1000)")
            .edgeCaseMdx("count MAX_VALUE",     "Tail([Geo].Members, 2147483647)")
            .edgeCaseMdx("count MIN_VALUE",     "Tail([Geo].Members, -2147483648)")
            .edgeCaseMdx("count NULL",          "Tail([Geo].Members, NULL)")

            // Tail keeps the set's natural order and takes from the end — the mirror image of
            // Head, whose order is already pinned down by HeadContract.
            .value("SetToStr(Tail([Geo].Members, 1))", "{[Geo].[All Geo].[South].[E]}")
            .value("SetToStr(Tail([Geo].Members))",    "{[Geo].[All Geo].[South].[E]}")   // default count is 1
            .value("Count(Tail([Geo].Members, 0))",    "0")
            .value("Count(Tail({}, 5))",                  "0")
            .value("Count(Tail([Geo].Members, 1000))", "8")
            .value("Count(Tail([Geo].Members, NULL))", "0")

            .dependsOn("Tail([Geo].Members, 2)")
            .doesNotDependOn("Tail([Geo].Members, [Measures].[Amount])", "[Measures]")

            .resultStyle("Tail([Geo].Members, 2)", ResultStyle.MUTABLE_LIST, ResultStyle.MUTABLE_LIST)
            .resultStyle("Tail([Geo].Members, 2)", ResultStyle.ITERABLE, ResultStyle.MUTABLE_LIST)
            .independentMutableList("Tail([Geo].Members, 2)")
            .independentMutableList("Tail(Order([Geo].Members, [Measures].[Amount]), 1)")
            .build();
}
