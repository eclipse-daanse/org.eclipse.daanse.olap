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
package org.eclipse.daanse.olap.function.def.lastperiods;

import org.eclipse.daanse.olap.testkit.function.FunctionContract.Promise;
import org.eclipse.daanse.olap.testkit.function.FunctionContract;
import static org.eclipse.daanse.olap.api.DataType.DIMENSION;
import static org.eclipse.daanse.olap.api.DataType.HIERARCHY;
import static org.eclipse.daanse.olap.api.DataType.INTEGER;
import static org.eclipse.daanse.olap.api.DataType.MEMBER;
import static org.eclipse.daanse.olap.api.DataType.NUMERIC;
import static org.eclipse.daanse.olap.api.DataType.SET;

import org.eclipse.daanse.olap.api.calc.ResultStyle;

/**
 * The contract of the MDX function {@code LastPeriods}. One overload, {@code (Index,
 * Member?)}: with the Member argument omitted, {@code getResultType}/{@code compileCall} fall
 * back to {@code Cube.getTimeHierarchy(...).CurrentMember} — real cube metadata, so that path
 * is only exercised through the {@code Member}-supplied edge cases and values below; the
 * default-Time edge case is included purely for resolution/structural coverage.
 *
 * <p>{@code LastPeriodsCalc.lastPeriods} used to check {@code indexValue == 0} — auto-unboxing
 * a null {@code Integer} — before a null Index was guarded; a literal NULL Index now returns
 * the empty set like {@code indexValue == 0} does, matching {@link HeadContract}/{@link
 * TailContract}'s {@code count == null} guard.
 */
public final class LastPeriodsContract {

    private LastPeriodsContract() {
    }

    public static final FunctionContract CONTRACT = FunctionContract.of("LastPeriods")
            .signatures("<Set> LastPeriods(<Numeric Expression>, <Member>)")
            .returns(SET)
            .arity(1, 2)                          // the second parameter is .asOptional()

            .resolvesTo(LastPeriodsFunDef.class, NUMERIC)
            .resolvesTo(LastPeriodsFunDef.class, NUMERIC, MEMBER)
            .resolvesTo(LastPeriodsFunDef.class, INTEGER)          // Integer -> Numeric, free
            .resolvesWithCost(1, LastPeriodsFunDef.class, NUMERIC, HIERARCHY)   // Hierarchy -> Member
            .resolvesWithCost(2, LastPeriodsFunDef.class, NUMERIC, DIMENSION)   // Dimension -> Member
            .rejects(SET)                  // Set does not convert to Numeric
            .rejects(NUMERIC, SET)         // Set does not convert to Member
            .rejects()                     // arity 0
            .rejects(NUMERIC, MEMBER, MEMBER)   // arity 3

            .autoEdgeCases()
            .edgeCaseMdx("index zero",                 "LastPeriods(0, [Geo].[All Geo].[North])")
            .edgeCaseMdx("index one",                   "LastPeriods(1, [Geo].[All Geo].[North])")
            .edgeCaseMdx("index negative one",          "LastPeriods(-1, [Geo].[All Geo].[North])")
            .edgeCaseMdx("index NULL",                  "LastPeriods(NULL, [Geo].[All Geo].[North])")
            .edgeCaseMdx("index beyond the start",      "LastPeriods(1000, [Geo].[All Geo].[South])")
            .edgeCaseMdx("negative index beyond the end", "LastPeriods(-1000, [Geo].[All Geo].[North])")
            .edgeCaseMdx("null member",                 "LastPeriods(2, [Geo].[All Geo].[North].Parent.Parent)")
            .edgeCaseMdx("default member (Time.CurrentMember)", "LastPeriods(3)")

            // [Geo] is a single flat level with two members, F (index 0) then M (index 1);
            // LastPeriods treats it as an ordered "period" axis just like Time. A positive
            // Index counts backward from Member; a negative Index counts forward from Member.
            .value("Count(LastPeriods(0, [Geo].[All Geo].[North]))",   "0")
            .value("Count(LastPeriods(1, [Geo].[All Geo].[North]))",   "1")
            .value("Count(LastPeriods(NULL, [Geo].[All Geo].[North]))", "0")
            .value("SetToStr(LastPeriods(2, [Geo].[All Geo].[South]))", "{[Geo].[All Geo].[North], [Geo].[All Geo].[South]}")
            .value("SetToStr(LastPeriods(-2, [Geo].[All Geo].[North]))", "{[Geo].[All Geo].[North], [Geo].[All Geo].[South]}")
            // Falling off the start/end of the level clamps to the first/last member instead
            // of returning fewer periods than asked for.
            .value("SetToStr(LastPeriods(1000, [Geo].[All Geo].[South]))", "{[Geo].[All Geo].[North], [Geo].[All Geo].[South]}")

            .dependsOn("LastPeriods(2, [Geo].[All Geo].[South])")

            .resultStyle("LastPeriods(2, [Geo].[All Geo].[South])", ResultStyle.MUTABLE_LIST, ResultStyle.MUTABLE_LIST)
            .resultStyle("LastPeriods(2, [Geo].[All Geo].[South])", ResultStyle.ITERABLE, ResultStyle.MUTABLE_LIST)
            .independentMutableList("LastPeriods(2, [Geo].[All Geo].[South])")

            .build();
}
