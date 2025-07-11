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
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.RandomAccess;

import org.eclipse.daanse.olap.api.Evaluator;
import org.eclipse.daanse.olap.api.calc.todo.TupleCursor;
import org.eclipse.daanse.olap.api.calc.todo.TupleIterator;
import org.eclipse.daanse.olap.api.calc.todo.TupleList;
import org.eclipse.daanse.olap.api.element.Member;

/**
 * Abstract implementation of {@link TupleList}.
 *
 * @author jhyde
 */
public abstract class AbstractTupleList
extends AbstractList<List<Member>>
implements RandomAccess, Cloneable, TupleList
{
    protected final int arity;
    protected boolean mutable = true;

    protected AbstractTupleList(int arity) {
        this.arity = arity;
    }

    @Override
    public int getArity() {
        return arity;
    }

    protected abstract TupleIterator tupleIteratorInternal();

    @Override
    public abstract TupleList subList(int fromIndex, int toIndex);

    @Override
    public TupleList fix() {
        return new DelegatingTupleList(
                arity,
                new ArrayList<>(this));
    }

    @Override
    public final Iterator<List<Member>> iterator() {
        return tupleIteratorInternal();
    }

    @Override
    public final TupleIterator tupleIterator() {
        return tupleIteratorInternal();
    }

    /**
     * Creates a {@link TupleCursor} over this list.
     *
     * Any implementation of {@link TupleList} must implement all three
     * methods {@link #iterator()}, {@link #tupleIterator()} and
     * {@code tupleCursor}. The default implementation returns the same
     * for all three, but a derived classes can override this method to create a
     * more efficient implementation that implements cursor but not iterator.
     *
     * @return A cursor over this list
     */
    @Override
    public TupleCursor tupleCursor() {
        return tupleIteratorInternal();
    }

    @Override
    public void addCurrent(TupleCursor tupleIter) {
        add(tupleIter.current());
    }

    @Override
    public Member get(int slice, int index) {
        return get(index).get(slice);
    }

    /**
     * Implementation of {@link org.eclipse.daanse.olap.api.calc.todo.TupleIterator} for
     * {@link ArrayTupleList}.
     * Based upon AbstractList.Itr, but with concurrent modification checking
     * removed.
     */
    protected class AbstractTupleListIterator
    implements TupleIterator
    {
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
        public void currentToArray(Member[] members, int offset) {
            final List<Member> current = current();
            if (offset == 0) {
                current.toArray(members);
            } else {
                //noinspection SuspiciousSystemArraycopy
                System.arraycopy(current.toArray(), 0, members, offset, arity);
            }
        }

        @Override
        public int getArity() {
            return AbstractTupleList.this.getArity();
        }

        @Override
        public void remove() {
            assert mutable;
            if (lastRet == -1) {
                throw new IllegalStateException();
            }
            try {
                AbstractTupleList.this.remove(lastRet);
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
            evaluator.setContext(current());
        }

        @Override
        public Member member(int column) {
            return get(lastRet).get(column);
        }
    }
}
