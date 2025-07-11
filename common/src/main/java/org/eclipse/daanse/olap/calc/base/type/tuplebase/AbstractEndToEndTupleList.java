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
import java.util.Collection;
import java.util.List;

import org.eclipse.daanse.olap.api.calc.todo.TupleList;
import org.eclipse.daanse.olap.api.element.Member;

/**
 * Abstract implementation of a {@link org.eclipse.daanse.olap.api.calc.todo.TupleList} that stores
 * tuples in end-to-end format.
 *
 * For example, if the arity is 3, the tuples {(A1, B1, C1), (A1, B2, C2)}
 * will be stored as {A1, B1, C1, A2, B2, C2}. This is memory-efficient (only
 * one array, compared to 3 arrays or one array per tuple in other
 * representations), has good locality of reference, and typical operations
 * require few indirections.
 *
 * Concrete subclasses can store the data in various backing lists.
 *
 * @author jhyde
 */
abstract class AbstractEndToEndTupleList extends AbstractTupleList {

    AbstractEndToEndTupleList(int arity) {
        super(arity);
    }

    @Override
    public TupleList project(final int[] destIndices) {
        final List<Member> backingList = backingList();
        final int originalArity = getArity();
        return new DelegatingTupleList(
                destIndices.length,
                new AbstractList<List<Member>>() {
                    @Override
                    public List<Member> get(int index) {
                        final int n = index * originalArity;
                        return new AbstractList<>() {
                            @Override
                            public Member get(int index) {
                                return backingList.get(n + destIndices[index]);
                            }

                            @Override
                            public int size() {
                                return destIndices.length;
                            }
                        };
                    }

                    @Override
                    public int size() {
                        return backingList.size() / originalArity;
                    }
                });
    }

    protected abstract List<Member> backingList();

    @Override
    public List<Member> set(int index, List<Member> element) {
        assert mutable;
        final List<Member> list = backingList();
        for (int i = 0, startIndex = index * arity; i < arity; i++) {
            list.set(startIndex + i, element.get(i));
        }
        return null; // not compliant with List contract
    }

    @Override
    public boolean addAll(Collection<? extends List<Member>> c) {
        return addAll(size(), c);
    }

    @Override
    public boolean addAll(int i, Collection<? extends List<Member>> c) {
        assert mutable;
        if (c instanceof AbstractEndToEndTupleList abstractEndToEndTupleList) {
            return backingList().addAll(
                    i * arity,
                abstractEndToEndTupleList.backingList());
        }
        return super.addAll(i, c);
    }

    @Override
    public TupleList subList(int fromIndex, int toIndex) {
        return new ListTupleList(
                arity,
                backingList().subList(fromIndex * arity, toIndex * arity));
    }

    @Override
    public TupleList withPositionCallback(
            final PositionCallback positionCallback)
    {
        return new ListTupleList(
                arity, new PositionSensingList(positionCallback));
    }

    private class PositionSensingList extends AbstractList<Member> {
        private final PositionCallback positionCallback;
        private final List<Member> backingList = backingList();

        public PositionSensingList(
                PositionCallback positionCallback)
        {
            this.positionCallback = positionCallback;
        }

        @Override
        public Member get(int index) {
            positionCallback.onPosition(index / arity);
            return backingList.get(index);
        }

        @Override
        public int size() {
            return backingList.size();
        }

        @Override
        public Member set(int index, Member element) {
            assert mutable;
            positionCallback.onPosition(index / arity);
            return backingList.set(index, element);
        }

        @Override
        public void add(int index, Member element) {
            assert mutable;
            positionCallback.onPosition(index);
            backingList.add(index, element);
        }

        @Override
        public Member remove(int index) {
            assert mutable;
            positionCallback.onPosition(index);
            return backingList.remove(index);
        }
    }
}
