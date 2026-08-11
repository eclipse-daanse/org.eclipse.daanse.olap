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

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.eclipse.daanse.olap.api.element.Cube;
import org.eclipse.daanse.olap.api.element.Dimension;
import org.eclipse.daanse.olap.api.element.Hierarchy;
import org.eclipse.daanse.olap.api.element.Member;
import org.eclipse.daanse.olap.api.element.PseudoLeafMember;
import org.eclipse.daanse.olap.api.execution.Statement;
import org.eclipse.daanse.olap.api.query.NameSegment;
import org.eclipse.daanse.olap.api.query.component.QueryComponent;
import org.eclipse.daanse.olap.api.result.Cell;
import org.eclipse.daanse.olap.api.result.CellSet;
import org.eclipse.daanse.olap.api.result.CellSetAxis;
import org.eclipse.daanse.olap.api.result.CellSetAxisMetaData;
import org.eclipse.daanse.olap.api.result.Datatype;
import org.eclipse.daanse.olap.api.result.IDaanseOlap4jProperty;
import org.eclipse.daanse.olap.api.result.Position;
import org.eclipse.daanse.olap.api.result.Property;
import org.eclipse.daanse.olap.common.Util;
import org.eclipse.daanse.xmla.model.io.ElementNames;
import org.eclipse.daanse.xmla.model.io.ValueInfo;
import org.eclipse.daanse.xmla.model.mddataset.Axes;
import org.eclipse.daanse.xmla.model.mddataset.AxesInfo;
import org.eclipse.daanse.xmla.model.mddataset.Axis;
import org.eclipse.daanse.xmla.model.mddataset.AxisInfo;
import org.eclipse.daanse.xmla.model.mddataset.CellData;
import org.eclipse.daanse.xmla.model.mddataset.CellInfo;
import org.eclipse.daanse.xmla.model.mddataset.CellInfoItem;
import org.eclipse.daanse.xmla.model.mddataset.CellProperty;
import org.eclipse.daanse.xmla.model.mddataset.CellType;
import org.eclipse.daanse.xmla.model.mddataset.CellTypeValue;
import org.eclipse.daanse.xmla.model.mddataset.CubeInfo;
import org.eclipse.daanse.xmla.model.mddataset.HierarchyInfo;
import org.eclipse.daanse.xmla.model.mddataset.MdDataset;
import org.eclipse.daanse.xmla.model.mddataset.MdDatasetFactory;
import org.eclipse.daanse.xmla.model.mddataset.MemberType;
import org.eclipse.daanse.xmla.model.mddataset.OlapInfo;
import org.eclipse.daanse.xmla.model.mddataset.OlapInfoCube;
import org.eclipse.daanse.xmla.model.mddataset.TupleType;
import org.eclipse.daanse.xmla.model.mddataset.TuplesType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A {@link CellSet} as the model's {@code MdDataset}.
 * <p>
 * Ported from the multidimensional half of the bridge's
 * {@code XmlaResponseConverter}, with the api records replaced by the EObjects
 * the codec writes: same axes, same slicer treatment (including the two 'empty
 * slicer' cases the comment there explains), same DISPLAY_INFO arithmetic, same
 * null-cell elision — SSAS omits a null cell unless it is the very first — and
 * the same numeric normalisation of cell values.
 */
public final class CellSetToMdDataset {

    private static final Logger LOGGER = LoggerFactory.getLogger(CellSetToMdDataset.class);

    private static final MdDatasetFactory FACTORY = MdDatasetFactory.eINSTANCE;

    public static final String SLICER_AXIS = "SlicerAxis";
    public static final String CELL_ORDINAL = "CELL_ORDINAL";
    public static final String VALUE = "VALUE";
    public static final String FORMATTED_VALUE = "FORMATTED_VALUE";
    private static final String XSD_UNSIGNED_INT = "xsd:unsignedInt";

    /** The short wire names against the long property names they stand for. */
    private static final Map<String, Property.StandardMemberProperty> LONG_PROPS = new HashMap<>();

    static {
        LONG_PROPS.put("UName", Property.StandardMemberProperty.MEMBER_UNIQUE_NAME);
        LONG_PROPS.put("Caption", Property.StandardMemberProperty.MEMBER_CAPTION);
        LONG_PROPS.put("LName", Property.StandardMemberProperty.LEVEL_UNIQUE_NAME);
        LONG_PROPS.put("LNum", Property.StandardMemberProperty.LEVEL_NUMBER);
        LONG_PROPS.put("DisplayInfo", Property.StandardMemberProperty.DISPLAY_INFO);
    }

    /**
     * One queryable cell property: its name, its wire alias, its xsd type if fixed.
     */
    record WireCellProperty(String name, String alias, String xsdType) {
    }

    private static final Map<String, WireCellProperty> CELL_PROPERTIES = new HashMap<>();

    static {
        CELL_PROPERTIES.put(CELL_ORDINAL, new WireCellProperty(CELL_ORDINAL, "CellOrdinal", XSD_UNSIGNED_INT));
        CELL_PROPERTIES.put(VALUE, new WireCellProperty(VALUE, "Value", null));
        CELL_PROPERTIES.put(FORMATTED_VALUE, new WireCellProperty(FORMATTED_VALUE, "FmtValue", "xsd:string"));
        CELL_PROPERTIES.put("FORMAT_STRING", new WireCellProperty("FORMAT_STRING", "FormatString", "xsd:string"));
        CELL_PROPERTIES.put("LANGUAGE", new WireCellProperty("LANGUAGE", "Language", XSD_UNSIGNED_INT));
        CELL_PROPERTIES.put("BACK_COLOR", new WireCellProperty("BACK_COLOR", "BackColor", XSD_UNSIGNED_INT));
        CELL_PROPERTIES.put("FORE_COLOR", new WireCellProperty("FORE_COLOR", "ForeColor", XSD_UNSIGNED_INT));
        CELL_PROPERTIES.put("FONT_FLAGS", new WireCellProperty("FONT_FLAGS", "FontFlags", "xsd:int"));
        CELL_PROPERTIES.put("UPDATEABLE", new WireCellProperty("UPDATEABLE", "Updateable", XSD_UNSIGNED_INT));
    }

    private static final List<Property> DEFAULT_PROPS = List.of(
            rename(Property.StandardMemberProperty.MEMBER_UNIQUE_NAME, "UName"),
            rename(Property.StandardMemberProperty.MEMBER_CAPTION, "Caption"),
            rename(Property.StandardMemberProperty.LEVEL_UNIQUE_NAME, "LName"),
            rename(Property.StandardMemberProperty.LEVEL_NUMBER, "LNum"),
            rename(Property.StandardMemberProperty.DISPLAY_INFO, "DisplayInfo"));

    private CellSetToMdDataset() {
        // static access only
    }

    /** The whole dataset: OlapInfo, axes with slicer, cells. */
    public static MdDataset toMdDataset(CellSet cellSet, boolean omitDefaultSlicerInfo) {
        List<String> queryCellPropertyNames = queryCellPropertyNames(cellSet);
        List<Hierarchy> slicerAxisHierarchies = slicerAxisHierarchies(cellSet, omitDefaultSlicerInfo);

        MdDataset dataset = FACTORY.createMdDataset();
        dataset.setOlapInfo(olapInfo(cellSet, queryCellPropertyNames, omitDefaultSlicerInfo));
        dataset.setAxes(axes(cellSet, omitDefaultSlicerInfo, slicerAxisHierarchies));
        dataset.setCellData(cellData(cellSet, queryCellPropertyNames));
        return dataset;
    }

    // --- OlapInfo ---

    private static OlapInfo olapInfo(CellSet cellSet, List<String> queryCellPropertyNames,
            boolean omitDefaultSlicerInfo) {
        Cube cube = cellSet.getMetaData().getCube();

        OlapInfoCube infoCube = FACTORY.createOlapInfoCube();
        infoCube.setCubeName(cube.getName());
        infoCube.setLastDataUpdate(Instant.now().toString());
        infoCube.setLastSchemaUpdate(Instant.now().toString());
        CubeInfo cubeInfo = FACTORY.createCubeInfo();
        cubeInfo.getCube().add(infoCube);

        OlapInfo olapInfo = FACTORY.createOlapInfo();
        olapInfo.setCubeInfo(cubeInfo);
        olapInfo.setAxesInfo(axesInfo(cellSet, cube, omitDefaultSlicerInfo));
        olapInfo.setCellInfo(cellInfo(queryCellPropertyNames));
        return olapInfo;
    }

    private static CellInfo cellInfo(List<String> queryCellPropertyNames) {
        CellInfo cellInfo = FACTORY.createCellInfo();
        for (String propertyName : queryCellPropertyNames) {
            if (propertyName != null) {
                propertyName = propertyName.toUpperCase();
            }
            WireCellProperty property = CELL_PROPERTIES.get(propertyName);
            if (property != null) {
                CellInfoItem item = FACTORY.createCellInfoItem();
                item.setTagName(property.alias());
                item.setName(propertyName);
                if (property.xsdType() != null) {
                    item.setType(property.xsdType());
                }
                cellInfo.getAny().add(item);
            }
        }
        return cellInfo;
    }

    private static AxesInfo axesInfo(CellSet cellSet, Cube cube, boolean omitDefaultSlicerInfo) {
        AxesInfo axesInfo = FACTORY.createAxesInfo();
        final List<CellSetAxis> axes = cellSet.getAxes();
        List<Hierarchy> axisHierarchyList = new ArrayList<>();
        for (int i = 0; i < axes.size(); i++) {
            axesInfo.getAxisInfo().add(axisInfo(axes.get(i), "Axis" + i));
            axisHierarchyList.addAll(hierarchiesOf(axes.get(i)));
        }

        CellSetAxis slicerAxis = cellSet.getFilterAxis();
        if (omitDefaultSlicerInfo) {
            axesInfo.getAxisInfo().add(axisInfo(slicerAxis, SLICER_AXIS));
        } else {
            // The slicer axis carries the default hierarchy of each dimension unseen on
            // another axis.
            List<Dimension> unseen = new ArrayList<>(cube.getDimensions() != null ? cube.getDimensions() : List.of());
            for (Hierarchy onAxis : axisHierarchyList) {
                unseen.remove(onAxis.getDimension());
            }
            List<Hierarchy> hierarchies = new ArrayList<>();
            for (Dimension dimension : unseen) {
                hierarchies.addAll(dimension.getHierarchies());
            }
            AxisInfo slicerInfo = FACTORY.createAxisInfo();
            slicerInfo.setName(SLICER_AXIS);
            hierarchyInfos(slicerInfo, hierarchies, props(slicerAxis.getAxisMetaData()));
            axesInfo.getAxisInfo().add(slicerInfo);
        }
        return axesInfo;
    }

    private static AxisInfo axisInfo(CellSetAxis axis, String axisName) {
        List<Hierarchy> hierarchies = hierarchiesOf(axis);
        List<Property> props = new ArrayList<>(props(axis.getAxisMetaData()));
        props.removeIf(prop -> !isValidProp(axis.getPositions(), prop));

        AxisInfo axisInfo = FACTORY.createAxisInfo();
        axisInfo.setName(axisName);
        hierarchyInfos(axisInfo, hierarchies, props);
        return axisInfo;
    }

    private static void hierarchyInfos(AxisInfo into, List<Hierarchy> hierarchies, List<Property> props) {
        for (Hierarchy hierarchy : hierarchies) {
            HierarchyInfo info = FACTORY.createHierarchyInfo();
            info.setName(hierarchy.getUniqueName());
            for (Property prop : props) {
                if (prop instanceof IDaanseOlap4jProperty own) {
                    if (hierarchy.getName().equals(own.getLevel().getHierarchy().getName())) {
                        info.getAny().add(cellInfoItem(hierarchy, prop));
                    }
                } else {
                    info.getAny().add(cellInfoItem(hierarchy, prop));
                }
            }
            into.getHierarchyInfo().add(info);
        }
    }

    private static CellInfoItem cellInfoItem(Hierarchy hierarchy, Property prop) {
        Property longProp = LONG_PROPS.getOrDefault(prop.getName(), null);
        if (longProp == null) {
            longProp = prop;
        }
        CellInfoItem item = FACTORY.createCellInfoItem();
        item.setTagName(ElementNames.encode(prop.getName()));
        item.setName(hierarchy.getUniqueName() + "." + Util.quoteMdxIdentifier(longProp.getName()));
        if (!(longProp instanceof IDaanseOlap4jProperty)) {
            String type = xsdType(longProp);
            if (type != null) {
                item.setType(type);
            }
        }
        return item;
    }

    private static List<Hierarchy> hierarchiesOf(CellSetAxis axis) {
        Iterator<Position> it = axis.getPositions().iterator();
        if (it.hasNext()) {
            List<Hierarchy> hierarchies = new ArrayList<>();
            for (Member member : it.next()) {
                hierarchies.add(member.getHierarchy());
            }
            return hierarchies;
        }
        return axis.getAxisMetaData().getHierarchies();
    }

    // --- axes ---

    private static Axes axes(CellSet cellSet, boolean omitDefaultSlicerInfo, List<Hierarchy> slicerAxisHierarchies) {
        Axes axesResult = FACTORY.createAxes();
        final List<CellSetAxis> axes = cellSet.getAxes();
        for (int i = 0; i < axes.size(); i++) {
            final CellSetAxis axis = axes.get(i);
            axesResult.getAxis().add(axis(cellSet, axis, props(axis.getAxisMetaData()), "Axis" + i));
        }

        CellSetAxis slicerAxis = cellSet.getFilterAxis();
        if (omitDefaultSlicerInfo) {
            // We always write a slicer axis. There are two 'empty' cases: zero positions
            // (the
            // WHERE clause evaluated to an empty set) or one position of zero members (no
            // WHERE
            // clause at all), and a client needs to tell them apart.
            if (!slicerAxisHierarchies.isEmpty()) {
                axesResult.getAxis().add(axis(cellSet, slicerAxis, props(slicerAxis.getAxisMetaData()), SLICER_AXIS));
            }
        } else {
            Axis slicer = FACTORY.createAxis();
            slicer.setName(SLICER_AXIS);
            TuplesType tuples = FACTORY.createTuplesType();
            TupleType tuple = FACTORY.createTupleType();
            tuples.getTuple().add(tuple);
            slicer.getSetType().add(tuples);

            Map<String, Integer> memberMap = new HashMap<>();
            final List<Position> slicerPositions = slicerAxis.getPositions();
            if (slicerPositions != null && !slicerPositions.isEmpty()) {
                int i = 0;
                for (Member member : slicerPositions.getFirst().getMembers()) {
                    memberMap.put(member.getHierarchy().getName(), i++);
                }
            }
            final List<Member> slicerMembers = slicerPositions == null || slicerPositions.isEmpty() ? List.of()
                    : slicerPositions.getFirst().getMembers();
            for (Hierarchy hierarchy : slicerAxisHierarchies) {
                // The member on the slicer, or the hierarchy's default when it is not there.
                Member member = hierarchy.getDefaultMember();
                Integer indexPosition = memberMap.get(hierarchy.getName());
                Member positionMember = indexPosition == null ? null : slicerMembers.get(indexPosition);
                for (Member slicerMember : slicerMembers) {
                    if (slicerMember.getHierarchy().equals(hierarchy)) {
                        member = slicerMember;
                        break;
                    }
                }
                if (member == null) {
                    LOGGER.warn("Can not create SlicerAxis: null default member for " + "Hierarchy {}",
                            hierarchy.getUniqueName());
                    continue;
                }
                if (positionMember != null) {
                    tuple.getMember().add(member(positionMember, null, slicerPositions.getFirst(), indexPosition,
                            props(slicerAxis.getAxisMetaData())));
                } else {
                    tuple.getMember().add(slicerMember(member, props(slicerAxis.getAxisMetaData())));
                }
            }
            axesResult.getAxis().add(slicer);
        }
        return axesResult;
    }

    private static Axis axis(CellSet cellSet, CellSetAxis axis, List<Property> props, String name) {
        Axis result = FACTORY.createAxis();
        result.setName(name);

        TuplesType tuples = FACTORY.createTuplesType();
        result.getSetType().add(tuples);

        List<Position> positions = axis.getPositions();
        Iterator<Position> it = positions.iterator();
        Position prevPosition = null;
        Position position = it.hasNext() ? it.next() : null;
        Position nextPosition = it.hasNext() ? it.next() : null;
        while (position != null) {
            TupleType tuple = FACTORY.createTupleType();
            tuples.getTuple().add(tuple);
            int k = 0;
            for (Member member : position.getMembers()) {
                tuple.getMember().add(member(member, prevPosition, nextPosition, k++, props));
            }
            prevPosition = position;
            position = nextPosition;
            nextPosition = it.hasNext() ? it.next() : null;
        }
        return result;
    }

    private static MemberType member(Member member, Position prevPosition, Position nextPosition, int k,
            List<Property> props) {
        MemberType result = FACTORY.createMemberType();
        result.setHierarchy(member.getHierarchy().getUniqueName());
        for (final Property prop : props) {
            Object value;
            Property longProp = LONG_PROPS.get(prop.getName());
            if (longProp == null) {
                longProp = prop;
            }
            if (longProp == Property.StandardMemberProperty.DISPLAY_INFO) {
                Integer childrenCard = (Integer) member
                        .getPropertyValue(Property.StandardMemberProperty.CHILDREN_CARDINALITY.getName());
                value = displayInfo(prevPosition, nextPosition, member, k, childrenCard);
            } else if (longProp == Property.StandardMemberProperty.DEPTH) {
                value = member.getDepth();
            } else if (longProp instanceof IDaanseOlap4jProperty own) {
                if (!member.getHierarchy().getName().equals(own.getLevel().getHierarchy().getName())) {
                    // The property belongs to another hierarchy.
                    continue;
                }
                value = member.getPropertyValue(own.getName());
            } else {
                value = member.getPropertyValue(longProp.getName());
            }
            if (longProp != prop && value == null) {
                value = defaultValue(prop);
            }
            if (value != null) {
                CellProperty property = FACTORY.createCellProperty();
                property.setTagName(ElementNames.encode(prop.getName()));
                property.setValue(value.toString());
                if (longProp instanceof IDaanseOlap4jProperty) {
                    String type = xsdType(prop);
                    if (type != null) {
                        property.setType(type);
                    }
                }
                result.getAny().add(property);
            }
        }
        return result;
    }

    private static MemberType slicerMember(Member member, List<Property> props) {
        MemberType result = FACTORY.createMemberType();
        result.setHierarchy(member.getHierarchy().getUniqueName());
        for (Property prop : props) {
            Object value;
            Property longProp = LONG_PROPS.get(prop.getName());
            if (longProp == null) {
                longProp = prop;
            }
            if (longProp == Property.StandardMemberProperty.DISPLAY_INFO) {
                Integer childrenCard = (Integer) member
                        .getPropertyValue(Property.StandardMemberProperty.CHILDREN_CARDINALITY.getName());
                value = childrenCard == null ? null : 0xffff & childrenCard;
            } else if (longProp == Property.StandardMemberProperty.DEPTH) {
                value = member.getDepth();
            } else {
                value = member.getPropertyValue(longProp.getName());
            }
            if (value == null) {
                value = defaultValue(prop);
            }
            if (value != null) {
                CellProperty property = FACTORY.createCellProperty();
                property.setTagName(ElementNames.encode(prop.getName()));
                property.setValue(value.toString());
                result.getAny().add(property);
            }
        }
        return result;
    }

    // --- cells ---

    private static CellData cellData(CellSet cellSet, List<String> queryCellPropertyNames) {
        CellData cellData = FACTORY.createCellData();
        final int axisCount = cellSet.getAxes().size();
        List<Integer> pos = new ArrayList<>();
        for (int i = 0; i < axisCount; i++) {
            pos.add(-1);
        }
        int[] cellOrdinal = new int[] { 0 };
        recurse(cellSet, pos, axisCount - 1, cellOrdinal, queryCellPropertyNames, cellData.getCell());
        return cellData;
    }

    private static void recurse(CellSet cellSet, List<Integer> pos, int axisOrdinal, int[] cellOrdinal,
            List<String> queryCellPropertyNames, List<CellType> into) {
        if (axisOrdinal < 0) {
            cell(cellSet, pos, cellOrdinal[0]++, queryCellPropertyNames, into);
        } else {
            CellSetAxis axis = cellSet.getAxes().get(axisOrdinal);
            List<Position> positions = axis.getPositions();
            for (int i = 0, n = positions.size(); i < n; i++) {
                pos.set(axisOrdinal, i);
                recurse(cellSet, pos, axisOrdinal - 1, cellOrdinal, queryCellPropertyNames, into);
            }
        }
    }

    private static void cell(CellSet cellSet, List<Integer> pos, int ordinal, List<String> queryCellPropertyNames,
            List<CellType> into) {
        Cell cell = cellSet.getCell(pos);

        boolean allPropertiesEmpty = true;
        for (String propertyName : queryCellPropertyNames) {
            if (cell.getPropertyValue(propertyName) != null) {
                allPropertiesEmpty = false;
                break;
            }
        }
        if (cell.isNull() && allPropertiesEmpty && ordinal != 0) {
            // Ignore null cells like MS AS, except for the 0th ordinal.
            return;
        }

        CellType cellType = FACTORY.createCellType();
        cellType.setCellOrdinal((long) ordinal);
        for (String propertyName : queryCellPropertyNames) {
            if (propertyName != null && propertyName.toUpperCase().equals(CELL_ORDINAL)) {
                continue;
            }
            Object value = cell.getPropertyValue(propertyName);
            if (value == null) {
                continue;
            }
            if (Property.StandardCellProperty.VALUE.getName().equals(propertyName)) {
                if (cell.isNull()) {
                    // Return the cell without a value, as AS2005 does.
                    continue;
                }
                final ValueInfo vi = new ValueInfo(null, value);
                final String valueString;
                if (vi.value instanceof Double doubleValue && doubleValue == Double.POSITIVE_INFINITY) {
                    valueString = "INF";
                } else if (vi.isDecimal) {
                    valueString = ElementNames.normalizeNumericString(vi.value.toString());
                } else {
                    valueString = vi.value.toString();
                }
                CellTypeValue cellValue = FACTORY.createCellTypeValue();
                cellValue.setType(vi.valueType);
                cellValue.setValue(valueString);
                cellType.setValue(cellValue);
            } else {
                WireCellProperty wire = CELL_PROPERTIES.get(propertyName.toUpperCase());
                CellProperty property = FACTORY.createCellProperty();
                property.setTagName(wire == null ? propertyName : wire.alias());
                property.setValue(value.toString());
                cellType.getAny().add(property);
            }
        }
        into.add(cellType);
    }

    // --- shared reckonings, unchanged from the bridge ---

    private static int displayInfo(Position prevPosition, Position nextPosition, Member currentMember,
            int memberOrdinal, Integer childrenCount) {
        if (currentMember instanceof PseudoLeafMember) {
            return 0;
        }
        int displayInfo = 0xffff & (childrenCount == null ? 0 : childrenCount);
        if (nextPosition != null) {
            Member nextMember = nextPosition.getMembers().get(memberOrdinal);
            if (currentMember.equals(nextMember.getParentMember())) {
                displayInfo |= 0x10000;
            }
        }
        if (prevPosition != null) {
            String currentParent = parentUniqueName(currentMember);
            Member prevMember = prevPosition.getMembers().get(memberOrdinal);
            if (currentParent != null && currentParent.equals(parentUniqueName(prevMember))) {
                displayInfo |= 0x20000;
            }
        }
        return displayInfo;
    }

    private static String parentUniqueName(Member member) {
        final Member parent = member.getParentMember();
        return parent == null ? null : parent.getUniqueName();
    }

    private static Object defaultValue(Property property) {
        Datatype datatype = property.getDatatype();
        if (datatype == null) {
            return null;
        }
        switch (datatype) {
        case UNSIGNED_INTEGER:
        case INTEGER:
            return 0;
        case DOUBLE:
            return 0d;
        case LARGE_INTEGER:
            return 0L;
        case BOOLEAN:
            return false;
        default:
            return null;
        }
    }

    private static String xsdType(Property property) {
        Datatype datatype = property.getDatatype();
        if (datatype == null) {
            return null;
        }
        switch (datatype) {
        case UNSIGNED_INTEGER:
            return "xsd:unsignedInt";
        case DOUBLE:
            return "xsd:double";
        case LARGE_INTEGER:
            return "xsd:long";
        case INTEGER:
            return "xsd:int";
        case BOOLEAN:
            return "xsd:boolean";
        default:
            return "xsd:string";
        }
    }

    private static boolean isValidProp(List<Position> positions, Property prop) {
        if (!(prop instanceof IDaanseOlap4jProperty own)) {
            return true;
        }
        for (Position pos : positions) {
            for (Member member : pos.getMembers()) {
                if (member.getHierarchy().getName().equals(own.getLevel().getHierarchy().getName())
                        && Objects.nonNull(member.getPropertyValue(own.getName()))) {
                    return true;
                }
            }
        }
        return false;
    }

    private static List<Property> props(CellSetAxisMetaData queryAxis) {
        if (queryAxis == null) {
            return DEFAULT_PROPS;
        }
        List<Property> composite = new ArrayList<>(DEFAULT_PROPS);
        composite.addAll(queryAxis.getProperties());
        return composite;
    }

    static List<Hierarchy> slicerAxisHierarchies(CellSet cellSet, boolean omitDefaultSlicerInfo) {
        Cube cube = cellSet.getMetaData().getCube();
        if (omitDefaultSlicerInfo) {
            return hierarchiesOf(cellSet.getFilterAxis());
        }
        List<Hierarchy> axisHierarchyList = new ArrayList<>();
        for (CellSetAxis axis : cellSet.getAxes()) {
            axisHierarchyList.addAll(hierarchiesOf(axis));
        }
        List<Dimension> unseen = new ArrayList<>(cube.getDimensions() != null ? cube.getDimensions() : List.of());
        for (Hierarchy onAxis : axisHierarchyList) {
            unseen.remove(onAxis.getDimension());
        }
        List<Hierarchy> hierarchies = new ArrayList<>();
        for (Dimension dimension : unseen) {
            hierarchies.addAll(dimension.getHierarchies());
        }
        return hierarchies;
    }

    static List<String> queryCellPropertyNames(CellSet cellSet) {
        List<String> names = new ArrayList<>();
        final Statement statement = cellSet.getStatement();
        for (QueryComponent queryPart : statement.getQuery().getCellProperties()) {
            org.eclipse.daanse.olap.api.query.component.CellProperty cellProperty = (org.eclipse.daanse.olap.api.query.component.CellProperty) queryPart;
            names.add(((NameSegment) Util.parseIdentifier(cellProperty.toString()).get(0)).getName());
        }
        if (names.isEmpty()) {
            names.add(VALUE);
            names.add(FORMATTED_VALUE);
        }
        return names;
    }

    private static Property rename(final Property property, final String name) {
        return new Property() {
            @Override
            public Datatype getDatatype() {
                return property.getDatatype();
            }

            @Override
            public Set<TypeFlag> getType() {
                return property.getType();
            }

            @Override
            public String getCaption() {
                return property.getCaption();
            }

            @Override
            public String getName() {
                return name;
            }
        };
    }
}
