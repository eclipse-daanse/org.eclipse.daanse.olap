/*
 * Copyright (c) 2026 Contributors to the Eclipse Foundation.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   SmartCity Jena - initial
 *   Stefan Bischof (bipolis.org) - initial
 */
package org.eclipse.daanse.olap.api.element;

import java.util.List;

import org.eclipse.daanse.olap.api.evaluator.Evaluator;
import org.eclipse.daanse.olap.api.query.component.Expression;

/**
 * A member the {@code VisualTotals} function puts in place of a real member.
 * <p>
 * It stands for the member it wraps ({@link #getMember()}) but its value is
 * the aggregate of the members that follow it in the set, so a total shows
 * only what is visible. Providers create it through
 * {@link Hierarchy#createVisualTotalMember(Member, String, String, Expression)}
 * because the engine's evaluator may require a provider-specific member type.
 * <p>
 * A visual total member compares equal to the member it wraps, so set
 * operations such as {@code Intersect} match the two.
 */
public interface VisualTotalMember extends Member {

    /** The real member this visual total stands for. */
    Member getMember();

    /** The aggregate expression; the calculation behind this member's value. */
    @Override
    Expression getExpression();

    /** Replaces the aggregate expression. */
    void setExpression(Expression exp);

    /**
     * Rebuilds the aggregate expression over {@code childMembers} and validates it
     * against the evaluator's query.
     */
    void setExpression(Evaluator evaluator, List<Member> childMembers);
}
