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
package org.eclipse.daanse.olap.function.def.nonemptycrossjoin;

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
 * The contract of the MDX function {@code NonEmptyCrossJoin(<Set1>, <Set2>)} — a two-argument
 * cross join, unlike {@code Crossjoin} (which accepts any number via a repeatable second
 * parameter, see {@link CrossjoinContract}) — {@code NonEmptyCrossJoinResolver} declares
 * exactly two required {@code Set} parameters, so a third argument is rejected outright.
 *
 * <p>{@code NonEmptyCrossJoinFunDef} extends {@code CrossJoinFunDef} (inheriting its {@code
 * getResultType}) but overrides {@code compileCall} to read {@code call.getArg(0)}/{@code (1)}
 * unconditionally — safe here only because the declared signature's fixed arity of 2 is
 * enforced generically by {@code FunctionMetaDataMatcher.match} before {@code compileCall} is
 * ever reached (Stage B, not exercised in this test kit anyway).
 */
public final class NonEmptyCrossJoinContract {

    private NonEmptyCrossJoinContract() {
    }

    public static final FunctionContract CONTRACT = FunctionContract.of("NonEmptyCrossJoin")
            .signatures("<Set> NonEmptyCrossJoin(<Set>, <Set>)")
            .returns(SET)
            .arity(2, 2)

            .resolvesTo(NonEmptyCrossJoinFunDef.class, SET, SET)
            .resolvesWithCost(2, NonEmptyCrossJoinFunDef.class, MEMBER, SET)   // Member -> Set
            .resolvesWithCost(2, NonEmptyCrossJoinFunDef.class, SET, TUPLE)    // Tuple -> Set
            .resolvesWithCost(1, NonEmptyCrossJoinFunDef.class, LEVEL, SET)    // Level -> Set
            .rejects(DIMENSION, SET)
            .rejects(NUMERIC, SET)
            .rejects(SET)                // arity 1
            .rejects()                   // arity 0
            .rejects(SET, SET, SET)      // arity 3

            .autoEdgeCases()
            .edgeCaseMdx("first set empty",     "NonEmptyCrossJoin({}, [Geo].Members)")
            .edgeCaseMdx("second set empty",    "NonEmptyCrossJoin([Geo].Members, {})")
            .edgeCaseMdx("both sets empty",     "NonEmptyCrossJoin({}, {})")
            .edgeCaseMdx("member operand",      "NonEmptyCrossJoin([Geo].[All Geo].[North], {[Measures].[Amount]})")

            .value("Count(NonEmptyCrossJoin([Geo].Members, {[Measures].[Amount]}))", "8")
            .value("Count(NonEmptyCrossJoin({}, [Geo].Members))", "0")

            .doesNotDependOn("NonEmptyCrossJoin([Geo].Members, {[Measures].[Amount]})",
                       "[Geo].[Region]", "[Measures]")

            .resultStyleKnownDefect("NonEmptyCrossJoin([Geo].Members, {[Measures].[Amount]})",
                         ResultStyle.MUTABLE_LIST, ResultStyle.MUTABLE_LIST,
                            "the calc answers the mutability question at construction with no,"
                            + " and so reports LIST however the compiler asks, including for"
                            + " MUTABLE_LIST. It cannot simply answer yes: the ordinary path"
                            + " builds its result with mutableCrossJoin and could honour that,"
                            + " but the native path returns what the native evaluator produces"
                            + " for LIST, and no provider here can try that path")
            // Answering LIST to a request for something merely iterable is fine: a list can
            // be iterated. Only the request above, for a list the caller may modify, is
            // answered with something weaker than asked.
            .resultStyle("NonEmptyCrossJoin([Geo].Members, {[Measures].[Amount]})",
                         ResultStyle.ITERABLE, ResultStyle.LIST)
            .independentMutableListKnownDefect("NonEmptyCrossJoin([Geo].Members, {[Measures].[Amount]})",
                            "the calc answers the mutability question at construction with no,"
                            + " and so reports LIST however the compiler asks, including for"
                            + " MUTABLE_LIST. It cannot simply answer yes: the ordinary path"
                            + " builds its result with mutableCrossJoin and could honour that,"
                            + " but the native path returns what the native evaluator produces"
                            + " for LIST, and no provider here can try that path")

            .build();
}
