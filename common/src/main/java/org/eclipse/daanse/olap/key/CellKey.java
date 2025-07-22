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
 * jhyde, 10 August, 2001
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


package org.eclipse.daanse.olap.key;

import java.io.Serializable;
import java.util.Arrays;

/**
 * A CellKey is used as a key in maps which access cells by their
 * position.
 *
 * CellKey is also used within
 * org.eclipse.daanse.rolap.common.agg.SparseSegmentDataset to store values within
 * aggregations.
 *
 * It is important that CellKey is memory-efficient, and that the
 * {@link Object#hashCode} and {@link Object#equals} methods are extremely
 * efficient. There are particular implementations for the most likely cases
 * where the number of axes is 1, 2, 3 and 4 as well as a general
 * implementation.
 *
 * To create a key, call the
 * org.eclipse.daanse.olap.common.CellKey.Generator#newCellKey(int[]) method.
 *
 * @author jhyde
 * @since 10 August, 2001
 */
public interface CellKey extends Serializable {
    /**
     * Returns the number of axes.
     *
     * @return number of axes
     */
    int size();

    /**
     * Returns the axis keys as an array.
     *
     * Note: caller should treat the array as immutable. If the contents of
     * the array are modified, behavior is unspecified.
     *
     * @return Array of axis keys
     */
    int[] getOrdinals();

    /**
     * This method make a copy of the int array parameter.
     * Throws a RuntimeException if the int array size is not the
     * size of the CellKey.
     *
     * @param pos Array of axis keys
     */
    void setOrdinals(int[] pos);

    /**
     * Returns the axisth axis value.
     *
     * @param axis Axis ordinal
     * @return Value of the axisth axis
     * @throws ArrayIndexOutOfBoundsException if axis is out of range
     */
    int getAxis(int axis);

    /**
     * Sets a given axis.
     *
     * @param axis Axis ordinal
     * @param value Value
     * @throws RuntimeException if axis parameter is larger than {@link #size()}
     */
    void setAxis(int axis, int value);

    /**
     * Returns a mutable copy of this CellKey.
     *
     * @return Mutable copy
     */
    CellKey copy();

    /**
     * Returns the offset of the cell in a raster-scan order.
     *
     * For example, if the axes have lengths {2, 5, 10} then cell (2, 3, 4)
     * has offset
     *
     * 
     * (2 * mulitiplers[0]) + (3 * multipliers[1]) + (4 * multipliers[2])
     * = (2 * 50) + (3 * 10) + (4 * 1)
     * = 134
     *
     * The multipliers are the product of the lengths of all later axes, in
     * this case (50, 10, 1).
     *
     * @param axisMultipliers For each axis, the product of the lengths of later
     *     axes
     * @return The raster-scan ordinal of this cell
     */
    int getOffset(int[] axisMultipliers);

    public abstract class Generator {

        private Generator() {
        }

        /**
         * Creates a CellKey with a given number of axes.
         *
         * @param size Number of axes
         * @return new CellKey
         */
        public static CellKey newCellKey(int size) {
            switch (size) {
            case 0:
                return Zero.INSTANCE;
            case 1:
                return new One(0);
            case 2:
                return new Two(0, 0);
            case 3:
                return new Three(0, 0, 0);
            case 4:
                return new Four(0, 0, 0, 0);
            default:
                return new Many(new int[size]);
            }
        }

        /**
         * Creates a CellKey populated with the given coordinates.
         *
         * @param pos Coordinate array
         * @return CellKey
         */
        public static CellKey newCellKey(int[] pos) {
            switch (pos.length) {
            case 0:
                return Zero.INSTANCE;
            case 1:
                return new One(pos[0]);
            case 2:
                return new Two(pos[0], pos[1]);
            case 3:
                return new Three(pos[0], pos[1], pos[2]);
            case 4:
                return new Four(pos[0], pos[1], pos[2], pos[3]);
            default:
                return new Many(pos.clone());
            }
        }

        /**
         * Creates a CellKey implemented by an array to hold the coordinates,
         * regardless of the size.
         *
         * This is used for testing only. The CellKey will fail to compare
         * equal to naturally created keys of size 0, 1, 2 or 3.
         *
         * @param size Number of coordinates
         * @return CallKey
         */
        public static CellKey newManyCellKey(int size) {
            return new Many(new int[size]);
        }

        public static int getOffset(
            int[] ordinals,
            int[] axisMultipliers)
        {
            // TODO: We know that axisMultiplers[ordinals.length - 1] == 1. Can
            // we avoid multiplying by it?
            int offset = 0;
            for (int i = 0; i < ordinals.length; i++) {
                offset += ordinals[i] * axisMultipliers[i];
            }
            return offset;
        }
    }

    public class Zero implements CellKey {
        private static final long serialVersionUID = 6063541581473797367L;
        private static final int[] EMPTY_INT_ARRAY = new int[0];
        public static final Zero INSTANCE = new Zero();

        /**
         * Use singleton {@link #INSTANCE}.
         */
        private Zero() {
        }

        @Override
		public Zero copy() {
            // no need to make copy since there is no state
            return this;
        }

        @Override
		public int getOffset(int[] axisMultipliers) {
            return 0;
        }

        @Override
		public boolean equals(Object o) {
            return o == this;
        }

        @Override
		public int hashCode() {
            return 11;
        }

        @Override
		public int size() {
            return 0;
        }

        @Override
		public int[] getOrdinals() {
            return EMPTY_INT_ARRAY;
        }

        @Override
		public void setOrdinals(int[] pos) {
            if (pos.length != 0) {
                throw new IllegalArgumentException();
            }
        }

        @Override
		public int getAxis(int axis) {
            throw new ArrayIndexOutOfBoundsException(axis);
        }

        @Override
		public void setAxis(int axis, int value) {
            throw new ArrayIndexOutOfBoundsException(axis);
        }
    }

    public class One implements CellKey {
        private static final long serialVersionUID = 2160238882970820960L;
        private int ordinal0;

        /**
         * Creates a One.
         *
         * @param ordinal0 Ordinate #0
         */
        private One(int ordinal0) {
            this.ordinal0 = ordinal0;
        }

        @Override
		public int size() {
            return 1;
        }

        @Override
		public int[] getOrdinals() {
            return new int[] {ordinal0};
        }

        @Override
		public void setOrdinals(int[] pos) {
            if (pos.length != 1) {
                throw new IllegalArgumentException();
            }
            ordinal0 = pos[0];
        }

        @Override
		public int getAxis(int axis) {
            if (axis == 0){
                return ordinal0;
            } else {
                throw new ArrayIndexOutOfBoundsException(axis);
            }
        }

        @Override
		public void setAxis(int axis, int value) {
            if (axis == 0) {
                ordinal0 = value;
            } else {
                throw new ArrayIndexOutOfBoundsException(axis);
            }
        }

        @Override
		public One copy() {
            return new One(ordinal0);
        }

        @Override
		public int getOffset(int[] axisMultipliers) {
            return ordinal0;
        }

        @Override
		public boolean equals(Object o) {
            // here we cheat, we know that all CellKey's will be the same size
            if (o instanceof One other) {
                return (this.ordinal0 == other.ordinal0);
            } else {
                return false;
            }
        }

        @Override
		public String toString() {
            return new StringBuilder("(").append(ordinal0).append(")").toString();
        }

        @Override
		public int hashCode() {
            return 17 + ordinal0;
        }
    }

    public class Two implements CellKey {
        private static final long serialVersionUID = 1901188836648369359L;
        private int ordinal0;
        private int ordinal1;

        /**
         * Creates a Two.
         *
         * @param ordinal0 Ordinate #0
         * @param ordinal1 Ordinate #1
         */
        private Two(int ordinal0, int ordinal1) {
            this.ordinal0 = ordinal0;
            this.ordinal1 = ordinal1;
        }

        @Override
		public String toString() {
            return new StringBuilder("(").append(ordinal0).append(", ").append(ordinal1).append(")").toString();
        }

        @Override
		public Two copy() {
            return new Two(ordinal0, ordinal1);
        }

        @Override
		public int getOffset(int[] axisMultipliers) {
            return ordinal0 * axisMultipliers[0]
                + ordinal1;
        }

        @Override
		public boolean equals(Object o) {
            if (o instanceof Two other) {
                return (other.ordinal0 == this.ordinal0)
                    && (other.ordinal1 == this.ordinal1);
            } else {
                return false;
            }
        }

        @Override
		public int hashCode() {
            int h0 = 17 + ordinal0;
            return h0 * 37 + ordinal1;
        }

        @Override
		public int size() {
            return 2;
        }

        @Override
		public int[] getOrdinals() {
            return new int[] {ordinal0, ordinal1};
        }

        @Override
		public void setOrdinals(int[] pos) {
            if (pos.length != 2) {
                throw new IllegalArgumentException();
            }
            ordinal0 = pos[0];
            ordinal1 = pos[1];
        }

        @Override
		public int getAxis(int axis) {
            switch (axis) {
            case 0:
                return ordinal0;
            case 1:
                return ordinal1;
            default:
                throw new ArrayIndexOutOfBoundsException(axis);
            }
        }

        @Override
		public void setAxis(int axis, int value) {
            switch (axis) {
            case 0:
                ordinal0 = value;
                break;
            case 1:
                ordinal1 = value;
                break;
            default:
                throw new ArrayIndexOutOfBoundsException(axis);
            }
        }
    }

    class Three implements CellKey {
        private static final long serialVersionUID = -2645858781233421151L;
        private int ordinal0;
        private int ordinal1;
        private int ordinal2;

        /**
         * Creates a Three.
         *
         * @param ordinal0 Ordinate #0
         * @param ordinal1 Ordinate #1
         * @param ordinal2 Ordinate #2
         */
        private Three(int ordinal0, int ordinal1, int ordinal2) {
            this.ordinal0 = ordinal0;
            this.ordinal1 = ordinal1;
            this.ordinal2 = ordinal2;
        }

        @Override
		public String toString() {
            return new StringBuilder("(").append(ordinal0).append(", ")
                .append(ordinal1).append(", ").append(ordinal2).append(")").toString();
        }

        @Override
		public Three copy() {
            return new Three(ordinal0, ordinal1, ordinal2);
        }

        @Override
		public int getOffset(int[] axisMultipliers) {
            return ordinal0 * axisMultipliers[0]
                + ordinal1 * axisMultipliers[1]
                + ordinal2;
        }

        @Override
		public boolean equals(Object o) {
            // here we cheat, we know that all CellKey's will be the same size
            if (o instanceof Three other) {
                return (other.ordinal0 == this.ordinal0)
                    && (other.ordinal1 == this.ordinal1)
                    && (other.ordinal2 == this.ordinal2);
            } else {
                return false;
            }
        }

        @Override
		public int hashCode() {
            int h0 = 17 + ordinal0;
            int h1 = h0 * 37 + ordinal1;
            return h1 * 37 + ordinal2;
        }

        @Override
		public int getAxis(int axis) {
            switch (axis) {
            case 0:
                return ordinal0;
            case 1:
                return ordinal1;
            case 2:
                return ordinal2;
            default:
                throw new ArrayIndexOutOfBoundsException(axis);
            }
        }

        @Override
		public void setAxis(int axis, int value) {
            switch (axis) {
            case 0:
                ordinal0 = value;
                break;
            case 1:
                ordinal1 = value;
                break;
            case 2:
                ordinal2 = value;
                break;
            default:
                throw new ArrayIndexOutOfBoundsException(axis);
            }
        }

        @Override
		public int size() {
            return 3;
        }

        @Override
		public int[] getOrdinals() {
            return new int[] {ordinal0, ordinal1, ordinal2};
        }

        @Override
		public void setOrdinals(int[] pos) {
            if (pos.length != 3) {
                throw new IllegalArgumentException();
            }
            ordinal0 = pos[0];
            ordinal1 = pos[1];
            ordinal2 = pos[2];
        }
    }

    class Four implements CellKey {
        private static final long serialVersionUID = -2645858781233421151L;
        private int ordinal0;
        private int ordinal1;
        private int ordinal2;
        private int ordinal3;

        /**
         * Creates a Four.
         */
        private Four(
            int ordinal0,
            int ordinal1,
            int ordinal2,
            int ordinal3)
        {
            this.ordinal0 = ordinal0;
            this.ordinal1 = ordinal1;
            this.ordinal2 = ordinal2;
            this.ordinal3 = ordinal3;
        }

        @Override
		public String toString() {
            return
                new StringBuilder("(").append(ordinal0).append(", ").append(ordinal1)
                .append(", ").append(ordinal2).append(", ").append(ordinal3).append(")").toString();
        }

        @Override
		public Four copy() {
            return new Four(ordinal0, ordinal1, ordinal2, ordinal3);
        }

        @Override
		public int getOffset(int[] axisMultipliers) {
            return ordinal0 * axisMultipliers[0]
                + ordinal1 * axisMultipliers[1]
                + ordinal2 * axisMultipliers[2]
                + ordinal3;
        }

        @Override
		public boolean equals(Object o) {
            // here we cheat, we know that all CellKey's will be the same size
            if (o instanceof Four other) {
                return (other.ordinal0 == this.ordinal0)
                    && (other.ordinal1 == this.ordinal1)
                    && (other.ordinal2 == this.ordinal2)
                    && (other.ordinal3 == this.ordinal3);
            } else {
                return false;
            }
        }

        @Override
		public int hashCode() {
            int h0 = 17 + ordinal0;
            int h1 = h0 * 37 + ordinal1;
            int h2 = h1 * 37 + ordinal2;
            return h2 * 37 + ordinal3;
        }

        @Override
		public int getAxis(int axis) {
            switch (axis) {
            case 0:
                return ordinal0;
            case 1:
                return ordinal1;
            case 2:
                return ordinal2;
            case 3:
                return ordinal3;
            default:
                throw new ArrayIndexOutOfBoundsException(axis);
            }
        }

        @Override
		public void setAxis(int axis, int value) {
            switch (axis) {
            case 0:
                ordinal0 = value;
                break;
            case 1:
                ordinal1 = value;
                break;
            case 2:
                ordinal2 = value;
                break;
            case 3:
                ordinal3 = value;
                break;
            default:
                throw new ArrayIndexOutOfBoundsException(axis);
            }
        }

        @Override
		public int size() {
            return 4;
        }

        @Override
		public int[] getOrdinals() {
            return new int[] {ordinal0, ordinal1, ordinal2, ordinal3};
        }

        @Override
		public void setOrdinals(int[] pos) {
            if (pos.length != 4) {
                throw new IllegalArgumentException();
            }
            ordinal0 = pos[0];
            ordinal1 = pos[1];
            ordinal2 = pos[2];
            ordinal3 = pos[3];
        }
    }

    public class Many implements CellKey {
        private static final long serialVersionUID = 3438398157192694834L;
        private final int[] ordinals;

        /**
         * Creates a Many.
         * @param ordinals Ordinates
         */
        protected Many(int[] ordinals) {
            this.ordinals = ordinals;
        }

        @Override
		public final int size() {
            return this.ordinals.length;
        }

        @Override
		public final void setOrdinals(int[] pos) {
            if (ordinals.length != pos.length) {
                throw new IllegalArgumentException();
            }
            System.arraycopy(pos, 0, this.ordinals, 0, ordinals.length);
        }

        @Override
		public final int[] getOrdinals() {
            return this.ordinals;
        }

        @Override
		public void setAxis(int axis, int value) {
            this.ordinals[axis] = value;
        }

        @Override
		public int getAxis(int axis) {
            return this.ordinals[axis];
        }

        @Override
		public String toString() {
            StringBuilder buf = new StringBuilder();
            buf.append('(');
            for (int i = 0; i < ordinals.length; i++) {
                if (i > 0) {
                    buf.append(',');
                }
                buf.append(ordinals[i]);
            }
            buf.append(')');
            return buf.toString();
        }

        @Override
		public Many copy() {
            return new Many(this.ordinals.clone());
        }

        @Override
		public int getOffset(int[] axisMultipliers) {
            return Generator.getOffset(ordinals, axisMultipliers);
        }

        @Override
		public int hashCode() {
            int h = 17;
            for (int ordinal : ordinals) {
                h = (h * 37) + ordinal;
            }
            return h;
        }

        @Override
		public boolean equals(Object o) {
            if (o instanceof Many that) {
                return Arrays.equals(this.ordinals, that.ordinals);
            }
            return false;
        }
    }
}
