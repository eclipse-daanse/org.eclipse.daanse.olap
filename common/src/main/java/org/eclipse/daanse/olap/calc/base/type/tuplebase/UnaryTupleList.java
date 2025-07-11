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
 */

package org.eclipse.daanse.olap.calc.base.type.tuplebase;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Collections;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import org.eclipse.daanse.olap.api.Evaluator;
import org.eclipse.daanse.olap.api.calc.todo.TupleCursor;
import org.eclipse.daanse.olap.api.calc.todo.TupleIterator;
import org.eclipse.daanse.olap.api.calc.todo.TupleList;
import org.eclipse.daanse.olap.api.element.Member;

/**
 * Implementation of {@link TupleList} where the tuples are unary (each tuple
 * consists of just one {@link Member}).
 *
 * It is implemented as a straightforward wrapper on a backing list. You
 * can provide the backing list explicitly using the
 * {@link #UnaryTupleList(java.util.List)} constructor, and you can access the
 * backing list by calling {@link #slice}(0).
 *
 * @author jhyde
 */
public class UnaryTupleList
extends AbstractList<List<Member>>
implements TupleList
{
    final List<Member> list;

    /**
     * Creates an empty UnaryTupleList.
     */
    public UnaryTupleList() {
        this(new ArrayList<>());
    }

    /**
     * Creates a UnaryTupleList with a given backing list.
     *
     * @param list Backing list
     */
    public UnaryTupleList(List<Member> list) {
        this.list = list;
    }

    @Override
    public Member get(int slice, int index) {
        if (slice != 0) {
            throw new IllegalArgumentException("Invalid slice: " + slice);
        }
        return list.get(index);
    }

    @Override
    public List<Member> get(int index) {
        return Collections.singletonList(list.get(index));
    }

    @Override
    public void add(int index, List<Member> element) {
        list.add(index, element.get(0));
    }

    @Override
    public boolean add(List<Member> element) {
        return list.add(element.get(0));
    }

    @Override
    public TupleList fix() {
        return this;
    }

    @Override
    public List<Member> set(int index, List<Member> element) {
        final Member member = list.set(index, element.get(0));
        return member == null
                ? null
                        : Collections.singletonList(member);
    }

    @Override
    public List<Member> remove(int index) {
        final Member member = list.remove(index);
        return member == null
                ? null
                        : Collections.singletonList(member);
    }

    @Override
    public void clear() {
        list.clear();
    }

    @Override
    public int size() {
        return list.size();
    }

    @Override
    public int getArity() {
        return 1;
    }

    @Override
    public List<Member> slice(int column) {
        return list;
    }

    @Override
    public TupleList copyList(int capacity) {
        return new UnaryTupleList(
                capacity < 0
                ? new ArrayList<>(list)
                        : new ArrayList<Member>(capacity));
    }

    @Override
    public TupleCursor tupleCursor() {
        return tupleIterator();
    }

    @Override
    public TupleIterator tupleIterator() {
        return new UnaryIterator();
    }

    @Override
    public final Iterator<List<Member>> iterator() {
        return tupleIterator();
    }

    @Override
    public TupleList project(int[] destIndices) {
        // REVIEW: Is 0-ary valid?
        assert destIndices.length == 1;
        assert destIndices[0] == 0;
        return this;
    }

    @Override
    public void addTuple(Member... members) {
        assert members.length == 1;
        list.add(members[0]);
    }

    @Override
    public void addCurrent(TupleCursor tupleIter) {
        list.add(tupleIter.member(0));
    }

    @Override
    public TupleList subList(int fromIndex, int toIndex) {
        return new ListTupleList(
                1,
                list.subList(fromIndex, toIndex));
    }

    @Override
    public TupleList withPositionCallback(
            final PositionCallback positionCallback)
    {
        return new UnaryTupleList(
                new AbstractList<Member>() {
                    @Override
                    public Member get(int index) {
                        positionCallback.onPosition(index);
                        return list.get(index);
                    }

                    @Override
                    public int size() {
                        return list.size();
                    }

                    @Override
                    public Member set(int index, Member element) {
                        positionCallback.onPosition(index);
                        return list.set(index, element);
                    }

                    @Override
                    public void add(int index, Member element) {
                        positionCallback.onPosition(index);
                        list.add(index, element);
                    }

                    @Override
                    public Member remove(int index) {
                        positionCallback.onPosition(index);
                        return list.remove(index);
                    }
                }
                );
    }

    /**
     * Implementation of {@link org.eclipse.daanse.olap.api.calc.todo.TupleIterator} for {@link UnaryTupleList}.
     * Based upon AbstractList.Itr, but with concurrent modification checking
     * removed.
     */
    private class UnaryIterator implements TupleIterator {
        /**
         * Index of element to be returned by subsequent call to next.
         */
        int cursor = 0;

        /**
         * Index of element returned by most recent call to next or
         * previous.  Reset to -1 if this element is deleted by a call
         * to remove.
         */
        int lastRet = -1;

        @Override
        public boolean hasNext() {
            return cursor != size();
        }

        @Override
        public List<Member> next() {
            try {
                final List<Member> next = get(cursor);
                lastRet = cursor++;
                return next;
            } catch (final IndexOutOfBoundsException e) {
                throw new NoSuchElementException();
            }
        }

        @Override
        public void currentToArray(Member[] members, int offset) {
            members[offset] = list.get(lastRet);
        }

        @Override
        public boolean forward() {
            if (cursor == size()) {
                return false;
            }
            lastRet = cursor++;
            return true;
        }

        @Override
        public List<Member> current() {
            return get(lastRet);
        }

        @Override
        public int getArity() {
            return 1;
        }

        @Override
        public void remove() {
            if (lastRet == -1) {
                throw new IllegalStateException();
            }
            try {
                UnaryTupleList.this.remove(lastRet);
                if (lastRet < cursor) {
                    cursor--;
                }
                lastRet = -1;
            } catch (final IndexOutOfBoundsException e) {
                throw new ConcurrentModificationException();
            }
        }

        @Override
        public void setContext(Evaluator evaluator) {
            evaluator.setContext(list.get(lastRet));
        }

        @Override
        public Member member(int column) {
            assert column == 0;
            return list.get(lastRet);
        }
    }
}
