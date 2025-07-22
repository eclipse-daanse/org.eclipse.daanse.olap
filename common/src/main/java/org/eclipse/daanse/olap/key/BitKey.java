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
 * jhyde, 30 August, 2001
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
import java.util.BitSet;
import java.util.Iterator;

/**
 * Represents a set of bits.
 *
 * Unlike {@link java.util.BitSet}, the number of bits cannot be changed
 * after the BitKey is created. This allows us to optimize.
 *
 * If you have a collection of immutable objects, each of which has a unique
 * positive number and you wish to do comparisons between subsets of those
 * objects testing for equality, then encoding the subsets as BitKeys is very
 * efficient.
 *
 * There are two implementations that target groups of objects with maximum
 * number less than 64 and less than 128; and there is one implements that is
 * general for any positive number.
 *
 * One caution: if the maximum number assigned to one of the
 * objects is large, then this representation might be sparse and therefore
 * not efficient.
 *
 * @author Richard M. Emberson
 */
public interface BitKey
        extends Serializable, Comparable<BitKey>, Iterable<Integer>
{
    /**
     * The BitKey with no bits set.
     */
    BitKey EMPTY = Factory.makeBitKey(0);

    /**
     * Sets the bit at the specified index to the specified value.
     */
    void set(int bitIndex, boolean value);

    /**
     * Sets the bit at the specified index to <code>true</code>.
     */
    void set(int bitIndex);

    /**
     * Returns the value of the bit with the specified index. The value
     * is true if the bit with the index bitIndex
     * is currently set in this BitKey; otherwise, the result
     * is false.
     */
    boolean get(int bitIndex);

    /**
     * Sets the bit specified by the index to <code>false</code>.
     */
    void clear(int bitIndex);

    /**
     * Sets all of the bits in this BitKey to <code>false</code>.
     */
    void clear();

    /**
     * Is every bit set in the parameter bitKey also set in
     * this.
     * If one switches this with the parameter bitKey
     * one gets the equivalent of isSubSetOf.
     *
     * @param bitKey Bit key
     */
    boolean isSuperSetOf(BitKey bitKey);

    /**
     * Or the parameter <code>BitKey</code> with <code>this</code>.
     *
     * @param bitKey Bit key
     */
    BitKey or(BitKey bitKey);

    /**
     * XOr the parameter BitKey with this.
     *
     * @param bitKey Bit key
     */
    BitKey orNot(BitKey bitKey);

    /**
     * Returns the boolean AND of this bitkey and the given bitkey.
     *
     * @param bitKey Bit key
     */
    BitKey and(BitKey bitKey);

    /**
     * Returns a BitKey containing all of the bits in this
     * BitSet whose corresponding
     * bit is NOT set in the specified BitSet.
     */
    BitKey andNot(BitKey bitKey);

    /**
     * Returns a copy of this BitKey.
     *
     * @return copy of BitKey
     */
    BitKey copy();

    /**
     * Returns an empty BitKey of the same type. This is the same
     * as calling {@link #copy} followed by {@link #clear()}.
     *
     * @return BitKey of same type
     */
    BitKey emptyCopy();

    /**
     * Returns true if this BitKey contains no bits that are set
     * to true.
     */
    boolean isEmpty();

    /**
     * Returns whether this BitKey has any bits in common with a given BitKey.
     */
    boolean intersects(BitKey bitKey);

    /**
     * Returns a {@link BitSet} with the same contents as this BitKey.
     */
    BitSet toBitSet();

    /**
     * An Iterator over the bit positions.
     * For example, if the BitKey had positions 3 and 4 set, then
     * the Iterator would return the values 3 and then 4. The bit
     * positions returned by the iterator are in the order, from
     * smallest to largest, as they are set in the BitKey.
     */
    @Override
	Iterator<Integer> iterator();

    /**
     * Returns the index of the first bit that is set to <code>true</code>
     * that occurs on or after the specified starting index. If no such
     * bit exists then -1 is returned.
     *
     * To iterate over the true bits in a BitKey,
     * use the following loop:
     *
     * 
     * for (int i = bk.nextSetBit(0); i >= 0; i = bk.nextSetBit(i + 1)) {
     *     // operate on index i here
     * }
     *
     * @param   fromIndex the index to start checking from (inclusive)
     * @return  the index of the next set bit
     * @throws  IndexOutOfBoundsException if the specified index is negative
     */
    int nextSetBit(int fromIndex);

    /**
     * Returns the number of bits set.
     *
     * @return Number of bits set
     */
    int cardinality();

    public abstract class Factory {

        private Factory() {
            // constructor
        }

        /**
         * Creates a {@link BitKey} with a capacity for a given number of bits.
         * @param size Number of bits in key
         */
        public static BitKey makeBitKey(int size) {
            return makeBitKey(size, false);
        }

        /**
         * Creates a {@link BitKey} with a capacity for a given number of bits.
         * @param size Number of bits in key
         * @param init The default value of all bits.
         */
        public static BitKey makeBitKey(int size, boolean init) {
            if (size < 0) {
                String msg = new StringBuilder("Negative size \"").append(size).append("\" not allowed").toString();
                throw new IllegalArgumentException(msg);
            }
            final BitKey bk;
            if (size < 64) {
                bk = new BitKey.Small();
            } else if (size < 128) {
                bk = new BitKey.Mid128();
            } else {
                bk = new BitKey.Big(size);
            }
            if (init) {
                for (int i = 0; i < size; i++) {
                    bk.set(i, init);
                }
            }
            return bk;
        }

        /**
         * Creates a {@link BitKey} with the same contents as a {@link BitSet}.
         */
        public static BitKey makeBitKey(BitSet bitSet) {
            BitKey bitKey = makeBitKey(bitSet.length());
            for (int i = bitSet.nextSetBit(0);
                i >= 0;
                i = bitSet.nextSetBit(i + 1))
            {
                bitKey.set(i);
            }
            return bitKey;
        }
    }

    /**
     * Abstract implementation of {@link BitKey}.
     */
    abstract class AbstractBitKey implements BitKey {
        private static final long serialVersionUID = -2942302671676103450L;
        // chunk is a long, which has 64 bits
        protected static final int CHUNK_BIT_COUNT = 6;
        protected static final int MASK = 63;
        protected static final long WORD_MASK = 0xffffffffffffffffL;

        /**
         * Creates a chunk containing a single bit.
         */
        protected static long bit(int pos) {
            return (1L << (pos & MASK));
        }

        /**
         * Returns which chunk a given bit falls into.
         * Bits 0 to 63 fall in chunk 0, bits 64 to 127 fall into chunk 1.
         */
        protected static int chunkPos(int size) {
            return (size >> CHUNK_BIT_COUNT);
        }

        /**
         * Returns the number of chunks required for a given number of bits.
         *
         * <p>0 bits requires 0 chunks; 1 - 64 bits requires 1 chunk; etc.
         */
        protected static int chunkCount(int size) {
            return (size + 63) >> CHUNK_BIT_COUNT;
        }

        /**
         * Returns the number of one-bits in the two's complement binary
         * representation of the specified long value.  This function
         * is sometimes referred to as the population count.
         *
         * (Copied from {@link java.lang.Long#bitCount(long)}, which was
         * introduced in JDK 1.5, but we need the functionality in JDK 1.4.)
         *
         * @return the number of one-bits in the two's complement binary
         *     representation of the specified long value.
         * @since 1.5
         */
         protected static int bitCount(long i) {
            i = i - ((i >>> 1) & 0x5555555555555555L);
            i = (i & 0x3333333333333333L) + ((i >>> 2) & 0x3333333333333333L);
            i = (i + (i >>> 4)) & 0x0f0f0f0f0f0f0f0fL;
            i = i + (i >>> 8);
            i = i + (i >>> 16);
            i = i + (i >>> 32);
            return (int)i & 0x7f;
        }

        @Override
		public final void set(int pos, boolean value) {
            if (value) {
                set(pos);
            } else {
                clear(pos);
            }
        }

        /**
         * Copies a byte into a bit set at a particular position.
         *
         * @param bitSet Bit set
         * @param pos Position
         * @param x Byte
         */
        protected static void copyFromByte(BitSet bitSet, int pos, byte x)
        {
            if (x == 0) {
                return;
            }
            if ((x & 0x01) != 0) {
                bitSet.set(pos, true);
            }
            ++pos;
            if ((x & 0x02) != 0) {
                bitSet.set(pos, true);
            }
            ++pos;
            if ((x & 0x04) != 0) {
                bitSet.set(pos, true);
            }
            ++pos;
            if ((x & 0x08) != 0) {
                bitSet.set(pos, true);
            }
            ++pos;
            if ((x & 0x10) != 0) {
                bitSet.set(pos, true);
            }
            ++pos;
            if ((x & 0x20) != 0) {
                bitSet.set(pos, true);
            }
            ++pos;
            if ((x & 0x40) != 0) {
                bitSet.set(pos, true);
            }
            ++pos;
            if ((x & 0x80) != 0) {
                bitSet.set(pos, true);
            }
        }

        /**
         * Copies a {@code long} value (interpreted as 64 bits) into a bit set.
         *
         * @param bitSet Bit set
         * @param pos Position
         * @param x Byte
         */
        protected static void copyFromLong(
            final BitSet bitSet,
            int pos,
            long x)
        {
            while (x != 0) {
                copyFromByte(bitSet, pos, (byte) (x & 0xff));
                x >>>= 8;
                pos += 8;
            }
        }

        protected IllegalArgumentException createException(BitKey bitKey) {
            final String msg = (bitKey == null)
                ? "Null BitKey"
                : "Bad BitKey type: " + bitKey.getClass().getName();
            return new IllegalArgumentException(msg);
        }

        /**
         * Compares a pair of {@code long} arrays, using unsigned comparison
         * semantics and padding to the left with 0s.
         *
         * Values are treated as unsigned for the purposes of comparison.
         *
         * If the arrays have different lengths, the shorter is padded with
         * 0s.
         *
         * @param a1 First array
         * @param a2 Second array
         * @return -1 if a1 compares less to a2,
         * 0 if a1 is equal to a2,
         * 1 if a1 is greater than a2
         */
        public static int compareUnsignedArrays(long[] a1, long[] a2) {
            int i1 = a1.length - 1;
            int i2 = a2.length - 1;
            if (i1 > i2) {
                do {
                    if (a1[i1] != 0) {
                        return 1;
                    }
                    --i1;
                } while (i1 > i2);
            } else if (i2 > i1) {
                do {
                    if (a2[i2] != 0) {
                        return -1;
                    }
                    --i2;
                } while (i2 > i1);
            }
            assert i1 == i2;
            for (; i1 >= 0; --i1) {
                int c = compareUnsigned(a1[i1], a2[i1]);
                if (c != 0) {
                    return c;
                }
            }
            return 0;
        }

        /**
         * Performs unsigned comparison on two {@code long} values.
         *
         * @param i1 First value
         * @param i2 Second value
         * @return -1 if i1 is less than i2,
         * 1 if i1 is greater than i2,
         * 0 if i1 equals i2
         */
        public static int compareUnsigned(long i1, long i2) {
            // We want to do unsigned comparison.
            // Signed comparison returns the correct result except
            // if i1<0 & i2>=0
            // or i1>=0 & i2<0
            if (i1 == i2) {
                return 0;
            } else if ((i1 < 0) == (i2 < 0)) {
                // Same signs, signed comparison gives the right result
                return i1 < i2 ? -1 : 1;
            } else {
                // Different signs, use signed comparison and invert the result
                return i1 < i2 ? 1 : -1;
            }
        }
    }

    /**
     * Implementation of {@link BitKey} for bit counts less than 64.
     */
    public class Small extends AbstractBitKey {

        private static final long serialVersionUID = -7891880560056571197L;
        public static final String REMOVE = "remove";
        private long bits;

        /**
         * Creates a Small with no bits set.
         */
        private Small() {
        }

        /**
         * Creates a Small and initializes it to the 64 bit value.
         *
         * @param bits 64 bit value
         */
        private Small(long bits) {
            this.bits = bits;
        }

        @Override
		public void set(int pos) {
            if (pos < 64) {
                bits |= bit(pos);
            } else {
                throw new IllegalArgumentException(
                    new StringBuilder("pos ").append(pos).append(" exceeds capacity 64").toString());
            }
        }

        @Override
		public boolean get(int pos) {
            return pos < 64 && ((bits & bit(pos)) != 0);
        }

        @Override
		public void clear(int pos) {
            bits &= ~bit(pos);
        }

        @Override
		public void clear() {
            bits = 0;
        }

        @Override
		public int cardinality() {
            return bitCount(bits);
        }

        private void or(long bits) {
            this.bits |= bits;
        }

        private void orNot(long bits) {
            this.bits ^= bits;
        }

        private void and(long bits) {
            this.bits &= bits;
        }

        @Override
		public BitKey or(BitKey bitKey) {
            if (bitKey instanceof BitKey.Small other) {
                final BitKey.Small bk = (BitKey.Small) copy();
                bk.or(other.bits);
                return bk;

            } else if (bitKey instanceof BitKey.Mid128 other) {
                final BitKey.Mid128 bk = (BitKey.Mid128) other.copy();
                bk.or(this.bits, 0);
                return bk;

            } else if (bitKey instanceof BitKey.Big other) {
                final BitKey.Big bk = (BitKey.Big) other.copy();
                bk.or(this.bits);
                return bk;
            }

            throw createException(bitKey);
        }

        @Override
		public BitKey orNot(BitKey bitKey) {
            if (bitKey instanceof BitKey.Small other) {
                final BitKey.Small bk = (BitKey.Small) copy();
                bk.orNot(other.bits);
                return bk;

            } else if (bitKey instanceof BitKey.Mid128 other) {
                final BitKey.Mid128 bk = (BitKey.Mid128) other.copy();
                bk.orNot(this.bits, 0);
                return bk;

            } else if (bitKey instanceof BitKey.Big other) {
                final BitKey.Big bk = (BitKey.Big) other.copy();
                bk.orNot(this.bits);
                return bk;
            }

            throw createException(bitKey);
        }

        @Override
		public BitKey and(BitKey bitKey) {
            if (bitKey instanceof BitKey.Small other) {
                final BitKey.Small bk = (BitKey.Small) copy();
                bk.and(other.bits);
                return bk;

            } else if (bitKey instanceof BitKey.Mid128 other) {
                final BitKey.Small bk = (BitKey.Small) copy();
                bk.and(other.bits0);
                return bk;

            } else if (bitKey instanceof BitKey.Big other) {
                final BitKey.Small bk = (BitKey.Small) copy();
                bk.and(other.bits[0]);
                return bk;
            }

            throw createException(bitKey);
        }

        @Override
		public BitKey andNot(BitKey bitKey) {
            if (bitKey instanceof BitKey.Small other) {
                final BitKey.Small bk = (BitKey.Small) copy();
                bk.andNot(other.bits);
                return bk;

            } else if (bitKey instanceof BitKey.Mid128 other) {
                final BitKey.Small bk = (BitKey.Small) copy();
                bk.andNot(other.bits0);
                return bk;

            } else if (bitKey instanceof BitKey.Big other) {
                final BitKey.Small bk = (BitKey.Small) copy();
                bk.andNot(other.bits[0]);
                return bk;
            }

            throw createException(bitKey);
        }

        private void andNot(long bits) {
            this.bits &= ~bits;
        }

        @Override
		public boolean isSuperSetOf(BitKey bitKey) {
            if (bitKey instanceof BitKey.Small other) {
                return ((this.bits | other.bits) == this.bits);

            } else if (bitKey instanceof BitKey.Mid128 other) {
                return ((this.bits | other.bits0) == this.bits)
                    && (other.bits1 == 0);

            } else if (bitKey instanceof BitKey.Big other) {
                if ((this.bits | other.bits[0]) != this.bits) {
                    return false;
                } else {
                    for (int i = 1; i < other.bits.length; i++) {
                        if (other.bits[i] != 0) {
                            return false;
                        }
                    }
                    return true;
                }
            }
            return false;
        }

        @Override
		public boolean intersects(BitKey bitKey) {
            if (bitKey instanceof BitKey.Small other) {
                return (this.bits & other.bits) != 0;

            } else if (bitKey instanceof BitKey.Mid128 other) {
                return (this.bits & other.bits0) != 0;

            } else if (bitKey instanceof BitKey.Big other) {
                return (this.bits & other.bits[0]) != 0;
            }
            return false;
        }

        @Override
		public BitSet toBitSet() {
            final BitSet bitSet = new BitSet(64);
            long x = bits;
            int pos = 0;
            while (x != 0) {
                copyFromByte(bitSet, pos, (byte) (x & 0xff));
                x >>>= 8;
                pos += 8;
            }
            return bitSet;
        }

        /**
         * To say that I am happy about this algorithm (or the variations
         * of the algorithm used for the Mid128 and Big BitKey implementations)
         * would be a stretch. It works but there has to be a more
         * elegant and faster one but this is the best I could come up
         * with in a couple of hours.
         *
         */
        @Override
		public Iterator<Integer> iterator() {
            return new Iterator<>() {
                int pos = -1;
                long bits = Small.this.bits;
                @Override
				public boolean hasNext() {
                    if (bits == 0) {
                        return false;
                    }
                    // This is a special case
                    // Long.MIN_VALUE == -9223372036854775808
                    if (bits == Long.MIN_VALUE) {
                        pos = 63;
                        bits = 0;
                        return true;
                    }
                    long b = (bits & -bits);
                    if (b == 0) {
                        // should never happen
                        return false;
                    }
                    int delta = 0;
                    while (b >= 256) {
                        b = (b >> 8);
                        delta += 8;
                    }
                    int p = bitPositionTable[(int) b];
                    if (p >= 0) {
                        p += delta;
                    } else {
                        p = delta;
                    }
                    if (pos < 0) {
                        // first time
                        pos = p;
                    } else if (p == 0) {
                        pos++;
                    } else {
                        pos += (p + 1);
                    }
                    bits = bits >>> (p + 1);
                    return true;
                }
                @Override
                @SuppressWarnings("java:S2272")
				public Integer next() {
                    return Integer.valueOf(pos);
                }
                @Override
				public void remove() {
                    throw new UnsupportedOperationException(REMOVE);
                }
            };
        }

        @Override
		public int nextSetBit(int fromIndex) {
            if (fromIndex < 0) {
                throw new IndexOutOfBoundsException(
                    "fromIndex < 0: " + fromIndex);
            }

            if (fromIndex < 64) {
                long word = bits & (WORD_MASK << fromIndex);
                if (word != 0) {
                    return Long.numberOfTrailingZeros(word);
                }
            }
            return -1;
        }

        @Override
		public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o instanceof BitKey.Small other) {
                return (this.bits == other.bits);

            } else if (o instanceof BitKey.Mid128 other) {
                return (this.bits == other.bits0) && (other.bits1 == 0);

            } else if (o instanceof BitKey.Big other) {
                if (this.bits != other.bits[0]) {
                    return false;
                } else {
                    for (int i = 1; i < other.bits.length; i++) {
                        if (other.bits[i] != 0) {
                            return false;
                        }
                    }
                    return true;
                }
            }
            return false;
        }

        @Override
		public int hashCode() {
            return (int)(1234L ^ bits ^ (bits >>> 32));
        }

        @Override
		public int compareTo(BitKey bitKey) {
            if (bitKey instanceof Small that) {
                if (this.bits == that.bits) {
                    return  0;
                } else {
                    return this.bits < that.bits ? -1 : 1;
                }
            } else if (bitKey instanceof Mid128 that) {
                if (that.bits1 != 0) {
                    return -1;
                }
                return compareUnsigned(this.bits, that.bits0);
            } else {
                return compareToBig((Big) bitKey);
            }
        }

        protected int compareToBig(Big that) {
            int thatBitsLength = that.effectiveSize();
            switch (thatBitsLength) {
            case 0:
                return this.bits == 0 ? 0 : 1;
            case 1:
                return compareUnsigned(this.bits, that.bits[0]);
            default:
                return -1;
            }
        }

        @Override
		public String toString() {
            StringBuilder buf = new StringBuilder(64);
            buf.append("0x");
            for (int i = 63; i >= 0; i--) {
                buf.append((get(i)) ? '1' : '0');
            }
            return buf.toString();
        }

        @Override
		public BitKey copy() {
            return new Small(this.bits);
        }

        @Override
		public BitKey emptyCopy() {
            return new Small();
        }

        @Override
		public boolean isEmpty() {
            return bits == 0;
        }
    }

    /**
     * Implementation of {@link BitKey} good for sizes less than 128.
     */
    public class Mid128 extends AbstractBitKey {
        private static final long serialVersionUID = -8409143207943258659L;
        private long bits0;
        private long bits1;

        private Mid128() {
        }

        private Mid128(Mid128 mid) {
            this.bits0 = mid.bits0;
            this.bits1 = mid.bits1;
        }

        @Override
		public void set(int pos) {
            if (pos < 64) {
                bits0 |= bit(pos);
            } else if (pos < 128) {
                bits1 |= bit(pos);
            } else {
                throw new IllegalArgumentException(
                    new StringBuilder("pos ").append(pos).append(" exceeds capacity 128").toString());
            }
        }

        @Override
		public boolean get(int pos) {
            if (pos < 64) {
                return (bits0 & bit(pos)) != 0;
            } else if (pos < 128) {
                return (bits1 & bit(pos)) != 0;
            } else {
                return false;
            }
        }

        @Override
		public void clear(int pos) {
            if (pos < 64) {
                bits0 &= ~bit(pos);
            } else if (pos < 128) {
                bits1 &= ~bit(pos);
            } else {
                throw new IndexOutOfBoundsException(
                    new StringBuilder("pos ").append(pos).append(" exceeds size ").append(128).toString());
            }
        }

        @Override
		public void clear() {
            bits0 = 0;
            bits1 = 0;
        }

        @Override
		public int cardinality() {
            return bitCount(bits0)
               + bitCount(bits1);
        }

        private void or(long bits0, long bits1) {
            this.bits0 |= bits0;
            this.bits1 |= bits1;
        }

        private void orNot(long bits0, long bits1) {
            this.bits0 ^= bits0;
            this.bits1 ^= bits1;
        }

        private void and(long bits0, long bits1) {
            this.bits0 &= bits0;
            this.bits1 &= bits1;
        }

        @Override
		public BitKey or(BitKey bitKey) {
            if (bitKey instanceof BitKey.Small other) {
                final BitKey.Mid128 bk = (BitKey.Mid128) copy();
                bk.or(other.bits, 0);
                return bk;

            } else if (bitKey instanceof BitKey.Mid128 other) {
                final BitKey.Mid128 bk = (BitKey.Mid128) copy();
                bk.or(other.bits0, other.bits1);
                return bk;

            } else if (bitKey instanceof BitKey.Big other) {
                final BitKey.Big bk = (BitKey.Big) other.copy();
                bk.or(this.bits0, this.bits1);
                return bk;
            }

            throw createException(bitKey);
        }

        @Override
		public BitKey orNot(BitKey bitKey) {
            if (bitKey instanceof BitKey.Small other) {
                final BitKey.Mid128 bk = (BitKey.Mid128) copy();
                bk.orNot(other.bits, 0);
                return bk;

            } else if (bitKey instanceof BitKey.Mid128 other) {
                final BitKey.Mid128 bk = (BitKey.Mid128) copy();
                bk.orNot(other.bits0, other.bits1);
                return bk;

            } else if (bitKey instanceof BitKey.Big other) {
                final BitKey.Big bk = (BitKey.Big) other.copy();
                bk.orNot(this.bits0, this.bits1);
                return bk;
            }

            throw createException(bitKey);
        }

        @Override
		public BitKey and(BitKey bitKey) {
            if (bitKey instanceof BitKey.Small other) {
                final BitKey.Mid128 bk = (BitKey.Mid128) copy();
                bk.and(other.bits, 0);
                return bk;

            } else if (bitKey instanceof BitKey.Mid128 other) {
                final BitKey.Mid128 bk = (BitKey.Mid128) copy();
                bk.and(other.bits0, other.bits1);
                return bk;

            } else if (bitKey instanceof BitKey.Big other) {
                final BitKey.Mid128 bk = (BitKey.Mid128) copy();
                bk.and(other.bits[0], other.bits[1]);
                return bk;
            }

            throw createException(bitKey);
        }

        @Override
		public BitKey andNot(BitKey bitKey) {
            if (bitKey instanceof BitKey.Small other) {
                final BitKey.Mid128 bk = (BitKey.Mid128) copy();
                bk.andNot(other.bits, 0);
                return bk;

            } else if (bitKey instanceof BitKey.Mid128 other) {
                final BitKey.Mid128 bk = (BitKey.Mid128) copy();
                bk.andNot(other.bits0, other.bits1);
                return bk;

            } else if (bitKey instanceof BitKey.Big other) {
                final BitKey.Mid128 bk = (BitKey.Mid128) copy();
                bk.andNot(other.bits[0], other.bits[1]);
                return bk;
            }

            throw createException(bitKey);
        }

        private void andNot(long bits0, long bits1) {
            this.bits0 &= ~bits0;
            this.bits1 &= ~bits1;
        }

        @Override
		public boolean isSuperSetOf(BitKey bitKey) {
            if (bitKey instanceof BitKey.Small other) {
                return ((this.bits0 | other.bits) == this.bits0);

            } else if (bitKey instanceof BitKey.Mid128 other) {
                return ((this.bits0 | other.bits0) == this.bits0)
                    && ((this.bits1 | other.bits1) == this.bits1);

            } else if (bitKey instanceof BitKey.Big other) {
                if ((this.bits0 | other.bits[0]) != this.bits0) {
                    return false;
                } else if ((this.bits1 | other.bits[1]) != this.bits1) {
                    return false;
                } else {
                    for (int i = 2; i < other.bits.length; i++) {
                        if (other.bits[i] != 0) {
                            return false;
                        }
                    }
                    return true;
                }
            }
            return false;
        }

        @Override
		public boolean intersects(BitKey bitKey) {
            if (bitKey instanceof BitKey.Small other) {
                return (this.bits0 & other.bits) != 0;

            } else if (bitKey instanceof BitKey.Mid128 other) {
                return (this.bits0 & other.bits0) != 0
                    || (this.bits1 & other.bits1) != 0;

            } else if (bitKey instanceof BitKey.Big other) {
                if ((this.bits0 & other.bits[0]) != 0) {
                    return true;
                } else {
                    return ((this.bits1 & other.bits[1]) != 0);
                }
            }
            return false;
        }

        @Override
		public BitSet toBitSet() {
            final BitSet bitSet = new BitSet(128);
            copyFromLong(bitSet, 0, bits0);
            copyFromLong(bitSet, 64, bits1);
            return bitSet;
        }
        @Override
		public Iterator<Integer> iterator() {
            return new Iterator<>() {
                long bits0 = Mid128.this.bits0;
                long bits1 = Mid128.this.bits1;
                int pos = -1;
                @Override
				public boolean hasNext() {
                    if (bits0 != 0) {
                        if (bits0 == Long.MIN_VALUE) {
                            pos = 63;
                            bits0 = 0;
                            return true;
                        }
                        long b = (bits0&-bits0);
                        int delta = 0;
                        while (b >= 256) {
                            b = (b >> 8);
                            delta += 8;
                        }
                        int p = bitPositionTable[(int) b];
                        if (p >= 0) {
                            p += delta;
                        } else {
                            p = delta;
                        }
                        if (pos < 0) {
                            pos = p;
                        } else if (p == 0) {
                            pos++;
                        } else {
                            pos += (p + 1);
                        }
                        bits0 = bits0 >>> (p + 1);
                        return true;
                    } else {
                        if (pos < 63) {
                            pos = 63;
                        }
                        if (bits1 == Long.MIN_VALUE) {
                            pos = 127;
                            bits1 = 0;
                            return true;
                        }
                        long b = (bits1&-bits1);
                        if (b == 0) {
                            return false;
                        }
                        int delta = 0;
                        while (b >= 256) {
                            b = (b >> 8);
                            delta += 8;
                        }
                        int p = bitPositionTable[(int) b];
                        if (p >= 0) {
                            p += delta;
                        } else {
                            p = delta;
                        }
                        if (pos < 0) {
                            pos = p;
                        } else if (p == 63) {
                            pos++;
                        } else {
                            pos += (p + 1);
                        }
                        bits1 = bits1 >>> (p + 1);
                        return true;
                    }
                }
                @Override
                @SuppressWarnings("java:S2272")
				public Integer next() {
                    return Integer.valueOf(pos);
                }
                @Override
				public void remove() {
                    throw new UnsupportedOperationException(Small.REMOVE);
                }
            };
        }

        @Override
		public int nextSetBit(int fromIndex) {
            if (fromIndex < 0) {
                throw new IndexOutOfBoundsException(
                    "fromIndex < 0: " + fromIndex);
            }

            int u = fromIndex >> 6;
            long word;
            switch (u) {
            case 0:
                word = bits0 & (WORD_MASK << fromIndex);
                if (word != 0) {
                    return Long.numberOfTrailingZeros(word);
                }
                word = bits1;
                if (word != 0) {
                    return 64 + Long.numberOfTrailingZeros(word);
                }
                return -1;
            case 1:
                word = bits1 & (WORD_MASK << fromIndex);
                if (word != 0) {
                    return 64 + Long.numberOfTrailingZeros(word);
                }
                return -1;
            default:
                return -1;
            }
        }

        @Override
		public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o instanceof BitKey.Small other) {
                return (this.bits0 == other.bits) && (this.bits1 == 0);

            } else if (o instanceof BitKey.Mid128 other) {
                return (this.bits0 == other.bits0)
                    && (this.bits1 == other.bits1);

            } else if (o instanceof BitKey.Big other) {
                if (this.bits0 != other.bits[0]) {
                    return false;
                } else if (this.bits1 != other.bits[1]) {
                    return false;
                } else {
                    for (int i = 2; i < other.bits.length; i++) {
                        if (other.bits[i] != 0) {
                            return false;
                        }
                    }
                    return true;
                }
            }
            return false;
        }

        @Override
		public int hashCode() {
            long h = 1234;
            h ^= bits0;
            h ^= bits1 * 2;
            return (int)((h >> 32) ^ h);
        }

        @Override
		public String toString() {
            StringBuilder buf = new StringBuilder(64);
            buf.append("0x");
            for (int i = 127; i >= 0; i--) {
                buf.append((get(i)) ? '1' : '0');
            }
            return buf.toString();
        }

        @Override
		public BitKey copy() {
            return new Mid128(this);
        }

        @Override
		public BitKey emptyCopy() {
            return new Mid128();
        }

        @Override
		public boolean isEmpty() {
            return bits0 == 0
                && bits1 == 0;
        }

        // implement Comparable (in lazy, expensive fashion)
        @Override
		public int compareTo(BitKey bitKey) {
            if (bitKey instanceof Mid128 that) {
                if (this.bits1 != that.bits1) {
                    return compareUnsigned(this.bits1, that.bits1);
                }
                return compareUnsigned(this.bits0, that.bits0);
            } else if (bitKey instanceof Small that) {
                if (this.bits1 != 0) {
                    return 1;
                }
                return compareUnsigned(this.bits0, that.bits);
            } else {
                return compareToBig((Big) bitKey);
            }
        }

        int compareToBig(Big that) {
            int thatBitsLength = that.effectiveSize();
            switch (thatBitsLength) {
            case 0:
                return this.bits1 == 0
                    && this.bits0 == 0
                    ? 0
                    : 1;
            case 1:
                if (this.bits1 != 0) {
                    return 1;
                }
                return compareUnsigned(this.bits0, that.bits[0]);
            case 2:
                if (this.bits1 != that.bits[1]) {
                    return compareUnsigned(this.bits1, that.bits[1]);
                }
                return compareUnsigned(this.bits0, that.bits[0]);
            default:
                return -1;
            }
        }
    }

    /**
     * Implementation of {@link BitKey} with more than 64 bits. Similar to
     * {@link java.util.BitSet}, but does not require dynamic resizing.
     */
    public class Big extends AbstractBitKey {
        private static final long serialVersionUID = -3715282769845236295L;
        private long[] bits;

        private Big(int size) {
            bits = new long[chunkCount(size + 1)];
        }

        private Big(Big big) {
            bits = big.bits.clone();
        }

        private int size() {
            return bits.length;
        }

        /**
         * Returns the number of chunks, ignoring any chunks on the leading
         * edge that are all zero.
         *
         * @return number of chunks that are not on the leading edge
         */
        private int effectiveSize() {
            int n = bits.length;
            while (n > 0 && bits[n - 1] == 0) {
                --n;
            }
            return n;
        }

        @Override
		public void set(int pos) {
            bits[chunkPos(pos)] |= bit(pos);
        }

        @Override
		public boolean get(int pos) {
            return (bits[chunkPos(pos)] & bit(pos)) != 0;
        }

        @Override
		public void clear(int pos) {
            bits[chunkPos(pos)] &= ~bit(pos);
        }

        @Override
		public void clear() {
            for (int i = 0; i < bits.length; i++) {
                bits[i] = 0;
            }
        }

        @Override
		public int cardinality() {
            int n = 0;
            for (int i = 0; i < bits.length; i++) {
                n += bitCount(bits[i]);
            }
            return n;
        }

        private void or(long bits0) {
            this.bits[0] |= bits0;
        }

        private void or(long bits0, long bits1) {
            this.bits[0] |= bits0;
            this.bits[1] |= bits1;
        }

        private void or(long[] bits) {
            for (int i = 0; i < bits.length; i++) {
                this.bits[i] |= bits[i];
            }
        }

        private void orNot(long bits0) {
            this.bits[0] ^= bits0;
        }

        private void orNot(long bits0, long bits1) {
            this.bits[0] ^= bits0;
            this.bits[1] ^= bits1;
        }

        private void orNot(long[] bits) {
            for (int i = 0; i < bits.length; i++) {
                this.bits[i] ^= bits[i];
            }
        }

        private void and(long[] bits) {
            int length = Math.min(bits.length, this.bits.length);
            for (int i = 0; i < length; i++) {
                this.bits[i] &= bits[i];
            }
            for (int i = bits.length; i < this.bits.length; i++) {
                this.bits[i] = 0;
            }
        }

        @Override
		public BitKey or(BitKey bitKey) {
            if (bitKey instanceof BitKey.Small other) {
                final BitKey.Big bk = (BitKey.Big) copy();
                bk.or(other.bits);
                return bk;

            } else if (bitKey instanceof BitKey.Mid128 other) {
                final BitKey.Big bk = (BitKey.Big) copy();
                bk.or(other.bits0, other.bits1);
                return bk;

            } else if (bitKey instanceof BitKey.Big other) {
                if (other.size() > size()) {
                    final BitKey.Big bk = (BitKey.Big) other.copy();
                    bk.or(bits);
                    return bk;
                } else {
                    final BitKey.Big bk = (BitKey.Big) copy();
                    bk.or(other.bits);
                    return bk;
                }
            }

            throw createException(bitKey);
        }

        @Override
		public BitKey orNot(BitKey bitKey) {
            if (bitKey instanceof BitKey.Small other) {
                final BitKey.Big bk = (BitKey.Big) copy();
                bk.orNot(other.bits);
                return bk;

            } else if (bitKey instanceof BitKey.Mid128 other) {
                final BitKey.Big bk = (BitKey.Big) copy();
                bk.orNot(other.bits0, other.bits1);
                return bk;

            } else if (bitKey instanceof BitKey.Big other) {
                if (other.size() > size()) {
                    final BitKey.Big bk = (BitKey.Big) other.copy();
                    bk.orNot(bits);
                    return bk;
                } else {
                    final BitKey.Big bk = (BitKey.Big) copy();
                    bk.orNot(other.bits);
                    return bk;
                }
            }

            throw createException(bitKey);
        }

        @Override
		public BitKey and(BitKey bitKey) {
            if (bitKey instanceof BitKey.Small) {
                final BitKey.Small bk = (BitKey.Small) bitKey.copy();
                bk.and(bits[0]);
                return bk;

            } else if (bitKey instanceof BitKey.Mid128) {
                final BitKey.Mid128 bk = (BitKey.Mid128) bitKey.copy();
                bk.and(bits[0], bits[1]);
                return bk;

            } else if (bitKey instanceof BitKey.Big other) {
                if (other.size() < size()) {
                    final BitKey.Big bk = (BitKey.Big) other.copy();
                    bk.and(bits);
                    return bk;
                } else {
                    final BitKey.Big bk = (BitKey.Big) copy();
                    bk.and(other.bits);
                    return bk;
                }
            }

            throw createException(bitKey);
        }

        @Override
		public BitKey andNot(BitKey bitKey) {
            if (bitKey instanceof BitKey.Small other) {
                final BitKey.Big bk = (BitKey.Big) copy();
                bk.andNot(other.bits);
                return bk;

            } else if (bitKey instanceof BitKey.Mid128 other) {
                final BitKey.Big bk = (BitKey.Big) copy();
                bk.andNot(other.bits0, other.bits1);
                return bk;

            } else if (bitKey instanceof BitKey.Big other) {
                final BitKey.Big bk = (BitKey.Big) copy();
                bk.andNot(other.bits);
                return bk;
            }

            throw createException(bitKey);
        }

        private void andNot(long[] bits) {
            for (int i = 0; i < bits.length; i++) {
                this.bits[i] &= ~bits[i];
            }
        }

        private void andNot(long bits0, long bits1) {
            this.bits[0] &= ~bits0;
            this.bits[1] &= ~bits1;
        }

        private void andNot(long bits) {
            this.bits[0] &= ~bits;
        }

        @Override
		public boolean isSuperSetOf(BitKey bitKey) {
            if (bitKey instanceof BitKey.Small other) {
                return ((this.bits[0] | other.bits) == this.bits[0]);

            } else if (bitKey instanceof BitKey.Mid128 other) {
                return ((this.bits[0] | other.bits0) == this.bits[0])
                    && ((this.bits[1] | other.bits1) == this.bits[1]);

            } else if (bitKey instanceof BitKey.Big other) {
                int len = Math.min(bits.length, other.bits.length);
                for (int i = 0; i < len; i++) {
                    if ((this.bits[i] | other.bits[i]) != this.bits[i]) {
                        return false;
                    }
                }
                if (other.bits.length > this.bits.length) {
                    for (int i = len; i < other.bits.length; i++) {
                        if (other.bits[i] != 0) {
                            return false;
                        }
                    }
                }
                return true;
            }
            return false;
        }

        @Override
		public boolean intersects(BitKey bitKey) {
            if (bitKey instanceof BitKey.Small other) {
                return (this.bits[0] & other.bits) != 0;

            } else if (bitKey instanceof BitKey.Mid128 other) {
                return (this.bits[0] & other.bits0) != 0
                    || (this.bits[1] & other.bits1) != 0;

            } else if (bitKey instanceof BitKey.Big other) {
                int len = Math.min(bits.length, other.bits.length);
                for (int i = 0; i < len; i++) {
                    if ((this.bits[i] & other.bits[i]) != 0) {
                        return true;
                    }
                }
                return false;
            }
            return false;
        }

        @Override
		public BitSet toBitSet() {
            final BitSet bitSet = new BitSet(64);
            int pos = 0;
            for (int i = 0; i < bits.length; i++) {
                copyFromLong(bitSet, pos, bits[i]);
                pos += 64;
            }
            return bitSet;
        }

        @Override
		public Iterator<Integer> iterator() {
            return new Iterator<>() {
                long[] bits = Big.this.bits.clone();
                int pos = -1;
                int index = 0;
                @Override
				public boolean hasNext() {
                    if (index >= bits.length) {
                        return false;
                    }
                    if (pos < 0) {
                        while (bits[index] == 0) {
                            index++;
                            if (index >= bits.length) {
                                return false;
                            }
                        }
                        pos = (64 * index) - 1;
                    }
                    long bs = bits[index];
                    if (bs == 0) {
                        while (bits[index] == 0) {
                            index++;
                            if (index >= bits.length) {
                                return false;
                            }
                        }
                        pos = (64 * index) - 1;
                        bs = bits[index];
                    }
                    if (bs != 0) {
                        if (bs == Long.MIN_VALUE) {
                            pos = (64 * index) + 63;
                            bits[index] = 0;
                            return true;
                        }
                        long b = (bs&-bs);
                        int delta = 0;
                        while (b >= 256) {
                            b = (b >> 8);
                            delta += 8;
                        }
                        int p = bitPositionTable[(int) b];
                        if (p >= 0) {
                            p += delta;
                        } else {
                            p = delta;
                        }
                        if (pos < 0) {
                            pos = p;
                        } else if (p == 0) {
                            pos++;
                        } else {
                            pos += (p + 1);
                        }
                        bits[index] = bits[index] >>> (p + 1);
                        return true;
                    }
                    return false;
                }

                @Override
                @SuppressWarnings("java:S2272")
				public Integer next() {
                    return Integer.valueOf(pos);
                }

                @Override
				public void remove() {
                    throw new UnsupportedOperationException(Small.REMOVE);
                }
            };
        }

        @Override
		public int nextSetBit(int fromIndex) {
            if (fromIndex < 0) {
                throw new IndexOutOfBoundsException(
                    "fromIndex < 0: " + fromIndex);
            }

            int u = chunkPos(fromIndex);
            if (u >= bits.length) {
                return -1;
            }
            long word = bits[u] & (WORD_MASK << fromIndex);

            while (true) {
                if (word != 0) {
                    return (u * 64) + Long.numberOfTrailingZeros(word);
                }
                if (++u == bits.length) {
                    return -1;
                }
                word = bits[u];
            }
        }

        @Override
		public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o instanceof BitKey.Small other) {
                if (this.bits[0] != other.bits) {
                    return false;
                } else {
                    for (int i = 1; i < this.bits.length; i++) {
                        if (this.bits[i] != 0) {
                            return false;
                        }
                    }
                    return true;
                }

            } else if (o instanceof BitKey.Mid128 other) {
                if (this.bits[0] != other.bits0) {
                    return false;
                } else if (this.bits[1] != other.bits1) {
                    return false;
                } else {
                    for (int i = 2; i < this.bits.length; i++) {
                        if (this.bits[i] != 0) {
                            return false;
                        }
                    }
                    return true;
                }

            } else if (o instanceof BitKey.Big other) {
                int len = Math.min(bits.length, other.bits.length);
                for (int i = 0; i < len; i++) {
                    if (this.bits[i] != other.bits[i]) {
                        return false;
                    }
                }
                if (this.bits.length > other.bits.length) {
                    for (int i = len; i < this.bits.length; i++) {
                        if (this.bits[i] != 0) {
                            return false;
                        }
                    }
                } else if (other.bits.length > this.bits.length) {
                    for (int i = len; i < other.bits.length; i++) {
                        if (other.bits[i] != 0) {
                            return false;
                        }
                    }
                }
                return true;
            }
            return false;
        }

        @Override
		public int hashCode() {
            // It is important that leading 0s, and bits.length do not affect
            // the hash code. For instance, we want {1} to be equal to
            // {1, 0, 0}. This algorithm in fact ignores all 0s.
            //
            // It is also important that the hash code is the same as produced
            // by Small and Mid128.
            long h = 1234;
            for (int i = bits.length; --i >= 0;) {
                h ^= bits[i] * (i + 1);
            }
            return (int)((h >> 32) ^ h);
        }

        @Override
		public String toString() {
            StringBuilder buf = new StringBuilder(64);
            buf.append("0x");
            int start = bits.length * 64 - 1;
            for (int i = start; i >= 0; i--) {
                buf.append((get(i)) ? '1' : '0');
            }
            return buf.toString();
        }

        @Override
		public BitKey copy() {
            return new Big(this);
        }

        @Override
		public BitKey emptyCopy() {
            return new Big(bits.length << CHUNK_BIT_COUNT);
        }

        @Override
		public boolean isEmpty() {
            for (long bit : bits) {
                if (bit != 0) {
                    return false;
                }
            }
            return true;
        }

        @Override
		public int compareTo(BitKey bitKey) {
            if (bitKey instanceof Big big) {
                return compareUnsignedArrays(this.bits, big.bits);
            } else if (bitKey instanceof Mid128 that) {
                return -that.compareToBig(this);
            } else {
                Small that = (Small) bitKey;
                return -that.compareToBig(this);
            }
        }
    }

    static final byte[] bitPositionTable = {
       -1, 0, 1, 0, 2, 0, 1, 0, 3, 0, 1, 0, 2, 0, 1, 0,
        4, 0, 1, 0, 2, 0, 1, 0, 3, 0, 1, 0, 2, 0, 1, 0,
        5, 0, 1, 0, 2, 0, 1, 0, 3, 0, 1, 0, 2, 0, 1, 0,
        4, 0, 1, 0, 2, 0, 1, 0, 3, 0, 1, 0, 2, 0, 1, 0,
        6, 0, 1, 0, 2, 0, 1, 0, 3, 0, 1, 0, 2, 0, 1, 0,
        4, 0, 1, 0, 2, 0, 1, 0, 3, 0, 1, 0, 2, 0, 1, 0,
        5, 0, 1, 0, 2, 0, 1, 0, 3, 0, 1, 0, 2, 0, 1, 0,
        4, 0, 1, 0, 2, 0, 1, 0, 3, 0, 1, 0, 2, 0, 1, 0,
        7, 0, 1, 0, 2, 0, 1, 0, 3, 0, 1, 0, 2, 0, 1, 0,
        4, 0, 1, 0, 2, 0, 1, 0, 3, 0, 1, 0, 2, 0, 1, 0,
        5, 0, 1, 0, 2, 0, 1, 0, 3, 0, 1, 0, 2, 0, 1, 0,
        4, 0, 1, 0, 2, 0, 1, 0, 3, 0, 1, 0, 2, 0, 1, 0,
        6, 0, 1, 0, 2, 0, 1, 0, 3, 0, 1, 0, 2, 0, 1, 0,
        4, 0, 1, 0, 2, 0, 1, 0, 3, 0, 1, 0, 2, 0, 1, 0,
        5, 0, 1, 0, 2, 0, 1, 0, 3, 0, 1, 0, 2, 0, 1, 0,
        4, 0, 1, 0, 2, 0, 1, 0, 3, 0, 1, 0, 2, 0, 1, 0
    };
}
