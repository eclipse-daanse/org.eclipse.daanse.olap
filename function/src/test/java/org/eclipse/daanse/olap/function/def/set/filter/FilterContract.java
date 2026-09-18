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
package org.eclipse.daanse.olap.function.def.set.filter;

import org.eclipse.daanse.olap.testkit.function.FunctionContract.Promise;
import org.eclipse.daanse.olap.testkit.function.FunctionContract;
import static org.eclipse.daanse.olap.api.DataType.LEVEL;
import static org.eclipse.daanse.olap.api.DataType.LOGICAL;
import static org.eclipse.daanse.olap.api.DataType.MEMBER;
import static org.eclipse.daanse.olap.api.DataType.NUMERIC;
import static org.eclipse.daanse.olap.api.DataType.SET;
import static org.eclipse.daanse.olap.api.DataType.STRING;

import org.eclipse.daanse.olap.api.calc.ResultStyle;

/**
 * The contract of the MDX function {@code Filter}. One overload, {@code (Set, Logical
 * Expression)}. Unlike most of the functions contracted so far, {@code FilterFunDef}
 * genuinely branches on {@code ExpressionCompiler.getAcceptableResultStyles()} — it picks
 * between an iterable-producing and a list-producing compiled form, and among several Calc
 * subclasses depending on what the Set child itself returns — rather than always compiling
 * one fixed shape and relying on the compiler to wrap it.
 *
 * <p>{@code BaseListFilterCalc}/{@code BaseIterFilterCalc.dependsOn} use {@code
 * HierarchyDependsChecker.checkAnyDependsButFirst}: the Set argument's own hierarchy is
 * excluded, exactly like {@link DrilldownLevelBottomContract}. The Logical Expression
 * argument's own dependencies are not excluded, though — a condition that references {@code
 * CurrentMember} of the very hierarchy being filtered still reports it, since that reference
 * lives in the second child Calc, not the first.
 */
public final class FilterContract {

    private FilterContract() {
    }

    public static final FunctionContract CONTRACT = FunctionContract.of("Filter")
            .signatures("<Set> Filter(<Set>, <Logical Expression>)")
            .returns(SET)
            .arity(2, 2)

            .resolvesTo(FilterFunDef.class, SET, LOGICAL)
            .resolvesWithCost(2, FilterFunDef.class, MEMBER, LOGICAL)   // Member -> Set
            .resolvesWithCost(1, FilterFunDef.class, LEVEL, LOGICAL)    // Level -> Set
            .resolvesWithCost(2, FilterFunDef.class, SET, NUMERIC)      // Numeric -> Logical
            .rejects(SET)               // arity 1
            .rejects()                  // arity 0
            .rejects(SET, STRING)       // String does not convert to Logical
            .rejects(SET, SET)          // Set does not convert to Logical
            .rejects(SET, LOGICAL, LOGICAL)   // arity 3

            .autoEdgeCases()
            .edgeCaseMdx("empty set",               "Filter({}, 1 = 1)")
            .edgeCaseMdx("condition always true",    "Filter([Geo].Members, 1 = 1)")
            .edgeCaseMdx("condition always false",   "Filter([Geo].Members, 1 = 0)")
            .edgeCaseMdx("condition on CurrentMember", "Filter([Geo].Members, [Geo].CurrentMember IS [Geo].[All Geo].[North])")
            .edgeCaseMdx("named-set alias forces iterable", "Filter([Geo].Members AS t, 1 = 1)")

            .value("Count(Filter([Geo].Members, 1 = 1))", "8")
            .value("Count(Filter([Geo].Members, 1 = 0))", "0")
            .value("SetToStr(Filter([Geo].Members, [Geo].CurrentMember IS [Geo].[All Geo].[North]))", "{[Geo].[All Geo].[North]}")
            .value("Count(Filter({}, 1 = 1))", "0")

            // The Set argument's own hierarchy is excluded from the reported dependencies
            // (see the class Javadoc). The predicate's own [Measures].[Amount] literal
            // is coerced to a scalar via the MemberValueCalc-style wrapper (see
            // MinusContract/FormatContract), which fixes its own hierarchy but depends on
            // every other one — combined with the Set-hierarchy exclusion, the call depends
            // on neither Gender nor Measures (verified against a real connection).
            .doesNotDependOn("Filter([Geo].Members, [Measures].[Amount] > 1000)",
                    "[Geo].[Region]", "[Measures]")
            // A condition that reads the filtered hierarchy's CurrentMember is still bound to
            // the iteration itself (the predicate is evaluated once per set element, shadowing
            // the outer context), so it does not add an outer dependency either — the whole
            // call depends on nothing.
            .dependsOn("Filter([Geo].Members, [Geo].CurrentMember IS [Geo].[All Geo].[North])")

            .resultStyle("Filter([Geo].Members, 1 = 1)", ResultStyle.MUTABLE_LIST, ResultStyle.MUTABLE_LIST)
            .resultStyle("Filter([Geo].Members, 1 = 1)", ResultStyle.ITERABLE, ResultStyle.ITERABLE)
            .independentMutableList("Filter([Geo].Members, 1 = 1)")

            .build();
}
