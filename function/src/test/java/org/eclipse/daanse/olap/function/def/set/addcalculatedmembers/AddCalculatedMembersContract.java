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
package org.eclipse.daanse.olap.function.def.set.addcalculatedmembers;

import org.eclipse.daanse.olap.testkit.function.FunctionContract;
import static org.eclipse.daanse.olap.api.DataType.MEMBER;
import static org.eclipse.daanse.olap.api.DataType.NUMERIC;
import static org.eclipse.daanse.olap.api.DataType.SET;

import org.eclipse.daanse.olap.api.calc.ResultStyle;
import org.eclipse.daanse.olap.testkit.function.FunctionContract.Promise;

/** The contract of the MDX function {@code AddCalculatedMembers}. */
public final class AddCalculatedMembersContract {

    private AddCalculatedMembersContract() {
    }

    public static final FunctionContract CONTRACT = FunctionContract.of("AddCalculatedMembers")
            .signatures("<Set> AddCalculatedMembers(<Set>)")
            .returns(SET)
            .arity(1, 1)

            // AddCalculatedMembersResolver.checkExpressions requires the argument to be a
            // literal Set of single-hierarchy members: a Member argument type-matches the
            // declared <Set> parameter (a member converts to a singleton set) but is still
            // rejected because the raw expression itself is not a SetType.
            .resolvesTo(AddCalculatedMembersFunDef.class, SET)
            .rejects(MEMBER)
            .rejects(NUMERIC)
            .rejects()                             // arity 0
            .rejects(SET, SET)                     // arity 2

            .autoEdgeCases()
            .edgeCaseMdx("empty set",            "AddCalculatedMembers({})")
            .edgeCaseMdx("no calculated members", "AddCalculatedMembers([Geo].Members)")

            // [Geo] has no calculated members in the Sales cube: a pure pass-through.
            .value("SetToStr(AddCalculatedMembers([Geo].Members))", "{[Geo].[All Geo], [Geo].[All Geo].[North], "
                            + "[Geo].[All Geo].[North].[A], [Geo].[All Geo].[North].[B], "
                            + "[Geo].[All Geo].[South], [Geo].[All Geo].[South].[C], "
                            + "[Geo].[All Geo].[South].[D], [Geo].[All Geo].[South].[E]}")
            // AddCalculatedMembers only ever appends; it never removes a member that was
            // already in the set — true regardless of how many calculated members exist.
            .value("Count(AddCalculatedMembers([Measures].Members)) >= Count([Measures].Members)", "true")

            .dependsOn("AddCalculatedMembers([Geo].Members)")
            .dependsOn("AddCalculatedMembers([Measures].Members)")

            .resultStyle("AddCalculatedMembers([Geo].Members)", ResultStyle.MUTABLE_LIST, ResultStyle.MUTABLE_LIST)
            .resultStyle("AddCalculatedMembers([Geo].Members)", ResultStyle.ITERABLE, ResultStyle.MUTABLE_LIST)
            .independentMutableList("AddCalculatedMembers([Geo].Members)")

            // AddCalculatedMembersResolver.checkExpressions requires the *raw* argument
            // expression to already be a SetType; it does not honor the implicit Member/Level
            // -> Set conversion that FunctionMetaDataMatcher applies when matching the
            // declared <Set> parameter. So the generic accepted-calls probe (which drives
            // every overload it can type-match, including converted ones) finds Member/Level/
            // Tuple calls the matcher accepts but the resolver deliberately does not. That is
            // the resolver narrowing its own declaration on purpose, not an undocumented gap.
            .waive(Promise.SIGNATURE,
                    "checkExpressions requires a literal SetType argument; FunctionMetaDataMatcher's "
                            + "generic probe still matches Member/Level/Tuple against <Set> via implicit "
                            + "conversion, which the resolver deliberately rejects (see rejects(MEMBER) above)")
            .build();
}
