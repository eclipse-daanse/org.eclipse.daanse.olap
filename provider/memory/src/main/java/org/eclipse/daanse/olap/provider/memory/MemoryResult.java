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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.daanse.olap.api.calc.Calc;
import org.eclipse.daanse.olap.api.calc.MemberCalc;
import org.eclipse.daanse.olap.api.calc.TupleCalc;
import org.eclipse.daanse.olap.api.calc.ResultStyle;
import org.eclipse.daanse.olap.api.calc.tuple.TupleList;
import org.eclipse.daanse.olap.api.calc.tuple.TupleListCalc;
import org.eclipse.daanse.olap.api.element.Member;
import org.eclipse.daanse.olap.api.execution.Execution;
import org.eclipse.daanse.olap.api.execution.ExecutionContext;
import org.eclipse.daanse.olap.api.query.component.Query;
import org.eclipse.daanse.olap.api.query.component.QueryAxis;
import org.eclipse.daanse.olap.api.result.Axis;
import org.eclipse.daanse.olap.api.result.Cell;
import org.eclipse.daanse.olap.calc.base.type.tuplebase.TupleCollections;
import org.eclipse.daanse.olap.result.ResultBase;
import org.eclipse.daanse.olap.evaluator.EvaluatorImpl;
import org.eclipse.daanse.olap.result.AxisImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The result of a query against this provider.
 *
 * <p>Deliberately not a copy of the relational result. That one is built around a
 * two-pass protocol: evaluate once, discover which cells are not loaded, fetch them in a
 * batch, evaluate again. This provider never defers, so there is nothing to fetch and
 * nothing to repeat, and the single pass below is the whole of it.
 *
 * <p>Each axis is evaluated once into a tuple list. A cell is then the value the evaluator
 * reads once the members of its coordinate are in context.
 */
public class MemoryResult extends ResultBase {

    private static final Logger LOGGER = LoggerFactory.getLogger(MemoryResult.class);

    private final EvaluatorImpl evaluator;
    private final List<TupleList> axisTuples = new ArrayList<>();
    private List<Member> slicerMembers = List.of();

    private final ExecutionContext scope;

    MemoryResult(Execution execution, EvaluatorImpl evaluator) {
        super(execution, new Axis[execution.getDaanseStatement().getQuery().getAxes().length]);
        this.evaluator = evaluator;
        this.scope = execution.asContext();
        inScope(() -> {
            evaluateAxes();
            return null;
        });
    }

    /**
     * Runs inside this result's execution scope.
     *
     * <p>Functions look the current execution up while they run, for a deadline or to see
     * whether the query was cancelled. Axes are evaluated when the result is built, but a
     * cell is evaluated whenever the caller asks for it, long after execute() returned, so
     * the scope has to be re-entered at both points rather than wrapped around the call
     * that produced the result.
     */
    private <T> T inScope(java.util.function.Supplier<T> body) {
        if (scope == null || ExecutionContext.currentOrNull() != null) {
            return body.get();
        }
        java.util.concurrent.atomic.AtomicReference<T> out = new java.util.concurrent.atomic.AtomicReference<>();
        ExecutionContext.where(scope, () -> out.set(body.get()));
        return out.get();
    }

    private void evaluateAxes() {
        Query query = getQuery();

        // The slicer first: it names the context every cell is read in, so it has to be
        // in the evaluator before any axis or cell is looked at.
        TupleList slicerTuples = evaluateSlicer(query);
        slicerAxis = new AxisImpl(slicerTuples);
        if (!slicerTuples.isEmpty()) {
            slicerMembers = List.copyOf(slicerTuples.get(0));
            for (Member member : slicerMembers) {
                evaluator.setContext(member);
            }
        }

        QueryAxis[] queryAxes = query.getAxes();
        for (int i = 0; i < queryAxes.length; i++) {
            TupleList tuples = evaluateAxis(query, queryAxes[i]);
            axisTuples.add(tuples);
            axes[i] = new AxisImpl(tuples);
        }
    }

    /**
     * The slicer, whatever shape it was written in.
     *
     * <p>{@code WHERE [Measures].[Amount]} is a member, {@code WHERE (a, b)} is a tuple and
     * {@code WHERE &#123;a, b&#125;} is a set. The compiler produces a different calc for
     * each, so the shape is decided here rather than assumed.
     */
    private TupleList evaluateSlicer(Query query) {
        QueryAxis slicer = query.getSlicerAxis();
        if (slicer == null || slicer.getSet() == null) {
            return TupleCollections.emptyList(1);
        }
        Calc<?> calc = query.compileExpression(slicer.getSet(), false, ResultStyle.ITERABLE);
        if (calc instanceof TupleListCalc listCalc) {
            return listCalc.evaluate(evaluator);
        }
        if (calc instanceof MemberCalc memberCalc) {
            Member member = memberCalc.evaluate(evaluator);
            TupleList one = TupleCollections.createList(1);
            one.add(List.of(member));
            return one;
        }
        if (calc instanceof TupleCalc tupleCalc) {
            Member[] tuple = tupleCalc.evaluate(evaluator);
            TupleList one = TupleCollections.createList(tuple.length);
            one.add(List.of(tuple));
            return one;
        }
        throw new UnsupportedOperationException(
                "MemoryResult: a slicer compiled to " + calc.getClass().getSimpleName()
                        + ", which this provider does not know how to read");
    }

    private TupleList evaluateAxis(Query query, QueryAxis axis) {
        Calc<?> calc = query.compileExpression(axis.getSet(), false, ResultStyle.ITERABLE);
        return ((TupleListCalc) calc).evaluate(evaluator);
    }

    /**
     * The members addressed by a cell position, one per axis.
     *
     * <p>A position names a tuple on each axis, and a tuple may span several hierarchies,
     * so the members of all of them together are the coordinate.
     */
    @Override
    public Member[] getCellMembers(int[] pos) {
        return membersAt(pos).toArray(new Member[0]);
    }

    private List<Member> membersAt(int[] pos) {
        List<Member> members = new ArrayList<>();
        for (int axis = 0; axis < pos.length; axis++) {
            members.addAll(axisTuples.get(axis).get(pos[axis]));
        }
        return members;
    }

    @Override
    public Cell getCell(int[] pos) {
        return inScope(() -> cellAt(pos));
    }

    private Cell cellAt(int[] pos) {
        List<Member> members = membersAt(pos);
        int savepoint = evaluator.savepoint();
        try {
            // The slicer is set again for every cell, not once for the result. Evaluating
            // an axis may have pushed and popped the context in between, and a cell read
            // in the wrong context silently returns the wrong measure.
            for (Member member : slicerMembers) {
                evaluator.setContext(member);
            }
            for (Member member : members) {
                evaluator.setContext(member);
            }
            Object value = evaluator.evaluateCurrent();
            List<Integer> coordinates = new ArrayList<>(pos.length);
            for (int p : pos) {
                coordinates.add(p);
            }
            return new MemoryCell(coordinates, members, value, evaluator.getFormatString());
        } finally {
            evaluator.restore(savepoint);
        }
    }

    @Override
    protected Logger getLogger() {
        return LOGGER;
    }
}
