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
package org.eclipse.daanse.olap.function.def.member.firstchild;

import org.eclipse.daanse.olap.testkit.function.FunctionContract;
import static org.eclipse.daanse.olap.api.DataType.DIMENSION;
import static org.eclipse.daanse.olap.api.DataType.HIERARCHY;
import static org.eclipse.daanse.olap.api.DataType.LEVEL;
import static org.eclipse.daanse.olap.api.DataType.MEMBER;
import static org.eclipse.daanse.olap.api.DataType.NUMERIC;
import static org.eclipse.daanse.olap.api.DataType.SET;
import static org.eclipse.daanse.olap.api.DataType.STRING;
import static org.eclipse.daanse.olap.api.DataType.TUPLE;

import org.eclipse.daanse.mdx.model.api.expression.operation.PlainPropertyOperationAtom;
import org.eclipse.daanse.olap.testkit.function.FunctionContract.Promise;

/**
 * The contract of the MDX property {@code FirstChild}: {@code <Member>.FirstChild}. A single
 * {@code ParametersCheckingFunctionDefinitionResolver} over one {@code MEMBER} parameter —
 * {@code resolve()} delegates entirely to the generic {@code FunctionMetaDataMatcher.match}, so
 * there is no hand-written resolver code to diverge from the declared signature or throw
 * instead of returning empty. {@code FirstChildCalc.firstChild} falls back to {@code
 * member.getHierarchy().getNullMember()} when the member has no children — mirrors {@code
 * ChildrenFunDef}'s own {@code <Member>.Children} shape exactly (same atom family, same
 * MEMBER/HIERARCHY/DIMENSION cost ladder), only returning the first element instead of the
 * whole set.
 */
public final class FirstChildContract {

    private FirstChildContract() {
    }

    public static final FunctionContract CONTRACT = FunctionContract.of("FirstChild")
            .atom(PlainPropertyOperationAtom.class)
            .signatures("<Member>.FirstChild")
            .returns(MEMBER)
            .arity(1, 1)

            .resolvesTo(FirstChildFunDef.class, MEMBER)
            .resolvesWithCost(1, FirstChildFunDef.class, HIERARCHY)   // Hierarchy -> Member
            .resolvesWithCost(2, FirstChildFunDef.class, DIMENSION)   // Dimension -> Member
            .rejects(LEVEL)     // Level converts to Hierarchy/Set/Dimension, never Member
            .rejects(SET)
            .rejects(TUPLE)
            .rejects(NUMERIC)
            .rejects(STRING)
            .rejects()                     // arity 0
            .rejects(MEMBER, MEMBER)       // arity 2

            .autoEdgeCases()
            .edgeCaseMdx("leaf member",         "[Geo].[All Geo].[North].FirstChild")
            .edgeCaseMdx("member with children", "[Geo].[All Geo].[North].Parent.FirstChild")
            .edgeCaseMdx("null member",          "[Geo].[All Geo].[North].Parent.Parent.FirstChild")

            // [Geo] is a one-level hierarchy under an All member: a leaf has no children
            // (falls back to the null-member sentinel), and the All member's first child is
            // the established [Geo].[All Geo].[North] (index 0 — same ordering HeadContract/ItemContract
            // rely on).
            .value("([Geo].[All Geo].[North].FirstChild IS [Geo].[All Geo].[North].Parent.Parent)", "false")
            .value("([Geo].[All Geo].[North].Parent.FirstChild IS [Geo].[All Geo].[North])", "true")

            .dependsOn("[Geo].[All Geo].[North].FirstChild")
            .dependsOn("[Geo].[All Geo].[North].Parent.FirstChild")

            // A scalar call has no set to shape. That it answers VALUE was asserted in
            // prose here and never checked, so it is a case now.
            .scalarResultStyle("([Geo].[All Geo].[North].FirstChild IS [Geo].[All Geo].[North].Parent.Parent)")

            .build();
}
