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
import java.util.List;

import org.eclipse.daanse.olap.api.calc.todo.TupleIterator;
import org.eclipse.daanse.olap.api.calc.todo.TupleList;
import org.eclipse.daanse.olap.api.element.Member;
import org.eclipse.daanse.olap.common.Util;

/**
 * Implementation of {@link org.eclipse.daanse.olap.api.calc.todo.TupleList} based on a list of
 * {@code List<Member>} tuples.
 *
 * @author jhyde
 */
public class DelegatingTupleList extends AbstractTupleList
{
    private final List<List<Member>> list;

    /**
     * Creates a DelegatingTupleList.
     *
     * @param arity Arity
     * @param list Backing list
     */
    public DelegatingTupleList(int arity, List<List<Member>> list) {
        super(arity);
        this.list = list;
        assert list.isEmpty()
        || list.get(0) instanceof List
        && (list.get(0).isEmpty()
                || list.get(0).get(0) == null
                || list.get(0).get(0) instanceof Member)
        : "sanity check failed: " + list;
    }

    @Override
    protected TupleIterator tupleIteratorInternal() {
        return new AbstractTupleListIterator();
    }

    @Override
    public TupleList subList(int fromIndex, int toIndex) {
        return new DelegatingTupleList(arity, list.subList(fromIndex, toIndex));
    }

    @Override
    public List<Member> get(int index) {
        return list.get(index);
    }

    @Override
    public int size() {
        return list.size();
    }

    @Override
    public List<Member> slice(final int column) {
        return new AbstractList<>() {
            @Override
            public Member get(int index) {
                return list.get(index).get(column);
            }
            @Override
            public int size() {
                return list.size();
            }
            @Override
            public Member set(int index, Member element) {
                final List<Member> subList = list.get(index);
                if (subList.size() == 1) {
                    // The sub list is probably a singleton list.
                    // calling set() on it will fail. We have to
                    // create a new singleton list.
                    return list.set(index, Collections.singletonList(element))
                            .get(0);
                }
                return subList.set(column, element);
            }
        };
    }

    @Override
    public TupleList copyList(int capacity) {
        return new DelegatingTupleList(
                arity,
                capacity < 0
                ? new ArrayList<>(list)
                        : new ArrayList<List<Member>>(capacity));
    }

    @Override
    public List<Member> set(int index, List<Member> element) {
        return list.set(index, element);
    }

    @Override
    public void add(int index, List<Member> element) {
        list.add(index, element);
    }

    @Override
    public void addTuple(Member... members) {
        list.add(  List.of(members));
    }

    @Override
    public TupleList project(final int[] destIndices) {
        return new DelegatingTupleList(
                destIndices.length,
                new AbstractList<List<Member>>() {
                    @Override
                    public List<Member> get(final int index) {
                        return new AbstractList<>() {
                            @Override
                            public Member get(int column) {
                                return list.get(index).get(destIndices[column]);
                            }

                            @Override
                            public int size() {
                                return destIndices.length;
                            }

                            @Override
                            public Member set(int column, Member element) {
                                return list.get(index).set(index, element);
                            }
                        };
                    }

                    @Override
                    public List<Member> set(int index, List<Member> element) {
                        return list.set(index, element);
                    }

                    @Override
                    public int size() {
                        return list.size();
                    }
                }
                );
    }

    @Override
    public TupleList withPositionCallback(
            final PositionCallback positionCallback)
    {
        return new DelegatingTupleList(
                arity,
                new AbstractList<List<Member>>() {
                    @Override
                    public List<Member> get(int index) {
                        positionCallback.onPosition(index);
                        return list.get(index);
                    }

                    @Override
                    public int size() {
                        return list.size();
                    }

                    @Override
                    public List<Member> set(int index, List<Member> element) {
                        positionCallback.onPosition(index);
                        return list.set(index, element);
                    }

                    @Override
                    public void add(int index, List<Member> element) {
                        positionCallback.onPosition(index);
                        list.add(index, element);
                    }

                    @Override
                    public List<Member> remove(int index) {
                        positionCallback.onPosition(index);
                        return list.remove(index);
                    }
                }
                );
    }
}
