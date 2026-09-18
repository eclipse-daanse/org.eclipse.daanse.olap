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
package org.eclipse.daanse.olap.function.def.drilldownleveltopbottom;

import org.eclipse.daanse.olap.testkit.function.FunctionContract.Promise;
import org.eclipse.daanse.olap.testkit.function.FunctionContract;
import static org.eclipse.daanse.olap.api.DataType.DIMENSION;
import static org.eclipse.daanse.olap.api.DataType.EMPTY;
import static org.eclipse.daanse.olap.api.DataType.LEVEL;
import static org.eclipse.daanse.olap.api.DataType.MEMBER;
import static org.eclipse.daanse.olap.api.DataType.NUMERIC;
import static org.eclipse.daanse.olap.api.DataType.SET;
import static org.eclipse.daanse.olap.api.DataType.STRING;

import org.eclipse.daanse.olap.api.calc.ResultStyle;

/**
 * The contract of the MDX function {@code DrilldownLevelTop} — the {@code top=true} sibling
 * of {@link DrilldownLevelBottomContract}, sharing the same {@code DrilldownLevelTopBottomFunDef}
 * and overload shapes under a different atom. See {@code DrilldownLevelBottomContract} for the
 * "double comma" elided-Level overload and the {@code checkAnyDependsButFirst} dependency
 * behavior, both identical here.
 */
public final class DrilldownLevelTopContract {

    private DrilldownLevelTopContract() {
    }

    public static final FunctionContract CONTRACT = FunctionContract.of("DrilldownLevelTop")
            .signatures(
                    "<Set> DrilldownLevelTop(<Set>, <Numeric Expression>)",
                    "<Set> DrilldownLevelTop(<Set>, <Numeric Expression>, <Level>)",
                    "<Set> DrilldownLevelTop(<Set>, <Numeric Expression>, <Level>, <Numeric Expression>)",
                    "<Set> DrilldownLevelTop(<Set>, <Numeric Expression>, <Empty>, <Numeric Expression>)")
            .returns(SET)
            .arity(2, 4)

            .resolvesTo(DrilldownLevelTopBottomFunDef.class, SET, NUMERIC)
            .resolvesTo(DrilldownLevelTopBottomFunDef.class, SET, NUMERIC, LEVEL)
            .resolvesTo(DrilldownLevelTopBottomFunDef.class, SET, NUMERIC, LEVEL, NUMERIC)
            .resolvesTo(DrilldownLevelTopBottomFunDef.class, SET, NUMERIC, EMPTY, NUMERIC)
            .resolvesWithCost(2, DrilldownLevelTopBottomFunDef.class, MEMBER, NUMERIC)         // Member -> Set
            .resolvesWithCost(1, DrilldownLevelTopBottomFunDef.class, LEVEL, NUMERIC)          // Level -> Set
            .resolvesWithCost(3, DrilldownLevelTopBottomFunDef.class, SET, NUMERIC, DIMENSION) // Dimension -> Level
            .rejects(SET)                          // arity 1, Count is required
            .rejects()                             // arity 0
            .rejects(SET, STRING)                  // String does not convert to Numeric
            .rejects(SET, NUMERIC, STRING)         // third arg must be a Level (or elided)
            .rejects(SET, NUMERIC, NUMERIC)        // an Order needs the elided-Level ", ," form
            .rejects(SET, NUMERIC, LEVEL, LEVEL)   // fourth arg must be an Order expression
            .rejects(SET, NUMERIC, LEVEL, NUMERIC, NUMERIC)   // arity 5

            .autoEdgeCases()
            .edgeCaseMdx("count zero",                 "DrilldownLevelTop([Geo].Members, 0)")
            .edgeCaseMdx("count one",                   "DrilldownLevelTop([Geo].Members, 1)")
            .edgeCaseMdx("count negative",              "DrilldownLevelTop([Geo].Members, -1)")
            .edgeCaseMdx("count MAX_VALUE",             "DrilldownLevelTop([Geo].Members, 2147483647)")
            .edgeCaseMdx("count MIN_VALUE",             "DrilldownLevelTop([Geo].Members, -2147483648)")
            .edgeCaseMdx("count NULL",                  "DrilldownLevelTop([Geo].Members, NULL)")
            .edgeCaseMdx("with level",                  "DrilldownLevelTop([Geo].Members, 5, [Geo].[All Geo].[North].Level)")
            .edgeCaseMdx("with level and order",        "DrilldownLevelTop([Geo].Members, 5, [Geo].[All Geo].[North].Level, [Measures].[Amount])")
            .edgeCaseMdx("elided level with order",     "DrilldownLevelTop([Geo].Members, 5, , [Measures].[Amount])")
            .edgeCaseMdx("empty set",                   "DrilldownLevelTop({}, 5)")

            // [Geo] has one real level beneath (All); its members are leaves, so there are
            // no children to drill down to and the set comes back unchanged either way.
            .value("Count(DrilldownLevelTop([Geo].Members, 5))", "15")
            .value("SetToStr(DrilldownLevelTop([Geo].Members, 5))",
                    "{[Geo].[All Geo], [Geo].[All Geo].[South], [Geo].[All Geo].[North], "
                            + "[Geo].[All Geo].[North], [Geo].[All Geo].[North].[B], "
                            + "[Geo].[All Geo].[North].[A], [Geo].[All Geo].[North].[A], "
                            + "[Geo].[All Geo].[North].[B], [Geo].[All Geo].[South], "
                            + "[Geo].[All Geo].[South].[E], [Geo].[All Geo].[South].[D], "
                            + "[Geo].[All Geo].[South].[C], [Geo].[All Geo].[South].[C], "
                            + "[Geo].[All Geo].[South].[D], [Geo].[All Geo].[South].[E]}")
            .value("Count(DrilldownLevelTop([Geo].Members, 0))", "8")   // n <= 0 short-circuits
            .value("Count(DrilldownLevelTop({}, 5))", "0")

            .doesNotDependOn("DrilldownLevelTop([Geo].Members, 5)", "[Geo].[Region]")
            .doesNotDependOn("DrilldownLevelTop([Geo].Members, 5, , [Measures].[Amount])",
                       "[Measures]")

            .resultStyle("DrilldownLevelTop([Geo].Members, 5)", ResultStyle.MUTABLE_LIST, ResultStyle.MUTABLE_LIST)
            .resultStyle("DrilldownLevelTop([Geo].Members, 5)", ResultStyle.ITERABLE, ResultStyle.MUTABLE_LIST)
            .independentMutableList("DrilldownLevelTop([Geo].Members, 5)")

            .build();
}
