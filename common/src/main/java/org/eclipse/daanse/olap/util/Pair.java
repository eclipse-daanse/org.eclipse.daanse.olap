 /*
 * This software is subject to the terms of the Eclipse Public License v1.0
 * Agreement, available at the following URL:
 * http://www.eclipse.org/legal/epl-v10.html.
 * You must accept the terms of that agreement to use this software.
 *
 * Copyright (c) 2002-2017 Hitachi Vantara..  All rights reserved.
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
 *   Stefan Bischof (bipolis.org) - initial
 */


package org.eclipse.daanse.olap.util;

import java.util.AbstractList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Pair of values.
 *
 * Because a pair implements {@link #equals(Object)}, {@link #hashCode()} and
 * {@link #compareTo(Pair)}, it can be used in any kind of
 * {@link java.util.Collection}.
 *
 * @author jhyde
 * @since Apr 19, 2007
 */
public class Pair <L, R>
    implements Comparable<Pair<L, R>>, Map.Entry<L, R>
{
    public L left;
    public R right;

    /**
     * Creates a pair.
     *
     * @param left Left value
     * @param right Right value
     */
    public Pair(L left, R right) {
        this.left = left;
        this.right = right;
    }

    /**
     * Creates a pair representing the same mapping as the
     * specified entry.
     *
     * @param entry the entry to copy
     */
    public Pair(Map.Entry<? extends L, ? extends R> entry) {
        this.left = entry.getKey();
        this.right = entry.getValue();
    }

    /**
     * Creates a Pair.
     *
     * @param left Left value
     * @param right Right value
     * @return a new Pair
     */
    public static <L, R> Pair<L, R> of(L left, R right) {
        return new Pair<>(left, right);
    }

    @Override
	public boolean equals(Object obj) {
        if (obj instanceof Pair) {
            //noinspection unchecked
            Pair<L, R> pair = (Pair) obj;
            return Objects.equals(this.left, pair.left)
                && Objects.equals(this.right, pair.right);
        }
        return false;
    }

    @Override
	public int hashCode() {
        int k = (left == null) ? 0 : left.hashCode();
        int k1 = (right == null) ? 0 : right.hashCode();
        return ((k << 4) | k) ^ k1;
    }


    @Override
	public int compareTo(Pair<L, R> that) {
        int c = compare((Comparable) this.left, (Comparable)that.left);
        if (c == 0) {
            c = compare((Comparable) this.right, (Comparable)that.right);
        }
        return c;
    }

    @Override
	public String toString() {
        return new StringBuilder("<").append(left).append(", ").append(right).append(">").toString();
    }

    // implement Map.Entry
    @Override
	public L getKey() {
        return left;
    }

    // implement Map.Entry
    @Override
	public R getValue() {
        return right;
    }

    // implement Map.Entry
    @Override
	public R setValue(R value) {
        R previous = right;
        right = value;
        return previous;
    }

    /**
     * Compares a pair of comparable values of the same type. Null collates
     * less than everything else, but equal to itself.
     *
     * @param c1 First value
     * @param c2 Second value
     * @return  a negative integer, zero, or a positive integer if c1
     *          is less than, equal to, or greater than c2.
     */
    private static <C extends Comparable<C>> int compare(C c1, C c2) {
        if (c1 == null) {
            if (c2 == null) {
                return 0;
            } else {
                return -1;
            }
        } else if (c2 == null) {
            return 1;
        } else {
            return c1.compareTo(c2);
        }
    }

    /**
     * Returns an iterable over the left slice of an iterable.
     *
     * @param iterable Iterable over pairs
     * @param <L> Left type
     * @param <R> Right type
     * @return Iterable over the left elements
     */
    public static <L, R> Iterable<L> leftIter(
        final Iterable<Pair<L, R>> iterable)
    {
        return new Iterable<>() {
            @Override
			public Iterator<L> iterator() {
                final Iterator<Pair<L, R>> iterator = iterable.iterator();
                return new Iterator<>() {
                    @Override
					public boolean hasNext() {
                        return iterator.hasNext();
                    }

                    @Override
					public L next() {
                        return iterator.next().left;
                    }

                    @Override
					public void remove() {
                        iterator.remove();
                    }
                };
            }
        };
    }

    /**
     * Returns an iterable over the right slice of an iterable.
     *
     * @param iterable Iterable over pairs
     * @param <L> right type
     * @param <R> Right type
     * @return Iterable over the right elements
     */
    public static <L, R> Iterable<R> rightIter(
        final Iterable<Pair<L, R>> iterable)
    {
        return new Iterable<>() {
            @Override
			public Iterator<R> iterator() {
                final Iterator<Pair<L, R>> iterator = iterable.iterator();
                return new Iterator<>() {
                    @Override
					public boolean hasNext() {
                        return iterator.hasNext();
                    }

                    @Override
					public R next() {
                        return iterator.next().right;
                    }

                    @Override
					public void remove() {
                        iterator.remove();
                    }
                };
            }
        };
    }

    /**
     * Returns a list of the left elements of a list of pairs.
     */
    public static <L, R> List<L> left(final List<Pair<L, R>> list) {
        return new AbstractList<>() {
            @Override
			public L get(int index) {
                return list.get(index).left;
            }

            @Override
			public int size() {
                return list.size();
            }

            @Override
			public L remove(int index) {
                Pair<L, R> pair = list.remove(index);
                return pair == null ? null : pair.left;
            }
        };
    }

    /**
     * Returns a list of the right elements of a list of pairs.
     */
    public static <L, R> List<R> right(final List<Pair<L, R>> list) {
        return new AbstractList<>() {
            @Override
			public R get(int index) {
                return list.get(index).right;
            }

            @Override
			public int size() {
                return list.size();
            }

            @Override
			public R remove(int index) {
                Pair<L, R> pair = list.remove(index);
                return pair == null ? null : pair.right;
            }
        };
    }
}
