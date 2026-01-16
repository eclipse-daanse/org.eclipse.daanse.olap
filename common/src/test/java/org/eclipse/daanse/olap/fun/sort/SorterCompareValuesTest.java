/*
 * Copyright (c) 2026 Contributors to the Eclipse Foundation.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.daanse.olap.fun.sort;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.eclipse.daanse.olap.api.element.Member;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

/**
 * Tests for {@link Sorter#compareValues(Object, Object)} method.
 */
class SorterCompareValuesTest {

    /**
     * Test that compareValues correctly compares two different OrderKey objects.
     * This test exposes the bug where value0 was checked twice instead of value0 and value1,
     * causing all OrderKey comparisons to incorrectly return 0.
     */
    @Test
    void testCompareValuesWithDifferentOrderKeys() {
        // Create two members with different order keys
        Member memberA = mock(Member.class, Mockito.withSettings().stubOnly());
        Member memberB = mock(Member.class, Mockito.withSettings().stubOnly());

        when(memberA.isCalculatedInQuery()).thenReturn(false);
        when(memberB.isCalculatedInQuery()).thenReturn(false);
        when(memberA.getOrderKey()).thenReturn("A");
        when(memberB.getOrderKey()).thenReturn("B");

        OrderKey orderKey1 = new OrderKey(memberA);
        OrderKey orderKey2 = new OrderKey(memberB);

        // The comparison should NOT be 0 since they have different order keys
        int result = Sorter.compareValues(orderKey1, orderKey2);

        // "A".compareTo("B") should return negative value
        assertThat(result)
            .as("Comparing OrderKey with 'A' to OrderKey with 'B' should return negative")
            .isLessThan(0);

        // Also test the reverse
        int reverseResult = Sorter.compareValues(orderKey2, orderKey1);
        assertThat(reverseResult)
            .as("Comparing OrderKey with 'B' to OrderKey with 'A' should return positive")
            .isGreaterThan(0);
    }

    /**
     * Test that compareValues returns 0 for equal OrderKey objects.
     */
    @Test
    void testCompareValuesWithEqualOrderKeys() {
        Member memberA = mock(Member.class, Mockito.withSettings().stubOnly());
        Member memberB = mock(Member.class, Mockito.withSettings().stubOnly());

        when(memberA.isCalculatedInQuery()).thenReturn(false);
        when(memberB.isCalculatedInQuery()).thenReturn(false);
        when(memberA.getOrderKey()).thenReturn("Same");
        when(memberB.getOrderKey()).thenReturn("Same");

        OrderKey orderKey1 = new OrderKey(memberA);
        OrderKey orderKey2 = new OrderKey(memberB);

        int result = Sorter.compareValues(orderKey1, orderKey2);

        assertThat(result)
            .as("Comparing OrderKeys with same value 'Same' should return 0")
            .isEqualTo(0);
    }
}
