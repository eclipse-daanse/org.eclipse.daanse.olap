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

import java.util.HashMap;
import java.util.Map;

import org.eclipse.daanse.olap.key.CellKey;

/**
 * {@link CellInfoContainer} over a {@link Map}.
 * <p>
 * The {@link CellKey} point is the same object (not a copy) that is used and
 * modified during the recursive evaluation; {@link #create} relies on this.
 */
public class CellInfoMap implements CellInfoContainer {
    private final Map<CellKey, CellInfo> map;
    private final CellKey point;

    /**
     * Creates a CellInfoMap.
     *
     * @param point Cell position
     */
    public CellInfoMap(CellKey point) {
        this.point = point;
        this.map = new HashMap<>();
    }

    @Override
    public int size() {
        return this.map.size();
    }

    @Override
    public void trimToSize() {
        // empty
    }

    @Override
    public void clear() {
        this.map.clear();
    }

    @Override
    public CellInfo create(int[] pos) {
        CellKey key = this.point.copy();
        return map.computeIfAbsent(key, k -> new CellInfo(0));
    }

    @Override
    public CellInfo lookup(int[] pos) {
        CellKey key = CellKey.Generator.newCellKey(pos);
        return this.map.get(key);
    }
}
