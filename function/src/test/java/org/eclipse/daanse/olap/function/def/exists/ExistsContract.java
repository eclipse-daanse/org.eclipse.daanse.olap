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
package org.eclipse.daanse.olap.function.def.exists;

import org.eclipse.daanse.olap.testkit.function.FunctionContract.Promise;
import org.eclipse.daanse.olap.testkit.function.FunctionContract;
import static org.eclipse.daanse.olap.api.DataType.LEVEL;
import static org.eclipse.daanse.olap.api.DataType.MEMBER;
import static org.eclipse.daanse.olap.api.DataType.SET;
import static org.eclipse.daanse.olap.api.DataType.STRING;

import org.eclipse.daanse.olap.api.calc.ResultStyle;

/**
 * The contract of the MDX function {@code Exists}. One overload, {@code (Set1, Set2)}, no
 * optional third argument (unlike {@link ExceptContract} and
 * {@link DrilldownMemberContract}) and no reserved words. Note the asymmetry with {@code
 * Except}: an empty Set2 makes {@code ExistsCalc} return an empty result (nothing can be
 * shown to exist against nothing), whereas {@code Except} with an empty Set2 returns Set1
 * unchanged (nothing to exclude). {@code ExistsCalc} does not override {@code dependsOn}: like
 * {@code ExceptCalc}, both arguments here are constant, so the whole call depends on nothing
 * (verified against a real connection).
 */
public final class ExistsContract {

    private ExistsContract() {
    }

    public static final FunctionContract CONTRACT = FunctionContract.of("Exists")
            .signatures("<Set> Exists(<Set>, <Set>)")
            .returns(SET)
            .arity(2, 2)

            .resolvesTo(ExistsFunDef.class, SET, SET)
            .resolvesWithCost(2, ExistsFunDef.class, MEMBER, SET)   // Member -> Set
            .resolvesWithCost(2, ExistsFunDef.class, SET, MEMBER)   // Member -> Set
            .resolvesWithCost(1, ExistsFunDef.class, LEVEL, SET)    // Level -> Set
            .rejects(SET)              // arity 1
            .rejects()                 // arity 0
            .rejects(SET, STRING)      // String does not convert to Set
            .rejects(SET, SET, SET)    // arity 3

            .autoEdgeCases()
            .edgeCaseMdx("empty first set",              "Exists({}, [Geo].Members)")
            .edgeCaseMdx("empty second set",             "Exists([Geo].Members, {})")
            .edgeCaseMdx("member present in Set2",       "Exists([Geo].Members, {[Geo].[All Geo].[North]})")
            .edgeCaseMdx("Set2 from another hierarchy",  "Exists([Geo].Members, {[Measures].[Amount]})")

            // A tuple "exists" only if it is on the same hierarchy chain as some tuple of
            // Set2; a Gender member not equal to (or an ancestor/descendant of) the one member
            // named in Set2 is filtered out.
            .value("Count(Exists([Geo].Members, {[Geo].[All Geo].[North]}))", "4")
            .value("SetToStr(Exists([Geo].Members, {[Geo].[All Geo].[North]}))", "{[Geo].[All Geo], [Geo].[All Geo].[North], "
                            + "[Geo].[All Geo].[North].[A], [Geo].[All Geo].[North].[B]}")
            .value("Count(Exists([Geo].Members, [Geo].Members))", "8")
            .value("Count(Exists({}, [Geo].Members))", "0")
            .value("Count(Exists([Geo].Members, {}))", "0")

            .dependsOn("Exists([Geo].Members, {[Geo].[All Geo].[North]})")
            .dependsOn("Exists([Geo].Members, {[Measures].[Amount]})")

            .resultStyle("Exists([Geo].Members, {[Geo].[All Geo].[North]})", ResultStyle.MUTABLE_LIST, ResultStyle.MUTABLE_LIST)
            .resultStyle("Exists([Geo].Members, {[Geo].[All Geo].[North]})", ResultStyle.ITERABLE, ResultStyle.MUTABLE_LIST)
            .independentMutableList("Exists([Geo].Members, {[Geo].[All Geo].[North]})")

            .build();
}
