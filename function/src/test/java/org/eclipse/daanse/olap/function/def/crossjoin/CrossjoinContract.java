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
package org.eclipse.daanse.olap.function.def.crossjoin;

import org.eclipse.daanse.olap.testkit.function.FunctionContract.Promise;
import org.eclipse.daanse.olap.testkit.function.FunctionContract;
import static org.eclipse.daanse.olap.api.DataType.DIMENSION;
import static org.eclipse.daanse.olap.api.DataType.LEVEL;
import static org.eclipse.daanse.olap.api.DataType.MEMBER;
import static org.eclipse.daanse.olap.api.DataType.NUMERIC;
import static org.eclipse.daanse.olap.api.DataType.SET;
import static org.eclipse.daanse.olap.api.DataType.TUPLE;

import org.eclipse.daanse.olap.api.calc.ResultStyle;

/**
 * The contract of the MDX function {@code Crossjoin}. Unlike the {@code "*"} operator (see
 * {@link StarContract}), {@code Crossjoin} has exactly one resolver ({@code CrossJoinResolver})
 * and every argument is independently coerced to {@code <Set>} — a Member, Tuple or Level
 * argument is accepted and implicitly wrapped in braces, matching what
 * {@code CrossJoinResolver.resolve()} and the declared {@code Set1, Set2, ...} overload both
 * accept.
 */
public final class CrossjoinContract {

    private CrossjoinContract() {
    }

    public static final FunctionContract CONTRACT = FunctionContract.of("Crossjoin")
            .signatures("<Set> Crossjoin(<Set>, <Set>)")
            .returns(SET)
            .arity(2, Integer.MAX_VALUE)

            .resolvesTo(CrossJoinFunDef.class, SET, SET)
            .resolvesTo(CrossJoinFunDef.class, SET, SET, SET)
            .resolvesWithCost(2, CrossJoinFunDef.class, MEMBER, SET)   // Member -> Set, cost 2
            .resolvesWithCost(2, CrossJoinFunDef.class, SET, TUPLE)    // Tuple -> Set, cost 2
            .resolvesWithCost(1, CrossJoinFunDef.class, LEVEL, SET)    // Level -> Set, cost 1
            .resolvesWithCost(4, CrossJoinFunDef.class, MEMBER, MEMBER)   // Member -> Set twice, cost 2 + 2
            .rejects(DIMENSION, SET)
            .rejects(NUMERIC, SET)
            .rejects(SET)
            .rejects()

            .autoEdgeCases()
            .edgeCaseMdx("first set empty",     "Crossjoin({}, [Geo].Members)")
            .edgeCaseMdx("second set empty",    "Crossjoin([Geo].Members, {})")
            .edgeCaseMdx("both sets empty",     "Crossjoin({}, {})")
            .edgeCaseMdx("member operand",      "Crossjoin([Geo].[All Geo].[North], {[Measures].[Amount]})")
            .edgeCaseMdx("duplicate hierarchy", "Crossjoin([Geo].Members, [Geo].Members)")

            .value("SetToStr(Crossjoin({[Geo].[All Geo].[North]}, {[Measures].[Amount]}))",
                   "{([Geo].[All Geo].[North], [Measures].[Amount])}")
            .value("Count(Crossjoin([Geo].Members, {[Measures].[Amount]}))", "8")
            .value("Count(Crossjoin({}, [Geo].Members))", "0")

            .dependsOn("Crossjoin([Geo].Members, {[Measures].[Amount]})")

            .resultStyle("Crossjoin([Geo].Members, {[Measures].[Amount]})",
                         ResultStyle.MUTABLE_LIST, ResultStyle.MUTABLE_LIST)
            .resultStyle("Crossjoin([Geo].Members, {[Measures].[Amount]})",
                         ResultStyle.ITERABLE, ResultStyle.ITERABLE)
            .independentMutableList("Crossjoin([Geo].Members, {[Measures].[Amount]})")

            .build();
}
