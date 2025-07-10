/*
 * Copyright (c) 2025 Contributors to the Eclipse Foundation.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   SmartCity Jena - initial
 *   Stefan Bischof (bipolis.org) - initial
 */
package org.eclipse.daanse.olap.impl;

import java.util.AbstractList;
import java.util.List;

import org.eclipse.daanse.olap.api.calc.todo.TupleList;
import org.eclipse.daanse.olap.api.element.Member;
import org.eclipse.daanse.olap.api.result.Position;

public class PositionImpl extends AbstractList<Member> implements Position {

    private final TupleList tupleList;
    private final int offset;

    PositionImpl(TupleList tupleList, int offset) {
        this.tupleList = tupleList;
        this.offset = offset;
    }

    @Override
    public Member get(int index) {
        return tupleList.get(index, offset);
    }

    @Override
    public int size() {
        return tupleList.getArity();
    }

    @Override
    public List<Member> getMembers() {
        return new AbstractList<>() {
            @Override
            public Member get(int slice) {
                return tupleList.get(slice, offset);
            }

            @Override
            public int size() {
                return tupleList.getArity();
            }
        };

    }
}
