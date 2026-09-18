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
package org.eclipse.daanse.olap.function.def.drilldownlevel;

import org.eclipse.daanse.olap.testkit.function.FunctionContract.Promise;
import org.eclipse.daanse.olap.testkit.function.FunctionContract;
import static org.eclipse.daanse.olap.api.DataType.DIMENSION;
import static org.eclipse.daanse.olap.api.DataType.EMPTY;
import static org.eclipse.daanse.olap.api.DataType.LEVEL;
import static org.eclipse.daanse.olap.api.DataType.MEMBER;
import static org.eclipse.daanse.olap.api.DataType.NUMERIC;
import static org.eclipse.daanse.olap.api.DataType.SET;
import static org.eclipse.daanse.olap.api.DataType.STRING;
import static org.eclipse.daanse.olap.api.DataType.SYMBOL;

import org.eclipse.daanse.olap.api.calc.ResultStyle;

/**
 * The contract of the MDX function {@code DrilldownLevel}. Five overloads share the atom:
 * {@code (Set)}, {@code (Set, Level)}, and three that use an {@code <Empty>}-typed parameter
 * to mark an elided position — {@code (Set, , Index)}, {@code (Set, , Index, Symbol)} and
 * {@code (Set, , , Symbol)} — the classic "double comma" MDX syntax for skipping the Level
 * argument while still supplying an Index and/or the {@code INCLUDE_CALC_MEMBERS} flag. An
 * {@code <Empty>}-typed argument only ever matches an {@code <Empty>} parameter: unlike every
 * other category, {@code TypeUtil.canConvert} never coerces it to anything else.
 */
public final class DrilldownLevelContract {

    private DrilldownLevelContract() {
    }

    public static final FunctionContract CONTRACT = FunctionContract.of("DrilldownLevel")
            .signatures(
                    "<Set> DrilldownLevel(<Set>)",
                    "<Set> DrilldownLevel(<Set>, <Level>)",
                    "<Set> DrilldownLevel(<Set>, <Empty>, <Numeric Expression>)",
                    "<Set> DrilldownLevel(<Set>, <Empty>, <Numeric Expression>, <Symbol>)",
                    "<Set> DrilldownLevel(<Set>, <Empty>, <Empty>, <Symbol>)")
            .returns(SET)
            .arity(1, 4)
            .reservedWords("INCLUDE_CALC_MEMBERS")

            .resolvesTo(DrilldownLevelFunDef.class, SET)
            .resolvesTo(DrilldownLevelFunDef.class, SET, LEVEL)
            .resolvesTo(DrilldownLevelFunDef.class, SET, EMPTY, NUMERIC)
            .resolvesTo(DrilldownLevelFunDef.class, SET, EMPTY, NUMERIC, SYMBOL)
            .resolvesTo(DrilldownLevelFunDef.class, SET, EMPTY, EMPTY, SYMBOL)
            .resolvesWithCost(2, DrilldownLevelFunDef.class, MEMBER)          // Member -> Set
            .resolvesWithCost(1, DrilldownLevelFunDef.class, LEVEL)           // Level -> Set
            .resolvesWithCost(3, DrilldownLevelFunDef.class, SET, DIMENSION)  // Dimension -> Level
            .rejects(NUMERIC)              // arity 1, not Set-convertible
            .rejects(SET, STRING)          // arity 2, String does not convert to Level
            .rejects(SET, NUMERIC)         // an Index needs the elided-Level ", ," form
            .rejects(SET, LEVEL, NUMERIC)  // a real Level can't be combined with an Index
            .rejects()                     // arity 0
            .rejects(SET, EMPTY, NUMERIC, NUMERIC, SYMBOL)   // arity 5

            .autoEdgeCases()
            .edgeCaseMdx("no level or index",                     "DrilldownLevel([Geo].Members)")
            .edgeCaseMdx("with level",                            "DrilldownLevel([Geo].Members, [Geo].[All Geo].[North].Level)")
            .edgeCaseMdx("with index",                            "DrilldownLevel([Geo].Members, , 0)")
            .edgeCaseMdx("index one",                              "DrilldownLevel([Geo].Members, , 1)")
            .edgeCaseMdx("negative index",                        "DrilldownLevel([Geo].Members, , -1)")
            .edgeCaseMdx("index MAX_VALUE",                        "DrilldownLevel([Geo].Members, , 2147483647)")
            .edgeCaseMdx("index MIN_VALUE",                        "DrilldownLevel([Geo].Members, , -2147483648)")
            // A null Index is treated as out of range (set returned unchanged) —
            // DrilldownLevelWithIndexCalc checks "index == null || index < 0 || index >= arity"
            // before ever unboxing index.
            .edgeCaseMdx("index NULL",                             "DrilldownLevel([Geo].Members, , NULL)")
            .edgeCaseMdx("with index and INCLUDE_CALC_MEMBERS",   "DrilldownLevel([Geo].Members, , 0, INCLUDE_CALC_MEMBERS)")
            .edgeCaseMdx("INCLUDE_CALC_MEMBERS only",              "DrilldownLevel([Geo].Members, , , INCLUDE_CALC_MEMBERS)")
            // "RECURSIVE" is not DrilldownLevel's own reserved word, but reserved-word
            // recognition is global (see DescendantsContract's matching case): DrilldownMember
            // registers it, so it still parses as a SYMBOL literal here. Unlike Descendants/
            // DrilldownMember, this neither crashes nor is validated: DrilldownLevelFunDef
            // checks "INCLUDE_CALC_MEMBERS.equals(literal.getValue())" directly (no
            // FunUtil.getLiteralArg call), so any other symbol just silently means
            // includeCalcMembers = false — a wrong flag is misread, not rejected.
            .edgeCaseMdx("symbol reserved by another function",
                         "DrilldownLevel([Geo].Members, , 0, RECURSIVE)")
            .edgeCaseMdx("empty set",                              "DrilldownLevel({})")

            // [Geo] has one real level beneath (All); its members are leaves, so drilling
            // down without a Level argument (which searches the deepest depth already present)
            // finds no children to add and returns the set unchanged.
            .value("Count(DrilldownLevel([Geo].Members))", "8")
            .value("SetToStr(DrilldownLevel([Geo].Members))", "{[Geo].[All Geo], [Geo].[All Geo].[North], "
                            + "[Geo].[All Geo].[North].[A], [Geo].[All Geo].[North].[B], "
                            + "[Geo].[All Geo].[South], [Geo].[All Geo].[South].[C], "
                            + "[Geo].[All Geo].[South].[D], [Geo].[All Geo].[South].[E]}")
            .value("Count(DrilldownLevel({}))", "0")
            .value("Count(DrilldownLevel([Geo].Members, , NULL))", "8")

            .dependsOn("DrilldownLevel([Geo].Members)")
            .dependsOn("DrilldownLevel([Geo].Members, [Geo].[All Geo].[North].Level)")

            .resultStyle("DrilldownLevel([Geo].Members)", ResultStyle.MUTABLE_LIST, ResultStyle.MUTABLE_LIST)
            .resultStyle("DrilldownLevel([Geo].Members)", ResultStyle.ITERABLE, ResultStyle.MUTABLE_LIST)
            .independentMutableList("DrilldownLevel([Geo].Members)")

            .build();
}
