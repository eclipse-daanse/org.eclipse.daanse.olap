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

import java.util.ArrayList;
import java.util.List;

/**
 * Collects the cubes of a catalog and closes it.
 *
 * <p>The cube, dimension, hierarchy, level and member builders are the element objects
 * themselves, so the shape of the code follows the shape of the catalog:
 *
 * <pre>
 * MemoryCatalogBuilder builder = MemoryCatalog.builder("Test");
 * MemoryCube cube = builder.cube("Test");
 *
 * MemoryHierarchy geo = cube.dimension("Geo").hierarchy("Geo");
 * geo.level("(All)").level("Region").level("City");
 * MemoryMember allGeo = geo.member("All Geo");
 * allGeo.child("North").children("A", "B");
 * allGeo.child("South").children("C", "D", "E");
 *
 * MemoryCatalog catalog = builder.build();
 * </pre>
 */
public class MemoryCatalogBuilder {

    private final String name;
    private final List<MemoryCube> cubes = new ArrayList<>();

    MemoryCatalogBuilder(String name) {
        this.name = name;
    }

    /** Adds a cube and returns it, so that its dimensions can be declared on it. */
    public MemoryCube cube(String cubeName) {
        MemoryCube cube = new MemoryCube(cubeName);
        cubes.add(cube);
        return cube;
    }

    /** Closes the catalog. */
    public MemoryCatalog build() {
        if (cubes.isEmpty()) {
            throw new IllegalStateException("catalog " + name + " has no cube");
        }
        return new MemoryCatalog(name, cubes);
    }
}
