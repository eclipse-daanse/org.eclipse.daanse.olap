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
            .value("SetToStr([Geo].AllMembers)",
                    "{[Geo].[All Geo], [Geo].[All Geo].[North], [Geo].[All Geo].[North].[A], [Geo].[All Geo].[North].[B],"
                    + " [Geo].[All Geo].[South], [Geo].[All Geo].[South].[C], [Geo].[All Geo].[South].[D],"
                    + " [Geo].[All Geo].[South].[E]}")
            .value("SetToStr([Geo].[All Geo].[North].Level.AllMembers)", "{[Geo].[All Geo].[North], [Geo].[All Geo].[South]}")

            .dependsOn("[Geo].AllMembers")
            .dependsOn("[Geo].[All Geo].[North].Level.AllMembers")

            .resultStyle("[Geo].AllMembers", ResultStyle.MUTABLE_LIST, ResultStyle.MUTABLE_LIST)
            .resultStyle("[Geo].AllMembers", ResultStyle.ITERABLE, ResultStyle.MUTABLE_LIST)
            .independentMutableList("[Geo].AllMembers")
            .independentMutableList("[Geo].[All Geo].[North].Level.AllMembers")
            .build();
}
