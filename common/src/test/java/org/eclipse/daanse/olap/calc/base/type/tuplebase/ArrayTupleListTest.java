/*
 * Copyright (c) 2025 Contributors to the Eclipse Foundation.
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
package org.eclipse.daanse.olap.calc.base.type.tuplebase;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.ListAssert.assertThatList;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.eclipse.daanse.olap.api.element.Member;
import org.eclipse.daanse.olap.common.ResourceLimitExceededException;
import org.eclipse.daanse.olap.common.SystemWideProperties;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoSettings;

@MockitoSettings
class ArrayTupleListTest {

    @AfterEach
    public void afterEach() {
        SystemWideProperties.instance().populateInitial();
    }

    @Mock
    private Member m1;
    @Mock
    private Member m2;

    private ArrayTupleList atlList;

    @Test
    void addOverInitialCapacity() {
        SystemWideProperties.instance().ResultLimit = 0;
        int count = 50;

        atlList = new ArrayTupleList(2, 10);
        addTuplesToList(atlList, count);

        assertThatList(atlList).hasSize(count).anySatisfy(tuple -> assertEquals(tuple.get(0), m1))
                .anySatisfy(tuple -> assertEquals(tuple.get(1), m2));
    }

    @Test
    void addOverResultLimit() {
        SystemWideProperties.instance().ResultLimit = 15;
        atlList = new ArrayTupleList(2, 10);

        assertThatThrownBy(() -> addTuplesToList(atlList, 16)).isInstanceOf(ResourceLimitExceededException.class)
                .hasMessageContaining("result (16) exceeded limit (15)");
    }

    private void addTuplesToList(ArrayTupleList list, int n) {
        for (int i = 0; i < n; i++) {
            list.addTuple(m1, m2);
        }
    }
}
