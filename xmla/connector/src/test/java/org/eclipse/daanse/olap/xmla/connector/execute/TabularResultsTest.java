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
package org.eclipse.daanse.olap.xmla.connector.execute;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

import org.eclipse.daanse.olap.api.result.CellSet;
import org.eclipse.daanse.olap.api.result.CellSetAxis;
import org.eclipse.daanse.xmla.model.xmla.RowsetResult;
import org.junit.jupiter.api.Test;

class TabularResultsTest {

    /**
     * SELECT NON EMPTY {[Measures].[Profit]} ON COLUMNS of no cells: the columns
     * axis has no positions, so there is no row.
     */
    @Test
    void emptyColumnsAxisAloneHasNoRows() {
        CellSetAxis columns = mock(CellSetAxis.class);
        when(columns.getPositions()).thenReturn(List.of());
        CellSetAxis filter = mock(CellSetAxis.class);
        when(filter.getPositions()).thenReturn(List.of());
        CellSet cellSet = mock(CellSet.class);
        when(cellSet.getAxes()).thenReturn(List.of(columns));
        when(cellSet.getFilterAxis()).thenReturn(filter);

        RowsetResult result = TabularResults.fromCellSet(cellSet, false);

        assertThat(result.getColumns()).isEmpty();
        assertThat(result.getRows()).isEmpty();
    }
}
