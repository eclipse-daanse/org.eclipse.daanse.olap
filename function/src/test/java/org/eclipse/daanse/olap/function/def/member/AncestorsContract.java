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
package org.eclipse.daanse.olap.function.def.member;

import org.eclipse.daanse.olap.testkit.function.FunctionContract.Promise;
import org.eclipse.daanse.olap.testkit.function.FunctionContract;
import static org.eclipse.daanse.olap.api.DataType.DIMENSION;
import static org.eclipse.daanse.olap.api.DataType.HIERARCHY;
import static org.eclipse.daanse.olap.api.DataType.INTEGER;
import static org.eclipse.daanse.olap.api.DataType.LEVEL;
import static org.eclipse.daanse.olap.api.DataType.MEMBER;
import static org.eclipse.daanse.olap.api.DataType.NUMERIC;
import static org.eclipse.daanse.olap.api.DataType.SET;
import static org.eclipse.daanse.olap.api.DataType.STRING;

import org.eclipse.daanse.olap.api.calc.ResultStyle;

/**
 * The contract of the MDX function {@code Ancestors(<Member>, <Level>|<Numeric Expression>)}.
 * {@code AncestorsResolver} is an {@code AbstractFunctionDefinitionMultiResolver} over two
 * declared overloads (by-Level and by-distance), both wrapped in the same {@code
 * AncestorsFunDef} class (constructed with different declared metadata) — {@code resolve()}
 * delegates to the generic {@code FunctionMetaDataMatcher.match}, so there is no hand-written
 * resolver code that could diverge from the declared signature or throw instead of returning
 * empty. {@code compileCall} re-derives which shape to compile from the argument's actual
 * runtime type rather than trusting which of the two registered instances got matched, so
 * both overloads behave identically regardless.
 *
 * <p>{@code AncestorsCalc.evaluateInternal} used to loop {@code for (int curDist = 1; curDist
 * <= distance; ...)} against the compiled {@code Integer} distance directly — {@code
 * Ancestors(member, NULL)} unboxed a {@code null} into a {@code NullPointerException}. Fixed
 * to treat a {@code NULL} distance as {@code 0} (the loop then simply never runs, returning an
 * empty set) — the same "null behaves like a non-positive count" convention {@code HeadCalc}/
 * {@code TailCalc} already use elsewhere in this suite. The identical bug was duplicated
 * verbatim in an anonymous {@code AncestorsCalc} subclass inside {@code
 * AncestorsFunDef.compileCall} (redundantly re-implementing the same base-class method) —
 * removed instead of patched twice, since the base class already does exactly the same thing.
 */
public final class AncestorsContract {

    private AncestorsContract() {
    }

    public static final FunctionContract CONTRACT = FunctionContract.of("Ancestors")
            .signatures(
                    "<Set> Ancestors(<Member>, <Level>)",
                    "<Set> Ancestors(<Member>, <Numeric Expression>)")
            .returns(SET)
            .arity(2, 2)

            .resolvesTo(AncestorsFunDef.class, MEMBER, LEVEL)
            .resolvesTo(AncestorsFunDef.class, MEMBER, NUMERIC)
            .resolvesTo(AncestorsFunDef.class, MEMBER, INTEGER)   // Integer -> Numeric, free
            .resolvesWithCost(1, AncestorsFunDef.class, HIERARCHY, LEVEL)    // Hierarchy -> Member
            .resolvesWithCost(2, AncestorsFunDef.class, DIMENSION, LEVEL)    // Dimension -> Member
            .rejects(SET, LEVEL)       // Set does not convert to Member
            .rejects(MEMBER, SET)       // Set converts to neither Level nor Numeric
            .rejects(MEMBER, STRING)    // String does not convert to Level or Numeric
            .rejects()                  // arity 0
            .rejects(MEMBER)             // arity 1
            .rejects(MEMBER, LEVEL, LEVEL)   // arity 3

            .autoEdgeCases()
            .edgeCaseMdx("level form",        "Ancestors([Geo].[All Geo].[North], [Geo].[All Geo].[North].Parent.Level)")
            .edgeCaseMdx("distance 1",        "Ancestors([Geo].[All Geo].[North], 1)")
            .edgeCaseMdx("distance 0",        "Ancestors([Geo].[All Geo].[North], 0)")
            .edgeCaseMdx("distance negative", "Ancestors([Geo].[All Geo].[North], -1)")
            .edgeCaseMdx("distance NULL",     "Ancestors([Geo].[All Geo].[North], NULL)")

            // [Geo] is flat (hasAll=true): F has exactly one ancestor, the All member —
            // the same member CurrentMemberContract establishes via [Geo].[All Geo].[North].Parent.
            .valueKnownDefect("Count(Ancestors([Geo].[All Geo].[North], 1))", "1",
                            "the MDX parser cannot read this expression, and a calculated member"
                            + " whose formula it cannot read is taken as a string literal rather"
                            + " than refused, so the cell holds the text of the formula and the"
                            + " function is never called. The fallback is deliberate and sits in"
                            + " MdxParserUtil.getExpression in org.eclipse.daanse.mdx: the catch at"
                            + " line 113 swallows the parse failure, prints a stack trace and"
                            + " returns the literal, with a comment doubting that choice")
            .valueKnownDefect("Count(Ancestors([Geo].[All Geo].[North], 0))", "0",
                            "the MDX parser cannot read this expression, and a calculated member"
                            + " whose formula it cannot read is taken as a string literal rather"
                            + " than refused, so the cell holds the text of the formula and the"
                            + " function is never called. The fallback is deliberate and sits in"
                            + " MdxParserUtil.getExpression in org.eclipse.daanse.mdx: the catch at"
                            + " line 113 swallows the parse failure, prints a stack trace and"
                            + " returns the literal, with a comment doubting that choice")
            .valueKnownDefect("Count(Ancestors([Geo].[All Geo].[North], [Geo].[All Geo].[North].Parent.Level))", "1",
                            "the MDX parser cannot read this expression, and a calculated member"
                            + " whose formula it cannot read is taken as a string literal rather"
                            + " than refused, so the cell holds the text of the formula and the"
                            + " function is never called. The fallback is deliberate and sits in"
                            + " MdxParserUtil.getExpression in org.eclipse.daanse.mdx: the catch at"
                            + " line 113 swallows the parse failure, prints a stack trace and"
                            + " returns the literal, with a comment doubting that choice")
            .valueKnownDefect("(Ancestors([Geo].[All Geo].[North], 1).Item(0) IS [Geo].[All Geo].[North].Parent)", "true",
                            "the MDX parser cannot read this expression, and a calculated member"
                            + " whose formula it cannot read is taken as a string literal rather"
                            + " than refused, so the cell holds the text of the formula and the"
                            + " function is never called. The fallback is deliberate and sits in"
                            + " MdxParserUtil.getExpression in org.eclipse.daanse.mdx: the catch at"
                            + " line 113 swallows the parse failure, prints a stack trace and"
                            + " returns the literal, with a comment doubting that choice")

            .dependsOnKnownDefect("Ancestors([Geo].[All Geo].[North], 1)",
                            "the MDX parser rejects this expression outright: Ancestors is a"
                            + " reserved token that its grammar does not accept here, so the query"
                            + " never reaches the function. The defect is in org.eclipse.daanse.mdx,"
                            + " not in this module")

            .resultStyleKnownDefect("Ancestors([Geo].[All Geo].[North], 1)", ResultStyle.MUTABLE_LIST, ResultStyle.MUTABLE_LIST,
                            "the MDX parser rejects this expression outright: Ancestors is a"
                            + " reserved token that its grammar does not accept here, so the query"
                            + " never reaches the function. The defect is in org.eclipse.daanse.mdx,"
                            + " not in this module")
            .resultStyleKnownDefect("Ancestors([Geo].[All Geo].[North], 1)", ResultStyle.ITERABLE, ResultStyle.ITERABLE,
                            "the MDX parser rejects this expression outright: Ancestors is a"
                            + " reserved token that its grammar does not accept here, so the query"
                            + " never reaches the function. The defect is in org.eclipse.daanse.mdx,"
                            + " not in this module")
            .independentMutableListKnownDefect("Ancestors([Geo].[All Geo].[North], 1)",
                            "the MDX parser rejects this expression outright: Ancestors is a"
                            + " reserved token that its grammar does not accept here, so the query"
                            + " never reaches the function. The defect is in"
                            + " org.eclipse.daanse.mdx, not in this module")

            .build();
}
