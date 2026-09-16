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


package org.eclipse.daanse.olap.evaluator;

import org.eclipse.daanse.olap.api.element.Cube;
import org.eclipse.daanse.olap.api.element.CubeLevel;
import org.eclipse.daanse.olap.api.element.Hierarchy;
import org.eclipse.daanse.olap.api.element.Level;
import org.eclipse.daanse.olap.api.element.Measure;
import org.eclipse.daanse.olap.api.element.Member;
import org.eclipse.daanse.olap.api.element.StoredMeasure;
import org.eclipse.daanse.olap.api.element.VirtualCube;
import org.eclipse.daanse.olap.api.result.CellReader;
import java.util.List;

import org.eclipse.daanse.olap.api.calc.Calc;
import org.eclipse.daanse.olap.common.Util;

/**
 * Implementation of {@link Calculation}
 * that changes one or more dimensions, then evaluates a given calculation.
 *
 * It is used to implement sets in slicers, in particular sets of tuples in
 * the slicer.
 *
 * @author jhyde
 * @since May 15, 2009
 */
public class TupleCalculation implements Calculation {
    private final List<Hierarchy> hierarchyList;
    private final Calc calc;
    private final int hashCode;

    /**
     * Creates a TupleCalculation.
     *
     * @param hierarchyList List of hierarchies to be replaced.
     * @param calc Compiled scalar expression to compute cell
     */
    public TupleCalculation(
        List<Hierarchy> hierarchyList,
        Calc calc)
    {
        this.hierarchyList = hierarchyList;
        this.calc = calc;
        this.hashCode = Util.hash(hierarchyList.hashCode(), calc);
    }

    @Override
    public int hashCode() {
        return hashCode;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof TupleCalculation calculation) {
            return this.hierarchyList.equals(calculation.hierarchyList)
                && this.calc.equals(calculation.calc);
        }
        return false;
    }

    @Override
    public String toString() {
        return calc.toString();
    }

    @Override
	public void setContextIn(EvaluatorImpl evaluator) {
        // Restore default member for each hierarchy
        // in the tuple.
        for (Hierarchy hierarchy : hierarchyList) {
            final int ordinal = hierarchy.getOrdinalInCube();
            final CalculableMember defaultMember =
                evaluator.root.defaultMembers[ordinal];
            evaluator.setContext(defaultMember);
        }

        evaluator.removeCalculation(this, true);
    }

    @Override
	public int getSolveOrder() {
        return Integer.MIN_VALUE;
    }

    @Override
	public int getHierarchyOrdinal() {
        throw new UnsupportedOperationException();
    }

    @Override
	public Calc getCompiledExpression(EvaluatorRoot root) {
        return calc;
    }

    @Override
	public boolean containsAggregateFunction() {
        return false;
    }

    @Override
	public boolean isCalculatedInQuery() {
        return true;
    }
}
