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
package org.eclipse.daanse.olap.function.def.hierarchize;

import org.eclipse.daanse.olap.testkit.function.FunctionContract.Promise;
import org.eclipse.daanse.olap.testkit.function.FunctionContract;
import static org.eclipse.daanse.olap.api.DataType.LEVEL;
import static org.eclipse.daanse.olap.api.DataType.MEMBER;
import static org.eclipse.daanse.olap.api.DataType.NUMERIC;
import static org.eclipse.daanse.olap.api.DataType.SET;
import static org.eclipse.daanse.olap.api.DataType.STRING;
import static org.eclipse.daanse.olap.api.DataType.SYMBOL;

import org.eclipse.daanse.olap.api.calc.ResultStyle;

/**
 * The contract of the MDX function {@code Hierarchize}. One overload, {@code (Set,
 * PrePost?)}. Unlike most of the two/three-argument SYMBOL-flag functions contracted so far
 * (compare {@link DescendantsContract}, {@link DrilldownMemberContract}), {@code
 * HierarchizeResolver} correctly overrides {@code getReservedWords()} with its own "PRE" and
 * "POST" — no borrowed cross-function coincidence needed for the flag to parse.
 */
public final class HierarchizeContract {

    private HierarchizeContract() {
    }

    public static final FunctionContract CONTRACT = FunctionContract.of("Hierarchize")
            .signatures("<Set> Hierarchize(<Set>, <Symbol>)")
            .returns(SET)
            .arity(1, 2)
            .reservedWords("PRE", "POST")

            .resolvesTo(HierarchizeFunDef.class, SET)
            .resolvesTo(HierarchizeFunDef.class, SET, SYMBOL)
            .resolvesWithCost(2, HierarchizeFunDef.class, MEMBER)   // Member -> Set
            .resolvesWithCost(1, HierarchizeFunDef.class, LEVEL)    // Level -> Set
            .rejects(NUMERIC)          // arity 1, not Set-convertible
            .rejects(SET, STRING)      // String does not convert to Symbol
            .rejects()                 // arity 0
            .rejects(SET, SYMBOL, SYMBOL)   // arity 3

            .autoEdgeCases()
            .edgeCaseMdx("empty set",   "Hierarchize({})")
            .edgeCaseMdx("default PRE", "Hierarchize([Geo].Members)")
            .edgeCaseMdx("explicit PRE",  "Hierarchize([Geo].Members, PRE)")
            .edgeCaseMdx("explicit POST", "Hierarchize([Geo].Members, POST)")
            // "ALL" is not one of Hierarchize's own PRE/POST words, but it is reserved
            // globally (ExceptResolver registers it — see DescendantsContract's matching
            // note), so it still parses as a SYMBOL literal here. FunUtil.getLiteralArg then
            // throws a diagnosed DaanseEvaluationException("Allowed values are: {PRE, POST}")
            // for the mismatch.
            .edgeCaseMdx("symbol reserved by another function", "Hierarchize([Geo].Members, ALL)")

            // [Geo] is flat (F and M are siblings under (All)), so PRE vs POST does not
            // visibly reorder [Geo].Members itself; pairing a member with its own Parent
            // shows the difference without needing to know the All member's schema-specific
            // name — the IIf compares names inside the MDX itself.
            .value("Count(Hierarchize([Geo].Members))", "8")
            .value("IIf(Hierarchize({[Geo].[All Geo].[North], [Geo].[All Geo].[North].Parent}, PRE).Item(0).Name = "
                   + "[Geo].[All Geo].[North].Parent.Name, \"YES\", \"NO\")", "YES")
            .value("IIf(Hierarchize({[Geo].[All Geo].[North], [Geo].[All Geo].[North].Parent}, POST).Item(0).Name = "
                   + "[Geo].[All Geo].[North].Parent.Name, \"YES\", \"NO\")", "NO")
            .value("IIf(Hierarchize({[Geo].[All Geo].[North], [Geo].[All Geo].[North].Parent}).Item(0).Name = "
                   + "[Geo].[All Geo].[North].Parent.Name, \"YES\", \"NO\")", "YES")   // default is PRE

            .dependsOn("Hierarchize([Geo].Members)")
            .dependsOn("Hierarchize([Geo].Members, POST)")

            .resultStyle("Hierarchize([Geo].Members)", ResultStyle.MUTABLE_LIST, ResultStyle.MUTABLE_LIST)
            .resultStyle("Hierarchize([Geo].Members)", ResultStyle.ITERABLE, ResultStyle.MUTABLE_LIST)
            .independentMutableList("Hierarchize([Geo].Members)")

            .build();
}
