/*
 * Copyright (c) 2026 Contributors to the Eclipse Foundation.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Stefan Bischof (bipolis.org) - initial
 */
package org.eclipse.daanse.olap.provider.memory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.daanse.olap.api.element.Cube;
import org.eclipse.daanse.olap.api.element.DimensionType;
import org.eclipse.daanse.olap.api.element.Hierarchy;
import org.eclipse.daanse.olap.element.DimensionBase;

/** A dimension the provider holds in memory. */
public class MemoryDimension extends DimensionBase {

    private static final Logger LOGGER = LoggerFactory.getLogger(MemoryDimension.class);

    private final MemoryCube cube;
    private final List<MemoryHierarchy> memoryHierarchies = new ArrayList<>();

    MemoryDimension(MemoryCube cube, String name, DimensionType dimensionType) {
        super(name, name, true, null, dimensionType);
        this.cube = cube;
        this.hierarchies = List.of();
    }

    /** Adds the hierarchy that carries this dimension's own name, with an All member. */
    public MemoryHierarchy hierarchy() {
        return hierarchy(getName(), true);
    }

    /** Adds a hierarchy with an All member. */
    public MemoryHierarchy hierarchy(String hierarchyName) {
        return hierarchy(hierarchyName, true);
    }

    /** Adds a hierarchy, with or without an All member. */
    public MemoryHierarchy hierarchy(String hierarchyName, boolean hasAll) {
        MemoryHierarchy h = new MemoryHierarchy(this, hierarchyName, hasAll);
        h.ordinalInCube(cube.nextHierarchyOrdinal());
        memoryHierarchies.add(h);
        // The base class reads its own field, so both have to see the same list.
        hierarchies = List.copyOf(memoryHierarchies);
        return h;
    }

    @Override
    public List<? extends Hierarchy> getHierarchies() {
        return Collections.unmodifiableList(memoryHierarchies);
    }

    @Override
    public Cube getCube() {
        return cube;
    }

    @Override
    protected Logger getLogger() {
        return LOGGER;
    }


    @Override
    public org.eclipse.daanse.olap.api.element.Catalog getCatalog() {
        return cube.getCatalog();
    }


    @Override
    public org.eclipse.daanse.olap.api.element.MetaData getMetaData() {
        return org.eclipse.daanse.olap.element.OlapMetaDataBase.empty();
    }

}
