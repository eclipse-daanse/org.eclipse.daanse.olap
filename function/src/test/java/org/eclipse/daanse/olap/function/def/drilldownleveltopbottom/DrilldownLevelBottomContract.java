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
 * The contract of the MDX function {@code DrilldownLevelBottom}. Four overloads share the
 * atom: {@code (Set, Count)}, {@code (Set, Count, Level)}, {@code (Set, Count, Level, Order)}
 * and — using the "double comma" elided-Level form, like {@link DrilldownLevelContract} —
 * {@code (Set, Count, <Empty>, Order)}.
 *
 * <p>{@code DrilldownLevelTopBottomCalc.dependsOn(Hierarchy)} is overridden to
 * {@code HierarchyDependsChecker.checkAnyDependsButFirst(getChildCalcs(), hierarchy)}: the
 * Set argument's own hierarchy is deliberately excluded from the reported dependencies (only
 * the Count and Order calcs count) — the function iterates the set argument internally and
 * does not need re-evaluating for a different current member of its hierarchy. When no Order
 * argument is given, {@code DrilldownLevelTopBottomFunDef.compileCall} defaults it to a
 * {@code CurrentValueUnknownCalc}, whose {@code dependsOn} unconditionally answers {@code
 * true} (it stands for "the current cell value", which genuinely varies with every
 * hierarchy) — so even the plain {@code (Set, Count)} overload ends up depending on every
 * hierarchy except the Set argument's own (verified against a real connection).
 */
public final class DrilldownLevelBottomContract {

    private DrilldownLevelBottomContract() {
    }

    public static final FunctionContract CONTRACT = FunctionContract.of("DrilldownLevelBottom")
            .signatures(
                    "<Set> DrilldownLevelBottom(<Set>, <Numeric Expression>)",
                    "<Set> DrilldownLevelBottom(<Set>, <Numeric Expression>, <Level>)",
                    "<Set> DrilldownLevelBottom(<Set>, <Numeric Expression>, <Level>, <Numeric Expression>)",
                    "<Set> DrilldownLevelBottom(<Set>, <Numeric Expression>, <Empty>, <Numeric Expression>)")
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
            .edgeCaseMdx("count zero",                 "DrilldownLevelBottom([Geo].Members, 0)")
            .edgeCaseMdx("count one",                   "DrilldownLevelBottom([Geo].Members, 1)")
            .edgeCaseMdx("count negative",              "DrilldownLevelBottom([Geo].Members, -1)")
            .edgeCaseMdx("count MAX_VALUE",             "DrilldownLevelBottom([Geo].Members, 2147483647)")
            .edgeCaseMdx("count MIN_VALUE",             "DrilldownLevelBottom([Geo].Members, -2147483648)")
            .edgeCaseMdx("count NULL",                  "DrilldownLevelBottom([Geo].Members, NULL)")
            .edgeCaseMdx("with level",                  "DrilldownLevelBottom([Geo].Members, 5, [Geo].[All Geo].[North].Level)")
            .edgeCaseMdx("with level and order",        "DrilldownLevelBottom([Geo].Members, 5, [Geo].[All Geo].[North].Level, [Measures].[Amount])")
            .edgeCaseMdx("elided level with order",     "DrilldownLevelBottom([Geo].Members, 5, , [Measures].[Amount])")
            .edgeCaseMdx("empty set",                   "DrilldownLevelBottom({}, 5)")

            // [Geo] has one real level beneath (All); its members are leaves, so there are
            // no children to drill down to and the set comes back unchanged either way.
            .value("Count(DrilldownLevelBottom([Geo].Members, 5))", "15")
            .value("SetToStr(DrilldownLevelBottom([Geo].Members, 5))",
                    "{[Geo].[All Geo], [Geo].[All Geo].[North], [Geo].[All Geo].[South], "
                            + "[Geo].[All Geo].[North], [Geo].[All Geo].[North].[A], "
                            + "[Geo].[All Geo].[North].[B], [Geo].[All Geo].[North].[A], "
                            + "[Geo].[All Geo].[North].[B], [Geo].[All Geo].[South], "
                            + "[Geo].[All Geo].[South].[C], [Geo].[All Geo].[South].[D], "
                            + "[Geo].[All Geo].[South].[E], [Geo].[All Geo].[South].[C], "
                            + "[Geo].[All Geo].[South].[D], [Geo].[All Geo].[South].[E]}")
            .value("Count(DrilldownLevelBottom([Geo].Members, 0))", "8")   // n <= 0 short-circuits
            .value("Count(DrilldownLevelBottom({}, 5))", "0")

            // No order expression: DrilldownLevelTopBottomFunDef defaults it to
            // CurrentValueUnknownCalc, whose dependsOn always reports true — so the call
            // depends on every hierarchy except the Set argument's own (Gender), which
            // checkAnyDependsButFirst always excludes.
            .doesNotDependOn("DrilldownLevelBottom([Geo].Members, 5)", "[Geo].[Region]")
            .doesNotDependOn("DrilldownLevelBottom([Geo].Members, 5, , [Measures].[Amount])",
                       "[Measures]")

            .resultStyle("DrilldownLevelBottom([Geo].Members, 5)", ResultStyle.MUTABLE_LIST, ResultStyle.MUTABLE_LIST)
            .resultStyle("DrilldownLevelBottom([Geo].Members, 5)", ResultStyle.ITERABLE, ResultStyle.MUTABLE_LIST)
            .independentMutableList("DrilldownLevelBottom([Geo].Members, 5)")

            .build();
}
