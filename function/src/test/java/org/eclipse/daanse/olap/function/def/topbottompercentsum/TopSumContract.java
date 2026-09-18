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
package org.eclipse.daanse.olap.function.def.topbottompercentsum;

import org.eclipse.daanse.olap.testkit.function.FunctionContract.Promise;
import org.eclipse.daanse.olap.testkit.function.FunctionContract;
import static org.eclipse.daanse.olap.api.DataType.INTEGER;
import static org.eclipse.daanse.olap.api.DataType.MEMBER;
import static org.eclipse.daanse.olap.api.DataType.NUMERIC;
import static org.eclipse.daanse.olap.api.DataType.SET;

import org.eclipse.daanse.olap.api.calc.ResultStyle;

/**
 * The contract of the MDX function {@code TopSum} — the mirror image of {@link
 * BottomSumContract}, sharing {@code TopBottomPercentSumFunDef}/{@code
 * TopBottomPercentSumCalc} (constructed with {@code top=true, percent=false}).
 *
 * <p>The target/running-total stopping logic ({@code runningTotal >= target} checked before
 * each member is added, a {@code NULL} target treated as {@code 0}) does not depend on the
 * {@code top} flag at all — only which end of the sorted list {@code Sorter.sortTuples} sorts
 * toward does — so every count-based finding {@link BottomSumContract} establishes (target ≤
 * 0 or {@code NULL} ⇒ empty; {@code SumContract}'s established {@code 266,773} total for
 * {@code [Measures].[Amount]} over {@code [Geo].Members} means a target well beyond
 * that is unreachable and keeps every member) applies identically here.
 */
public final class TopSumContract {

    private TopSumContract() {
    }

    public static final FunctionContract CONTRACT = FunctionContract.of("TopSum")
            .signatures("<Set> TopSum(<Set>, <Numeric Expression>, <Numeric Expression>)")
            .returns(SET)
            .arity(3, 3)                          // unlike TopCount, the value expression is required

            .resolvesTo(TopBottomPercentSumFunDef.class, SET, NUMERIC, NUMERIC)
            .resolvesTo(TopBottomPercentSumFunDef.class, SET, INTEGER, NUMERIC)
            .resolvesWithCost(2, TopBottomPercentSumFunDef.class, MEMBER, NUMERIC, NUMERIC)   // Member -> Set
            .rejects(NUMERIC, SET, NUMERIC)
            .rejects(SET, NUMERIC)                 // the value expression is required, not optional
            .rejects(SET, NUMERIC, NUMERIC, NUMERIC)   // arity 4
            .rejects()

            .autoEdgeCases()
            .edgeCaseMdx("empty set",              "TopSum({}, 50, [Measures].[Amount])")
            .edgeCaseMdx("target zero",             "TopSum([Geo].Members, 0, [Measures].[Amount])")
            .edgeCaseMdx("target -1",               "TopSum([Geo].Members, -1, [Measures].[Amount])")
            .edgeCaseMdx("target negative",         "TopSum([Geo].Members, -10, [Measures].[Amount])")
            .edgeCaseMdx("target fractional",       "TopSum([Geo].Members, 0.5, [Measures].[Amount])")
            .edgeCaseMdx("target beyond total",     "TopSum([Geo].Members, 1000000, [Measures].[Amount])")
            .edgeCaseMdx("target 1E308",            "TopSum([Geo].Members, 1E308, [Measures].[Amount])")
            .edgeCaseMdx("target NULL",             "TopSum([Geo].Members, NULL, [Measures].[Amount])")
            .edgeCaseMdx("value expression zero",       "TopSum([Geo].Members, 50, 0)")
            .edgeCaseMdx("value expression negative",   "TopSum([Geo].Members, 50, -1)")
            .edgeCaseMdx("value expression fractional", "TopSum([Geo].Members, 50, 0.5)")
            .edgeCaseMdx("value expression 1E308",      "TopSum([Geo].Members, 50, 1E308)")
            .edgeCaseMdx("value expression NULL",   "TopSum([Geo].Members, 50, NULL)")

            .value("Count(TopSum([Geo].Members, 0, [Measures].[Amount]))",    "0")
            .value("Count(TopSum([Geo].Members, -1, [Measures].[Amount]))",   "0")
            .value("Count(TopSum([Geo].Members, -10, [Measures].[Amount]))",  "0")
            .value("Count(TopSum([Geo].Members, NULL, [Measures].[Amount]))", "0")
            .value("Count(TopSum({}, 50, [Measures].[Amount]))",                 "0")
            .value("Count(TopSum([Geo].Members, 1000000, [Measures].[Amount]))", "8")

            .doesNotDependOn("TopSum([Geo].Members, 50, [Measures].[Amount])",
                       "[Geo].[Region]", "[Measures]")
            .dependsOn("TopSum([Geo].Members, 50, 1)")

            .resultStyle("TopSum([Geo].Members, 50, [Measures].[Amount])",
                         ResultStyle.MUTABLE_LIST, ResultStyle.MUTABLE_LIST)
            .resultStyle("TopSum([Geo].Members, 50, [Measures].[Amount])",
                         ResultStyle.ITERABLE, ResultStyle.MUTABLE_LIST)
            .independentMutableList("TopSum([Geo].Members, 50, [Measures].[Amount])")
            .build();
}
