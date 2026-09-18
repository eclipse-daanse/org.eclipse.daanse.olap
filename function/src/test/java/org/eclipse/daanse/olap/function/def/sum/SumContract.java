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
package org.eclipse.daanse.olap.function.def.sum;

import static org.eclipse.daanse.olap.api.DataType.MEMBER;
import static org.eclipse.daanse.olap.api.DataType.NUMERIC;
import static org.eclipse.daanse.olap.api.DataType.SET;
import static org.eclipse.daanse.olap.testkit.function.eval.CalcAssertions.assertThatScalarExpr;

import org.eclipse.daanse.olap.testkit.function.FunctionContract;
import org.eclipse.daanse.olap.testkit.function.FunctionContract.Promise;

/** The contract of the MDX function {@code Sum}. */
public final class SumContract {

    private SumContract() {
    }

    public static final FunctionContract CONTRACT = FunctionContract.of("Sum")
            .signatures("<Numeric Expression> Sum(<Set>, <Numeric Expression>)")
            .returns(NUMERIC)
            .arity(1, 2)

            .resolvesTo(SumFunDef.class, SET)
            .resolvesTo(SumFunDef.class, SET, NUMERIC)
            .resolvesWithCost(3, SumFunDef.class, SET, MEMBER)   // Member -> Numeric, cost 3
            .rejects(NUMERIC)
            .rejects()

            .autoEdgeCases()
            .edgeCaseMdx("empty set",              "Sum({})")
            .edgeCaseMdx("empty set with measure", "Sum({}, [Measures].[Amount])")
            .edgeCaseMdx("value expression zero",       "Sum([Geo].Members, 0)")
            .edgeCaseMdx("value expression negative",   "Sum([Geo].Members, -1)")
            .edgeCaseMdx("value expression fractional", "Sum([Geo].Members, 0.5)")
            .edgeCaseMdx("value expression 1E308",      "Sum([Geo].Members, 1E308)")
            .edgeCaseMdx("null value expression",  "Sum([Geo].Members, NULL)")

            // MDX distinguishes Sum({}) = NULL from Sum({0}) = 0.
            .value("Sum({[Geo].[All Geo].[North], [Geo].[All Geo].[South]}, [Measures].[Amount])", "150")
            // A constant value expression is evaluated once per member of the set: [Geo]
            // has 3 members (All, F, M — hasAll=true), so three times a constant -1 sums to -3.
            .value("Sum([Geo].Members, -1)", "-8")
            .valueIsNull("Sum({})")
            .valueIsNull("Sum({}, [Measures].[Amount])")

            // The heart of Sum: the set's hierarchy is bound (excluded via
            // checkAnyDependsButFirst), and the literal-member value expression fixes its own
            // hierarchy (see MinusContract) — so the call depends on neither.
            .scalarDoesNotDependOn("Sum([Geo].Members, [Measures].[Amount])",
                             "[Geo].[Region]", "[Measures]")
            // With the implicit current measure, the value expression is a
            // CurrentValueUnknownCalc (depends on everything, Measures included), so only the
            // set's own hierarchy is excluded.
            .scalarDoesNotDependOn("Sum([Geo].Members)", "[Geo].[Region]")

            .waive(Promise.RESULT_SHAPE,
                    "returns a scalar; the set argument's result shape is Aggregate's business")
            .build();
}