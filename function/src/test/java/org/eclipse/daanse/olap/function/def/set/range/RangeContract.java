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
package org.eclipse.daanse.olap.function.def.set.range;

import org.eclipse.daanse.olap.testkit.function.FunctionContract.Promise;
import org.eclipse.daanse.olap.testkit.function.FunctionContract;
import static org.eclipse.daanse.olap.api.DataType.DIMENSION;
import static org.eclipse.daanse.olap.api.DataType.HIERARCHY;
import static org.eclipse.daanse.olap.api.DataType.LEVEL;
import static org.eclipse.daanse.olap.api.DataType.MEMBER;
import static org.eclipse.daanse.olap.api.DataType.NUMERIC;
import static org.eclipse.daanse.olap.api.DataType.SET;

import org.eclipse.daanse.mdx.model.api.expression.operation.InfixOperationAtom;
import org.eclipse.daanse.olap.api.calc.ResultStyle;

/** The contract of the MDX operator {@code :} (Range). */
public final class RangeContract {

    private RangeContract() {
    }

    public static final FunctionContract CONTRACT = FunctionContract.of(":")
            .atom(InfixOperationAtom.class)
            .signatures("<Member> : <Member>")
            .returns(SET)
            .arity(2, 2)

            .resolvesTo(RangeFunDef.class, MEMBER, MEMBER)
            .resolvesWithCost(1, RangeFunDef.class, HIERARCHY, MEMBER)   // Hierarchy -> Member
            .resolvesWithCost(2, RangeFunDef.class, DIMENSION, MEMBER)   // Dimension -> Member
            .rejects(LEVEL, MEMBER)    // Level converts to Hierarchy/Set/Dimension, never Member
            .rejects(SET, MEMBER)
            .rejects(NUMERIC, MEMBER)
            .rejects()                     // arity 0
            .rejects(MEMBER, MEMBER, MEMBER)   // arity 3

            .autoEdgeCases()
            .edgeCaseMdx("same member",             "[Geo].[All Geo].[North] : [Geo].[All Geo].[North]")
            .edgeCaseMdx("reversed order",           "[Geo].[All Geo].[South] : [Geo].[All Geo].[North]")
            .edgeCaseMdx("different levels",         "[Geo].[All Geo].[North] : [Measures].[Amount]")
            .edgeCaseMdx("one side runtime null",    "[Geo].[All Geo].[North].PrevMember : [Geo].[All Geo].[South]")
            .edgeCaseMdx("one side compile-time NULL", "NULL : [Geo].[All Geo].[North]")
            // RangeFunDef.compileMembers throws a diagnosed OlapRuntimeException
            // ("Function does not support two NULL member parameters") at compile time.
            .edgeCaseMdx("both sides NULL",          "NULL : NULL")

            .value("Count([Geo].[All Geo].[North] : [Geo].[All Geo].[South])",   "2")
            .value("SetToStr([Geo].[All Geo].[North] : [Geo].[All Geo].[North])", "{[Geo].[All Geo].[North]}")
            // RangeCalc.evaluateInternal returns {} whenever either endpoint is the null member,
            // whether that null came in as a compile-time NULL literal or a runtime .PrevMember.
            .value("Count([Geo].[All Geo].[North].PrevMember : [Geo].[All Geo].[South])", "0")
            .valueKnownDefect("Count(NULL : [Geo].[All Geo].[North])", "0",
                            "a range with a NULL endpoint is refused during validation,"
                            + " because no type can be deduced for the call, so the expression"
                            + " has no value to compare. The refusal is diagnosed now rather"
                            + " than an IllegalArgumentException, which is why the edge case"
                            + " passes; whether MDX ought to answer the empty set here instead"
                            + " of refusing is a separate question")

            .dependsOn("[Geo].[All Geo].[North] : [Geo].[All Geo].[South]")
            .dependsOn("[Geo].[All Geo].[North] : [Geo].CurrentMember", "[Geo]")

            .resultStyle("[Geo].[All Geo].[North] : [Geo].[All Geo].[South]", ResultStyle.MUTABLE_LIST, ResultStyle.MUTABLE_LIST)
            .resultStyle("[Geo].[All Geo].[North] : [Geo].[All Geo].[South]", ResultStyle.ITERABLE, ResultStyle.MUTABLE_LIST)
            .independentMutableList("[Geo].[All Geo].[North] : [Geo].[All Geo].[South]")
            .build();
}
