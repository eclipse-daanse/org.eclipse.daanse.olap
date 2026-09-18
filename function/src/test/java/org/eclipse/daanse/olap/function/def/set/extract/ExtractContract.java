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
package org.eclipse.daanse.olap.function.def.set.extract;

import org.eclipse.daanse.olap.testkit.function.FunctionContract.Promise;
import org.eclipse.daanse.olap.testkit.function.FunctionContract;
import static org.eclipse.daanse.olap.api.DataType.DIMENSION;
import static org.eclipse.daanse.olap.api.DataType.HIERARCHY;
import static org.eclipse.daanse.olap.api.DataType.LEVEL;
import static org.eclipse.daanse.olap.api.DataType.MEMBER;
import static org.eclipse.daanse.olap.api.DataType.NUMERIC;
import static org.eclipse.daanse.olap.api.DataType.SET;
import static org.eclipse.daanse.olap.api.DataType.STRING;

import org.eclipse.daanse.olap.api.calc.ResultStyle;

/**
 * The contract of the MDX function {@code Extract} — "the opposite of Crossjoin". A
 * hand-written {@code NoExpressionRequiredFunctionResolver} (like {@code CrossJoinResolver}):
 * one {@code <Set>} followed by one or more repeatable {@code <Hierarchy>} arguments.
 *
 * <p>{@code ExtractResolver.resolve()} only checks that the arguments type-convert to Set and
 * Hierarchy — a pure, cube-metadata-free shape check, exactly like every other resolver here.
 * The deeper semantic check (each named hierarchy must actually be one of the LHS set's own
 * hierarchies, and none may be named twice) needs the hierarchy identities themselves, which
 * requires real cube data; it is deliberately left to {@code
 * ExtractFunDef.getResultType}/{@code compileCall}, which run once resolution has already
 * matched this overload and can report a precise, diagnosed {@code OlapRuntimeException}
 * instead of the resolver either guessing or crashing. The two "hierarchy not ..." edge cases
 * below exercise exactly that path.
 */
public final class ExtractContract {

    private ExtractContract() {
    }

    public static final FunctionContract CONTRACT = FunctionContract.of("Extract")
            .signatures("<Set> Extract(<Set>, <Hierarchy>)")
            .returns(SET)
            .arity(2, Integer.MAX_VALUE)

            .resolvesTo(ExtractFunDef.class, SET, HIERARCHY)
            .resolvesTo(ExtractFunDef.class, SET, HIERARCHY, HIERARCHY)
            .resolvesWithCost(1, ExtractFunDef.class, SET, LEVEL)      // Level -> Hierarchy
            .resolvesWithCost(2, ExtractFunDef.class, SET, DIMENSION)  // Dimension -> Hierarchy
            .resolvesWithCost(1, ExtractFunDef.class, SET, MEMBER)     // Member -> Hierarchy
            .resolvesWithCost(2, ExtractFunDef.class, MEMBER, HIERARCHY)   // Member -> Set
            .resolvesWithCost(1, ExtractFunDef.class, LEVEL, HIERARCHY)    // Level -> Set
            .rejects(SET)              // arity 1, a Hierarchy is required
            .rejects()                 // arity 0
            .rejects(SET, STRING)      // String does not convert to Hierarchy
            .rejects(SET, NUMERIC)     // Numeric does not convert to Hierarchy

            .autoEdgeCases()
            .edgeCaseMdx("single hierarchy (degenerates to Distinct)",
                         "Extract([Geo].Members, [Geo])")
            .edgeCaseMdx("extract one hierarchy from a crossjoin",
                         "Extract(Crossjoin([Geo].Members, {[Measures].[Amount]}), [Measures])")
            .edgeCaseMdx("extract every hierarchy of a crossjoin",
                         "Extract(Crossjoin([Geo].Members, {[Measures].[Amount]}), [Geo], [Measures])")
            .edgeCaseMdx("empty set", "Extract({}, [Geo])")
            .edgeCaseMdx("hierarchy not one of the set's own",
                         "Extract([Geo].Members, [Measures])")
            .edgeCaseMdx("hierarchy extracted twice",
                         "Extract(Crossjoin([Geo].Members, {[Measures].[Amount]}), [Geo], [Geo])")

            // A single-hierarchy set extracting its own (only) hierarchy is just duplicate
            // elimination; [Geo].Members is already distinct.
            .value("Count(Extract([Geo].Members, [Geo]))", "8")
            .value("SetToStr(Extract([Geo].Members, [Geo]))", "{[Geo].[All Geo], [Geo].[All Geo].[North], "
                            + "[Geo].[All Geo].[North].[A], [Geo].[All Geo].[North].[B], "
                            + "[Geo].[All Geo].[South], [Geo].[All Geo].[South].[C], "
                            + "[Geo].[All Geo].[South].[D], [Geo].[All Geo].[South].[E]}")
            // The actual "opposite of Crossjoin" case: extracting Measures from a Gender x
            // Measures crossjoin collapses the two tuples back down to the one Measures member.
            .value("Count(Extract(Crossjoin([Geo].Members, {[Measures].[Amount]}), [Measures]))", "1")
            .value("Count(Extract(Crossjoin([Geo].Members, {[Measures].[Amount]}), [Geo]))", "8")

            .dependsOn("Extract([Geo].Members, [Geo])")
            // [Geo].Members and the literal-member singleton set {[Measures].[Amount]}
            // are both constant (see MembersContract/ExceptContract): a Set of literal members
            // does not get the MemberValueCalc-style scalar coercion that fixes-its-own-
            // hierarchy-but-depends-on-every-other-one, so the whole call depends on nothing.
            .dependsOn("Extract(Crossjoin([Geo].Members, {[Measures].[Amount]}), [Geo])")

            .resultStyle("Extract(Crossjoin([Geo].Members, {[Measures].[Amount]}), [Geo])",
                         ResultStyle.MUTABLE_LIST, ResultStyle.MUTABLE_LIST)
            .resultStyle("Extract(Crossjoin([Geo].Members, {[Measures].[Amount]}), [Geo])",
                         ResultStyle.ITERABLE, ResultStyle.MUTABLE_LIST)
            .independentMutableList("Extract(Crossjoin([Geo].Members, {[Measures].[Amount]}), [Geo])")

            .build();
}
