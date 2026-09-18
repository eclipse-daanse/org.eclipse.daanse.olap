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
package org.eclipse.daanse.olap.function.def.headtail;

import org.eclipse.daanse.olap.testkit.function.FunctionContract.Promise;
import org.eclipse.daanse.olap.testkit.function.FunctionContract;
import static org.eclipse.daanse.olap.api.DataType.INTEGER;
import static org.eclipse.daanse.olap.api.DataType.MEMBER;
import static org.eclipse.daanse.olap.api.DataType.NUMERIC;
import static org.eclipse.daanse.olap.api.DataType.SET;
import static org.eclipse.daanse.olap.testkit.function.FunctionAssertions.assertThatFunction;

import org.eclipse.daanse.olap.api.calc.ResultStyle;


/** The contract of the MDX function {@code Head}. */
public final class HeadContract {

    private HeadContract() {
    }

    public static final FunctionContract CONTRACT = FunctionContract.of("Head")
            .signatures("<Set> Head(<Set>, <Numeric Expression>)")
            .returns(SET)
            .arity(1, 2)                          // the second parameter is .asOptional()

            .resolvesTo(HeadTailFunDef.class, SET)
            .resolvesTo(HeadTailFunDef.class, SET, NUMERIC)
            .resolvesTo(HeadTailFunDef.class, SET, INTEGER)
            .resolvesWithCost(2, HeadTailFunDef.class, MEMBER, NUMERIC)   // Member -> Set
            .rejects(NUMERIC, SET)
            .rejects(SET, NUMERIC, NUMERIC)
            .rejects()

            .autoEdgeCases()
            .edgeCaseMdx("empty set",           "Head({}, 2)")
            .edgeCaseMdx("empty set, no count", "Head({})")
            .edgeCaseMdx("count zero",          "Head([Geo].Members, 0)")
            .edgeCaseMdx("count one",           "Head([Geo].Members, 1)")
            .edgeCaseMdx("count negative",      "Head([Geo].Members, -1)")
            .edgeCaseMdx("count beyond end",    "Head([Geo].Members, 1000)")
            .edgeCaseMdx("count MAX_VALUE",     "Head([Geo].Members, 2147483647)")
            .edgeCaseMdx("count MIN_VALUE",     "Head([Geo].Members, -2147483648)")
            // A null count is treated like count <= 0 (empty result) — HeadCalc.head checks
            // "count == null || count <= 0" before ever unboxing count.
            .edgeCaseMdx("count NULL",          "Head([Geo].Members, NULL)")


            .value("SetToStr(Head([Geo].Members, 1))", "{[Geo].[All Geo]}")
            .value("Count(Head([Geo].Members, 0))",    "0")
            .value("Count(Head({}, 5))",                  "0")
            .value("Count(Head([Geo].Members, 1000))", "8")
            .value("Count(Head([Geo].Members, NULL))", "0")


            // A level's members do not move with the current member of their own hierarchy,
            // and a literal count depends on nothing at all.
            .dependsOn("Head([Geo].Members, 2)")
            // The measure argument is coerced to a scalar, and the wrapper that does that
            // fixes its own hierarchy while depending on every other one. Which "every
            // other" is depends on the catalogue, so only the fixed one is pinned here.
            .doesNotDependOn("Head([Geo].Members, [Measures].[Amount])", "[Measures]")

            .resultStyle("Head([Geo].Members, 2)", ResultStyle.MUTABLE_LIST, ResultStyle.MUTABLE_LIST)
            .resultStyle("Head([Geo].Members, 2)", ResultStyle.ITERABLE, ResultStyle.MUTABLE_LIST)
            .independentMutableList("Head([Geo].Members, 2)")
            .independentMutableList("Head(Order([Geo].Members, [Measures].[Amount]), 1)")
            .build();
}