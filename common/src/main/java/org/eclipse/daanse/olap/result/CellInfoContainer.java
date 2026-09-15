/*
 * This software is subject to the terms of the Eclipse Public License v1.0
 * Agreement, available at the following URL:
 * http://www.eclipse.org/legal/epl-v10.html.
 * You must accept the terms of that agreement to use this software.
 *
 * Copyright (C) 2001-2005 Julian Hyde
 * Copyright (C) 2005-2017 Hitachi Vantara and others
 * All Rights Reserved.
 *
 * ---- All changes after Fork in 2023 ------------------------
 *
 * Project: Eclipse daanse
 *
 * Copyright (c) 2023 Contributors to the Eclipse Foundation.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors after Fork in 2023:
 *   SmartCity Jena - initial
 */
package org.eclipse.daanse.olap.result;

/**
 * Creation and lookup of {@link CellInfo} objects by cell position. Two
 * implementations: {@link CellInfoMap} over a map, {@link CellInfoPool} over an
 * object pool with a long key derived from the position.
 */
public interface CellInfoContainer {
    /** Returns the number of CellInfo objects in this container. */
    int size();

    /**
     * Reduces the size of the internal data structures needed to support the
     * current entries. Call after all CellInfo objects have been added.
     */
    void trimToSize();

    /** Removes all CellInfo objects. Does not change the size of the internal data structures. */
    void clear();

    /**
     * Creates a new CellInfo object at location {@code pos} and returns it.
     *
     * @param pos where to store the CellInfo object
     * @return the newly created CellInfo object
     */
    CellInfo create(int[] pos);

    /**
     * Gets the CellInfo object at location {@code pos}.
     *
     * @param pos where to find the CellInfo object
     * @return the CellInfo found, or null
     */
    CellInfo lookup(int[] pos);

    /**
     * The container for a result with {@code axisCount} axes: pooled with a long
     * key up to four axes, map-backed beyond that.
     */
    static CellInfoContainer forAxes(int axisCount, org.eclipse.daanse.olap.key.CellKey point) {
        return axisCount <= 4 ? new CellInfoPool(axisCount) : new CellInfoMap(point);
    }
}
