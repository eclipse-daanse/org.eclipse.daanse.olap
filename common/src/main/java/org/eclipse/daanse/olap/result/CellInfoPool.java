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

import org.eclipse.daanse.olap.api.exception.OlapRuntimeException;
import org.eclipse.daanse.olap.util.ObjectPool;

/**
 * {@link CellInfoContainer} over an {@link ObjectPool}, with the cell position
 * folded into one long key by a {@link CellKeyMaker} for zero to four axes.
 * <p>
 * The key uses a 'large number' per axis, greater than the number of members on
 * that axis; the product over all axes must stay below {@link Long#MAX_VALUE}
 * or keys collide. One axis: up to the long maximum. Two: its square root,
 * slightly above {@link Integer#MAX_VALUE}. Three: the cube root, about
 * 2,000,000. Four: about 50,000. Five or more axes fall back to
 * {@link CellInfoMap}.
 */
public class CellInfoPool implements CellInfoContainer {
    /** Members per axis when there are 2 axes. */
    protected static final long MAX_AXIS_SIZE_2 = 2147483647;
    /** Members per axis when there are 3 axes. */
    protected static final long MAX_AXIS_SIZE_3 = 2000000;
    /** Members per axis when there are 4 axes. */
    protected static final long MAX_AXIS_SIZE_4 = 50000;

    /** Converts the cell position integer array to a long. */
    interface CellKeyMaker {
        long generate(int[] pos);
    }

    /** For 0 axes. */
    static class Zero implements CellKeyMaker {
        @Override
        public long generate(int[] pos) {
            return 0;
        }
    }

    /** For 1 axis. */
    static class One implements CellKeyMaker {
        @Override
        public long generate(int[] pos) {
            return pos[0];
        }
    }

    /** For 2 axes. */
    static class Two implements CellKeyMaker {
        @Override
        public long generate(int[] pos) {
            long l = pos[0];
            l += (MAX_AXIS_SIZE_2 * pos[1]);
            return l;
        }
    }

    /** For 3 axes. */
    static class Three implements CellKeyMaker {
        @Override
        public long generate(int[] pos) {
            long l = pos[0];
            l += (MAX_AXIS_SIZE_3 * pos[1]);
            l += (MAX_AXIS_SIZE_3 * MAX_AXIS_SIZE_3 * pos[2]);
            return l;
        }
    }

    /** For 4 axes. */
    static class Four implements CellKeyMaker {
        @Override
        public long generate(int[] pos) {
            long l = pos[0];
            l += (MAX_AXIS_SIZE_4 * pos[1]);
            l += (MAX_AXIS_SIZE_4 * MAX_AXIS_SIZE_4 * pos[2]);
            l += (MAX_AXIS_SIZE_4 * MAX_AXIS_SIZE_4 * MAX_AXIS_SIZE_4 * pos[3]);
            return l;
        }
    }

    private final ObjectPool<CellInfo> cellInfoPool;
    private final CellKeyMaker cellKeyMaker;

    public CellInfoPool(int axisLength) {
        this.cellInfoPool = new ObjectPool<>();
        this.cellKeyMaker = createCellKeyMaker(axisLength);
    }

    public CellInfoPool(int axisLength, int initialSize) {
        this.cellInfoPool = new ObjectPool<>(initialSize);
        this.cellKeyMaker = createCellKeyMaker(axisLength);
    }

    static CellKeyMaker createCellKeyMaker(int axisLength) {
        return switch (axisLength) {
            case 0 -> new Zero();
            case 1 -> new One();
            case 2 -> new Two();
            case 3 -> new Three();
            case 4 -> new Four();
            default -> throw new OlapRuntimeException("Creating CellInfoPool with axisLength=" + axisLength);
        };
    }

    @Override
    public int size() {
        return this.cellInfoPool.size();
    }

    @Override
    public void trimToSize() {
        this.cellInfoPool.trimToSize();
    }

    @Override
    public void clear() {
        this.cellInfoPool.clear();
    }

    @Override
    public CellInfo create(int[] pos) {
        long key = this.cellKeyMaker.generate(pos);
        return this.cellInfoPool.add(new CellInfo(key));
    }

    @Override
    public CellInfo lookup(int[] pos) {
        return create(pos);
    }
}
