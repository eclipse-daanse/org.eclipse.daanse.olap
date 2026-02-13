/*
 * Copyright (c) 2023 Contributors to the Eclipse Foundation.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * 2002-2017 Hitachi Vantara.
 *
 * Contributors after Fork in 2023:
 *   SmartCity Jena - initial
 */
package org.eclipse.daanse.olap.calc.base.compiler;

import java.util.List;

import org.eclipse.daanse.olap.api.calc.Calc;
import org.eclipse.daanse.olap.api.element.Member;
import org.eclipse.daanse.olap.api.evaluator.Evaluator;

/**
 * Utility class for optimizing evaluator context during expression compilation.
 */
public class EvaluatorSimplifier {

    private EvaluatorSimplifier() {
        // Utility class - prevent instantiation
    }

    /**
     * Returns a simplified evaluator whose context is the same for every hierarchy
     * which an expression depends on, and the default member for every hierarchy
     * which it does not depend on.
     *
     * <p>The default member is often the 'all' member, so this evaluator is usually
     * the most efficient context in which to evaluate the expression.</p>
     *
     * <p>If NON EMPTY is present, the evaluator cannot be simplified because
     * the expression must be assumed to depend on everything.</p>
     *
     * @param calc the calculation whose dependencies determine which hierarchies can be simplified
     * @param evaluator the evaluator to potentially simplify
     * @return a simplified evaluator, or the original evaluator if no simplification is possible
     */
    public static Evaluator simplifyEvaluator(Calc<?> calc, Evaluator evaluator) {
        if (evaluator.isNonEmpty()) {
            return evaluator;
        }

        List<Member> membersToReset = evaluator.getCube().getHierarchies().stream()
                .filter(hierarchy -> !calc.dependsOn(hierarchy))
                .map(hierarchy -> new MemberContext(evaluator.getContext(hierarchy), hierarchy.getDefaultMember()))
                .filter(ctx -> !ctx.current().isAll())
                .filter(ctx -> ctx.current() != ctx.defaultMember())
                .map(MemberContext::defaultMember)
                .toList();

        if (membersToReset.isEmpty()) {
            return evaluator;
        }

        Evaluator simplifiedEvaluator = evaluator.push();
        membersToReset.forEach(simplifiedEvaluator::setContext);
        return simplifiedEvaluator;
    }

    /**
     * Helper record to hold current and default member context during stream processing.
     */
    private record MemberContext(Member current, Member defaultMember) {}
}
