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
package org.eclipse.daanse.olap.function.def.union;

import org.eclipse.daanse.olap.testkit.function.FunctionContract.Promise;
import org.eclipse.daanse.olap.testkit.function.FunctionContract;
import static org.eclipse.daanse.olap.api.DataType.DIMENSION;
import static org.eclipse.daanse.olap.api.DataType.LEVEL;
import static org.eclipse.daanse.olap.api.DataType.MEMBER;
import static org.eclipse.daanse.olap.api.DataType.SET;
import static org.eclipse.daanse.olap.api.DataType.STRING;
import static org.eclipse.daanse.olap.api.DataType.SYMBOL;

import org.eclipse.daanse.olap.api.calc.ResultStyle;

/**
 * The contract of the MDX function {@code Union(<Set1>, <Set2>[, ALL|DISTINCT])}. {@code
 * UnionResolver} is an {@code AbstractFunctionDefinitionMultiResolver} over one declared
 * overload with an optional {@code ALL}/{@code DISTINCT} symbol and its own correctly
 * declared reserved words (compare {@link HierarchizeContract}'s "PRE"/"POST", {@link
 * ToggleDrillStateContract}'s "RECURSIVE") — {@code resolve()} delegates to the generic
 * {@code FunctionMetaDataMatcher.match}, so there is no hand-written resolver code that could
 * diverge from the declared signature or throw instead of returning empty.
 *
 * <p>{@code UnionFunDef.compileCall} reads the {@code ALL}/{@code DISTINCT} flag via {@code
 * FunUtil.getLiteralArg} and cross-checks the two sets' hierarchy compatibility via {@code
 * FunUtil.checkCompatible} — both Stage B only (compile time), never {@code resolve()} (Stage
 * A), so inert in this test kit regardless of the (cube-free) stub category probed.
 *
 * <p>{@code UnionCalc} defaults to {@code DISTINCT} (deduplicating via {@code
 * FunUtil.addUnique}); {@code ALL} is a literal concatenation that keeps duplicates and short-
 * circuits to whichever operand is non-empty when the other is empty.
 */
public final class UnionContract {

    private UnionContract() {
    }

    public static final FunctionContract CONTRACT = FunctionContract.of("Union")
            .signatures("<Set> Union(<Set>, <Set>, <Symbol>)")
            .returns(SET)
            .arity(2, 3)
            .reservedWords("ALL", "DISTINCT")

            .resolvesTo(UnionFunDef.class, SET, SET)
            .resolvesTo(UnionFunDef.class, SET, SET, SYMBOL)
            .resolvesWithCost(2, UnionFunDef.class, MEMBER, SET)   // Member -> Set
            .resolvesWithCost(1, UnionFunDef.class, LEVEL, SET)    // Level -> Set
            .rejects(DIMENSION, SET)   // Dimension does not convert to Set
            .rejects(SET)               // arity 1
            .rejects()                  // arity 0
            .rejects(SET, SET, STRING)  // String does not convert to Symbol
            .rejects(SET, SET, SYMBOL, SYMBOL)   // arity 4

            .autoEdgeCases()
            .edgeCaseMdx("both sets empty",     "Union({}, {})")
            .edgeCaseMdx("first set empty",     "Union({}, [Geo].Members)")
            .edgeCaseMdx("second set empty",    "Union([Geo].Members, {})")
            .edgeCaseMdx("default (DISTINCT)",  "Union([Geo].Members, [Geo].Members)")
            .edgeCaseMdx("explicit DISTINCT",   "Union([Geo].Members, [Geo].Members, DISTINCT)")
            .edgeCaseMdx("explicit ALL",        "Union([Geo].Members, [Geo].Members, ALL)")
            // "PRE" is not ALL/DISTINCT, but it is reserved globally (HierarchizeResolver
            // registers it — see DescendantsContract's matching note), so it still parses as
            // a SYMBOL literal here. FunUtil.getLiteralArg then throws a diagnosed exception
            // for the mismatch — but only inside compileCall (Stage B), never resolve().
            .edgeCaseMdx("symbol reserved by another function", "Union([Geo].Members, [Geo].Members, PRE)")

            .value("Count(Union([Geo].Members, [Geo].Members))",      "8")
            .valueKnownDefect("Count(Union([Geo].Members, [Geo].Members, ALL))", "6",
                            "the MDX parser cannot read this expression, and a calculated member"
                            + " whose formula it cannot read is taken as a string literal rather"
                            + " than refused, so the cell holds the text of the formula and the"
                            + " function is never called. The fallback is deliberate and sits in"
                            + " MdxParserUtil.getExpression in org.eclipse.daanse.mdx: the catch at"
                            + " line 113 swallows the parse failure, prints a stack trace and"
                            + " returns the literal, with a comment doubting that choice")
            .value("Count(Union({}, [Geo].Members))",                    "8")
            .value("Count(Union([Geo].Members, {}))",                    "8")
            .value("SetToStr(Union({[Geo].[All Geo].[North]}, {[Geo].[All Geo].[South]}))", "{[Geo].[All Geo].[North], [Geo].[All Geo].[South]}")

            .dependsOn("Union([Geo].Members, [Geo].Members)")

            .resultStyle("Union([Geo].Members, [Geo].Members)", ResultStyle.MUTABLE_LIST, ResultStyle.MUTABLE_LIST)
            .resultStyle("Union([Geo].Members, [Geo].Members)", ResultStyle.ITERABLE, ResultStyle.MUTABLE_LIST)
            .independentMutableList("Union([Geo].Members, [Geo].Members)")

            .build();
}
