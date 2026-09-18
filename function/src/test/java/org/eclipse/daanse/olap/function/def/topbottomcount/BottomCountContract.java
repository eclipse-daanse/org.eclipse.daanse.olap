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
package org.eclipse.daanse.olap.function.def.topbottomcount;

import org.eclipse.daanse.olap.testkit.function.FunctionContract.Promise;
import org.eclipse.daanse.olap.testkit.function.FunctionContract;
import static org.eclipse.daanse.olap.api.DataType.INTEGER;
import static org.eclipse.daanse.olap.api.DataType.MEMBER;
import static org.eclipse.daanse.olap.api.DataType.NUMERIC;
import static org.eclipse.daanse.olap.api.DataType.SET;

import org.eclipse.daanse.olap.api.calc.ResultStyle;

/** The contract of the MDX function {@code BottomCount}. */
public final class BottomCountContract {

    private BottomCountContract() {
    }

    public static final FunctionContract CONTRACT = FunctionContract.of("BottomCount")
            .signatures("<Set> BottomCount(<Set>, <Numeric Expression>, <Numeric Expression>)")
            .returns(SET)
            .arity(2, 3)                          // the third parameter is .asOptional()

            .resolvesTo(TopBottomCountFunDef.class, SET, NUMERIC)
            .resolvesTo(TopBottomCountFunDef.class, SET, NUMERIC, NUMERIC)
            .resolvesTo(TopBottomCountFunDef.class, SET, INTEGER)
            .resolvesWithCost(2, TopBottomCountFunDef.class, MEMBER, NUMERIC)   // Member -> Set
            .rejects(NUMERIC, SET)
            .rejects(SET)                          // count is required, not optional
            .rejects(SET, NUMERIC, NUMERIC, NUMERIC)   // arity 4
            .rejects()

            .autoEdgeCases()
            .edgeCaseMdx("empty set",              "BottomCount({}, 2)")
            .edgeCaseMdx("count zero",              "BottomCount([Geo].Members, 0)")
            .edgeCaseMdx("count one",               "BottomCount([Geo].Members, 1)")
            // TopBottomCountCalc, unlike HeadCalc/TailCalc, does not clamp a negative count
            // before calling TupleList.subList: for BottomCount([Geo].Members, -1) it computes
            // list.subList(list.size() + 1, list.size()), a fromIndex past the list's size. That
            // is an IndexOutOfBoundsException, not a diagnosed OlapRuntimeException.
            .edgeCaseMdx("count negative",          "BottomCount([Geo].Members, -1)")
            .edgeCaseMdx("count beyond end",        "BottomCount([Geo].Members, 1000)")
            // Safe: "list instanceof AbstractList && list.size() <= n" is true here (2 <=
            // MAX_VALUE), so it takes the early-return branch and never reaches subList.
            .edgeCaseMdx("count MAX_VALUE",         "BottomCount([Geo].Members, 2147483647)")
            // Worse than plain "count negative": list.size() - n with n = Integer.MIN_VALUE
            // overflows int arithmetic (2 - (-2147483648) wraps around to a negative fromIndex)
            // before subList even gets a chance to reject it cleanly.
            .edgeCaseMdx("count MIN_VALUE",         "BottomCount([Geo].Members, -2147483648)")
            .edgeCaseMdx("count NULL",              "BottomCount([Geo].Members, NULL)")
            .edgeCaseMdx("with order expression",   "BottomCount([Geo].Members, 1, [Measures].[Amount])")

            // Without an order expression, BottomCount keeps the set's natural order and takes
            // the tail — the mirror image of Head/Tail, whose order is already pinned down by
            // HeadContract.
            .value("SetToStr(BottomCount([Geo].Members, 1))", "{[Geo].[All Geo].[South].[E]}")
            .value("Count(BottomCount([Geo].Members, 0))",    "0")
            .value("Count(BottomCount({}, 5))",                  "0")
            .value("Count(BottomCount([Geo].Members, 1000))", "8")
            .value("Count(BottomCount([Geo].Members, 1, [Measures].[Amount]))", "1")

            .dependsOn("BottomCount([Geo].Members, 2)")
            .doesNotDependOn("BottomCount([Geo].Members, 1, [Measures].[Amount])",
                       "[Geo].[Region]", "[Measures]")

            .resultStyle("BottomCount([Geo].Members, 2)", ResultStyle.MUTABLE_LIST, ResultStyle.MUTABLE_LIST)
            .resultStyle("BottomCount([Geo].Members, 2)", ResultStyle.ITERABLE, ResultStyle.MUTABLE_LIST)
            .independentMutableList("BottomCount([Geo].Members, 2)")
            .independentMutableList("BottomCount([Geo].Members, 1, [Measures].[Amount])")
            .build();
}
