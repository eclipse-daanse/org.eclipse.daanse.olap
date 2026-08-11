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
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

import org.eclipse.daanse.olap.api.Context;
import org.eclipse.daanse.olap.common.ExecutionConfig;
import org.eclipse.daanse.olap.api.connection.Connection;
import org.eclipse.daanse.olap.api.element.Cube;
import org.eclipse.daanse.olap.api.element.Hierarchy;
import org.eclipse.daanse.olap.api.element.Level;
import org.eclipse.daanse.olap.api.element.Member;
import org.eclipse.daanse.olap.api.execution.Statement;
import org.eclipse.daanse.olap.api.query.component.Query;
import org.eclipse.daanse.olap.api.query.component.QueryComponent;
import org.eclipse.daanse.olap.api.result.Cell;
import org.eclipse.daanse.olap.api.result.CellSet;
import org.eclipse.daanse.olap.api.result.CellSetAxis;
import org.eclipse.daanse.olap.api.result.CellSetAxisMetaData;
import org.eclipse.daanse.olap.api.result.CellSetMetaData;
import org.eclipse.daanse.olap.api.result.Position;
import org.eclipse.daanse.xmla.model.mddataset.CellType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * The bridge's {@code XmlaNullCellCharacterizationTest} against the connector's
 * {@link CellSetToMdDataset}: NULL cells are omitted MSAS-style except at
 * ordinal 0 or when a cell property still carries something; infinity
 * serializes as INF; an error cell carries the throwable and its {@code #ERR:}
 * formatted value.
 */
class NullCellCharacterizationTest {

    private CellSet cellSet;
    private CellSetAxis columnsAxis;

    @BeforeEach
    void setUp() {
        cellSet = mock(CellSet.class);

        Statement statement = mock(Statement.class);
        Query query = mock(Query.class);
        lenient().when(cellSet.getStatement()).thenReturn(statement);
        lenient().when(statement.getQuery()).thenReturn(query);

        Connection connection = mock(Connection.class);
        Context<?> context = mock(Context.class);
        lenient().when(statement.getDaanseConnection()).thenReturn(connection);
        lenient().doReturn(context).when(connection).getContext();
        lenient().when(context.getConfig()).thenReturn(ExecutionConfig.DEFAULTS);
        lenient().when(query.getCellProperties()).thenReturn(new QueryComponent[0]);

        CellSetMetaData metaData = mock(CellSetMetaData.class);
        Cube cube = mock(Cube.class);
        lenient().when(cellSet.getMetaData()).thenReturn(metaData);
        lenient().when(metaData.getCube()).thenReturn(cube);
        lenient().when(cube.getName()).thenReturn("SalesCube");

        columnsAxis = mock(CellSetAxis.class);
        CellSetAxis filterAxis = mock(CellSetAxis.class);
        lenient().when(cellSet.getAxes()).thenReturn(List.of(columnsAxis));
        lenient().when(cellSet.getFilterAxis()).thenReturn(filterAxis);

        CellSetAxisMetaData filterAxisMetaData = mock(CellSetAxisMetaData.class);
        lenient().when(filterAxis.getPositions()).thenReturn(List.of());
        lenient().when(filterAxis.getAxisMetaData()).thenReturn(filterAxisMetaData);
        lenient().when(filterAxisMetaData.getHierarchies()).thenReturn(List.of());
        lenient().when(filterAxisMetaData.getProperties()).thenReturn(List.of());
    }

    private void mockAxisPositions(int count) {
        Hierarchy hierarchy = mock(Hierarchy.class);
        lenient().when(hierarchy.getName()).thenReturn("Measures");
        lenient().when(hierarchy.getUniqueName()).thenReturn("[Measures]");
        Level level = mock(Level.class);

        Position[] positions = new Position[count];
        for (int i = 0; i < count; i++) {
            Member member = mock(Member.class);
            lenient().when(member.getLevel()).thenReturn(level);
            lenient().when(member.getHierarchy()).thenReturn(hierarchy);
            lenient().when(member.getPropertyValue("MEMBER_UNIQUE_NAME")).thenReturn("[Measures].[M" + i + "]");
            lenient().when(member.getPropertyValue("MEMBER_CAPTION")).thenReturn("M" + i);
            lenient().when(member.getPropertyValue("LEVEL_UNIQUE_NAME")).thenReturn("[Measures].[MeasuresLevel]");
            lenient().when(member.getPropertyValue("LEVEL_NUMBER")).thenReturn(0);
            lenient().when(member.getPropertyValue("CHILDREN_CARDINALITY")).thenReturn(0);

            Position position = mock(Position.class);
            List<Member> members = List.of(member);
            lenient().when(position.getMembers()).thenReturn(members);
            lenient().when(position.iterator()).thenAnswer(invocation -> members.iterator());
            positions[i] = position;
        }
        lenient().when(columnsAxis.getPositions()).thenReturn(List.of(positions));
        lenient().when(columnsAxis.getAxisMetaData()).thenReturn(null);
    }

    private void mockCells(Cell... cells) {
        when(cellSet.getCell(anyList())).thenAnswer(invocation -> {
            List<Integer> position = invocation.getArgument(0);
            return cells[position.get(0)];
        });
    }

    private static Cell valueCell(Object value, String formatted) {
        Cell cell = mock(Cell.class);
        lenient().when(cell.isNull()).thenReturn(false);
        lenient().when(cell.getValue()).thenReturn(value);
        lenient().when(cell.getPropertyValue("VALUE")).thenReturn(value);
        lenient().when(cell.getPropertyValue("FORMATTED_VALUE")).thenReturn(formatted);
        return cell;
    }

    private static Cell formattedNullCell() {
        Cell cell = mock(Cell.class);
        lenient().when(cell.isNull()).thenReturn(true);
        lenient().when(cell.getValue()).thenReturn(null);
        lenient().when(cell.getPropertyValue("VALUE")).thenReturn(null);
        lenient().when(cell.getPropertyValue("FORMATTED_VALUE")).thenReturn("");
        return cell;
    }

    private static Cell bareNullCell() {
        Cell cell = mock(Cell.class);
        lenient().when(cell.isNull()).thenReturn(true);
        lenient().when(cell.getValue()).thenReturn(null);
        lenient().when(cell.getPropertyValue("VALUE")).thenReturn(null);
        lenient().when(cell.getPropertyValue("FORMATTED_VALUE")).thenReturn(null);
        return cell;
    }

    private List<CellType> convert() {
        return CellSetToMdDataset.toMdDataset(cellSet, true).getCellData().getCell();
    }

    @Test
    @DisplayName("Regular cell keeps value; NULL cell with formatted value loses only <Value>")
    void nullCellWithFormattedValueIsEmittedWithoutValue() {
        mockAxisPositions(2);
        mockCells(valueCell(42.5d, "42.5"), formattedNullCell());

        List<CellType> cells = convert();
        assertThat(cells).hasSize(2);
        assertThat(cells.get(0).getCellOrdinal()).isZero();
        assertThat(cells.get(0).getValue().getValue()).isEqualTo("42.5");

        CellType nullCell = cells.get(1);
        assertThat(nullCell.getCellOrdinal()).isEqualTo(1L);
        assertThat(nullCell.getValue()).isNull();
        assertThat(nullCell.getAny()).anySatisfy(property -> {
            assertThat(property.getTagName()).isEqualTo("FmtValue");
            assertThat(property.getValue()).isEmpty();
        });
    }

    @Test
    @DisplayName("NULL cell with all-null properties is omitted, except at ordinal 0")
    void bareNullCellIsOmittedExceptAtOrdinalZero() {
        mockAxisPositions(3);
        mockCells(valueCell(1.0d, "1"), bareNullCell(), valueCell(2.0d, "2"));

        List<CellType> cells = convert();
        assertThat(cells).hasSize(2);
        assertThat(cells).extracting(CellType::getCellOrdinal).containsExactly(0L, 2L);
    }

    @Test
    @DisplayName("NULL cell at ordinal 0 is always emitted")
    void bareNullCellAtOrdinalZeroIsEmitted() {
        mockAxisPositions(1);
        mockCells(bareNullCell());

        List<CellType> cells = convert();
        assertThat(cells).hasSize(1);
        assertThat(cells.get(0).getCellOrdinal()).isZero();
        assertThat(cells.get(0).getValue()).isNull();
        assertThat(cells.get(0).getAny()).isEmpty();
    }

    @Test
    @DisplayName("Double.POSITIVE_INFINITY serializes as INF")
    void positiveInfinityIsSerializedAsInf() {
        mockAxisPositions(1);
        mockCells(valueCell(Double.POSITIVE_INFINITY, "Infinity"));

        List<CellType> cells = convert();
        assertThat(cells).hasSize(1);
        assertThat(cells.get(0).getValue().getValue()).isEqualTo("INF");
    }

    @Test
    @DisplayName("Error cell carries the throwable's text and the #ERR: formatted value")
    void errorCellSerializesThrowableAsString() {
        mockAxisPositions(1);
        RuntimeException failure = new RuntimeException("boom");
        Cell errorCell = mock(Cell.class);
        lenient().when(errorCell.isNull()).thenReturn(false);
        lenient().when(errorCell.getValue()).thenReturn(failure);
        lenient().when(errorCell.getPropertyValue("VALUE")).thenReturn(failure);
        lenient().when(errorCell.getPropertyValue("FORMATTED_VALUE")).thenReturn("#ERR: " + failure);
        mockCells(errorCell);

        List<CellType> cells = convert();
        assertThat(cells).hasSize(1);
        assertThat(cells.get(0).getValue().getValue()).contains("boom");
        assertThat(cells.get(0).getAny()).anySatisfy(property -> {
            assertThat(property.getTagName()).isEqualTo("FmtValue");
            assertThat(property.getValue()).startsWith("#ERR:");
        });
    }
}
