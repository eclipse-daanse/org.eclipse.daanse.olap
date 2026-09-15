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
package org.eclipse.daanse.olap.element;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

import org.eclipse.daanse.olap.api.element.Dimension;
import org.eclipse.daanse.olap.api.element.Hierarchy;
import org.eclipse.daanse.olap.api.element.Level;
import org.eclipse.daanse.olap.api.element.Member;
import org.eclipse.daanse.olap.api.element.Member.MemberType;
import org.eclipse.daanse.olap.api.element.VisualTotalMember;
import org.eclipse.daanse.olap.api.query.component.Expression;
import org.eclipse.daanse.olap.api.query.component.UnresolvedFunCall;
import org.eclipse.daanse.olap.common.StandardProperty;
import org.junit.jupiter.api.Test;

class VisualTotalMemberImplTest {

    private static Member member(String uniqueName, int ordinal) {
        Dimension dimension = mock(Dimension.class);
        when(dimension.isMeasures()).thenReturn(false);
        Hierarchy hierarchy = mock(Hierarchy.class);
        when(hierarchy.getDimension()).thenReturn(dimension);
        Level level = mock(Level.class);
        when(level.getHierarchy()).thenReturn(hierarchy);
        when(level.getDepth()).thenReturn(2);
        Member member = mock(Member.class);
        when(member.getLevel()).thenReturn(level);
        when(member.getHierarchy()).thenReturn(hierarchy);
        when(member.getMemberType()).thenReturn(MemberType.REGULAR);
        when(member.getUniqueName()).thenReturn(uniqueName);
        when(member.getName()).thenReturn(uniqueName.substring(uniqueName.lastIndexOf('[') + 1, uniqueName.length() - 1));
        when(member.getOrdinal()).thenReturn(ordinal);
        when(member.getDepth()).thenReturn(2);
        return member;
    }

    @Test
    void standsForTheMemberItWraps() {
        Member dairy = member("[Product].[Dairy]", 5);
        Expression exp = mock(Expression.class);
        VisualTotalMemberImpl total = new VisualTotalMemberImpl(dairy, "Dairy", "*Subtotal - Dairy", exp);

        assertThat(total.getMember()).isSameAs(dairy);
        assertThat(total.getDataMember()).isSameAs(dairy);
        assertThat(total.getName()).isEqualTo("Dairy");
        assertThat(total.getCaption()).isEqualTo("*Subtotal - Dairy");
        assertThat(total.getUniqueName()).isEqualTo("[Product].[Dairy]");
        assertThat(total.getOrdinal()).isEqualTo(5);
        assertThat(total.getDepth()).isEqualTo(2);
        assertThat(total.isCalculated()).isTrue();
        assertThat(total.getMemberType()).isEqualTo(MemberType.FORMULA);
        assertThat(total.getSolveOrder()).isEqualTo(VisualTotalMemberImpl.SOLVE_ORDER);
        assertThat(total.getExpression()).isSameAs(exp);
        assertThat(total).isEqualTo(dairy);
        assertThat(total.hashCode()).isEqualTo(dairy.hashCode());
        assertThat(total.getPropertyValue(StandardProperty.MEMBER_UNIQUE_NAME.getName())).isEqualTo("[Product].[Dairy]");
        assertThat(total.getPropertyValue(StandardProperty.LEVEL_NUMBER.getName())).isEqualTo(2);
    }

    @Test
    void twoTotalsCompareThroughTheirMembers() {
        Member a = member("[Product].[A]", 1);
        Member b = member("[Product].[B]", 2);
        when(a.compareTo(b)).thenReturn(-1);
        Expression exp = mock(Expression.class);
        VisualTotalMember ta = new VisualTotalMemberImpl(a, "A", "A", exp);
        VisualTotalMember tb = new VisualTotalMemberImpl(b, "B", "B", exp);
        assertThat(ta.compareTo(tb)).isEqualTo(-1);
        assertThat(ta).isNotEqualTo(tb);
    }

    @Test
    void anAllMemberStaysAnAllMember() {
        Member all = member("[Product].[All Products]", 0);
        when(all.getMemberType()).thenReturn(MemberType.ALL);
        VisualTotalMemberImpl total = new VisualTotalMemberImpl(all, "All Products", "All Products", mock(Expression.class));
        assertThat(total.isAll()).isTrue();
        assertThat(total.isCalculated()).isTrue();
    }

    @Test
    void theAggregateExpressionSpansTheGivenChildren() {
        Member a = member("[Product].[A]", 1);
        Member b = member("[Product].[B]", 2);
        Expression exp = VisualTotalMemberImpl.makeExpr(List.of(a, b));
        assertThat(exp).isInstanceOf(UnresolvedFunCall.class);
        UnresolvedFunCall aggregate = (UnresolvedFunCall) exp;
        assertThat(aggregate.getOperationAtom().name()).isEqualTo("Aggregate");
        assertThat(aggregate.getArgs()).hasSize(1);
        assertThat(((UnresolvedFunCall) aggregate.getArg(0)).getArgs()).hasSize(2);
    }
}
