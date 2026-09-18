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
package org.eclipse.daanse.olap.function.def.generate;

import org.eclipse.daanse.olap.testkit.function.FunctionContract.Promise;
import org.eclipse.daanse.olap.testkit.function.FunctionContract;
import static org.eclipse.daanse.olap.api.DataType.MEMBER;
import static org.eclipse.daanse.olap.api.DataType.NUMERIC;
import static org.eclipse.daanse.olap.api.DataType.SET;
import static org.eclipse.daanse.olap.api.DataType.STRING;
import static org.eclipse.daanse.olap.api.DataType.SYMBOL;

import org.eclipse.daanse.olap.api.calc.ResultStyle;

/**
 * The contract of the MDX function {@code Generate}. Two resolvers share the atom, both
 * producing {@code GenerateFunDef}, distinguished by {@code getResultType} inspecting the
 * second argument's type: {@code GenerateListResolver} — {@code (Set1, Set2, All?)}, unions
 * the per-member Set2 results into a Set — and {@code GenerateStringResolver} — {@code (Set,
 * String[, Separator])} / {@code (Set, Numeric, Separator)} — joins a per-member string (or
 * {@code Str(numeric)}) into one String. Like {@link StarContract}, the two return categories
 * (Set vs String) mean there is no single {@code .returns(...)} to declare.
 *
 * <p>Neither resolver overrides {@code getReservedWords()}, so {@code declaresReservedWords}
 * would see an empty set for this atom even though {@code Generate(Set1, Set2, ALL)} is a
 * documented, working overload: the bare word {@code ALL} only tokenizes as a SYMBOL literal
 * here because {@code ExceptResolver} happens to register it globally (see {@link
 * ExceptContract} and {@link DescendantsContract}'s "reserved-word recognition is global"
 * note). Generate's own ALL flag rides on that coincidence rather than declaring the word
 * itself — a fragile, undeclared cross-function coupling.
 *
 * <p>{@code GenerateListCalc}/{@code GenerateStringCalc.dependsOn} both use {@code
 * HierarchyDependsChecker.checkAnyDependsButFirst}: the outer Set1 argument's own hierarchy is
 * excluded, exactly like {@link FilterContract} and {@link DrilldownLevelBottomContract} — but
 * a Set2/value-expression that itself reads Set1's hierarchy via {@code CurrentMember} still
 * reports it, since that reference lives in the (not excluded) second child Calc.
 */
public final class GenerateContract {

    private GenerateContract() {
    }

    public static final FunctionContract CONTRACT = FunctionContract.of("Generate")
            .signatures(
                    "<Set> Generate(<Set>, <Set>, <Symbol>)",
                    "<String> Generate(<Set>, <String>)",
                    "<String> Generate(<Set>, <String>, <String>)",
                    "<String> Generate(<Set>, <Numeric Expression>, <String>)")
            .arity(2, 3)

            .resolvesTo(GenerateFunDef.class, SET, SET)
            .resolvesTo(GenerateFunDef.class, SET, SET, SYMBOL)
            .resolvesTo(GenerateFunDef.class, SET, STRING)
            .resolvesTo(GenerateFunDef.class, SET, STRING, STRING)
            .resolvesTo(GenerateFunDef.class, SET, NUMERIC, STRING)
            .resolvesWithCost(2, GenerateFunDef.class, MEMBER, SET)      // Member -> Set
            .resolvesWithCost(2, GenerateFunDef.class, MEMBER, STRING)   // Member -> Set (arg0 only)
            .rejects(SET)                  // arity 1
            .rejects()                     // arity 0
            .rejects(SET, NUMERIC)         // the numeric form needs the Separator: arity 3, not 2
            .rejects(SET, SET, STRING)     // the Set-form's third arg must be the ALL symbol
            .rejects(SET, STRING, SET)     // the String-form's Separator must be a String
            .rejects(SET, SET, SET, SET)   // arity 4

            .autoEdgeCases()
            .edgeCaseMdx("empty outer set",              "Generate({}, [Geo].Members)")
            .edgeCaseMdx("empty inner set",               "Generate([Geo].Members, {})")
            .edgeCaseMdx("duplicates removed by default", "Generate([Geo].Members, {[Measures].[Amount]})")
            .edgeCaseMdx("ALL retains duplicates",         "Generate([Geo].Members, {[Measures].[Amount]}, ALL)")
            .edgeCaseMdx("Set2 references CurrentMember",  "Generate([Geo].Members, {[Geo].CurrentMember})")
            .edgeCaseMdx("string form, default separator", "Generate([Geo].Members, [Geo].CurrentMember.Name)")
            .edgeCaseMdx("string form, explicit separator", "Generate([Geo].Members, [Geo].CurrentMember.Name, \", \")")
            .edgeCaseMdx("numeric form",                    "Generate([Geo].Members, 1, \", \")")
            .edgeCaseMdx("string form, empty set",          "Generate({}, [Geo].CurrentMember.Name)")

            // A constant Set2 (one that does not depend on the Gender member being iterated)
            // gets unioned once per [Geo] member; by default the identical single-tuple
            // result is deduplicated down to one row, but ALL keeps both.
            .value("Count(Generate([Geo].Members, {[Measures].[Amount]}))", "1")
            .valueKnownDefect("Count(Generate([Geo].Members, {[Measures].[Amount]}, ALL))", "3",
                            "the MDX parser cannot read this expression, and a calculated member"
                            + " whose formula it cannot read is taken as a string literal rather"
                            + " than refused, so the cell holds the text of the formula and the"
                            + " function is never called. The fallback is deliberate and sits in"
                            + " MdxParserUtil.getExpression in org.eclipse.daanse.mdx: the catch at"
                            + " line 113 swallows the parse failure, prints a stack trace and"
                            + " returns the literal, with a comment doubting that choice")
            .value("SetToStr(Generate([Geo].Members, {[Measures].[Amount]}))", "{[Measures].[Amount]}")
            .value("Generate([Geo].Members, [Geo].CurrentMember.Name)", "All GeoNorthABSouthCDE")
            .value("Generate([Geo].Members, [Geo].CurrentMember.Name, \", \")", "All Geo, North, A, B, South, C, D, E")

            // The outer Set1 argument's own hierarchy is excluded from the reported
            // dependencies (see the class Javadoc): a Set2 unrelated to Gender depends only on
            // Measures, NOT on Gender even though Gender is what is being iterated.
            .dependsOn("Generate([Geo].Members, {[Measures].[Amount]})")
            // A Set2 that reads the iterated hierarchy's CurrentMember is still bound to the
            // iteration itself (shadowing the outer context, exactly like FilterContract), so
            // it does not add an outer dependency either.
            .dependsOn("Generate([Geo].Members, {[Geo].CurrentMember})")
            .scalarDependsOn("Generate([Geo].Members, [Geo].CurrentMember.Name)")

            // Only reachable through the Set-returning overload: the String-returning overload
            // has no set-context ResultStyle promise to honor (same reasoning as SumContract's
            // waiver, but here the Set overload already covers the promise, so no waiver is
            // needed).
            .resultStyle("Generate([Geo].Members, {[Measures].[Amount]})",
                         ResultStyle.MUTABLE_LIST, ResultStyle.MUTABLE_LIST)
            .resultStyle("Generate([Geo].Members, {[Measures].[Amount]})",
                         ResultStyle.ITERABLE, ResultStyle.MUTABLE_LIST)
            .independentMutableList("Generate([Geo].Members, {[Measures].[Amount]})")

            .build();
}
