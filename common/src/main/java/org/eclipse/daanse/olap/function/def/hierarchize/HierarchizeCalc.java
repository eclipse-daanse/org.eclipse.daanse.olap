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
package org.eclipse.daanse.olap.function.def.hierarchize;

import org.eclipse.daanse.olap.api.Evaluator;
import org.eclipse.daanse.olap.api.calc.todo.TupleList;
import org.eclipse.daanse.olap.api.calc.todo.TupleListCalc;
import org.eclipse.daanse.olap.api.type.Type;
import org.eclipse.daanse.olap.calc.base.type.tuplebase.AbstractProfilingNestedTupleListCalc;
import org.eclipse.daanse.olap.fun.sort.Sorter;

public class HierarchizeCalc extends AbstractProfilingNestedTupleListCalc{

    private boolean post;

    protected HierarchizeCalc(Type type, TupleListCalc tupleListCalc, final boolean post) {
        super(type, tupleListCalc);
        this.post = post;
    }

    @Override
  public TupleList evaluate( Evaluator evaluator ) {
      TupleList list = getChildCalc(0, TupleListCalc.class).evaluate( evaluator );
      return Sorter.hierarchizeTupleList( list, post );
    }

}
