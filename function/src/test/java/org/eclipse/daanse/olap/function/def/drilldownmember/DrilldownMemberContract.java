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
package org.eclipse.daanse.olap.function.def.drilldownmember;

import org.eclipse.daanse.olap.testkit.function.FunctionContract.Promise;
import org.eclipse.daanse.olap.testkit.function.FunctionContract;
import static org.eclipse.daanse.olap.api.DataType.LEVEL;
import static org.eclipse.daanse.olap.api.DataType.MEMBER;
import static org.eclipse.daanse.olap.api.DataType.SET;
import static org.eclipse.daanse.olap.api.DataType.STRING;
import static org.eclipse.daanse.olap.api.DataType.SYMBOL;

import org.eclipse.daanse.olap.api.calc.ResultStyle;

/**
 * The contract of the MDX function {@code DrilldownMember}. One overload, three parameters —
 * {@code (Set1, Set2, Recursive)} — with the third, optional and {@code RECURSIVE}-only,
 * still printed in the declared signature (parameter optionality does not change how
 * {@code FunctionPrinter} renders the overload text). Unlike
 * {@link DrilldownLevelBottomContract}/{@link DrilldownLevelTopContract}, {@code
 * DrilldownMemberCalc} does not override {@code dependsOn}: the generic child-calc walk
 * applies. Both arguments here are constant (see {@link ExceptContract}), so the whole call
 * depends on nothing (verified against a real connection).
 */
public final class DrilldownMemberContract {

    private DrilldownMemberContract() {
    }

    public static final FunctionContract CONTRACT = FunctionContract.of("DrilldownMember")
            .signatures("<Set> DrilldownMember(<Set>, <Set>, <Symbol>)")
            .returns(SET)
            .arity(2, 3)
            .reservedWords("RECURSIVE")

            .resolvesTo(DrilldownMemberFunDef.class, SET, SET)
            .resolvesTo(DrilldownMemberFunDef.class, SET, SET, SYMBOL)
            .resolvesWithCost(2, DrilldownMemberFunDef.class, MEMBER, SET)   // Member -> Set
            .resolvesWithCost(2, DrilldownMemberFunDef.class, SET, MEMBER)   // Member -> Set
            .resolvesWithCost(1, DrilldownMemberFunDef.class, LEVEL, SET)    // Level -> Set
            .rejects(SET)                     // arity 1, Set2 is required
            .rejects()                        // arity 0
            .rejects(SET, STRING)             // String does not convert to Set
            .rejects(SET, SET, STRING)        // third arg must be the RECURSIVE symbol
            .rejects(SET, SET, SYMBOL, SYMBOL) // arity 4

            .autoEdgeCases()
            .edgeCaseMdx("empty first set",           "DrilldownMember({}, [Geo].Members)")
            .edgeCaseMdx("empty second set",          "DrilldownMember([Geo].Members, {})")
            .edgeCaseMdx("member present in Set2",    "DrilldownMember([Geo].Members, {[Geo].[All Geo].[North]})")
            .edgeCaseMdx("RECURSIVE flag",            "DrilldownMember([Geo].Members, {[Geo].[All Geo].[North]}, RECURSIVE)")
            .edgeCaseMdx("Set2 from another hierarchy", "DrilldownMember([Geo].Members, {[Measures].[Amount]})")
            // "ALL" is not DrilldownMember's own reserved word, but reserved-word recognition
            // is global (see DescendantsContract's matching case): Except registers it, so it
            // still parses as a SYMBOL literal here. DrilldownMemberFunDef.compileCall then
            // calls FunUtil.getLiteralArg(call, 2, "", List.of("RECURSIVE")), which throws a
            // diagnosed DaanseEvaluationException("Allowed values are: {RECURSIVE}") for the
            // mismatch.
            .edgeCaseMdx("symbol reserved by another function",
                         "DrilldownMember([Geo].Members, {[Geo].[All Geo].[North]}, ALL)")

            // [Geo] members are leaves (one real level beneath (All)): whether or not a
            // member is present in Set2, there are no children to drill down to.
            .value("Count(DrilldownMember([Geo].Members, {[Geo].[All Geo].[North]}))", "10")
            .value("SetToStr(DrilldownMember([Geo].Members, {[Geo].[All Geo].[North]}))", "{[Geo].[All Geo], [Geo].[All Geo].[North], "
                            + "[Geo].[All Geo].[North].[A], [Geo].[All Geo].[North].[B], "
                            + "[Geo].[All Geo].[North].[A], [Geo].[All Geo].[North].[B], "
                            + "[Geo].[All Geo].[South], [Geo].[All Geo].[South].[C], "
                            + "[Geo].[All Geo].[South].[D], [Geo].[All Geo].[South].[E]}")
            .value("Count(DrilldownMember({}, [Geo].Members))", "0")
            .value("Count(DrilldownMember([Geo].Members, {}))", "8")

            .dependsOn("DrilldownMember([Geo].Members, {[Geo].[All Geo].[North]})")
            .dependsOn("DrilldownMember([Geo].Members, {[Measures].[Amount]})")

            .resultStyle("DrilldownMember([Geo].Members, {[Geo].[All Geo].[North]})",
                         ResultStyle.MUTABLE_LIST, ResultStyle.MUTABLE_LIST)
            .resultStyle("DrilldownMember([Geo].Members, {[Geo].[All Geo].[North]})",
                         ResultStyle.ITERABLE, ResultStyle.MUTABLE_LIST)
            .independentMutableList("DrilldownMember([Geo].Members, {[Geo].[All Geo].[North]})")

            .build();
}
