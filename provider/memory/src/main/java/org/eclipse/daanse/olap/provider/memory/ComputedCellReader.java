/*
 * Copyright (c) 2026 Contributors to the Eclipse Foundation.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Stefan Bischof (bipolis.org) - initial
 */
package org.eclipse.daanse.olap.provider.memory;

import java.util.List;
import java.util.function.Function;

import org.eclipse.daanse.olap.api.element.Member;
import org.eclipse.daanse.olap.api.evaluator.Evaluator;
import org.eclipse.daanse.olap.api.result.CellValue;
import org.eclipse.daanse.olap.api.result.NullValue;
import org.eclipse.daanse.olap.api.result.ObjectValue;
import org.eclipse.daanse.olap.api.result.CellReader;

/**
 * Computes its answer instead of looking it up.
 *
 * <p>Nothing is stored. This is the shape an embedder uses to put a foreign backend behind
 * the OLAP interfaces without teaching this provider anything about it: the function may
 * calculate, consult a cache, or call out over the network.
 */
public class ComputedCellReader implements CellReader {

    private final Function<List<Member>, Object> function;

    public ComputedCellReader(Function<List<Member>, Object> function) {
        this.function = function;
    }

    @Override
    public CellValue get(Evaluator evaluator) {
        Object value = function.apply(List.of(evaluator.getMembers()));
        return value == null ? NullValue.INSTANCE : new ObjectValue(value);
    }

    @Override
    public int getMissCount() {
        return 0;
    }

    @Override
    public boolean isDirty() {
        return false;
    }
}
