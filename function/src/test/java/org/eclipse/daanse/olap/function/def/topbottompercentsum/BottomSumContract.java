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
package org.eclipse.daanse.olap.function.def.topbottompercentsum;

import org.eclipse.daanse.olap.testkit.function.FunctionContract.Promise;
import org.eclipse.daanse.olap.testkit.function.FunctionContract;
import static org.eclipse.daanse.olap.api.DataType.INTEGER;
import static org.eclipse.daanse.olap.api.DataType.MEMBER;
import static org.eclipse.daanse.olap.api.DataType.NUMERIC;
import static org.eclipse.daanse.olap.api.DataType.SET;

import org.eclipse.daanse.olap.api.calc.ResultStyle;

/** The contract of the MDX function {@code BottomSum}. */
public final class BottomSumContract {

    private BottomSumContract() {
    }

    public static final FunctionContract CONTRACT = FunctionContract.of("BottomSum")
            .signatures("<Set> BottomSum(<Set>, <Numeric Expression>, <Numeric Expression>)")
            .returns(SET)
            .arity(3, 3)                          // unlike BottomCount, the value expression is required

            .resolvesTo(TopBottomPercentSumFunDef.class, SET, NUMERIC, NUMERIC)
            .resolvesTo(TopBottomPercentSumFunDef.class, SET, INTEGER, NUMERIC)
            .resolvesWithCost(2, TopBottomPercentSumFunDef.class, MEMBER, NUMERIC, NUMERIC)   // Member -> Set
            .rejects(NUMERIC, SET, NUMERIC)
            .rejects(SET, NUMERIC)                 // the value expression is required, not optional
            .rejects(SET, NUMERIC, NUMERIC, NUMERIC)   // arity 4
            .rejects()

            .autoEdgeCases()
            .edgeCaseMdx("empty set",              "BottomSum({}, 50, [Measures].[Amount])")
            .edgeCaseMdx("target zero",             "BottomSum([Geo].Members, 0, [Measures].[Amount])")
            .edgeCaseMdx("target -1",               "BottomSum([Geo].Members, -1, [Measures].[Amount])")
            .edgeCaseMdx("target negative",         "BottomSum([Geo].Members, -10, [Measures].[Amount])")
            .edgeCaseMdx("target fractional",       "BottomSum([Geo].Members, 0.5, [Measures].[Amount])")
            .edgeCaseMdx("target beyond total",     "BottomSum([Geo].Members, 1000000, [Measures].[Amount])")
            .edgeCaseMdx("target 1E308",            "BottomSum([Geo].Members, 1E308, [Measures].[Amount])")
            .edgeCaseMdx("target NULL",             "BottomSum([Geo].Members, NULL, [Measures].[Amount])")
            .edgeCaseMdx("value expression zero",       "BottomSum([Geo].Members, 50, 0)")
            .edgeCaseMdx("value expression negative",   "BottomSum([Geo].Members, 50, -1)")
            .edgeCaseMdx("value expression fractional", "BottomSum([Geo].Members, 50, 0.5)")
            .edgeCaseMdx("value expression 1E308",      "BottomSum([Geo].Members, 50, 1E308)")
            .edgeCaseMdx("value expression NULL",   "BottomSum([Geo].Members, 50, NULL)")

            // TopBottomPercentSumCalc starts runningTotal at 0 and stops as soon as
            // runningTotal >= target, checked *before* the first member is added: a target of
            // 0 (or a NULL target, treated as 0) or below therefore yields the empty set before
            // any member is examined, regardless of the cube's data.
            .value("Count(BottomSum([Geo].Members, 0, [Measures].[Amount]))",    "0")
            .value("Count(BottomSum([Geo].Members, -1, [Measures].[Amount]))",   "0")
            .value("Count(BottomSum([Geo].Members, -10, [Measures].[Amount]))",  "0")
            .value("Count(BottomSum([Geo].Members, NULL, [Measures].[Amount]))", "0")
            .value("Count(BottomSum({}, 50, [Measures].[Amount]))",                 "0")
            // SumContract established Sum({[Geo].[All Geo].[North], [Geo].[All Geo].[South]}, [Measures].[Amount])
            // = 266,773 — a target well beyond that total is unreachable, so the loop runs to
            // completion and keeps every member.
            .value("Count(BottomSum([Geo].Members, 1000000, [Measures].[Amount]))", "8")

            .doesNotDependOn("BottomSum([Geo].Members, 50, [Measures].[Amount])",
                       "[Geo].[Region]", "[Measures]")
            .dependsOn("BottomSum([Geo].Members, 50, 1)")

            .resultStyle("BottomSum([Geo].Members, 50, [Measures].[Amount])",
                         ResultStyle.MUTABLE_LIST, ResultStyle.MUTABLE_LIST)
            .resultStyle("BottomSum([Geo].Members, 50, [Measures].[Amount])",
                         ResultStyle.ITERABLE, ResultStyle.MUTABLE_LIST)
            .independentMutableList("BottomSum([Geo].Members, 50, [Measures].[Amount])")
            .build();
}
