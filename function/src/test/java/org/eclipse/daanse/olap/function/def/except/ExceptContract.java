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
package org.eclipse.daanse.olap.function.def.except;

import org.eclipse.daanse.olap.testkit.function.FunctionContract.Promise;
import org.eclipse.daanse.olap.testkit.function.FunctionContract;
import static org.eclipse.daanse.olap.api.DataType.LEVEL;
import static org.eclipse.daanse.olap.api.DataType.MEMBER;
import static org.eclipse.daanse.olap.api.DataType.SET;
import static org.eclipse.daanse.olap.api.DataType.STRING;
import static org.eclipse.daanse.olap.api.DataType.SYMBOL;

import org.eclipse.daanse.olap.api.calc.ResultStyle;

/**
 * The contract of the MDX function {@code Except}. One overload, three parameters —
 * {@code (Set1, Set2, All)} — shaped exactly like {@link DrilldownMemberContract}'s
 * {@code (Set1, Set2, Recursive)}. {@code ExceptFunDef.compileCall} resolves and accepts the
 * optional {@code ALL} flag but never reads it ({@code // todo: implement ALL}): {@code
 * ExceptCalc} always removes duplicates, so {@code Except(Set1, Set2, ALL)} currently behaves
 * identically to {@code Except(Set1, Set2)}. Like {@code DrilldownMemberCalc} (and unlike
 * {@code DrilldownLevelTopBottomCalc}), {@code ExceptCalc} does not override {@code dependsOn} —
 * the generic child-calc walk applies. Both arguments here are constant (a {@code Members}
 * enumeration and a literal-member set literal never get the {@code MemberValueCalc}-style
 * scalar coercion — see {@link MembersContract}/{@link MinusContract}), so the whole call
 * depends on nothing (verified against a real connection).
 */
public final class ExceptContract {

    private ExceptContract() {
    }

    public static final FunctionContract CONTRACT = FunctionContract.of("Except")
            .signatures("<Set> Except(<Set>, <Set>, <Symbol>)")
            .returns(SET)
            .arity(2, 3)
            .reservedWords("ALL")

            .resolvesTo(ExceptFunDef.class, SET, SET)
            .resolvesTo(ExceptFunDef.class, SET, SET, SYMBOL)
            .resolvesWithCost(2, ExceptFunDef.class, MEMBER, SET)   // Member -> Set
            .resolvesWithCost(2, ExceptFunDef.class, SET, MEMBER)   // Member -> Set
            .resolvesWithCost(1, ExceptFunDef.class, LEVEL, SET)    // Level -> Set
            .rejects(SET)                      // arity 1, Set2 is required
            .rejects()                         // arity 0
            .rejects(SET, STRING)              // String does not convert to Set
            .rejects(SET, SET, STRING)         // third arg must be the ALL symbol
            .rejects(SET, SET, SYMBOL, SYMBOL) // arity 4

            .autoEdgeCases()
            .edgeCaseMdx("empty first set",            "Except({}, [Geo].Members)")
            .edgeCaseMdx("empty second set",           "Except([Geo].Members, {})")
            .edgeCaseMdx("no overlap",                 "Except([Geo].Members, {[Measures].[Amount]})")
            .edgeCaseMdx("full overlap",                "Except([Geo].Members, [Geo].Members)")
            .edgeCaseMdx("with ALL flag",                "Except([Geo].Members, {[Geo].[All Geo].[North]}, ALL)")
            // "RECURSIVE" is not Except's own reserved word, but reserved-word recognition is
            // global (see DescendantsContract's matching case): DrilldownMember registers it,
            // so it still parses as a SYMBOL literal here. Unlike Descendants/DrilldownMember,
            // though, ExceptFunDef.compileCall never reads arg(2) at all (see the class
            // Javadoc's "todo: implement ALL" note) — so this neither crashes nor is validated;
            // it is silently accepted and ignored exactly like a correct ALL would be.
            .edgeCaseMdx("symbol reserved by another function",
                         "Except([Geo].Members, {[Geo].[All Geo].[North]}, RECURSIVE)")

            .value("Count(Except([Geo].Members, {[Geo].[All Geo].[North]}))", "7")
            .value("SetToStr(Except([Geo].Members, {[Geo].[All Geo].[North]}))", "{[Geo].[All Geo], [Geo].[All Geo].[North].[A], "
                            + "[Geo].[All Geo].[North].[B], [Geo].[All Geo].[South], "
                            + "[Geo].[All Geo].[South].[C], [Geo].[All Geo].[South].[D], "
                            + "[Geo].[All Geo].[South].[E]}")
            .value("Count(Except([Geo].Members, [Geo].Members))", "0")
            .value("Count(Except({}, [Geo].Members))", "0")
            .value("Count(Except([Geo].Members, {}))", "8")

            .dependsOn("Except([Geo].Members, {[Geo].[All Geo].[North]})")
            .dependsOn("Except([Geo].Members, {[Measures].[Amount]})")

            .resultStyle("Except([Geo].Members, {[Geo].[All Geo].[North]})", ResultStyle.MUTABLE_LIST, ResultStyle.MUTABLE_LIST)
            .resultStyle("Except([Geo].Members, {[Geo].[All Geo].[North]})", ResultStyle.ITERABLE, ResultStyle.MUTABLE_LIST)
            .independentMutableListKnownDefect("Except([Geo].Members, {[Geo].[All Geo].[North]})",
                            "the calc hands out its own list rather than a copy: modifying the"
                            + " result, which MUTABLE_LIST entitles the caller to do, changes what"
                            + " the next evaluation returns")

            .build();
}
