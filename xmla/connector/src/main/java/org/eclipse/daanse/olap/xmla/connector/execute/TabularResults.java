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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.daanse.olap.api.element.Level;
import org.eclipse.daanse.olap.api.element.Member;
import org.eclipse.daanse.olap.api.result.Cell;
import org.eclipse.daanse.olap.api.result.Property;
import org.eclipse.daanse.olap.api.result.CellSet;
import org.eclipse.daanse.olap.api.result.CellSetAxis;
import org.eclipse.daanse.olap.api.result.Position;
import org.eclipse.daanse.olap.common.StandardProperty;
import org.eclipse.daanse.olap.common.Util;
import org.eclipse.daanse.xmla.model.io.ElementNames;
import org.eclipse.daanse.xmla.model.io.ValueInfo;
import org.eclipse.daanse.xmla.model.xmla.RowsetCell;
import org.eclipse.daanse.xmla.model.xmla.RowsetColumn;
import org.eclipse.daanse.xmla.model.xmla.RowsetResult;
import org.eclipse.daanse.xmla.model.xmla.RowsetRow;
import org.eclipse.daanse.xmla.model.xmla.XmlaFactory;

/**
 * The three sources of a tabular Execute answer, each rendered as the
 * {@code RowsetResult} the codec turns into {@code xmla-rs:root}.
 * <p>
 * Ported from the bridge's {@code XmlaResponseConverter} rowset half and its
 * DMV filter: a DMV query serves the typed rows of the discover implementation
 * with the SELECT's column projection and WHERE applied; a drill-through or SQL
 * statement serves its JDBC result set; a {@code Format=Tabular} MDX answer
 * flattens the cell set — one row per position of the last axis, member
 * property columns per level, one cell column per COLUMNS position.
 */
public final class TabularResults {

    private static final XmlaFactory FACTORY = XmlaFactory.eINSTANCE;

    private TabularResults() {
        // static access only
    }

    // --- DMV: typed rowset EObjects, projected and filtered
    // -----------------------------

    // --- MDX with Format=Tabular: the flattened cell set
    // --------------------------------

    /** A column of the flattened answer; it renders its cells for one row. */
    private abstract static class Column {

        protected final String name;
        protected final String encodedName;

        protected Column(String name) {
            this.name = name;
            this.encodedName = ElementNames.encode(name);
        }

        abstract void render(Cell cell, Member[] members, RowsetRow into);
    }

    private static final class CellColumn extends Column {

        CellColumn(String name) {
            super(name);
        }

        @Override
        void render(Cell cell, Member[] members, RowsetRow into) {
            if (cell.isNull()) {
                return;
            }
            Object value = cell.getValue();
            String dataType = (String) cell.getPropertyValue(StandardProperty.DATATYPE.getName());
            ValueInfo info = new ValueInfo(dataType, value);
            String text = info.value.toString();
            if (info.isDecimal) {
                text = ElementNames.normalizeNumericString(text);
            }
            RowsetCell rendered = FACTORY.createRowsetCell();
            rendered.setName(encodedName);
            rendered.setValue(text);
            into.getCells().add(rendered);
        }
    }

    private static final class MemberColumn extends Column {

        private final Property property;
        private final Level level;
        private final int memberOrdinal;

        MemberColumn(Property property, Level level, int memberOrdinal) {
            super(level.getUniqueName() + "." + Util.quoteMdxIdentifier(property.getName()));
            this.property = property;
            this.level = level;
            this.memberOrdinal = memberOrdinal;
        }

        @Override
        void render(Cell cell, Member[] members, RowsetRow into) {
            Member member = members[memberOrdinal];
            int depth = level.getDepth();
            if (member.getDepth() < depth) {
                // A level below the current member: nothing to write.
                return;
            }
            while (member.getDepth() > depth) {
                member = member.getParentMember();
            }
            Object value = member.getPropertyValue(property.getName());
            if (value == null) {
                return;
            }
            RowsetCell rendered = FACTORY.createRowsetCell();
            rendered.setName(encodedName);
            rendered.setValue(value.toString());
            into.getCells().add(rendered);
        }
    }

    public static RowsetResult fromCellSet(CellSet cellSet, boolean schemaIncluded) {
        List<CellSetAxis> axes = cellSet.getAxes();
        int axisCount = axes.size();
        int[] pos = new int[axisCount];
        List<Integer> posList = new PositionList(pos);

        boolean empty = false;
        int dimensionCount = 0;
        for (int index = axes.size() - 1; index > 0; index--) {
            CellSetAxis axis = axes.get(index);
            if (axis.getPositions().isEmpty()) {
                empty = true;
                continue;
            }
            dimensionCount += axis.getPositions().getFirst().getMembers().size();
        }

        Level[] levels = new Level[dimensionCount];
        List<Column> columns = new ArrayList<>();
        int memberOrdinal = 0;
        if (!empty) {
            for (int index = axes.size() - 1; index > 0; index--) {
                CellSetAxis axis = axes.get(index);
                int rewind = memberOrdinal;
                int positionIndex = 0;
                for (Position position : axis.getPositions()) {
                    memberOrdinal = rewind;
                    for (Member member : position.getMembers()) {
                        if (positionIndex == 0 || member.getLevel().getDepth() > levels[memberOrdinal].getDepth()) {
                            levels[memberOrdinal] = member.getLevel();
                        }
                        memberOrdinal++;
                    }
                    positionIndex++;
                }
                List<Property> dimensionProperties = axis.getAxisMetaData().getProperties();
                if (dimensionProperties.isEmpty()) {
                    dimensionProperties = List.of(Property.StandardMemberProperty.MEMBER_CAPTION);
                }
                for (int j = rewind; j < memberOrdinal; j++) {
                    Level level = levels[j];
                    for (int k = 0; k <= level.getDepth(); k++) {
                        Level upper = level.getHierarchy().getLevels().get(k);
                        for (Property property : dimensionProperties) {
                            columns.add(new MemberColumn(property, upper, j));
                        }
                    }
                }
            }
        }
        Member[] members = new Member[memberOrdinal + 1];

        if (!axes.isEmpty()) {
            for (Position position : axes.getFirst().getPositions()) {
                StringBuilder name = new StringBuilder();
                for (Member member : position.getMembers()) {
                    if (name.length() > 0) {
                        name.append('.');
                    }
                    name.append(member.getUniqueName());
                }
                columns.add(new CellColumn(name.toString()));
            }
        }

        RowsetResult result = FACTORY.createRowsetResult();
        result.setSchemaIncluded(schemaIncluded);
        for (Column column : columns) {
            RowsetColumn declared = FACTORY.createRowsetColumn();
            declared.setField(column.name);
            declared.setName(column.encodedName);
            // A member property is a string; a cell's type varies per row, so its
            // schema element names none, exactly like a variant rowset column.
            declared.setXsdType(column instanceof MemberColumn ? ValueInfo.XSD_STRING : null);
            result.getColumns().add(declared);
        }

        if (!empty) {
            if (axisCount == 0) {
                Cell cell = cellSet.getCell(posList);
                if (cell.getValue() != null) {
                    RowsetRow row = FACTORY.createRowsetRow();
                    for (Column column : columns) {
                        column.render(cell, members, row);
                    }
                    result.getRows().add(row);
                }
            } else {
                renderAxis(cellSet, axisCount - 1, 0, columns, members, pos, posList, result);
            }
        }
        return result;
    }

    private static void renderAxis(CellSet cellSet, int axis, int memberStart, List<Column> columns, Member[] members,
            int[] pos, List<Integer> posList, RowsetResult result) {
        List<Position> positions = cellSet.getAxes().get(axis).getPositions();
        int length = axis == 0 ? 1 : positions.size();
        for (int index = 0; index < length; index++) {
            Position position = positions.get(index);
            int ordinal = memberStart;
            List<Member> positionMembers = position.getMembers();
            for (int j = 0; j < positionMembers.size() && ordinal < members.length; j++, ordinal++) {
                members[ordinal] = positionMembers.get(j);
            }
            if (axis >= 2) {
                renderAxis(cellSet, axis - 1, ordinal, columns, members, pos, posList, result);
            } else {
                RowsetRow row = FACTORY.createRowsetRow();
                pos[axis] = index;
                pos[0] = 0;
                for (Column column : columns) {
                    if (column instanceof MemberColumn) {
                        column.render(null, members, row);
                    } else {
                        column.render(cellSet.getCell(posList), null, row);
                        pos[0]++;
                    }
                }
                result.getRows().add(row);
            }
        }
    }

    /** A live view of the coordinate array, as the cell lookup wants it. */
    private static final class PositionList extends java.util.AbstractList<Integer> {

        private final int[] values;

        PositionList(int[] values) {
            this.values = values;
        }

        @Override
        public Integer get(int index) {
            return values[index];
        }

        @Override
        public int size() {
            return values.length;
        }
    }
}
