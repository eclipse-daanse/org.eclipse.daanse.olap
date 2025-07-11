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
import java.util.Arrays;
import java.util.List;

import org.eclipse.daanse.olap.api.calc.todo.TupleIterator;
import org.eclipse.daanse.olap.api.calc.todo.TupleList;
import org.eclipse.daanse.olap.api.element.Member;

/**
 * Implementation of {@link org.eclipse.daanse.olap.api.calc.todo.TupleList} that stores tuples
 * end-to-end in a backing list.
 *
 *
 * l1: {A,B,C},{D,E,F}
 * l2: {a,b},{c,d},{e,f}
 *
 * externally looks like:
 *  [] - {A,B,C,a,b}
 *  [] - {A,B,C,c,d}
 *  [] - {A,B,C,e,f}
 *  [] - {D,E,F,a,b}
 *  [] - {D,E,F,c,d}
 *  [] - {D,E,F,e,d}
 *
 * but internally is:
 *  A,B,C,a,b,A,B,C,c,d,A,B,C,e,f,D,E,F,a,b,D,E,F,c,d,D,E,F,e,d
 *
 *
 * @author jhyde
 */
public class ListTupleList extends AbstractEndToEndTupleList
{
    private final List<Member> list;

    /**
     * Creates a ListTupleList.
     *
     * @param arity Arity
     * @param list Backing list
     */
    public ListTupleList(int arity, List<Member> list) {
        super(arity);
        this.list = list;
    }

    @Override
    protected List<Member> backingList() {
        return list;
    }

    @Override
    public Member get(int slice, int index) {
        return list.get(index * arity + slice);
    }

    @Override
    public List<Member> get(int index) {
        final int startIndex = index * arity;
        final List<Member> list1 =
                new AbstractList<>() {
            @Override
            public Member get(int index) {
                return list.get(startIndex + index);
            }

            @Override
            public int size() {
                return arity;
            }
        };
        if (mutable) {
            return List.copyOf(list1);
        }
        return list1;
    }

    @Override
    public void add(int index, List<Member> element) {
        assert mutable;
        list.addAll(index * arity, element);
    }

    @Override
    public void addTuple(Member... members) {
        assert mutable;
        list.addAll(Arrays.asList(members));
    }

    @Override
    public void clear() {
        assert mutable;
        list.clear();
    }

    @Override
    public List<Member> remove(int index) {
        assert mutable;
        for (int i = 0, n = index * arity; i < arity; i++) {
            list.remove(n);
        }
        return null; // breach of List contract
    }

    @Override
    protected void removeRange(int fromIndex, int toIndex) {
        assert mutable;
        list.subList(fromIndex * arity, toIndex * arity).clear();
    }

    @Override
    public int size() {
        return list.size() / arity;
    }

    @Override
    public List<Member> slice(final int column) {
        if (column < 0 || column >= arity) {
            throw new IllegalArgumentException();
        }
        return new AbstractList<>() {
            @Override
            public Member get(int index) {
                return ListTupleList.this.get(column, index);
            }

            @Override
            public int size() {
                return ListTupleList.this.size();
            }
        };
    }

    @Override
    public TupleList copyList(int capacity) {
        return new ListTupleList(
                arity,
                capacity < 0
                ? new ArrayList<>(list)
                        : new ArrayList<Member>(capacity * arity));
    }

    @Override
    public TupleIterator tupleIteratorInternal() {
        return new AbstractTupleListIterator();
    }
}
