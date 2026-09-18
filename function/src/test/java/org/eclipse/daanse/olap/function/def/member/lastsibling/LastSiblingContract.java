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
package org.eclipse.daanse.olap.function.def.member.lastsibling;

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
 * The contract of the MDX property {@code LastSibling}: {@code <Member>.LastSibling} — returns
 * the last child of the member's parent. Same shape as {@link FirstSiblingContract}: a single
 * {@code ParametersCheckingFunctionDefinitionResolver} over one {@code MEMBER} parameter,
 * {@code resolve()} delegating entirely to the generic {@code FunctionMetaDataMatcher.match},
 * no hand-written resolver code to diverge or throw.
 *
 * <p>{@code LastSiblingCalc}'s three branches mirror {@code FirstSiblingCalc}'s exactly (root
 * member uses {@code getHierarchyRootMembers}; non-root uses {@code getMemberChildren(parent)};
 * the hierarchy's own null member short-circuits and returns itself), indexing
 * {@code children.get(children.size() - 1)} instead of {@code children.get(0)} — same
 * never-empty invariant applies, since the member being queried is always itself one of the
 * children/root-members it looks up. Fixed a copy-paste-only naming glitch: the private helper
 * that computes the LAST sibling was still named {@code firstSibling} (evidently copied from
 * {@code FirstSiblingCalc}); its logic was already correct, only the identifier was misleading
 * — renamed to {@code lastSibling}, no behavior change.
 */
public final class LastSiblingContract {

    private LastSiblingContract() {
    }

    public static final FunctionContract CONTRACT = FunctionContract.of("LastSibling")
            .atom(PlainPropertyOperationAtom.class)
            .signatures("<Member>.LastSibling")
            .returns(MEMBER)
            .arity(1, 1)

            .resolvesTo(LastSiblingFunDef.class, MEMBER)
            .resolvesWithCost(1, LastSiblingFunDef.class, HIERARCHY)   // Hierarchy -> Member
            .resolvesWithCost(2, LastSiblingFunDef.class, DIMENSION)   // Dimension -> Member
            .rejects(LEVEL)     // Level converts to Hierarchy/Set/Dimension, never Member
            .rejects(SET)
            .rejects(TUPLE)
            .rejects(NUMERIC)
            .rejects(STRING)
            .rejects()                     // arity 0
            .rejects(MEMBER, MEMBER)       // arity 2

            .autoEdgeCases()
            .edgeCaseMdx("leaf member with siblings", "[Geo].[All Geo].[North].LastSibling")
            .edgeCaseMdx("root member (All)",          "[Geo].[All Geo].[North].Parent.LastSibling")
            .edgeCaseMdx("null member",                "[Geo].[All Geo].[North].Parent.Parent.LastSibling")

            // [Geo] is a one-level hierarchy under an All member: F is index 0, M is index 1
            // (same ordering FirstSiblingContract relies on).
            .value("([Geo].[All Geo].[North].LastSibling IS [Geo].[All Geo].[South])", "true")
            .value("([Geo].[All Geo].[South].LastSibling IS [Geo].[All Geo].[South])", "true")
            .value("([Geo].[All Geo].[North].Parent.LastSibling IS [Geo].[All Geo].[North].Parent)", "true")
            .value("([Geo].[All Geo].[North].Parent.Parent.LastSibling IS [Geo].[All Geo].[North].Parent.Parent)", "true")

            .dependsOn("[Geo].[All Geo].[North].LastSibling")
            .dependsOn("[Geo].[All Geo].[North].Parent.LastSibling")

            // A scalar call has no set to shape. That it answers VALUE was asserted in
            // prose here and never checked, so it is a case now.
            .scalarResultStyle("([Geo].[All Geo].[North].LastSibling IS [Geo].[All Geo].[South])")

            .build();
}
