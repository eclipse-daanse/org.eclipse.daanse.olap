/*
 * Copyright (c) 2024 Contributors to the Eclipse Foundation.
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
package org.eclipse.daanse.olap.function.def.nativizeset;

import org.eclipse.daanse.olap.api.Evaluator;
import org.eclipse.daanse.olap.api.calc.todo.TupleIterable;
import org.eclipse.daanse.olap.api.calc.todo.TupleIteratorCalc;

public class NonNativeIterCalc extends NonNativeCalc<TupleIterable> implements TupleIteratorCalc<TupleIterable> {
    protected NonNativeIterCalc(TupleIteratorCalc parent, boolean highCardinality) {
        super(parent, highCardinality);
    }

    TupleIteratorCalc<?> parent() {
        return (TupleIteratorCalc) parent;
    }

    @Override
    public TupleIterable evaluate(Evaluator evaluator) {
        evaluator.setNativeEnabled(nativeEnabled);
        return parent().evaluate(evaluator);
    }
}
