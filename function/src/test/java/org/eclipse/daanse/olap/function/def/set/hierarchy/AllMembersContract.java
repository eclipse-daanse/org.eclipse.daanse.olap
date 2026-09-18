/*
 * Copyright (c) 2026 Contributors to the Eclipse Foundation.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Stefan Bischof (bipolis.org) - initial
 */
package org.eclipse.daanse.olap.function.def.set.hierarchy;

import org.eclipse.daanse.olap.testkit.function.FunctionContract.Promise;
import org.eclipse.daanse.olap.testkit.function.FunctionContract;
import static org.eclipse.daanse.olap.api.DataType.DIMENSION;
import static org.eclipse.daanse.olap.api.DataType.HIERARCHY;
import static org.eclipse.daanse.olap.api.DataType.LEVEL;
import static org.eclipse.daanse.olap.api.DataType.NUMERIC;
import static org.eclipse.daanse.olap.api.DataType.SET;
import static org.eclipse.daanse.olap.api.DataType.STRING;

import org.eclipse.daanse.mdx.model.api.expression.operation.PlainPropertyOperationAtom;
import org.eclipse.daanse.olap.api.calc.ResultStyle;

/** The contract of the MDX property {@code AllMembers}: {@code <Hierarchy>.AllMembers} and
 * {@code <Level>.AllMembers}. Both overloads share one {@link PlainPropertyOperationAtom}. */
public final class AllMembersContract {

    private AllMembersContract() {
    }

    public static final FunctionContract CONTRACT = FunctionContract.of("AllMembers")
            .atom(PlainPropertyOperationAtom.class)
            .signatures("<Hierarchy>.AllMembers", "<Level>.AllMembers")
            .returns(SET)
            .arity(1, 1)

            // Both overloads live under the same atom; the validator keeps whichever is
            // cheaper when a type matches both. A <Level> argument type-matches <Hierarchy>
            // too (Level -> Hierarchy, cost 1) but resolves to the <Level> overload, cost 0.
            .resolvesTo(AllMembersFunDef.class, HIERARCHY)
            .resolvesTo(org.eclipse.daanse.olap.function.def.set.level.AllMembersFunDef.class, LEVEL)
            .resolvesWithCost(2, AllMembersFunDef.class, DIMENSION)   // Dimension -> Hierarchy;
                                                                       // cheaper than -> Level (cost 3)
            .rejects(NUMERIC)
            .rejects(SET)
            .rejects(STRING)
            .rejects()                       // arity 0
            .rejects(HIERARCHY, HIERARCHY)   // arity 2

            // NOTE (verified against StandardFunctions.standard(), not asserted below): a
            // <Member> argument, e.g. [Geo].[All Geo].[North].AllMembers, is genuinely ambiguous. Member ->
            // Hierarchy and Member -> Level both cost 1, so the two overloads tie and
            // ValidatorImpl.getDef fails with "More than one function matches signature". This
            // DSL has no case for "resolves, but ambiguously" (isRejected() needs zero best
            // matches, resolvesTo(...) needs exactly one), so there is no case for it here.

            .autoEdgeCases()
            .edgeCaseMdx("hierarchy", "[Geo].AllMembers")
            .edgeCaseMdx("level",     "[Geo].[All Geo].[North].Level.AllMembers")

            // [Geo] has no calculated members in the Sales cube: same members as .Members.
            .valueKnownDefect("SetToStr([Geo].AllMembers)", "{[Geo].[Geo].[All Geo], [Geo].[Region].[F], [Geo].[Region].[M]}",
                            "the MDX parser cannot read this expression, and a calculated member"
                            + " whose formula it cannot read is taken as a string literal rather"
                            + " than refused, so the cell holds the text of the formula and the"
                            + " function is never called. The fallback is deliberate and sits in"
                            + " MdxParserUtil.getExpression in org.eclipse.daanse.mdx: the catch at"
                            + " line 113 swallows the parse failure, prints a stack trace and"
                            + " returns the literal, with a comment doubting that choice")
            .valueKnownDefect("SetToStr([Geo].[All Geo].[North].Level.AllMembers)", "{[Geo].[Region].[F], [Geo].[Region].[M]}",
                            "the MDX parser cannot read this expression, and a calculated member"
                            + " whose formula it cannot read is taken as a string literal rather"
                            + " than refused, so the cell holds the text of the formula and the"
                            + " function is never called. The fallback is deliberate and sits in"
                            + " MdxParserUtil.getExpression in org.eclipse.daanse.mdx: the catch at"
                            + " line 113 swallows the parse failure, prints a stack trace and"
                            + " returns the literal, with a comment doubting that choice")

            .dependsOnKnownDefect("[Geo].AllMembers",
                            "the MDX parser rejects this expression outright: .AllMembers is a"
                            + " reserved token that its grammar does not accept here, so the query"
                            + " never reaches the function. The defect is in org.eclipse.daanse.mdx,"
                            + " not in this module")
            .dependsOnKnownDefect("[Geo].[All Geo].[North].Level.AllMembers",
                            "the MDX parser rejects this expression outright: .AllMembers is a"
                            + " reserved token that its grammar does not accept here, so the query"
                            + " never reaches the function. The defect is in org.eclipse.daanse.mdx,"
                            + " not in this module")

            .resultStyleKnownDefect("[Geo].AllMembers", ResultStyle.MUTABLE_LIST, ResultStyle.MUTABLE_LIST,
                            "the MDX parser rejects this expression outright: .AllMembers is a"
                            + " reserved token that its grammar does not accept here, so the query"
                            + " never reaches the function. The defect is in org.eclipse.daanse.mdx,"
                            + " not in this module")
            .resultStyleKnownDefect("[Geo].AllMembers", ResultStyle.ITERABLE, ResultStyle.ITERABLE,
                            "the MDX parser rejects this expression outright: .AllMembers is a"
                            + " reserved token that its grammar does not accept here, so the query"
                            + " never reaches the function. The defect is in org.eclipse.daanse.mdx,"
                            + " not in this module")
            .independentMutableListKnownDefect("[Geo].AllMembers",
                            "the MDX parser rejects this expression outright: .AllMembers is a"
                            + " reserved token that its grammar does not accept here, so the query"
                            + " never reaches the function. The defect is in"
                            + " org.eclipse.daanse.mdx, not in this module")
            .independentMutableListKnownDefect("[Geo].[All Geo].[North].Level.AllMembers",
                            "the MDX parser rejects this expression outright: .AllMembers is a"
                            + " reserved token that its grammar does not accept here, so the query"
                            + " never reaches the function. The defect is in"
                            + " org.eclipse.daanse.mdx, not in this module")
            .build();
}
