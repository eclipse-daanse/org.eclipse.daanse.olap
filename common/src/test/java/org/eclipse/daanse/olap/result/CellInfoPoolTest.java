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
package org.eclipse.daanse.olap.result;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.eclipse.daanse.olap.api.exception.OlapRuntimeException;
import org.eclipse.daanse.olap.api.result.ObjectValue;
import org.eclipse.daanse.olap.key.CellKey;
import org.junit.jupiter.api.Test;

class CellInfoPoolTest {

    @Test
    void theSamePositionYieldsTheSamePooledCellInfo() {
        CellInfoPool pool = new CellInfoPool(2);
        CellInfo first = pool.create(new int[] { 3, 7 });
        CellInfo again = pool.create(new int[] { 3, 7 });
        CellInfo other = pool.create(new int[] { 7, 3 });
        assertThat(again).isSameAs(first);
        assertThat(other).isNotSameAs(first);
        assertThat(pool.size()).isEqualTo(2);
        assertThat(pool.lookup(new int[] { 3, 7 })).isSameAs(first);
    }

    @Test
    void keysDoNotCollideAcrossAxesUpToFour() {
        for (int axes = 0; axes <= 4; axes++) {
            CellInfoPool.CellKeyMaker maker = CellInfoPool.createCellKeyMaker(axes);
            int[] a = new int[axes];
            int[] b = new int[axes];
            if (axes > 0) {
                a[axes - 1] = 1;
                b[0] = 1;
            }
            long ka = maker.generate(a);
            long kb = maker.generate(b);
            if (axes > 1) {
                assertThat(ka).isNotEqualTo(kb);
            } else {
                assertThat(ka).isEqualTo(kb);
            }
        }
    }

    @Test
    void fiveAxesAreRefusedByThePoolAndAnsweredByTheMap() {
        assertThatThrownBy(() -> new CellInfoPool(5)).isInstanceOf(OlapRuntimeException.class);
        CellInfoContainer container = CellInfoContainer.forAxes(5, CellKey.Generator.newCellKey(5));
        assertThat(container).isInstanceOf(CellInfoMap.class);
        assertThat(CellInfoContainer.forAxes(2, CellKey.Generator.newCellKey(2))).isInstanceOf(CellInfoPool.class);
    }

    @Test
    void aCellInfoFormatsThroughItsFormatter() {
        CellInfo info = new CellInfo(1, new ObjectValue(3.5), "#,##0.0", (value, format) -> format + ":" + value);
        assertThat(info.getFormatValue()).isEqualTo("#,##0.0:3.5");
        assertThat(new CellInfo(1)).isEqualTo(info);
        assertThat(new CellInfo(2)).isNotEqualTo(info);
        assertThat(new CellInfo(9).getFormatValue()).isEmpty();
    }
}
